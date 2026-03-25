import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
// import Link from "next/link";
import { ConditionalShell } from "@/components/conditional-shell";
import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { AuthProvider } from "@/components/auth/auth-provider";
import { Toaster } from "@/components/ui/sonner";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Contab - Contact Management",
  description: "A contact management application",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const session = await getServerSession(authOptions);
  const isAdmin = (session?.user as unknown as { role?: string })?.role === "ADMIN";
  const userName = (session?.user?.name as string) ?? (session?.user?.email as string) ?? "";
  return (
    <html lang="en" className="overflow-x-hidden overscroll-x-contain">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased overflow-x-hidden`}
      >
        <AuthProvider session={session}>
          <ConditionalShell isAdmin={isAdmin} userName={userName}>
            {children}
          </ConditionalShell>
        </AuthProvider>
        <Toaster richColors position="top-right" />
      </body>
    </html>
  );
}
