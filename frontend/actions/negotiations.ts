"use server"

import { negotiationFetch } from "@/lib/auth/negotiation-fetch"
import { getSession } from "@/lib/auth"

export type Negotiation = {
  id: number
  contractId: number
  clientId: number
  proposedValue: number | null
  proposedEndDate: string | null
  status: "DRAFT" | "SENT" | "ACCEPTED" | "REJECTED"
  notes: string | null
  createdByUserId: number
  createdByUserName: string | null
  createdAt: string
  resolvedAt: string | null
}

export type CreateNegotiationPayload = {
  contractId: number
  clientId: number
  proposedValue?: number | null
  proposedEndDate?: string | null
  notes?: string | null
  createdByUserName?: string | null
}

async function extractError(res: Response): Promise<string> {
  const text = await res.text()
  try {
    const json = JSON.parse(text)
    return json.detail ?? json.message ?? text
  } catch {
    return text
  }
}

export async function getNegotiationsForContract(contractId: number): Promise<Negotiation[]> {
  try {
    const res = await negotiationFetch(`/api/negotiations/contract/${contractId}`)
    if (res.status === 204) return []
    if (!res.ok) {
      console.error("Failed to fetch negotiations:", await res.text())
      return []
    }
    return res.json()
  } catch (error) {
    console.error("Error fetching negotiations:", error)
    return []
  }
}

export async function createNegotiation(payload: CreateNegotiationPayload) {
  try {
    const session = await getSession()
    const userId = Number(session?.user?.id)
    const userName = (session?.user as { name?: string | null })?.name
                  ?? (session?.user as { email?: string | null })?.email
                  ?? null

    if (!Number.isInteger(userId) || userId <= 0) {
      return { success: false, error: "Utilizator neautentificat" }
    }

    const res = await negotiationFetch("/api/negotiations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...payload, createdByUserId: userId, createdByUserName: userName }),
    })

    if (!res.ok) {
      return { success: false, error: await extractError(res) }
    }

    return { success: true, data: (await res.json()) as Negotiation }
  } catch (error) {
    console.error("Error creating negotiation:", error)
    return { success: false, error: "Eroare de rețea" }
  }
}

export async function acceptNegotiation(id: number) {
  try {
    const res = await negotiationFetch(`/api/negotiations/${id}/accept`, { method: "PATCH" })
    if (!res.ok) return { success: false, error: await extractError(res) }
    return { success: true, data: (await res.json()) as Negotiation }
  } catch (error) {
    console.error("Error accepting negotiation:", error)
    return { success: false, error: "Eroare de rețea" }
  }
}

export async function rejectNegotiation(id: number, notes?: string) {
  try {
    const res = await negotiationFetch(`/api/negotiations/${id}/reject`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: notes ? JSON.stringify({ notes }) : undefined,
    })
    if (!res.ok) return { success: false, error: await extractError(res) }
    return { success: true, data: (await res.json()) as Negotiation }
  } catch (error) {
    console.error("Error rejecting negotiation:", error)
    return { success: false, error: "Eroare de rețea" }
  }
}
