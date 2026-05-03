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
  const res = await clientServiceFetch(`/api/clients/${clientId}/details`, { cache: "no-store" })
  if (!res.ok) return null
  const d = await res.json()
  return {
    registruUC: !!d.ucRegistry,
    registruEvFiscala: d.fiscalEvidenceRegistry ?? "NU_E_CAZUL",
    ofSpalareBani: !!d.moneyLaunderingOffice,
    regulamentOrdineInterioara: !!d.internalRules,
    manualPoliticiContabile: !!d.accountingPoliciesManual,
    adresaRevisal: !!d.revisalAddress,
    parolaITM: d.itmPassword ?? undefined,
    depunereDeclaratiiOnline: !!d.onlineDeclarations,
    accesDosarFiscal: d.fiscalFileAccess ?? "NU_E_CAZUL",
  }
}

export async function upsertClientDetalii(clientId: number, formData: FormData): Promise<DetaliiValues> {
  "use server";
  const bool = (v: FormDataEntryValue | null) => v === "on" || v === "true" || v === "1"
  const str = (v: FormDataEntryValue | null) =>
    typeof v === "string" && v.trim() !== "" ? v : undefined

  const body = {
    ucRegistry: bool(formData.get("registruUC")),
    fiscalEvidenceRegistry: formData.get("registruEvFiscala") ?? "NU_E_CAZUL",
    moneyLaunderingOffice: bool(formData.get("ofSpalareBani")),
    internalRules: bool(formData.get("regulamentOrdineInterioara")),
    accountingPoliciesManual: bool(formData.get("manualPoliticiContabile")),
    revisalAddress: bool(formData.get("adresaRevisal")),
    itmPassword: str(formData.get("parolaITM")) ?? null,
    onlineDeclarations: bool(formData.get("depunereDeclaratiiOnline")),
    fiscalFileAccess: formData.get("accesDosarFiscal") ?? "NU_E_CAZUL",
  }

  const res = await clientServiceFetch(`/api/clients/${clientId}/details`, {
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
    registruUC: !!d.ucRegistry,
    registruEvFiscala: d.fiscalEvidenceRegistry ?? "NU_E_CAZUL",
    ofSpalareBani: !!d.moneyLaunderingOffice,
    regulamentOrdineInterioara: !!d.internalRules,
    manualPoliticiContabile: !!d.accountingPoliciesManual,
    adresaRevisal: !!d.revisalAddress,
    parolaITM: d.itmPassword ?? undefined,
    depunereDeclaratiiOnline: !!d.onlineDeclarations,
    accesDosarFiscal: d.fiscalFileAccess ?? "NU_E_CAZUL",
  }
}
