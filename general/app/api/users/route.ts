import { NextRequest, NextResponse } from 'next/server';
import { PrismaClient } from '../../../lib/generated/prisma-client';
import { hash } from 'bcryptjs';
import { getAdminSession } from '@/lib/auth';

const prisma = new PrismaClient();

// GET all users
export async function GET() {
  try {
    const users = await prisma.user.findMany({
      orderBy: { id: 'asc' },
      select: { id: true, email: true, name: true, rol: true },
    });
    return NextResponse.json(users);
  } catch (error) {
    console.error('Error fetching users:', error);
    return NextResponse.json(
      { error: 'Failed to fetch users' },
      { status: 500 }
    );
  }
}

// POST create new user
export async function POST(request: NextRequest) {
  try {
    const session = await getAdminSession();
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }
    const body = await request.json();
  const { email, name, rol, password } = body as { email?: string; name?: string; rol?: 'ADMIN'|'USER'|'MANAGER'; password?: string };

    if (!email || !rol) {
      return NextResponse.json(
        { error: 'Email and role are required' },
        { status: 400 }
      );
    }

  const data: { email: string; name: string | null; rol: 'ADMIN'|'USER'|'MANAGER'; password?: string } = { email, name: name || null, rol };
    if (password && typeof password === 'string') {
      const meetsComplexity = (pwd: string) => /[A-Z]/.test(pwd) && /[a-z]/.test(pwd) && /\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd) && pwd.length >= 8;
      if (!meetsComplexity(password)) {
        return NextResponse.json({ error: 'Password must be at least 8 chars and include upper, lower, number, and symbol' }, { status: 400 });
      }
      data.password = await hash(password, 10);
    }

    const user = await prisma.user.create({ data });

    return NextResponse.json({ id: user.id, email: user.email, name: user.name, rol: user.rol }, { status: 201 });
  } catch (error: unknown) {
    console.error('Error creating user:', error);
    
    // Handle unique constraint violation (duplicate email)
  type WithCode = { code: string };
  if (typeof error === 'object' && error && 'code' in error && (error as WithCode).code === 'P2002') {
      return NextResponse.json(
        { error: 'A user with this email already exists' },
        { status: 409 }
      );
    }

    return NextResponse.json(
      { error: 'Failed to create user' },
      { status: 500 }
    );
  }
}