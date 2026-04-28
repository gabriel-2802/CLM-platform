import { getServerSession } from "next-auth";
import type { Session } from "next-auth";
import { redirect } from "next/navigation";
import { authOptions } from "./config";

export { authOptions };

export async function getSession() {
	return getServerSession(authOptions);
}

export async function requireUser(redirectTo: string = "/(auth)/signin"): Promise<Session["user"]> {
	const session = await getServerSession(authOptions);
	if (!session?.user) {
		redirect(redirectTo);
	}
	return session.user as Session["user"];
}

export async function getAdminSession(): Promise<(Session & { user: Session["user"] & { role?: string } }) | null> {
	const session = await getServerSession(authOptions);
		if (session?.user && (session.user as unknown as { role?: string }).role === 'ADMIN') {
		return session as Session & { user: Session["user"] & { role?: string } };
	}
	return null;
}


