"use server";

import { PrismaClient } from "@/lib/generated/prisma-client";
import type { Prisma } from "@/lib/generated/prisma-client";
import { getSession } from "@/lib/auth";
import { unstable_noStore as noStore, revalidatePath } from "next/cache";

const prisma = new PrismaClient();

export type TaskRow = {
	id: number;
	title: string;
		date?: string; // dd/mm/yyyy
		dateTs?: number; // timestamp for sorting
	done: boolean;
	user?: string;
	userId?: number;
	client?: string;
	objective?: string;
	blocked?: string;
};

const toDMY = (d?: Date | null) => {
	if (!d) return undefined;
	const dd = String(d.getDate()).padStart(2, "0");
	const mm = String(d.getMonth() + 1).padStart(2, "0");
	const yy = d.getFullYear();
	return `${dd}/${mm}/${yy}`;
};

export async function getTaskRows(): Promise<TaskRow[]> {
	noStore();
	const session = await getSession();
	const role = (session?.user as unknown as { role?: string })?.role;
	const currentUserId = (session?.user as unknown as { id?: string })?.id;

	const where: Prisma.TaskWhereInput | undefined =
		role === "ADMIN" || role === "MANAGER"
			? undefined
			: currentUserId
			? { userId: Number(currentUserId) }
			: { id: -1 }; // no session: return empty

	const tasks = await prisma.task.findMany({
		where,
			orderBy: { date: "desc" },
		include: { user: true, client: true },
	});
	return tasks.map((t) => ({
		id: t.id,
		title: t.title,
			date: toDMY(t.date),
			dateTs: t.date ? new Date(t.date).getTime() : undefined,
		done: t.done,
		user: t.user?.name || t.user?.email || undefined,
			userId: t.userId,
		client: t.client?.denumire || undefined,
		objective: t.objective || undefined,
		blocked: t.blocked || undefined,
	}));
}

export type TaskDetails = {
	id: number;
	title: string;
	date?: string;
	done: boolean;
	notes?: string;
	blocked?: string;
	objective?: string;
	userId: number;
	clientId: number;
};

export async function getTask(id: number): Promise<TaskDetails | null> {
	noStore();
	const t = await prisma.task.findUnique({ where: { id } });
	if (!t) return null;
	return {
		id: t.id,
		title: t.title,
		date: toDMY(t.date),
		done: t.done,
		notes: t.notes ?? undefined,
		blocked: t.blocked ?? undefined,
		objective: t.objective ?? undefined,
		userId: t.userId,
		clientId: t.clientId,
	};
}

function bool(v: FormDataEntryValue | null): boolean {
	return v === "on" || v === "true" || v === "1";
}
function str(v: FormDataEntryValue | null | undefined): string | undefined {
	return typeof v === "string" && v.trim() !== "" ? v : undefined;
}
function dateStr(v: FormDataEntryValue | null): Date | null {
	if (typeof v !== "string" || !v) return null;
	// Accept dd/mm/yyyy or ISO-like yyyy-mm-dd
	const ddmmyyyy = /^(\d{2})\/(\d{2})\/(\d{4})$/;
	const m = v.match(ddmmyyyy);
	if (m) {
		const [, dd, mm, yyyy] = m;
		const d = new Date(Number(yyyy), Number(mm) - 1, Number(dd));
		return isNaN(d.getTime()) ? null : d;
	}
	const d = new Date(v);
	return isNaN(d.getTime()) ? null : d;
}
function num(v: FormDataEntryValue | null): number | null {
	if (typeof v !== "string" || v.trim() === "") return null;
	const n = Number(v);
	return Number.isNaN(n) ? null : n;
}

export async function createTask(formData: FormData) {
	const title = str(formData.get("title"));
	const date = dateStr(formData.get("date"));
	const done = bool(formData.get("done"));
	const notes = str(formData.get("notes"));
	const blocked = str(formData.get("blocked"));
	const objective = str(formData.get("objective"));
	const userId = num(formData.get("userId"));
	const clientId = num(formData.get("clientId"));
	if (!title || !date || userId === null || clientId === null) {
		throw new Error("Campuri lipsa");
	}
	const created = await prisma.task.create({
		data: {
			title,
			date,
			done,
			notes,
			blocked,
			objective,
			user: { connect: { id: userId } },
			client: { connect: { id: clientId } },
		},
		select: { id: true },
	});
	revalidatePath("/taskuri");
	return { id: created.id };
}

export async function updateTask(id: number, formData: FormData) {
	const title = str(formData.get("title"));
	const date = dateStr(formData.get("date"));
	const doneMaybe = formData.get("done");
	const notes = str(formData.get("notes"));
	const blocked = str(formData.get("blocked"));
	const objective = str(formData.get("objective"));
	const userId = num(formData.get("userId"));
	const clientId = num(formData.get("clientId"));
	const data: Partial<Prisma.TaskUpdateInput> = {};
	if (title !== undefined) data.title = title;
	if (date !== null) data.date = date || undefined;
	if (doneMaybe !== null) data.done = bool(doneMaybe);
	if (notes !== undefined) data.notes = notes;
	if (blocked !== undefined) data.blocked = blocked;
	if (objective !== undefined) data.objective = objective;
	if (userId !== null) data.user = { connect: { id: userId } };
	if (clientId !== null) data.client = { connect: { id: clientId } };
	const updated = await prisma.task.update({
		where: { id },
		data,
		select: {
			id: true,
			title: true,
			date: true,
			done: true,
			notes: true,
			blocked: true,
			objective: true,
			userId: true,
			clientId: true,
		},
	});
	revalidatePath(`/taskuri/edit/${id}`);
	revalidatePath("/taskuri");
	return {
		id: updated.id,
		title: updated.title,
		date: toDMY(updated.date),
		done: updated.done,
		notes: updated.notes ?? undefined,
		blocked: updated.blocked ?? undefined,
		objective: updated.objective ?? undefined,
		userId: updated.userId,
		clientId: updated.clientId,
	};
}

export async function deleteTask(id: number) {
	await prisma.task.delete({ where: { id } });
	revalidatePath("/taskuri");
	return { id };
}

export async function getTaskFormOptions() {
	noStore();
	const [users, clients] = await Promise.all([
		prisma.user.findMany({ select: { id: true, email: true, name: true }, orderBy: { email: "asc" } }),
		prisma.client.findMany({ select: { id: true, denumire: true }, orderBy: { denumire: "asc" } }),
	]);
	return {
		users: users.map((u) => ({ id: u.id, label: u.name || u.email })),
		clients: clients.map((c) => ({ id: c.id, label: c.denumire })),
	};
}
