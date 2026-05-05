import { NextRequest, NextResponse } from "next/server";
import { getAdminSession } from "@/lib/auth";
import { getUserById, updateUser, deleteUser, normalizeUser } from "@/lib/user-service-client";
import { revalidatePath } from "next/cache";

export const dynamic = "force-dynamic";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const session = await getAdminSession(request);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { id: idParam } = await context.params;
  const id = parseInt(idParam);
  if (isNaN(id)) {
    return NextResponse.json({ error: "Invalid user ID" }, { status: 400 });
  }

  const token = session.user.serviceToken!;
  const user = await getUserById(id, token);
  if (!user) {
    return NextResponse.json({ error: "User not found" }, { status: 404 });
  }

  return NextResponse.json(normalizeUser(user), {
    headers: { "Cache-Control": "no-store" },
  });
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const session = await getAdminSession(request);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { id: idParam } = await context.params;
  const id = parseInt(idParam);
  if (isNaN(id)) {
    return NextResponse.json({ error: "Invalid user ID" }, { status: 400 });
  }

  const body = await request.json();
  const { email, name, rol } = body as {
    email?: string;
    name?: string;
    rol?: "ADMIN" | "USER" | "MANAGER";
  };

  if (!email) {
    return NextResponse.json({ error: "Email is required" }, { status: 400 });
  }

  const token = session.user.serviceToken!;
  const { status, body: resBody } = await updateUser(
    id,
    { email, name: name ?? null, role: rol },
    token
  );

  if (status === 200) {
    const u = resBody as { id: number; email: string; name: string | null; roles: string[] };
    revalidatePath("/users");
    revalidatePath("/taskuri");
    revalidatePath("/taskuri/edit/new");
    revalidatePath("/taskuri/edit/nou");
    return NextResponse.json({ id: u.id, email: u.email, name: u.name, rol: rol ?? "USER" });
  }
  if (status === 404) return NextResponse.json({ error: "User not found" }, { status: 404 });
  if (status === 409) return NextResponse.json({ error: "A user with this email already exists" }, { status: 409 });
  return NextResponse.json({ error: "Failed to update user" }, { status: 500 });
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const session = await getAdminSession(request);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { id: idParam } = await context.params;
  const id = parseInt(idParam);
  if (isNaN(id)) {
    return NextResponse.json({ error: "Invalid user ID" }, { status: 400 });
  }

  const token = session.user.serviceToken!;
  const status = await deleteUser(id, token);

  if (status === 204) {
    revalidatePath("/users");
    revalidatePath("/taskuri");
    revalidatePath("/taskuri/edit/new");
    revalidatePath("/taskuri/edit/nou");
    return NextResponse.json({ message: "User deleted successfully" });
  }
  if (status === 404) return NextResponse.json({ error: "User not found" }, { status: 404 });
  return NextResponse.json({ error: "Failed to delete user" }, { status: 500 });
}
