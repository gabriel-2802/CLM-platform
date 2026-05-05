"use server";

import { getServiceToken } from "@/lib/auth";

import { CONTRACTS_SERVICE_URL } from "@/lib/config/server"

/**
 * Extracts the backend-issued JWT stored in the NextAuth session so it can be
 * forwarded as a Bearer token to the contracts service.
 */
async function getContractsToken(): Promise<string | null> {
  return getServiceToken();
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
