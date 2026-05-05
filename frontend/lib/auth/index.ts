import type { Session } from "next-auth";
import { redirect } from "next/navigation";
import { jwtVerify } from "jose";
import type { NextRequest } from "next/server";
import { cookies } from "next/headers";
import { authOptions } from "./config";

export { authOptions };

const toKey = (s: string) => new TextEncoder().encode(s);
const SECURE_COOKIE   = "__Secure-next-auth.session-token";
const INSECURE_COOKIE = "next-auth.session-token";

async function readToken(request?: Request): Promise<Record<string, unknown> | null> {
  const secret = process.env.NEXTAUTH_SECRET;
  if (!secret) return null;

  let raw: string | undefined;

  if (request) {
    const nextReq = request as NextRequest;
    if (typeof nextReq.cookies?.get === "function") {
      raw = nextReq.cookies.get(SECURE_COOKIE)?.value
         ?? nextReq.cookies.get(INSECURE_COOKIE)?.value;
    }
    if (!raw) {
      const header = request.headers.get("cookie") ?? "";
      for (const part of header.split(";")) {
        const [k, ...rest] = part.trim().split("=");
        const key = k.trim();
        if (key === SECURE_COOKIE || key === INSECURE_COOKIE) {
          raw = decodeURIComponent(rest.join("=").trim());
          if (key === SECURE_COOKIE) break;
        }
      }
    }
  } else {
    const jar = await cookies();
    raw = jar.get(SECURE_COOKIE)?.value ?? jar.get(INSECURE_COOKIE)?.value;
  }

  if (!raw) return null;

  try {
    const { payload } = await jwtVerify(raw, toKey(secret));
    return payload as Record<string, unknown>;
  } catch {
    return null;
  }
}

export async function getSession(request?: Request) {
  const p = await readToken(request);
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

export async function getAdminSession(request?: Request): Promise<(Session & {
  user: Session["user"] & { role?: string; serviceToken?: string };
}) | null> {
  const p = await readToken(request);
  if (!p || String(p.role) !== "ADMIN") return null;
  return {
    user: {
      id: p.id as string,
      email: p.email as string,
      name: p.name as string | null | undefined,
      role: p.role as string,
      serviceToken: p.serviceToken as string | undefined,
    },
  } as Session & { user: Session["user"] & { role?: string; serviceToken?: string } };
}

export async function getServiceToken(request?: Request): Promise<string | null> {
  const p = await readToken(request);
  return (p?.serviceToken as string) ?? null;
}

export async function requireUser(
  request?: Request,
  redirectTo: string = "/signin"
): Promise<Session["user"]> {
  const p = await readToken(request);
  if (!p) redirect(redirectTo);
  return { id: p.id as string, email: p.email as string, name: p.name as string } as Session["user"];
}
