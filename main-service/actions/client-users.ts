"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";
import { getSession } from "@/lib/auth";
import { getUsers } from "@/lib/user-service-client";

export type SimpleUser = { id: number; label: string };

export async function getClientUserAssignments(clientId: number): Promise<{ assigned: SimpleUser[]; available: SimpleUser[] }> {
  const session = await getSession()
  const token = (session?.user as any)?.serviceToken ?? ""

  const [allUsers, assignedRes] = await Promise.all([
    getUsers(token),
    clientServiceFetch(`/api/clients/${clientId}/users`, { cache: "no-store" }),
  ])

  if (!assignedRes.ok) return { assigned: [], available: allUsers.map((u: any) => ({ id: u.id, label: u.name || u.email })) }

  const assignedList = await assignedRes.json()
  const assignedIds = new Set<number>(assignedList?.userIds ?? [])

  const assigned: SimpleUser[] = allUsers
    .filter((u: any) => assignedIds.has(u.id))
    .map((u: any) => ({ id: u.id, label: u.name || u.email }))
    .sort((a: SimpleUser, b: SimpleUser) => a.label.localeCompare(b.label))

  const available: SimpleUser[] = allUsers
    .filter((u: any) => !assignedIds.has(u.id))
    .map((u: any) => ({ id: u.id, label: u.name || u.email }))
    .sort((a: SimpleUser, b: SimpleUser) => a.label.localeCompare(b.label))

  return { assigned, available }
}

export async function addUserToClient(clientId: number, userId: number) {
  const res = await clientServiceFetch(`/api/clients/${clientId}/users/${userId}`, {
    method: "POST",
  })
  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to assign user: ${err}`)
  }
  return { clientId, userId }
}

export async function removeUserFromClient(clientId: number, userId: number) {
  const res = await clientServiceFetch(`/api/clients/${clientId}/users/${userId}`, {
    method: "DELETE",
  })
  if (!res.ok && res.status !== 404) {
    throw new Error("Failed to remove user from client")
  }
  return { clientId, userId }
}
