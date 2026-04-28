import { PrismaClient } from "@/lib/generated/prisma-client";
import type { NextAuthOptions, User as NextAuthUser, Session } from "next-auth";
import { type JWT } from "next-auth/jwt";
import CredentialsProvider from "next-auth/providers/credentials";
import { compare } from "bcryptjs";
import { SignJWT, jwtVerify } from "jose";

// Ensure single instance in development
const globalForPrisma = global as unknown as { prisma: PrismaClient };
const prisma = globalForPrisma.prisma || new PrismaClient();
if (process.env.NODE_ENV !== "production") globalForPrisma.prisma = prisma;

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

        const user = await prisma.user.findUnique({
          where: { email: credentials.email },
        });
        if (!user || !user.password) return null;

        const ok = await compare(credentials.password, user.password);
        if (!ok) return null;

        type AppUser = NextAuthUser & { role: string };
        const result: AppUser = {
          id: String(user.id),
          email: user.email,
          name: user.name ?? undefined,
          role: user.rol,
        };
        return result as AppUser;
      },
    }),
  ],
  callbacks: {
    async redirect({ url, baseUrl }) {
      // Allows relative callback URLs
      if (url.startsWith("/")) return `${baseUrl}${url}`;
      // Allows callback URLs on the same origin
      else if (new URL(url).origin === baseUrl) return url;
      return baseUrl;
    },
    async jwt({ token, user }: { token: JWT; user?: NextAuthUser }) {
      if (user) {
        const u = user as NextAuthUser & { role?: string };
        return { ...token, id: u.id, role: u.role } as typeof token & { id: string; role?: string };
      }
      return token as typeof token & { id?: string; role?: string };
    },
    async session({ session, token }: { session: Session; token: JWT & { id?: string; role?: string } }) {
      const s = session as Session & { user: Session["user"] & { id?: string; role?: string } };
      if (s.user) {
        s.user.id = token.id;
        s.user.role = token.role;
      }
      return s;
    },
  },
};

