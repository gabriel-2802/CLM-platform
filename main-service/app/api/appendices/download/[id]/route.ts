import { NextRequest, NextResponse } from "next/server";
import { getToken } from "next-auth/jwt";

const API_BASE_URL = process.env.API_BASE_URL || "http://contracts:8081";

export async function GET(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const type = request.nextUrl.searchParams.get("type") ?? "unsigned";

  if (!id) {
    return new NextResponse("Not found", { status: 404 });
  }

  const token = await getToken({ req: request, raw: true, secret: process.env.NEXTAUTH_SECRET! });

  const res = await fetch(`${API_BASE_URL}/api/appendices/download/${id}/${type}/pdf`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (!res.ok) {
    return new NextResponse("Failed to download appendix", { status: res.status });
  }

  const headers = new Headers();
  headers.set("Content-Type", res.headers.get("content-type") || "application/pdf");
  headers.set("Content-Disposition", res.headers.get("content-disposition") || `attachment; filename="appendix-${id}.pdf"`);

  const buffer = await res.arrayBuffer();
  return new NextResponse(new Uint8Array(buffer), { headers, status: 200 });
}
