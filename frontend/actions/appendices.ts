"use server"

import { revalidatePath } from "next/cache"
import { contractsFetch } from "@/lib/auth/contracts-fetch"
import { getSession } from "@/lib/auth"

export type AppendixSummary = {
    id: number
    contractId: number
    templateId?: number | null
    title: string
    appendixStatus: string
    documentFormat?: string | null
    generatedByMail?: string | null
    notes?: string | null
    createdAt?: string | null
}

export type GenerateAppendixPayload = {
    contractId: number
    templateId: number
    title: string
    notes?: string | null
    mappings: Record<string, string>
    signDate?: string | null
    effectiveDate?: string | null
}

type SessionUser = {
    id?: string
    email?: string | null
}

export async function getAppendicesForContract(contractId: number): Promise<AppendixSummary[]> {
    try {
        const res = await contractsFetch(`/api/appendices/contract/${contractId}`, { cache: "no-store" })
        if (res.status === 204 || res.status === 404) return []
        if (!res.ok) return []
        return (await res.json()) as AppendixSummary[]
    } catch {
        return []
    }
}

export async function generateAppendix(payload: GenerateAppendixPayload) {
    try {
        const session = await getSession()
        const user = session?.user as SessionUser | undefined
        const userId = Number(user?.id)

        if (!Number.isInteger(userId) || userId <= 0 || !user?.email) {
            return { success: false, error: "Utilizator neautentificat sau sesiune incompleta" }
        }

        const res = await contractsFetch("/api/appendices/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ...payload, userId, userMail: user.email }),
        })

        if (!res.ok) return { success: false, error: await res.text() }
        const data = await res.json()
        revalidatePath("/clients", "page")
        revalidatePath("/clienti", "page")
        return { success: true, data }
    } catch {
        return { success: false, error: "Network error" }
    }
}

export async function updateContractTerms(
    contractId: number,
    appendixId: number,
    startDate: string,
    contractEndDate?: string,
    value?: number,
    balance?: number,
) {
    try {
        const session = await getSession()
        const userId = Number((session?.user as any)?.id)
        if (!Number.isInteger(userId) || userId <= 0) return { success: false, error: "Utilizator neautentificat" }

        const res = await contractsFetch(`/api/contracts/${contractId}/update-terms`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId,
                appendixId,
                startDate,
                contractEndDate: contractEndDate || null,
                value: value ?? null,
                balance: balance ?? null,
            }),
        })
        if (!res.ok) return { success: false, error: await res.text() }
        const data = await res.json()
        revalidatePath("/clients", "page")
        revalidatePath("/clienti", "page")
        revalidatePath("/contracts", "page")
        return { success: true, data }
    } catch {
        return { success: false, error: "Network error" }
    }
}

export async function terminateAppendix(appendixId: number) {
    try {
        const res = await contractsFetch(`/api/appendices/${appendixId}/terminate`, {
            method: "PATCH",
        })
        if (!res.ok) return { success: false, error: await res.text() }
        const data = await res.json()
        revalidatePath("/clients", "page")
        revalidatePath("/clienti", "page")
        return { success: true, data }
    } catch {
        return { success: false, error: "Network error" }
    }
}

export async function uploadSignedAppendix(appendixId: number, formData: FormData, signDate?: string, effectiveDate?: string) {
    try {
        const session = await getSession()
        const user = session?.user as SessionUser | undefined
        const userId = Number(user?.id)

        const file = formData.get("file") as File | null
        if (!file) return { success: false, error: "Fisier lipsa" }

        const backendFormData = new FormData()
        backendFormData.append("file", file)
        if (Number.isInteger(userId) && userId > 0) backendFormData.append("userId", String(userId))
        if (signDate) backendFormData.append("signDate", signDate)
        if (effectiveDate) backendFormData.append("effectiveDate", effectiveDate)

        const res = await contractsFetch(`/api/appendices/${appendixId}/upload-signed`, {
            method: "POST",
            body: backendFormData,
        })

        if (!res.ok) return { success: false, error: await res.text() }
        const data = await res.json()
        revalidatePath("/clients", "page")
        revalidatePath("/clienti", "page")
        return { success: true, data }
    } catch {
        return { success: false, error: "Network error" }
    }
}

export async function uploadDirectAppendix(contractId: number, title: string, formData: FormData, signDate?: string, effectiveDate?: string) {
    try {
        const session = await getSession()
        const user = session?.user as SessionUser | undefined
        const userId = Number(user?.id)

        const file = formData.get("file") as File | null
        if (!file) return { success: false, error: "Fisier lipsa" }

        const backendFormData = new FormData()
        backendFormData.append("contractId", String(contractId))
        backendFormData.append("title", title)
        backendFormData.append("file", file)
        if (Number.isInteger(userId) && userId > 0) backendFormData.append("userId", String(userId))
        if (user?.email) backendFormData.append("userMail", user.email)
        if (signDate) backendFormData.append("signDate", signDate)
        if (effectiveDate) backendFormData.append("effectiveDate", effectiveDate)

        const res = await contractsFetch("/api/appendices/upload", {
            method: "POST",
            body: backendFormData,
        })

        if (!res.ok) return { success: false, error: await res.text() }
        const data = await res.json()
        revalidatePath("/clients", "page")
        revalidatePath("/clienti", "page")
        return { success: true, data }
    } catch {
        return { success: false, error: "Network error" }
    }
}