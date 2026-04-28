import { NextResponse } from "next/server";
import { getAdminSession } from "@/lib/auth";
import { registerUser } from "@/lib/user-service-client";

export async function POST(req: Request) {
  const session = await getAdminSession();
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { email, name, password } = await req.json();

  if (!email || !password) {
    return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
  }

  const { status, body } = await registerUser({ email, name: name ?? "", password });

  if (status === 201) {
    const b = body as { user?: unknown };
    return NextResponse.json({ user: b.user ?? body }, { status: 201 });
  }
  if (status === 409) {
    return NextResponse.json({ error: "Email already in use" }, { status: 409 });
  }
  if (status === 400) {
    return NextResponse.json({ error: "Invalid request", details: body }, { status: 400 });
  }
  return NextResponse.json({ error: "Internal server error" }, { status: 500 });
}
