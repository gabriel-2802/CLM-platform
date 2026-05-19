import { getClientRows } from "@/actions/clients"
import Link from "next/link"

export const dynamic = "force-dynamic"

const TABS = [
  { label: "Detalii", value: "other" },
  { label: "Punct de lucru", value: "punct" },
  { label: "Istoric", value: "istoric" },
  { label: "Useri", value: "users" },
]

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

      <div className="overflow-hidden rounded-lg border border-slate-200 shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full text-xs md:text-sm">
            <thead>
              <tr className="bg-slate-700">
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Nume</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Tip</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide">Useri asignați</th>
                {TABS.map((tab) => (
                  <th key={tab.value} className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">
                    {tab.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-100">
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">Niciun client găsit.</td>
                </tr>
              ) : (
                rows.map((row) => (
                  <tr key={row.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-4 py-3 whitespace-nowrap">
                      <Link
                        href={`/clients/edit/${row.id}?tab=form`}
                        className="font-semibold text-slate-800 hover:text-slate-600 hover:underline"
                      >
                        {row.name}
                      </Link>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-600">
                        {row.tip || "—"}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {row.users && row.users.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {row.users.map((u) => (
                            <span key={u} className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                              {u}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-slate-400 text-xs">—</span>
                      )}
                    </td>
                    {TABS.map((tab) => (
                      <td key={tab.value} className="px-4 py-3 whitespace-nowrap">
                        <Link
                          href={`/clients/edit/${row.id}?tab=${tab.value}`}
                          className="inline-flex items-center px-2.5 py-1 rounded text-xs font-medium border border-slate-200 text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors"
                        >
                          {tab.label}
                        </Link>
                      </td>
                    ))}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
