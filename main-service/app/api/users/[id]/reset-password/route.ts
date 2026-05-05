import { NextRequest, NextResponse } from "next/server";
import { getAdminSession } from "@/lib/auth";
import { resetPassword } from "@/lib/user-service-client";

export const dynamic = "force-dynamic";

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ id: string }> }
) {
  const session = await getAdminSession(request);
  if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const { id: idParam } = await context.params;
  const id = parseInt(idParam);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid user ID" }, { status: 400 });

  const { password } = (await request.json()) as { password?: string };
  if (!password) return NextResponse.json({ error: "Password is required" }, { status: 400 });

  const token = session.user.serviceToken!;
  const { status, body } = await resetPassword(id, password, token);

  if (status === 200) return NextResponse.json(body);
  if (status === 404) return NextResponse.json({ error: "User not found" }, { status: 404 });
  if (status === 400) return NextResponse.json({ error: "Invalid password", details: body }, { status: 400 });
  return NextResponse.json({ error: "Failed to reset password" }, { status: 500 });
}
