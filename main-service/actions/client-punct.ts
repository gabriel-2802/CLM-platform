"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";

export type PunctDeLucruValues = {
  id?: number;
  denumire: string;
  deLa: string;
  panaLa?: string;
  administratie: string;
  registruUC: boolean;
  salariati: number;
  cui?: string;
  casaDeMarcat: boolean;
};

function toISODate(d: string | null | undefined): string | undefined {
  if (!d) return undefined
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return undefined
  return dt.toISOString().slice(0, 10)
}

function toDateTime(value: FormDataEntryValue | null): string | null {
  if (!value) return null
  const raw = String(value).trim()
  if (!raw) return null
  // Normalize date-only inputs to ISO date-time for the API
  if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) {
    return `${raw}T00:00:00.000Z`
  }
  const dt = new Date(raw)
  if (isNaN(dt.getTime())) return raw
  return dt.toISOString()
}

function mapPoint(p: any): PunctDeLucruValues {
  return {
    id: p.id,
    denumire: p.denumire ?? p.name,
    deLa: toISODate(p.deLa ?? p.startDate)!,
    panaLa: toISODate(p.panaLa ?? p.endDate),
    administratie: p.administratie ?? p.administration,
    registruUC: !!p.registruUC,
    salariati: p.salariati ?? p.employees ?? 0,
    cui: p.cui ?? p.taxId ?? undefined,
    casaDeMarcat: !!p.casaDeMarcat ?? !!p.cashRegister,
  }
}

export async function getClientPunctDeLucru(clientId: number): Promise<PunctDeLucruValues | null> {
  const res = await clientServiceFetch(`/api/clients/${clientId}/puncte-de-lucru`, { cache: "no-store" })
  if (!res.ok) return null
  const list = await res.json()
  if (!list || list.length === 0) return null
  return mapPoint(list[list.length - 1])
}

export async function getClientPuncteDeLucru(clientId: number): Promise<PunctDeLucruValues[]> {
  const res = await clientServiceFetch(`/api/clients/${clientId}/puncte-de-lucru`, { cache: "no-store" })
  if (!res.ok) return []
  const list = await res.json()
  return (list ?? []).map(mapPoint)
}

export async function upsertClientPunctDeLucru(clientId: number, formData: FormData): Promise<PunctDeLucruValues> {
  "use server";
  const idRaw = formData.get("id")
  const id = typeof idRaw === "string" && idRaw.trim() !== "" ? Number(idRaw) : 0

  const body = {
    denumire: (formData.get("denumire") as string)?.trim() || "",
    deLa: toDateTime(formData.get("deLa")),
    panaLa: toDateTime(formData.get("panaLa")),
    administratie: formData.get("administratie") ?? "SECTOR_1",
    registruUC: formData.get("registruUC") === "on" || formData.get("registruUC") === "true",
    salariati: parseInt(formData.get("salariati") as string ?? "0", 10) || 0,
    cui: formData.get("cui") || null,
    casaDeMarcat: formData.get("casaDeMarcat") === "on" || formData.get("casaDeMarcat") === "true",
  }

  let res: Response
  if (id > 0) {
    res = await clientServiceFetch(`/api/clients/${clientId}/puncte-de-lucru/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    })
  } else {
    res = await clientServiceFetch(`/api/clients/${clientId}/puncte-de-lucru`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    })
  }

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to save punct de lucru: ${err}`)
  }

  return mapPoint(await res.json())
}

export async function deleteClientPunctDeLucru(clientId: number, id: number): Promise<{ id: number }> {
  "use server";
  const res = await clientServiceFetch(`/api/clients/${clientId}/puncte-de-lucru/${id}`, {
    method: "DELETE",
  })
  if (!res.ok && res.status !== 404) {
    throw new Error("Failed to delete punct de lucru")
  }
  return { id }
}
