import { NextResponse } from 'next/server';
import { loginUser, getUsers, primaryRole, ServiceUser } from '@/lib/user-service-client';

const CLIENT_SERVICE_URL = process.env.CLIENT_SERVICE_URL || "http://client-service:8084";

async function checkBasicAuth(request: Request): Promise<{ ok: boolean; token?: string }> {
  const authHeader = request.headers.get('authorization');
  if (!authHeader?.startsWith('Basic ')) return { ok: false };
  const [email, password] = Buffer.from(authHeader.slice(6), 'base64').toString('utf-8').split(':');
  const result = await loginUser(email, password);
  if (!result) return { ok: false };
  return result.user.roles.includes('ROLE_ADMIN') ? { ok: true, token: result.token } : { ok: false };
}

function findAssignedUser(userIds: number[], allUsers: ServiceUser[]): ServiceUser | undefined {
  const assigned = allUsers.filter(u => userIds.includes(u.id));
  return assigned.find(u => primaryRole(u) === 'USER') ?? assigned.find(u => primaryRole(u) === 'MANAGER');
}

export async function POST(request: Request) {
  const auth = await checkBasicAuth(request);
  if (!auth.ok) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401, headers: { 'WWW-Authenticate': 'Basic realm="Task Generation API"' } });
  }

  const { searchParams } = new URL(request.url);
  const clientId = parseInt(searchParams.get('clientId') ?? '');
  const month = parseInt(searchParams.get('month') ?? '');
  const year = parseInt(searchParams.get('year') ?? '');
  const note = searchParams.get('note') || '390';

  if (!clientId || !month || month < 1 || month > 12 || !year) {
    return NextResponse.json({ error: 'Invalid parameters' }, { status: 400 });
  }

  const taskDate = new Date(Date.UTC(year, month - 1, 25)).toISOString();

  const [clientRes, allUsers] = await Promise.all([
    fetch(`${CLIENT_SERVICE_URL}/api/clients/${clientId}`, { headers: { Authorization: `Bearer ${auth.token}` } }),
    getUsers(auth.token),
  ]);

  if (!clientRes.ok) return NextResponse.json({ error: `Client ${clientId} not found` }, { status: 404 });

  const usersRes = await fetch(`${CLIENT_SERVICE_URL}/api/clients/${clientId}/users`, { headers: { Authorization: `Bearer ${auth.token}` } });
  if (!usersRes.ok) return NextResponse.json({ error: 'Failed to fetch user assignments' }, { status: 500 });

  const userLinks: any = await usersRes.json();
  const userIds = userLinks.userIds ?? [];
  const assignedUser = findAssignedUser(userIds, allUsers);
  if (!assignedUser) return NextResponse.json({ error: 'No USER or MANAGER assigned' }, { status: 400 });

  const results = [];
  for (const title of ['Generat declaratii', 'Depus declaratii']) {
    const existingRes = await fetch(`${CLIENT_SERVICE_URL}/api/tasks/by-client/${clientId}`, { headers: { Authorization: `Bearer ${auth.token}` } });
    const existing: any[] = existingRes.ok ? await existingRes.json() : [];
    const match = existing.find((t: any) => t.title === title && t.date?.startsWith(taskDate.slice(0, 10)));

    if (match) {
      const notesList = (match.notes || '').split(',').map((n: string) => n.trim()).filter(Boolean);
      if (!notesList.includes(note)) {
        notesList.push(note);
        const updatedNotes = notesList.join(',');
        await fetch(`${CLIENT_SERVICE_URL}/api/tasks/${match.id}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${auth.token}` },
          body: JSON.stringify({ notes: updatedNotes }),
        });
        results.push({ action: 'updated', title, taskId: match.id });
      } else {
        results.push({ action: 'skipped', title, taskId: match.id });
      }
    } else {
      const r = await fetch(`${CLIENT_SERVICE_URL}/api/tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${auth.token}` },
        body: JSON.stringify({ title, notes: note, date: taskDate, clientId, userId: assignedUser.id, done: false }),
      });
      if (r.ok) results.push({ action: 'created', title, taskId: (await r.json()).id });
    }
  }

  return NextResponse.json({ message: 'Tasks processed', clientId, month, year, note, results });
}
