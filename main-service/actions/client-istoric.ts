"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";

export type IstoricValues = {
  id?: number;
  anul: number;
  cifraAfaceri: number;
  inventar: boolean;
  bilantSemIun: string;
  bilantAnual: string;
};

function mapIstoric(i: any): IstoricValues {
  return {
    id: i.id,
    anul: i.year,
    cifraAfaceri: Number(i.turnover ?? 0),
    inventar: !!i.inventory,
    bilantSemIun: i.juneSemesterBalance ?? "NU_E_CAZUL",
    bilantAnual: i.annualBalance ?? "NU_E_CAZUL",
  }
}

export async function getClientIstoric(clientId: number): Promise<IstoricValues | null> {
  const res = await clientServiceFetch(`/api/clients/${clientId}/histories`, { cache: "no-store" })
  if (!res.ok) return null
  const list = await res.json()
  if (!list || list.length === 0) return null
  const sorted = [...list].sort((a: any, b: any) => b.year - a.year)
  return mapIstoric(sorted[0])
}

export async function getClientIstoricList(clientId: number): Promise<IstoricValues[]> {
  const res = await clientServiceFetch(`/api/clients/${clientId}/histories`, { cache: "no-store" })
  if (!res.ok) return []
  const list = await res.json()
  return (list ?? []).map(mapIstoric).sort((a: IstoricValues, b: IstoricValues) => b.anul - a.anul)
}

export async function upsertClientIstoric(clientId: number, formData: FormData): Promise<IstoricValues> {
  "use server";
  const year = parseInt(formData.get("anul") as string ?? "0", 10)
  const turnover = parseFloat(formData.get("cifraAfaceri") as string ?? "0") || 0
  const inventory = formData.get("inventar") === "on" || formData.get("inventar") === "true"
  const juneSemesterBalance = formData.get("bilantSemIun") ?? "NU_E_CAZUL"
  const annualBalance = formData.get("bilantAnual") ?? "NU_E_CAZUL"

  if (!year) throw new Error("Anul este obligatoriu")

  const body = { year, turnover, inventory, juneSemesterBalance, annualBalance }

  const res = await clientServiceFetch(`/api/clients/${clientId}/histories/${year}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to save istoric: ${err}`)
  }

  return mapIstoric(await res.json())
}

export async function deleteClientIstoric(clientId: number, id: number): Promise<{ id: number }> {
  "use server";
  const listRes = await clientServiceFetch(`/api/clients/${clientId}/histories`, { cache: "no-store" })
  if (!listRes.ok) return { id }
  const list = await listRes.json()
  const entry = list.find((i: any) => i.id === id)
  if (!entry) return { id }

  const year = entry.year
  const res = await clientServiceFetch(`/api/clients/${clientId}/histories/${year}`, { method: "DELETE" })
  if (!res.ok && res.status !== 404) {
    throw new Error("Failed to delete istoric")
  }
  return { id }
}
