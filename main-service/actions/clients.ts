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
  const res = await clientServiceFetch("/api/clients?size=1000&page=0", { cache: "no-store" })
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

  const contractEntries = await Promise.all(
    clients.map(async (c: any) => ({
      clientId: c.id,
      contract: await getContractByClientId(c.id),
    }))
  )
  const contractMap = new Map(
    contractEntries
      .filter((entry) => entry.contract)
      .map((entry) => [entry.clientId, entry.contract!])
  )

  const userMap = new Map(allUsers.map((u: any) => [u.id, u]))

  return clients.map((c: any) => {
    const workPoints: any[] = c.workPoints ?? []

    const earliestDeLa = workPoints
      .map((p: any) => p.deLa)
      .filter(Boolean)
      .sort()[0]

    const latestPanaLa = workPoints
      .map((p: any) => p.panaLa)
      .filter(Boolean)
      .sort()
      .at(-1)

    const details = c.details ?? {}
    const problems: string[] = []
    if (!details.manualPoliticiContabile) problems.push("Manual pol. contabile")
    if (!details.regulamentOrdineInterioara) problems.push("Regulament OI")
    if (!details.ofSpalareBani) problems.push("Of spalare bani")
    if (!details.registruUC) problems.push("Registru UC")

    const userIds: number[] = (c.userClients ?? []).map((uc: any) => uc.userId)

    const contract = contractMap.get(c.id)
    const contractId = contract?.id ?? c.contractId ?? c.contract?.id ?? c.currentContract?.id
    const contractStatus =
      contract?.contractStatus ?? c.contractStatus ?? c.contract?.contractStatus ?? c.currentContract?.contractStatus
    const contractSemnat =
      c.contractSemnat ?? c.contract?.signedFileName ?? c.contract?.signedDocumentName ?? c.signedContractName
    const contractStartDate = toISODate(contract?.contractStartDate ?? null)
    const contractEndDate = toISODate(contract?.contractEndDate ?? null)
    const contractValue = contract?.contractValue ?? null
    const autoRenew = contract?.autoRenew ?? null

    return {
      id: c.id,
      name: c.denumire ?? c.name,
      tip: c.tip ?? c.type,
      cui: c.cui ?? c.taxId,
      adresa: c.adresa ?? c.address,
      administratie: c.administratie ?? c.administration,
      deLa: toISODate(earliestDeLa ?? c.dataVerificarii ?? c.verificationDate),
      panaLa: toISODate(latestPanaLa),
      users: userIds
        .map((id) => {
          const u = userMap.get(id) as any
          return u ? (u.name || u.email) : null
        })
        .filter((s): s is string => !!s)
        .sort((a, b) => a.localeCompare(b)),
      contractId: contractId ? Number(contractId) : undefined,
      contractStatus: contractStatus ?? undefined,
      contractSemnat: contractSemnat ?? undefined,
      contractStartDate: contractStartDate ?? undefined,
      contractEndDate: contractEndDate ?? undefined,
      contractValue: contractValue != null ? Number(contractValue) : undefined,
      autoRenew: autoRenew != null ? Boolean(autoRenew) : undefined,
      probleme: problems.length ? problems : undefined,
    }
  })
}

export async function getClient(id: number): Promise<ClientDetails | null> {
  const res = await clientServiceFetch(`/api/clients/${id}`, { cache: "no-store" })
  if (!res.ok) return null
  const c = await res.json()

  const workPoints: any[] = c.workPoints ?? []
  const earliestDeLa = workPoints.map((p: any) => p.deLa).filter(Boolean).sort()[0]
  const latestPanaLa = workPoints.map((p: any) => p.panaLa).filter(Boolean).sort().at(-1)

  const details = c.details ?? {}
  const problems: string[] = []
  if (!details.manualPoliticiContabile) problems.push("Manual pol. contabile")
  if (!details.regulamentOrdineInterioara) problems.push("Regulament OI")
  if (!details.ofSpalareBani) problems.push("Of spalare bani")
  if (!details.registruUC) problems.push("Registru UC")

  return {
    id: c.id,
    name: c.denumire ?? c.name,
    tip: c.tip ?? c.type,
    deLa: toISODate(earliestDeLa ?? c.dataVerificarii ?? c.verificationDate),
    panaLa: toISODate(latestPanaLa),
    denumire: c.denumire ?? c.name,
    cui: c.cui ?? c.taxId,
    activa: c.activa ?? c.active ?? true,
    dataVerificarii: toISODate(c.dataVerificarii ?? c.verificationDate),
    adresa: c.adresa ?? c.address,
    administratie: c.administratie ?? c.administration,
    impozit: c.impozit ?? c.taxType ?? null,
    platitorTVA: c.platitorTVA ?? c.vatPayer,
    tvaLaIncasare: c.tvaLaIncasare ?? c.vatOnCollection ?? null,
    areCodTVAUE: c.areCodTVAUE ?? c.hasEuVatCode ?? null,
    codTVAUE: c.codTVAUE ?? c.euVatCode ?? undefined,
    operatiuneUE: c.operatiuneUE ?? c.euOperation ?? null,
    dividende: c.dividende ?? c.dividends ?? null,
    salariati: c.salariati ?? c.employees ?? null,
    casaDeMarcat: c.casaDeMarcat ?? c.cashRegister ?? null,
    dataExpSediuSocial: toISODate(c.dataExpSediuSocial ?? c.hqExpirationDate),
    dataExpMandatAdmin: toISODate(c.dataExpMandatAdmin ?? c.adminMandateExpiration),
    dataCertificatFiscal: toISODate(c.dataCertificatFiscal ?? c.fiscalCertificateDate),
    dataFisaPlatitor: toISODate(c.dataFisaPlatitor ?? c.payerSheetDate),
    dataVectFiscal: toISODate(c.dataVectFiscal ?? c.fiscalVectorDate),
    probleme: problems.length ? problems : undefined,
    detalii: details.id
      ? {
          manualPoliticiContabile: !!details.manualPoliticiContabile,
          regulamentOrdineInterioara: !!details.regulamentOrdineInterioara,
          ofSpalareBani: !!details.ofSpalareBani,
          registruUC: !!details.registruUC,
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
    denumire: formData.get("denumire"),
    tip: formData.get("tip"),
    cui: formData.get("cui"),
    activa: formData.get("activa") === "on" || formData.get("activa") === "true",
    dataVerificarii: formData.get("dataVerificarii") || null,
    adresa: formData.get("adresa") || null,
    administratie: formData.get("administratie"),
    impozit: formData.get("impozit") || null,
    platitorTVA: formData.get("platitorTVA"),
    tvaLaIncasare: formData.get("tvaLaIncasare") === "on" || formData.get("tvaLaIncasare") === "true",
    areCodTVAUE: formData.get("areCodTVAUE") === "on" || formData.get("areCodTVAUE") === "true",
    codTVAUE: formData.get("codTVAUE") || null,
    operatiuneUE: formData.get("operatiuneUE") === "on" || formData.get("operatiuneUE") === "true",
    dividende: formData.get("dividende") === "on" || formData.get("dividende") === "true",
    salariati: formData.get("salariati") || null,
    casaDeMarcat: formData.get("casaDeMarcat") === "on" || formData.get("casaDeMarcat") === "true",
    dataExpSediuSocial: formData.get("dataExpSediuSocial") || null,
    dataExpMandatAdmin: formData.get("dataExpMandatAdmin") || null,
    dataCertificatFiscal: formData.get("dataCertificatFiscal") || null,
    dataFisaPlatitor: formData.get("dataFisaPlatitor") || null,
    dataVectFiscal: formData.get("dataVectFiscal") || null,
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
    ["denumire", "denumire"],
    ["tip", "tip"],
    ["cui", "cui"],
    ["adresa", "adresa"],
    ["administratie", "administratie"],
    ["impozit", "impozit"],
    ["platitorTVA", "platitorTVA"],
    ["codTVAUE", "codTVAUE"],
    ["salariati", "salariati"],
    ["dataVerificarii", "dataVerificarii"],
    ["dataExpSediuSocial", "dataExpSediuSocial"],
    ["dataExpMandatAdmin", "dataExpMandatAdmin"],
    ["dataCertificatFiscal", "dataCertificatFiscal"],
    ["dataFisaPlatitor", "dataFisaPlatitor"],
    ["dataVectFiscal", "dataVectFiscal"],
  ]
  for (const [key, field] of fields) {
    const v = formData.get(key)
    if (v !== null) body[field] = v === "" ? null : v
  }

  const boolFields = ["activa", "tvaLaIncasare", "areCodTVAUE", "operatiuneUE", "dividende", "casaDeMarcat"]
  for (const key of boolFields) {
    const v = formData.get(key)
    if (v !== null) body[key] = v === "on" || v === "true" || v === "1"
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
    denumire: updated.denumire ?? updated.name,
    tip: updated.tip ?? updated.type,
    cui: updated.cui ?? updated.taxId,
    activa: updated.activa ?? updated.active,
    dataVerificarii: toISODate(updated.dataVerificarii ?? updated.verificationDate),
    adresa: updated.adresa ?? updated.address,
    administratie: updated.administratie ?? updated.administration,
    impozit: updated.impozit ?? updated.taxType ?? null,
    platitorTVA: updated.platitorTVA ?? updated.vatPayer,
    tvaLaIncasare: updated.tvaLaIncasare ?? updated.vatOnCollection ?? null,
    areCodTVAUE: updated.areCodTVAUE ?? updated.hasEuVatCode ?? null,
    codTVAUE: updated.codTVAUE ?? updated.euVatCode ?? undefined,
    operatiuneUE: updated.operatiuneUE ?? updated.euOperation ?? null,
    dividende: updated.dividende ?? updated.dividends ?? null,
    salariati: updated.salariati ?? updated.employees ?? null,
    casaDeMarcat: updated.casaDeMarcat ?? updated.cashRegister ?? null,
    dataExpSediuSocial: toISODate(updated.dataExpSediuSocial ?? updated.hqExpirationDate),
    dataExpMandatAdmin: toISODate(updated.dataExpMandatAdmin ?? updated.adminMandateExpiration),
    dataCertificatFiscal: toISODate(updated.dataCertificatFiscal ?? updated.fiscalCertificateDate),
    dataFisaPlatitor: toISODate(updated.dataFisaPlatitor ?? updated.payerSheetDate),
    dataVectFiscal: toISODate(updated.dataVectFiscal ?? updated.fiscalVectorDate),
  }
}
