import { prisma } from "@/lib/prisma";
import { PrismaClient, Client, RuleCondition } from '@/lib/generated/prisma-client';
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

function matchesAllConditions(client: Client, conditions: RuleCondition[]): boolean {
  return conditions.every((c) => {
    switch (c.operator) {
      case 'EQUALS':
        return (client as unknown as Record<string, unknown>)[c.field] === c.value;
      case 'IN':
        return c.value.split(',').includes(String((client as unknown as Record<string, unknown>)[c.field]));
      case 'IS_TRUE':
        return Boolean((client as unknown as Record<string, unknown>)[c.field]);
      default:
        return false;
    }
  });
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
    const month = searchParams.get('month');
    const year = searchParams.get('year');

    if (!month || !year) {
      return NextResponse.json({ error: 'Missing month or year parameter' }, { status: 400 });
    }

    const monthNum = parseInt(month);
    const yearNum = parseInt(year);

    if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
      return NextResponse.json({ error: 'Invalid month. Must be between 1 and 12' }, { status: 400 });
    }
    if (isNaN(yearNum) || yearNum < 1900 || yearNum > 2100) {
      return NextResponse.json({ error: 'Invalid year' }, { status: 400 });
    }

    const taskDate = new Date(Date.UTC(yearNum, monthNum - 1, 25));

    const rules = await prisma.rule.findMany({
      where: { active: true },
      include: { conditions: true },
    });

    if (rules.length === 0) {
      return NextResponse.json({ message: 'No active rules found.' });
    }

    const isQuarterlyMonth = [3, 6, 9, 12].includes(monthNum);

    const [clients, allUsers] = await Promise.all([
      prisma.client.findMany(),
      getUsers(auth.token!),
    ]);

    const tasks = [];

    for (const client of clients) {
      const collectedTitles: string[] = [];

      for (const rule of rules) {
        if (matchesAllConditions(client, rule.conditions)) {
          const shouldInclude =
            rule.frequency === 'MONTHLY' ||
            (rule.frequency === 'QUARTERLY' && isQuarterlyMonth);
          if (shouldInclude) collectedTitles.push(rule.taskTitle);
        }
      }

      if (collectedTitles.length === 0) continue;

      const uniqueTitles = [...new Set(collectedTitles.join(',').split(','))];
      const finalNotes = uniqueTitles.join(',');

      const userClientLinks = await prisma.userClient.findMany({
        where: { clientId: client.id },
        select: { userId: true },
      });

      const userIds = userClientLinks.map(uc => uc.userId);
      const assignedUser = findAssignedUser(userIds, allUsers);
      if (!assignedUser) continue;

      const task1 = await prisma.task.create({
        data: { title: 'Generat declaratii', notes: finalNotes, date: taskDate, clientId: client.id, userId: assignedUser.id },
      });
      const task2 = await prisma.task.create({
        data: { title: 'Depus declaratii', notes: finalNotes, date: taskDate, clientId: client.id, userId: assignedUser.id },
      });
      tasks.push(task1, task2);
    }

    return NextResponse.json({ message: 'Tasks generated successfully', tasks });
  } catch (error) {
    console.error('Error generating tasks:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
