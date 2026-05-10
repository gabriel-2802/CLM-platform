"use server";

import { getServiceToken } from "@/lib/auth";
import { NEGOTIATION_SERVICE_URL } from "@/lib/config/server";

async function getNegotiationToken(): Promise<string | null> {
  return getServiceToken();
}

export async function negotiationFetch(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const token = await getNegotiationToken();

  return fetch(`${NEGOTIATION_SERVICE_URL}${path}`, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
}
