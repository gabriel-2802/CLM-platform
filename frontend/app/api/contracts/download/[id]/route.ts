import { NextRequest, NextResponse } from "next/server";

import { getServiceToken } from "@/lib/auth";
import { CONTRACTS_SERVICE_URL } from "@/lib/config/server"

/**
 * Proxy for contract PDF downloads.
 * Forwards the backend-issued JWT as Bearer so Spring's security filter passes.
 * Called from the browser as /api/contracts/download/{id}?type=unsigned|signed
 */
export async function GET(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const type = request.nextUrl.searchParams.get("type") ?? "unsigned";

  if (!id) {
    return new NextResponse("Not found", { status: 404 });
  }

  const token = await getServiceToken(request);

  const res = await fetch(`${CONTRACTS_SERVICE_URL}/api/contracts/download/${id}/${type}/pdf`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (!res.ok) {
    const errorText = await res.text();
    return new NextResponse(errorText || "Failed to download contract", { status: res.status });
  }

  const headers = new Headers();
  headers.set("Content-Type", res.headers.get("content-type") || "application/pdf");
  headers.set("Content-Disposition", res.headers.get("content-disposition") || `attachment; filename="contract-${id}.pdf"`);

  const buffer = await res.arrayBuffer();
  return new NextResponse(new Uint8Array(buffer), { headers, status: 200 });
}
