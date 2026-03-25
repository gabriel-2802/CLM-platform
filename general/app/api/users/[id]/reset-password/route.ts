import { NextRequest, NextResponse } from 'next/server';
import { PrismaClient } from '../../../../../lib/generated/prisma-client';
import { hash } from 'bcryptjs';
import { getAdminSession } from '@/lib/auth';

const prisma = new PrismaClient();

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getAdminSession();
    if (!session) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    const { id: idParam } = await context.params;
    const id = parseInt(idParam);
    if (isNaN(id)) return NextResponse.json({ error: 'Invalid user ID' }, { status: 400 });

    const { password } = await request.json() as { password?: string };
    if (!password) return NextResponse.json({ error: 'Password is required' }, { status: 400 });

    const meetsComplexity = (pwd: string) => /[A-Z]/.test(pwd) && /[a-z]/.test(pwd) && /\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd) && pwd.length >= 8;
    if (!meetsComplexity(password)) {
      return NextResponse.json({ error: 'Password must be at least 8 chars and include upper, lower, number, and symbol' }, { status: 400 });
    }

    const hashed = await hash(password, 10);
    const user = await prisma.user.update({ where: { id }, data: { password: hashed } });
    return NextResponse.json({ id: user.id, email: user.email, name: user.name, rol: user.rol });
  } catch (error) {
    console.error('Reset password error:', error);
    return NextResponse.json({ error: 'Failed to reset password' }, { status: 500 });
  }
}
