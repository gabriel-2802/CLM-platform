import { withAuth } from "next-auth/middleware";

export default withAuth({
  pages: {
    // Route groups like (auth) are not part of the URL; the public path is "/signin"
    signIn: "/signin",
  },
});

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|api/auth|signin|register).*)"],
};
