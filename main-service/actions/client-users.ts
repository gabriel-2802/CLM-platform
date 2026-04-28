"use server";
import { prisma } from "@/lib/prisma";

import { revalidatePath, unstable_noStore as noStore } from "next/cache";
import { getSession } from "@/lib/auth";
import { getUsers } from "@/lib/user-service-client";


export type SimpleUser = { id: number; label: string };

export async function getClientUserAssignments(clientId: number): Promise<{ assigned: SimpleUser[]; available: SimpleUser[] }> {
  noStore();
  const session = await getSession();
  const token = (session?.user as unknown as { serviceToken?: string })?.serviceToken ?? "";

  const [allUsers, assignedLinks] = await Promise.all([
    getUsers(token),
    prisma.userClient.findMany({ where: { clientId }, select: { userId: true } }),
  ]);

  const assignedIds = new Set(assignedLinks.map((l) => l.userId));

  const assigned: SimpleUser[] = assignedLinks
    .map((l) => {
      const u = allUsers.find((u) => u.id === l.userId);
      return u ? { id: u.id, label: u.name || u.email } : null;
    })
    .filter((x): x is SimpleUser => x !== null)
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
