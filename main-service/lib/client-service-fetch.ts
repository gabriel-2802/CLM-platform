"use server";

import { cookies } from "next/headers";
import { getToken } from "next-auth/jwt";
import { authOptions } from "@/lib/auth/config";

const CLIENT_SERVICE_URL = process.env.CLIENT_SERVICE_URL || "http://client-service:8084";

async function getServiceToken(): Promise<string | null> {
  const cookieStore = await cookies();
  const token = await getToken({
    req: {
      cookies: Object.fromEntries(
        cookieStore.getAll().map((c) => [c.name, c.value])
      ),
    } as any,
    secret: process.env.NEXTAUTH_SECRET!,
    decode: authOptions.jwt!.decode,
  });
  return (token?.serviceToken as string) ?? null;
}

export async function clientServiceFetch(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const token = await getServiceToken();
  return fetch(`${CLIENT_SERVICE_URL}${path}`, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
}
