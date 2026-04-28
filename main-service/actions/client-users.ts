"use server";

import { PrismaClient } from "@/lib/generated/prisma-client";
import { revalidatePath, unstable_noStore as noStore } from "next/cache";

const prisma = new PrismaClient();

export type SimpleUser = { id: number; label: string };

export async function getClientUserAssignments(clientId: number): Promise<{ assigned: SimpleUser[]; available: SimpleUser[] }> {
  noStore();
  const [allUsers, assignedLinks] = await Promise.all([
    prisma.user.findMany({ select: { id: true, name: true, email: true }, orderBy: { email: "asc" } }),
    prisma.userClient.findMany({ where: { clientId }, select: { user: { select: { id: true, name: true, email: true } } } }),
  ]);
  const assignedIds = new Set(assignedLinks.map((l) => l.user.id));
  const assigned: SimpleUser[] = assignedLinks
    .map((l) => ({ id: l.user.id, label: l.user.name || l.user.email }))
    .sort((a, b) => a.label.localeCompare(b.label));
  const available: SimpleUser[] = allUsers
    .filter((u) => !assignedIds.has(u.id))
    .map((u) => ({ id: u.id, label: u.name || u.email }))
    .sort((a, b) => a.label.localeCompare(b.label));
  return { assigned, available };
}

export async function addUserToClient(clientId: number, userId: number) {
  await prisma.userClient.upsert({
    where: { userId_clientId: { userId, clientId } },
    update: {},
    create: { clientId, userId },
  });
  revalidatePath(`/clients/edit/${clientId}`);
  return { clientId, userId };
}

export async function removeUserFromClient(clientId: number, userId: number) {
  await prisma.userClient.delete({ where: { userId_clientId: { userId, clientId } } });
  revalidatePath(`/clients/edit/${clientId}`);
  return { clientId, userId };
}
