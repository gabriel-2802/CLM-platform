"use server"

import { contractsFetch } from "@/lib/auth/contracts-fetch"
import { clientServiceFetch } from "@/lib/client-service-fetch"
import { getSession } from "@/lib/auth"
import { getUsers } from "@/lib/user-service-client"
import { type Row } from "@/actions/clients"

function toISODate(d?: string | null): string | undefined {
  if (!d) return undefined
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return undefined
  return dt.toISOString().slice(0, 10)
}

export async function getContractRows(): Promise<Row[]> {
  const session = await getSession()
  const token = (session?.user as any)?.serviceToken ?? ""

  const contractsRes = await contractsFetch("/api/contracts/all?page=0&size=500", { cache: "no-store" })
  if (!contractsRes.ok || contractsRes.status === 204) return []
  const contracts: any[] = await contractsRes.json()

  const clientIds = [...new Set(contracts.map((c: any) => c.clientId).filter(Boolean))] as number[]

  const [clientDataArray, allUsers] = await Promise.all([
    Promise.all(
      clientIds.map((id) =>
        clientServiceFetch(`/api/clients/${id}`, { cache: "no-store" })
          .then((r) => (r.ok ? r.json() : null))
          .catch(() => null)
      )
    ),
    getUsers(token),
  ])

  const clientMap = new Map<number, any>(
    clientDataArray.filter(Boolean).map((c: any) => [c.id, c])
  )
  const userMap = new Map(allUsers.map((u: any) => [String(u.id), u]))

  return contracts.map((contract: any) => {
    const client = clientMap.get(contract.clientId) ?? {}
    const userIds: string[] = (client.userClients ?? []).map((uc: any) => String(uc.userId))

    return {
      id: contract.clientId ?? 0,
      name: client.name ?? `Client #${contract.clientId}`,
      tip: client.type ?? "",
      cui: client.taxId,
      adresa: client.address,
      administratie: client.administration,
      deLa: toISODate(contract.contractStartDate),
      panaLa: toISODate(contract.contractEndDate ?? contract.terminationDate),
      users: userIds
        .map((id) => {
          const u = userMap.get(id) as any
          return u ? (u.name || u.email) : null
        })
        .filter((s): s is string => !!s)
        .sort((a, b) => a.localeCompare(b)),
      contractId: contract.id ? Number(contract.id) : undefined,
      contractStatus: contract.contractStatus ?? undefined,
      contractStartDate: toISODate(contract.contractStartDate),
      contractEndDate: toISODate(contract.contractEndDate ?? contract.terminationDate),
      contractValue: contract.contractValue != null ? Number(contract.contractValue) : undefined,
      tarifConta: contract.contractValue != null ? Number(contract.contractValue) : undefined,
      tarifBilant: contract.contractBalance != null ? Number(contract.contractBalance) : undefined,
      autoRenew: contract.autoRenew != null ? Boolean(contract.autoRenew) : undefined,
    }
  })
}
