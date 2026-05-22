import { withAuth } from "next-auth/middleware";
import { jwtVerify } from "jose";

const toKey = (s: string) => new TextEncoder().encode(s);

export default withAuth({
  pages: {
    signIn: "/signin",
  },
  // Must match the custom encode/decode in lib/auth/config.ts —
  // withAuth has its own decoder that doesn't inherit authOptions.jwt callbacks.
  jwt: {
    decode: async ({ token, secret }) => {
      if (!token) return null;
      try {
        const { payload } = await jwtVerify(token, toKey(secret as string));
        return payload as any;
      } catch {
        return null;
      }
    },
  },
});

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|api/auth|signin|register).*)"],
};


