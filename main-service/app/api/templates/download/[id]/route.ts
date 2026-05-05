import { NextRequest, NextResponse } from "next/server";
import { getToken } from "next-auth/jwt";

import { CONTRACTS_SERVICE_URL } from "@/lib/config/server"

export async function GET(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;

  if (!id) {
    return new NextResponse("Not found", { status: 404 });
  }

  const token = await getToken({ req: request, raw: true, secret: process.env.NEXTAUTH_SECRET! });

  const res = await fetch(`${CONTRACTS_SERVICE_URL}/api/templates/download/${id}/docx`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (!res.ok) {
    return new NextResponse("Failed to download template", { status: res.status });
  }

  const headers = new Headers();
  headers.set("Content-Type", res.headers.get("content-type") || "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
  headers.set("Content-Disposition", res.headers.get("content-disposition") || `attachment; filename="template-${id}.docx"`);

  const buffer = await res.arrayBuffer();
  return new NextResponse(new Uint8Array(buffer), { headers, status: 200 });
}
