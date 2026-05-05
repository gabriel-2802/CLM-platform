import { NextResponse } from 'next/server';
import { loginUser, getUsers, primaryRole, ServiceUser } from '@/lib/user-service-client';

// Rules-based task generation is now handled by the client-service.
// This route proxies to /api/tasks/generate-with-rules on the client-service.

import { CLIENT_SERVICE_URL } from "@/lib/config/server"

async function checkBasicAuth(request: Request): Promise<{ ok: boolean; token?: string }> {
  const authHeader = request.headers.get('authorization');
  if (!authHeader?.startsWith('Basic ')) return { ok: false };
  const [email, password] = Buffer.from(authHeader.slice(6), 'base64').toString('utf-8').split(':');
  const result = await loginUser(email, password);
  if (!result) return { ok: false };
  return result.user.roles.includes('ROLE_ADMIN') ? { ok: true, token: result.token } : { ok: false };
}

export async function POST(request: Request) {
  const auth = await checkBasicAuth(request);
  if (!auth.ok) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401, headers: { 'WWW-Authenticate': 'Basic realm="Task Generation API"' } });
  }

  const { searchParams } = new URL(request.url);
  const month = searchParams.get('month');
  const year = searchParams.get('year');

  if (!month || !year) {
    return NextResponse.json({ error: 'Missing month or year' }, { status: 400 });
  }

  const res = await fetch(`${CLIENT_SERVICE_URL}/api/tasks/generate-with-rules?month=${month}&year=${year}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${auth.token}` },
  });

  const body = await res.text();
  return new NextResponse(body, { status: res.status, headers: { 'Content-Type': 'application/json' } });
}
