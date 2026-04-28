import { prisma } from "@/lib/prisma";
import { NextResponse } from 'next/server';
import { loginUser, getUsers, primaryRole, ServiceUser } from '@/lib/user-service-client';


async function checkBasicAuth(request: Request): Promise<{ ok: boolean; token?: string }> {
  const authHeader = request.headers.get('authorization');
  if (!authHeader?.startsWith('Basic ')) return { ok: false };

  const [email, password] = Buffer.from(authHeader.slice(6), 'base64').toString('utf-8').split(':');
  const result = await loginUser(email, password);
  if (!result) return { ok: false };

  const isAdmin = result.user.roles.includes('ROLE_ADMIN');
  return isAdmin ? { ok: true, token: result.token } : { ok: false };
}

function findAssignedUser(userIds: number[], allUsers: ServiceUser[]): ServiceUser | undefined {
  const assigned = allUsers.filter(u => userIds.includes(u.id));
  return (
    assigned.find(u => primaryRole(u) === 'USER') ??
    assigned.find(u => primaryRole(u) === 'MANAGER')
  );
}

export async function POST(request: Request) {
  try {
    const auth = await checkBasicAuth(request);
    if (!auth.ok) {
      return NextResponse.json(
        { error: 'Unauthorized - Admin credentials required' },
        { status: 401, headers: { 'WWW-Authenticate': 'Basic realm="Task Generation API"' } }
      );
    }

    const { searchParams } = new URL(request.url);
    const clientId = searchParams.get('clientId');
    const month = searchParams.get('month');
    const year = searchParams.get('year');
    const note = searchParams.get('note') || '390';

    if (!clientId || !month || !year) {
      return NextResponse.json({ error: 'Missing clientId, month, or year parameter' }, { status: 400 });
    }

    const clientIdNum = parseInt(clientId);
    const monthNum = parseInt(month);
    const yearNum = parseInt(year);

    if (isNaN(clientIdNum)) return NextResponse.json({ error: 'Invalid clientId' }, { status: 400 });
    if (isNaN(monthNum) || monthNum < 1 || monthNum > 12)
      return NextResponse.json({ error: 'Invalid month. Must be between 1 and 12' }, { status: 400 });
    if (isNaN(yearNum) || yearNum < 1900 || yearNum > 2100)
      return NextResponse.json({ error: 'Invalid year' }, { status: 400 });

    const client = await prisma.client.findUnique({ where: { id: clientIdNum } });
    if (!client) {
      return NextResponse.json({ error: `Client with id ${clientIdNum} not found` }, { status: 404 });
    }

    const taskDate = new Date(Date.UTC(yearNum, monthNum - 1, 25));

    const [userClientLinks, allUsers] = await Promise.all([
      prisma.userClient.findMany({ where: { clientId: clientIdNum }, select: { userId: true } }),
      getUsers(auth.token!),
    ]);

    const userIds = userClientLinks.map(uc => uc.userId);
    const assignedUser = findAssignedUser(userIds, allUsers);

    if (!assignedUser) {
      return NextResponse.json({ error: 'No USER or MANAGER assigned to this client' }, { status: 400 });
    }

    const results = [];
    const taskTitles = ['Generat declaratii', 'Depus declaratii'];

    for (const title of taskTitles) {
      const existingTask = await prisma.task.findFirst({
        where: { clientId: clientIdNum, title, date: taskDate },
      });

      if (existingTask) {
        const notesList = (existingTask.notes || '').split(',').map(n => n.trim()).filter(Boolean);
        if (!notesList.includes(note)) {
          notesList.push(note);
          const updatedNotes = notesList.join(',');
          const updated = await prisma.task.update({
            where: { id: existingTask.id },
            data: { notes: updatedNotes },
          });
          results.push({ action: 'updated', title, taskId: updated.id, notes: updatedNotes });
        } else {
          results.push({ action: 'skipped', title, taskId: existingTask.id, notes: existingTask.notes, reason: `Note "${note}" already exists` });
        }
      } else {
        const newTask = await prisma.task.create({
          data: { title, notes: note, date: taskDate, clientId: clientIdNum, userId: assignedUser.id },
        });
        results.push({ action: 'created', title, taskId: newTask.id, notes: note });
      }
    }

    return NextResponse.json({ message: 'Tasks processed successfully', clientId: clientIdNum, month: monthNum, year: yearNum, note, results });
  } catch (error) {
    console.error('Error processing tasks:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
