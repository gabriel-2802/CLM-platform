import type { NextAuthOptions, User as NextAuthUser, Session } from "next-auth";
import { type JWT } from "next-auth/jwt";
import CredentialsProvider from "next-auth/providers/credentials";
import { SignJWT, jwtVerify } from "jose";
import { loginUser, primaryRole } from "@/lib/user-service-client";

const toKey = (s: string) => new TextEncoder().encode(s);

export const authOptions: NextAuthOptions = {
  pages: {
    signIn: "/signin",
    error: "/auth/error",
  },
  session: { strategy: "jwt" },
  // Override NextAuth's default JWE (encrypted) tokens with plain HS256 JWS tokens.
  // This lets Spring validate the same token using the shared JWT_SECRET / NEXTAUTH_SECRET.
  jwt: {
    encode: async ({ token, secret, maxAge }) => {
      const { exp: _exp, iat: _iat, jti: _jti, ...payload } = token as Record<string, unknown>;
      return new SignJWT(payload)
        .setProtectedHeader({ alg: "HS256" })
        .setIssuedAt()
        .setExpirationTime(Math.floor(Date.now() / 1000) + (maxAge ?? 30 * 24 * 60 * 60))
        .sign(toKey(secret as string));
    },
    decode: async ({ token, secret }) => {
      if (!token) return null;
      try {
        const { payload } = await jwtVerify(token, toKey(secret as string));
        return payload as JWT;
      } catch {
        return null;
      }
    },
  },
  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials: Record<string, string> | undefined) {
        if (!credentials?.email || !credentials?.password) return null;

        const result = await loginUser(credentials.email, credentials.password);
        if (!result) return null;

        const role = primaryRole(result.user);

        type AppUser = NextAuthUser & { role: string; serviceToken: string };
        return {
          id: String(result.user.id),
          email: result.user.email,
          name: result.user.name ?? undefined,
          role,
          serviceToken: result.token,
        } as AppUser;
      },
    }),
  ],
  callbacks: {
    async redirect({ url, baseUrl }) {
      if (url.startsWith("/")) return `${baseUrl}${url}`;
      else if (new URL(url).origin === baseUrl) return url;
      return baseUrl;
    },
    async jwt({ token, user }: { token: JWT; user?: NextAuthUser }) {
      if (user) {
        const u = user as NextAuthUser & { role?: string; serviceToken?: string };
        return {
          ...token,
          id: u.id,
          role: u.role,
          serviceToken: u.serviceToken,
        } as typeof token & { id: string; role?: string; serviceToken?: string };
      }
      return token as typeof token & { id?: string; role?: string; serviceToken?: string };
    },
    async session({
      session,
      token,
    }: {
      session: Session;
      token: JWT & { id?: string; role?: string; serviceToken?: string };
    }) {
      const s = session as Session & {
        user: Session["user"] & { id?: string; role?: string; serviceToken?: string };
      };
      if (s.user) {
        s.user.id = token.id;
        s.user.role = token.role;
        s.user.serviceToken = token.serviceToken;
      }
      return s;
    },
  },
};
