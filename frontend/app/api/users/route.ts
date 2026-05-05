import { NextRequest, NextResponse } from "next/server";
import { getAdminSession } from "@/lib/auth";
import { getUsers, normalizeUser, registerUser } from "@/lib/user-service-client";
import { revalidatePath } from "next/cache";

export const dynamic = "force-dynamic";

export async function GET(request: NextRequest) {
  const session = await getAdminSession(request);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const token = session.user.serviceToken!;
  const users = await getUsers(token);
  return NextResponse.json(users.map(normalizeUser), {
    headers: { "Cache-Control": "no-store" },
  });
}

export async function POST(request: NextRequest) {
  const session = await getAdminSession(request);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const body = await request.json();
  const { email, name, rol, password } = body as {
    email?: string;
    name?: string;
    rol?: "ADMIN" | "USER" | "MANAGER";
    password?: string;
  };

  if (!email || !password) {
    return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
  }

  const adminCode =
    rol === "ADMIN" ? process.env.ADMIN_REGISTER_CODE : undefined;

  const { status, body: resBody } = await registerUser({
    email,
    name: name ?? "",
    password,
    adminCode,
  });

  if (status === 201) {
    const b = resBody as { user?: { id: number; email: string; name: string | null; roles: string[] } };
    const user = b.user;
    if (!user) return NextResponse.json(resBody, { status: 201 });

    if (rol === "MANAGER" && user.id) {
      const token = session.user.serviceToken!;
      const { updateUser } = await import("@/lib/user-service-client");
      await updateUser(user.id, { email: user.email, name: user.name, role: "MANAGER" }, token);
    }

    revalidatePath("/users");
    revalidatePath("/taskuri");
    revalidatePath("/taskuri/edit/new");
    revalidatePath("/taskuri/edit/nou");

    return NextResponse.json({ id: user.id, email: user.email, name: user.name, rol: rol ?? "USER" }, { status: 201 });
  }
  if (status === 409) {
    return NextResponse.json({ error: "A user with this email already exists" }, { status: 409 });
  }
  return NextResponse.json({ error: "Failed to create user" }, { status: 500 });
}
