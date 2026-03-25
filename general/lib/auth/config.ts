import { PrismaClient } from "@/lib/generated/prisma-client";
import type { NextAuthOptions, User as NextAuthUser, Session } from "next-auth";
import { type JWT } from "next-auth/jwt";
import CredentialsProvider from "next-auth/providers/credentials";
import { compare } from "bcryptjs";

// Ensure single instance in development
const globalForPrisma = global as unknown as { prisma: PrismaClient };
const prisma = globalForPrisma.prisma || new PrismaClient();
if (process.env.NODE_ENV !== "production") globalForPrisma.prisma = prisma;

export const authOptions: NextAuthOptions = {
  pages: {
    signIn: "/signin",
    error: "/auth/error",
  },
  session: { strategy: "jwt" },
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

