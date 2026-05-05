import type { Session } from "next-auth";
import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { jwtVerify } from "jose";
import { authOptions } from "./config";

export { authOptions };

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
// Bypass getToken / getServerSession — both have Next.js 15 compatibility
// issues (async cookies() change). Instead, read the cookie manually and
// decode it with jose directly, the same way the middleware does.

const toKey = (s: string) => new TextEncoder().encode(s);

// Cookie names next-auth uses depending on NEXTAUTH_URL protocol.
const SECURE_COOKIE   = "__Secure-next-auth.session-token";
const INSECURE_COOKIE = "next-auth.session-token";

async function readToken(): Promise<Record<string, unknown> | null> {
  const secret = process.env.NEXTAUTH_SECRET;
  if (!secret) return null;

  const cookieStore = await cookies();
  const all = cookieStore.getAll();

  const raw =
    all.find((c) => c.name === SECURE_COOKIE)?.value ??
    all.find((c) => c.name === INSECURE_COOKIE)?.value;

  if (!raw) return null;

  try {
    const { payload } = await jwtVerify(raw, toKey(secret));
    return payload as Record<string, unknown>;
  } catch {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

export async function getSession(): Promise<{
  user: { id?: string; email?: string; name?: string; role?: string; serviceToken?: string };
} | null> {
  const p = await readToken();
  if (!p) return null;
  return {
    user: {
      id: p.id as string | undefined,
      email: p.email as string | undefined,
      name: p.name as string | undefined,
      role: p.role as string | undefined,
      serviceToken: p.serviceToken as string | undefined,
    },
  };
}

export async function getServiceToken(): Promise<string | null> {
  const p = await readToken();
  return (p?.serviceToken as string) ?? null;
}

export async function requireUser(
  redirectTo: string = "/signin"
): Promise<Session["user"]> {
  const p = await readToken();
  if (!p) redirect(redirectTo);
  return {
    id: p.id as string,
    email: p.email as string,
    name: p.name as string,
  } as Session["user"];
}

export async function getAdminSession(): Promise<{
  user: { id?: string; email?: string; name?: string; role?: string; serviceToken?: string };
} | null> {
  const p = await readToken();
  if (!p || String(p.role) !== "ADMIN") return null;
  return {
    user: {
      id: p.id as string | undefined,
      email: p.email as string | undefined,
      name: p.name as string | undefined,
      role: p.role as string | undefined,
      serviceToken: p.serviceToken as string | undefined,
    },
  };
}
