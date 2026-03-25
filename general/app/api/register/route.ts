import { NextResponse } from "next/server";
import { PrismaClient } from "@/lib/generated/prisma-client";
import { getAdminSession } from "@/lib/auth";
import { hash } from "bcryptjs";

const prisma = new PrismaClient();

export async function POST(req: Request) {
  try {
    const session = await getAdminSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }
    const { email, name, password } = await req.json();

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
    }
    if (typeof email !== "string" || typeof password !== "string") {
      return NextResponse.json({ error: "Invalid payload" }, { status: 400 });
    }
    const meetsComplexity = (pwd: string) => /[A-Z]/.test(pwd) && /[a-z]/.test(pwd) && /\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd) && pwd.length >= 8;
    if (!meetsComplexity(password)) {
      return NextResponse.json({ error: "Password must be at least 8 chars and include upper, lower, number, and symbol" }, { status: 400 });
    }

    const existing = await prisma.user.findUnique({ where: { email } });
    if (existing) {
      return NextResponse.json({ error: "Email already in use" }, { status: 409 });
    }

    const hashed = await hash(password, 10);
    const user = await prisma.user.create({
      data: { email, name: name ?? null, password: hashed },
      select: { id: true, email: true, name: true },
    });

    return NextResponse.json({ user }, { status: 201 });
  } catch (err) {
    console.error("Register error", err);
    return NextResponse.json({ error: "Internal server error" }, { status: 500 });
  }
}
