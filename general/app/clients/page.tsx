import { getClientRows } from "@/actions/clients";
import ClientsTable from "@/components/clients/clients-table";

export default async function ClientsPage() {
  const rows = await getClientRows();
  return <ClientsTable rows={rows} />
}

