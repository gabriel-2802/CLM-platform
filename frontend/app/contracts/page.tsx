import { getContractRows } from "@/actions/contract-rows"
import ClientsTable from "@/components/clients/clients-table"
import { NewContractModal } from "@/components/contracts/new-contract-modal"

export default async function ContractsPage() {
  const rows = await getContractRows()
  return <ClientsTable rows={rows} headerExtra={<NewContractModal />} />
}
