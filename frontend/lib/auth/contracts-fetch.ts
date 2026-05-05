"use server";

import { cookies } from "next/headers";
import { getToken } from "next-auth/jwt";

import { CONTRACTS_SERVICE_URL } from "@/lib/config/server"

/**
 * Extracts the raw HS256 JWT from the NextAuth session cookie so it can be
 * forwarded as a Bearer token to the contracts service.
 */
async function getContractsToken(): Promise<string | null> {
  const cookieStore = await cookies();
  // next-auth/jwt's getToken accepts a req-like object with a cookies map
  return getToken({
    req: {
      cookies: Object.fromEntries(
        cookieStore.getAll().map((c) => [c.name, c.value])
      ),
    } as any,
    raw: true,
    secret: process.env.NEXTAUTH_SECRET!,
  });
}

/**
 * fetch wrapper that automatically attaches the NextAuth session token as a
 * Bearer header on every request to the contracts service.
 *
 * Usage:
 *   const res = await contractsFetch("/api/contracts/generate", {
 *     method: "POST",
 *     headers: { "Content-Type": "application/json" },
 *     body: JSON.stringify(payload),
 *   });
 */
export async function contractsFetch(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const token = await getContractsToken();

  return fetch(`${CONTRACTS_SERVICE_URL}${path}`, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
}
