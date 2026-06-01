"use server"

import { clientServiceFetch } from "@/lib/client-service-fetch"
import { getSession } from "@/lib/auth"
import { getUsers } from "@/lib/user-service-client"
import { getContractByClientId } from "@/actions/contracts"

export type Row = {
  id: number
  name: string
  tip: string
  cui?: string
  adresa?: string
  administratie?: string
  dataVerificarii?: string
  platitorTVA?: string
  deLa?: string
  panaLa?: string
  users?: string[]
  tarifConta?: number
  tarifBilant?: number
  contractGen?: string
  contractSemnat?: string
  contractId?: number
  contractStatus?: string
  contractStartDate?: string
  contractEndDate?: string
  terminationDate?: string
  contractValue?: number
  autoRenew?: boolean
  probleme?: string[]
}

export type ClientDetails = {
  id: number
  name: string
  tip: string
  deLa?: string
  panaLa?: string
  probleme?: string[]
  denumire: string
  cui: string
  activa: boolean
  dataVerificarii?: string
  adresa?: string
  administratie: string
  impozit: string | null
  platitorTVA: string
  tvaLaIncasare: boolean | null
  areCodTVAUE: boolean | null
  codTVAUE?: string
  operatiuneUE: boolean | null
  dividende: boolean | null
  salariati: string | null
  casaDeMarcat: boolean | null
  dataExpSediuSocial?: string
  dataExpMandatAdmin?: string
  dataCertificatFiscal?: string
  dataFisaPlatitor?: string
  dataVectFiscal?: string
  detalii?: {
    manualPoliticiContabile: boolean
    regulamentOrdineInterioara: boolean
    ofSpalareBani: boolean
    registruUC: boolean
  }
}

function toISODate(d?: string | null): string | undefined {
  if (!d) return undefined
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return undefined
  return dt.toISOString().slice(0, 10)
}

async function fetchAllClients() {
  const params = new URLSearchParams()
  params.append("request.page", "0")
  params.append("request.size", "1000")
  const res = await clientServiceFetch(`/api/clients?${params}`, { cache: "no-store" })
  if (!res.ok) throw new Error("Failed to fetch clients")
  const data = await res.json()
  return data.content ?? data ?? []
}

export async function getClientRows(): Promise<Row[]> {
  const session = await getSession()
  const token = (session?.user as any)?.serviceToken ?? ""

  const [clients, allUsers] = await Promise.all([
    fetchAllClients(),
    getUsers(token),
  ])

  const [contractEntries, userAssignmentEntries] = await Promise.all([
    Promise.all(
      clients.map(async (c: any) => ({
        clientId: c.id,
        contract: await getContractByClientId(c.id),
      }))
    ),
    Promise.all(
      clients.map(async (c: any) => {
        const res = await clientServiceFetch(`/api/clients/${c.id}/users`, { cache: "no-store" }).catch(() => null)
        if (!res || !res.ok) return { clientId: c.id, userIds: [] as number[] }
        const data = await res.json().catch(() => ({}))
        return { clientId: c.id, userIds: (data.userIds ?? []) as number[] }
      })
    ),
  ])

  const contractMap = new Map(
    contractEntries
      .filter((entry) => entry.contract)
      .map((entry) => [entry.clientId, entry.contract!])
  )

  const userAssignmentMap = new Map<number, number[]>(
    userAssignmentEntries.map((e) => [e.clientId, e.userIds])
  )

  const userMap = new Map(allUsers.map((u: any) => [String(u.id), u]))

  return clients.map((c: any) => {
    const workPoints: any[] = c.workPoints ?? []

    const earliestValidFrom = workPoints
      .map((p: any) => p.validFrom)
      .filter(Boolean)
      .sort()[0]

    const latestValidTo = workPoints
      .map((p: any) => p.validTo)
      .filter(Boolean)
      .sort()
      .at(-1)

    const details = c.details ?? {}
    const problems: string[] = []
    if (!details.accountingPoliciesManual) problems.push("Manual pol. contabile")
    if (!details.internalRules) problems.push("Regulament OI")
    if (!details.moneyLaunderingOffice) problems.push("Of spalare bani")
    if (!details.ucRegistry) problems.push("Registru UC")

    const userIds: string[] = (userAssignmentMap.get(c.id) ?? []).map(String)

    const contract = contractMap.get(c.id)
    const contractId = contract?.id ?? c.contractId ?? c.contract?.id ?? c.currentContract?.id
    const contractStatus =
      contract?.contractStatus ?? c.contractStatus ?? c.contract?.contractStatus ?? c.currentContract?.contractStatus
    const contractSemnat =
      c.contractSemnat ?? c.contract?.signedFileName ?? c.contract?.signedDocumentName ?? c.signedContractName
    const contractStartDate = toISODate(contract?.contractStartDate ?? null)
    const contractEndDate = toISODate(contract?.contractEndDate ?? null)
    const contractValue = contract?.contractValue ?? null
    const contractBalance = contract?.contractBalance ?? null
    const autoRenew = contract?.autoRenew ?? null

    return {
      id: c.id,
      name: c.name,
      tip: c.type,
      cui: c.taxId,
      adresa: c.address,
      administratie: c.administration,
      dataVerificarii: toISODate(c.verificationDate),
      platitorTVA: c.vatPayer,
      deLa: toISODate(earliestValidFrom ?? c.verificationDate) ?? contractStartDate ?? undefined,
      panaLa: toISODate(latestValidTo) ?? contractEndDate ?? undefined,
      users: userIds
        .map((id) => {
          const u = userMap.get(String(id)) as any
          return u ? (u.name || u.email) : null
        })
        .filter((s): s is string => !!s)
        .sort((a, b) => a.localeCompare(b)),
      contractId: contractId ? Number(contractId) : undefined,
      contractStatus: contractStatus ?? undefined,
      contractSemnat: contractSemnat ?? undefined,
      contractStartDate: contractStartDate ?? undefined,
      contractEndDate: contractEndDate ?? undefined,
      tarifConta: contractValue != null ? Number(contractValue) : undefined,
      tarifBilant: contractBalance != null ? Number(contractBalance) : undefined,
      contractValue: contractValue != null ? Number(contractValue) : undefined,
      autoRenew: autoRenew != null ? Boolean(autoRenew) : undefined,
      probleme: problems.length ? problems : undefined,
    }
  })
}

export async function getClientList(): Promise<{ id: number; name: string }[]> {
  const clients: any[] = await fetchAllClients().catch(() => [])
  return clients
    .map((c: any) => ({ id: c.id, name: c.name }))
    .sort((a, b) => a.name.localeCompare(b.name))
}

export async function getClient(id: number): Promise<ClientDetails | null> {
  const res = await clientServiceFetch(`/api/clients/${id}`, { cache: "no-store" })
  if (!res.ok) return null
  const c = await res.json()

  const workPoints: any[] = c.workPoints ?? []
  const earliestValidFrom = workPoints.map((p: any) => p.validFrom).filter(Boolean).sort()[0]
  const latestValidTo = workPoints.map((p: any) => p.validTo).filter(Boolean).sort().at(-1)

  const details = c.details ?? {}
  const problems: string[] = []
  if (!details.accountingPoliciesManual) problems.push("Manual pol. contabile")
  if (!details.internalRules) problems.push("Regulament OI")
  if (!details.moneyLaunderingOffice) problems.push("Of spalare bani")
  if (!details.ucRegistry) problems.push("Registru UC")

  return {
    id: c.id,
    name: c.name,
    tip: c.type,
    deLa: toISODate(earliestValidFrom ?? c.verificationDate),
    panaLa: toISODate(latestValidTo),
    denumire: c.name,
    cui: c.taxId,
    activa: c.active ?? true,
    dataVerificarii: toISODate(c.verificationDate),
    adresa: c.address,
    administratie: c.administration,
    impozit: c.taxType ?? null,
    platitorTVA: c.vatPayer,
    tvaLaIncasare: c.vatOnCollection ?? null,
    areCodTVAUE: c.hasEuVatCode ?? null,
    codTVAUE: c.euVatCode ?? undefined,
    operatiuneUE: c.euOperation ?? null,
    dividende: c.dividends ?? null,
    salariati: c.employees ?? null,
    casaDeMarcat: c.cashRegister ?? null,
    dataExpSediuSocial: toISODate(c.hqExpirationDate),
    dataExpMandatAdmin: toISODate(c.adminMandateExpiration),
    dataCertificatFiscal: toISODate(c.fiscalCertificateDate),
    dataFisaPlatitor: toISODate(c.payerSheetDate),
    dataVectFiscal: toISODate(c.fiscalVectorDate),
    probleme: problems.length ? problems : undefined,
    detalii: details.id
      ? {
          manualPoliticiContabile: !!details.accountingPoliciesManual,
          regulamentOrdineInterioara: !!details.internalRules,
          ofSpalareBani: !!details.moneyLaunderingOffice,
          registruUC: !!details.ucRegistry,
        }
      : undefined,
  }
}

export async function getClientTemplateSource(id: number): Promise<Record<string, unknown> | null> {
  const res = await clientServiceFetch(`/api/clients/${id}`, { cache: "no-store" })
  if (!res.ok) return null
  return (await res.json()) as Record<string, unknown>
}

export async function createClient(formData: FormData) {
  "use server"
  const body = {
    name: formData.get("denumire"),
    type: formData.get("tip"),
    taxId: formData.get("cui"),
    active: formData.get("activa") === "on" || formData.get("activa") === "true",
    verificationDate: formData.get("dataVerificarii") || null,
    address: formData.get("adresa") || null,
    administration: formData.get("administratie"),
    taxType: formData.get("impozit") || null,
    vatPayer: formData.get("platitorTVA"),
    vatOnCollection: formData.get("tvaLaIncasare") === "on" || formData.get("tvaLaIncasare") === "true",
    hasEuVatCode: formData.get("areCodTVAUE") === "on" || formData.get("areCodTVAUE") === "true",
    euVatCode: formData.get("codTVAUE") || null,
    euOperation: formData.get("operatiuneUE") === "on" || formData.get("operatiuneUE") === "true",
    dividends: formData.get("dividende") === "on" || formData.get("dividende") === "true",
    employees: formData.get("salariati") || null,
    cashRegister: formData.get("casaDeMarcat") === "on" || formData.get("casaDeMarcat") === "true",
    hqExpirationDate: formData.get("dataExpSediuSocial") || null,
    adminMandateExpiration: formData.get("dataExpMandatAdmin") || null,
    fiscalCertificateDate: formData.get("dataCertificatFiscal") || null,
    payerSheetDate: formData.get("dataFisaPlatitor") || null,
    fiscalVectorDate: formData.get("dataVectFiscal") || null,
  }

  const res = await clientServiceFetch("/api/clients", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to create client: ${err}`)
  }

  const created = await res.json()
  return { id: created.id }
}

export async function updateClient(id: number, formData: FormData) {
  "use server"
  const body: Record<string, any> = {}

  const fields: Array<[string, string]> = [
    ["denumire", "name"],
    ["tip", "type"],
    ["cui", "taxId"],
    ["adresa", "address"],
    ["administratie", "administration"],
    ["impozit", "taxType"],
    ["platitorTVA", "vatPayer"],
    ["codTVAUE", "euVatCode"],
    ["salariati", "employees"],
    ["dataVerificarii", "verificationDate"],
    ["dataExpSediuSocial", "hqExpirationDate"],
    ["dataExpMandatAdmin", "adminMandateExpiration"],
    ["dataCertificatFiscal", "fiscalCertificateDate"],
    ["dataFisaPlatitor", "payerSheetDate"],
    ["dataVectFiscal", "fiscalVectorDate"],
  ]
  for (const [key, field] of fields) {
    const v = formData.get(key)
    if (v !== null) body[field] = v === "" ? null : v
  }

  const boolFields: Array<[string, string]> = [
    ["activa", "active"],
    ["tvaLaIncasare", "vatOnCollection"],
    ["areCodTVAUE", "hasEuVatCode"],
    ["operatiuneUE", "euOperation"],
    ["dividende", "dividends"],
    ["casaDeMarcat", "cashRegister"],
  ]
  for (const [key, field] of boolFields) {
    const v = formData.get(key)
    if (v !== null) body[field] = v === "on" || v === "true" || v === "1"
  }

  const res = await clientServiceFetch(`/api/clients/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to update client: ${err}`)
  }

  const updated = await res.json()
  return {
    id: updated.id,
    denumire: updated.name,
    tip: updated.type,
    cui: updated.taxId,
    activa: updated.active,
    dataVerificarii: toISODate(updated.verificationDate),
    adresa: updated.address,
    administratie: updated.administration,
    impozit: updated.taxType ?? null,
    platitorTVA: updated.vatPayer,
    tvaLaIncasare: updated.vatOnCollection ?? null,
    areCodTVAUE: updated.hasEuVatCode ?? null,
    codTVAUE: updated.euVatCode ?? undefined,
    operatiuneUE: updated.euOperation ?? null,
    dividende: updated.dividends ?? null,
    salariati: updated.employees ?? null,
    casaDeMarcat: updated.cashRegister ?? null,
    dataExpSediuSocial: toISODate(updated.hqExpirationDate),
    dataExpMandatAdmin: toISODate(updated.adminMandateExpiration),
    dataCertificatFiscal: toISODate(updated.fiscalCertificateDate),
    dataFisaPlatitor: toISODate(updated.payerSheetDate),
    dataVectFiscal: toISODate(updated.fiscalVectorDate),
  }
}
