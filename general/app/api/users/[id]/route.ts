import { NextRequest, NextResponse } from 'next/server';
import { PrismaClient } from '../../../../lib/generated/prisma-client';
import { hash } from 'bcryptjs';
import { getAdminSession } from '@/lib/auth';

const prisma = new PrismaClient();

// GET single user
export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getAdminSession();
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }
    const { id: idParam } = await context.params;
    const id = parseInt(idParam);
    
    if (isNaN(id)) {
      return NextResponse.json(
        { error: 'Invalid user ID' },
        { status: 400 }
      );
    }

    const user = await prisma.user.findUnique({
      where: { id },
      select: { id: true, email: true, name: true, rol: true }
    });

    if (!user) {
      return NextResponse.json(
        { error: 'User not found' },
        { status: 404 }
      );
    }

    return NextResponse.json(user);
  } catch (error) {
    console.error('Error fetching user:', error);
    return NextResponse.json(
      { error: 'Failed to fetch user' },
      { status: 500 }
    );
  }
}

// PUT update user
export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getAdminSession();
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }
    const { id: idParam } = await context.params;
    const id = parseInt(idParam);
    
    if (isNaN(id)) {
      return NextResponse.json(
        { error: 'Invalid user ID' },
        { status: 400 }
      );
    }

  const body = await request.json();
  const { email, name, rol, password } = body as { email?: string; name?: string; rol?: 'ADMIN'|'USER'|'MANAGER'; password?: string };

    if (!email || !rol) {
      return NextResponse.json(
        { error: 'Email and role are required' },
        { status: 400 }
      );
    }

  const data: { email: string; name: string | null; rol: 'ADMIN'|'USER'|'MANAGER'; password?: string } = {
      email,
      name: name || null,
      rol,
    };
    if (password) {
      const meetsComplexity = (pwd: string) => /[A-Z]/.test(pwd) && /[a-z]/.test(pwd) && /\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd) && pwd.length >= 8;
      if (!meetsComplexity(password)) {
        return NextResponse.json({ error: 'Password must be at least 8 chars and include upper, lower, number, and symbol' }, { status: 400 });
      }
      data.password = await hash(password, 10);
    }

    const user = await prisma.user.update({ where: { id }, data });

  return NextResponse.json({ id: user.id, email: user.email, name: user.name, rol: user.rol });
  } catch (error: unknown) {
    console.error('Error updating user:', error);
    
    type WithCode = { code: string };
    if (typeof error === 'object' && error && 'code' in error && (error as WithCode).code === 'P2002') {
      return NextResponse.json(
        { error: 'A user with this email already exists' },
        { status: 409 }
      );
    }

    if (typeof error === 'object' && error && 'code' in error && (error as WithCode).code === 'P2025') {
      return NextResponse.json(
        { error: 'User not found' },
        { status: 404 }
      );
    }

    return NextResponse.json(
      { error: 'Failed to update user' },
      { status: 500 }
    );
  }
}

// DELETE user
export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  try {
    const { id: idParam } = await context.params;
    const id = parseInt(idParam);
    
    if (isNaN(id)) {
      return NextResponse.json(
        { error: 'Invalid user ID' },
        { status: 400 }
      );
    }

    await prisma.user.delete({
      where: { id }
    });

    return NextResponse.json({ message: 'User deleted successfully' });
  } catch (error: unknown) {
    console.error('Error deleting user:', error);
    
    type WithCode = { code: string };
    if (typeof error === 'object' && error && 'code' in error && (error as WithCode).code === 'P2025') {
      return NextResponse.json(
        { error: 'User not found' },
        { status: 404 }
      );
    }

    return NextResponse.json(
      { error: 'Failed to delete user' },
      { status: 500 }
    );
  }
}