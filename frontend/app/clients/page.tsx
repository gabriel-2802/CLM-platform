import { getClientRows } from "@/actions/clients"
import Link from "next/link"
import { ClientsFilterTable } from "@/components/clients/clients-filter-table"

export const dynamic = "force-dynamic"

export default async function ClientsPage() {
  const rows = await getClientRows()

  return (
    <div className="p-6 space-y-4 w-full">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">Clienți</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Lista tuturor clienților</p>
        </div>
        <Link href="/clients/new" className="inline-flex items-center gap-1 text-sm border border-slate-200 rounded-md px-3 py-1.5 hover:bg-slate-50 transition-colors">
          + client nou
        </Link>
      </div>

      <ClientsFilterTable rows={rows} />
    </div>
  )
}
