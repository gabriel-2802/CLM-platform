"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";

export type DetaliiValues = {
  registruUC: boolean
  registruEvFiscala: string
  ofSpalareBani: boolean
  regulamentOrdineInterioara: boolean
  manualPoliticiContabile: boolean
  adresaRevisal: boolean
  parolaITM?: string
  depunereDeclaratiiOnline: boolean
  accesDosarFiscal: string
}

export async function getClientDetalii(clientId: number): Promise<DetaliiValues | null> {
  const res = await clientServiceFetch(`/api/clients/${clientId}/detalii`, { cache: "no-store" })
  if (!res.ok) return null
  const d = await res.json()
  return {
    registruUC: !!d.registruUC,
    registruEvFiscala: d.registruEvFiscala ?? "NU_E_CAZUL",
    ofSpalareBani: !!d.ofSpalareBani,
    regulamentOrdineInterioara: !!d.regulamentOrdineInterioara,
    manualPoliticiContabile: !!d.manualPoliticiContabile,
    adresaRevisal: !!d.adresaRevisal,
    parolaITM: d.parolaITM ?? undefined,
    depunereDeclaratiiOnline: !!d.depunereDeclaratiiOnline,
    accesDosarFiscal: d.accesDosarFiscal ?? "NU_E_CAZUL",
  }
}

export async function upsertClientDetalii(clientId: number, formData: FormData): Promise<DetaliiValues> {
  "use server";
  const bool = (v: FormDataEntryValue | null) => v === "on" || v === "true" || v === "1"
  const str = (v: FormDataEntryValue | null) =>
    typeof v === "string" && v.trim() !== "" ? v : undefined

  const body = {
    registruUC: bool(formData.get("registruUC")),
    registruEvFiscala: formData.get("registruEvFiscala") ?? "NU_E_CAZUL",
    ofSpalareBani: bool(formData.get("ofSpalareBani")),
    regulamentOrdineInterioara: bool(formData.get("regulamentOrdineInterioara")),
    manualPoliticiContabile: bool(formData.get("manualPoliticiContabile")),
    adresaRevisal: bool(formData.get("adresaRevisal")),
    parolaITM: str(formData.get("parolaITM")) ?? null,
    depunereDeclaratiiOnline: bool(formData.get("depunereDeclaratiiOnline")),
    accesDosarFiscal: formData.get("accesDosarFiscal") ?? "NU_E_CAZUL",
  }

  const res = await clientServiceFetch(`/api/clients/${clientId}/detalii`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to save detalii: ${err}`)
  }

  const d = await res.json()
  return {
    registruUC: !!d.registruUC,
    registruEvFiscala: d.registruEvFiscala ?? "NU_E_CAZUL",
    ofSpalareBani: !!d.ofSpalareBani,
    regulamentOrdineInterioara: !!d.regulamentOrdineInterioara,
    manualPoliticiContabile: !!d.manualPoliticiContabile,
    adresaRevisal: !!d.adresaRevisal,
    parolaITM: d.parolaITM ?? undefined,
    depunereDeclaratiiOnline: !!d.depunereDeclaratiiOnline,
    accesDosarFiscal: d.accesDosarFiscal ?? "NU_E_CAZUL",
  }
}
