import { NextResponse } from 'next/server';
import { loginUser, getUsers, primaryRole, ServiceUser } from '@/lib/user-service-client';

import { CLIENT_SERVICE_URL } from "@/lib/config/server"

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
  const month = parseInt(searchParams.get('month') ?? '');
  const year = parseInt(searchParams.get('year') ?? '');

  if (!month || month < 1 || month > 12 || !year) {
    return NextResponse.json({ error: 'Invalid month or year' }, { status: 400 });
  }

  const taskDate = new Date(Date.UTC(year, month - 1, 1)).toISOString();

  const params = new URLSearchParams();
  params.append('request.page', '0');
  params.append('request.size', '1000');

  const [clientsRes, allUsers] = await Promise.all([
    fetch(`${CLIENT_SERVICE_URL}/api/clients?${params}`, { headers: { Authorization: `Bearer ${auth.token}` } }),
    getUsers(auth.token),
  ]);

  if (!clientsRes.ok) return NextResponse.json({ error: 'Failed to fetch clients' }, { status: 500 });
  const clientsData = await clientsRes.json();
  const clients: any[] = clientsData.content ?? clientsData ?? [];

  const tasks = [];
  for (const client of clients) {
    const usersRes = await fetch(`${CLIENT_SERVICE_URL}/api/clients/${client.id}/users`, { headers: { Authorization: `Bearer ${auth.token}` } });
    if (!usersRes.ok) continue;
    const userLinks: any = await usersRes.json();
    const userIds = userLinks.userIds ?? [];
    const assignedUser = findAssignedUser(userIds, allUsers);
    if (!assignedUser) continue;

    for (const title of ['Avem acte', 'Introdus acte', 'Verificat acte', 'Luna printata']) {
      const r = await fetch(`${CLIENT_SERVICE_URL}/api/tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${auth.token}` },
        body: JSON.stringify({ title, date: taskDate, clientId: client.id, userId: assignedUser.id, done: false }),
      });
      if (r.ok) tasks.push(await r.json());
    }
  }

  return NextResponse.json({ message: 'Tasks generated successfully', count: tasks.length });
}
