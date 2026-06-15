"use client"

import { Fragment, useState, useMemo, useCallback } from "react"
import { Filter, ChevronDown, ChevronUp } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useEnums } from "@/hooks/use-enums"
import type { Row } from "@/actions/clients"
import type { ContractLookup } from "@/actions/contracts"
import { getContractsByClientId } from "@/actions/contracts"

export function ClientsFilterTable({ rows }: { rows: Row[] }) {
  const enums = useEnums()

  const [showFilters, setShowFilters] = useState(false)
  const [expandedId, setExpandedId] = useState<number | null>(null)
  const [contractsCache, setContractsCache] = useState<Record<number, ContractLookup[]>>({})

  const handleRowClick = useCallback(async (id: number) => {
    if (expandedId === id) {
      setExpandedId(null)
      return
    }
    setExpandedId(id)
    if (!contractsCache[id]) {
      const contracts = await getContractsByClientId(id)
      setContractsCache((prev) => ({ ...prev, [id]: contracts }))
    }
  }, [expandedId, contractsCache])
  const [filterDenumire, setFilterDenumire] = useState("")
  const [filterTip, setFilterTip] = useState("")
  const [filterCui, setFilterCui] = useState("")
  const [filterAdresa, setFilterAdresa] = useState("")
  const [filterAdministratie, setFilterAdministratie] = useState("")
  const [filterPlatitorTVA, setFilterPlatitorTVA] = useState("")
  const [filterVerificareFrom, setFilterVerificareFrom] = useState("")
  const [filterVerificareTo, setFilterVerificareTo] = useState("")

  const hasFilters =
    filterDenumire !== "" ||
    filterTip !== "" ||
    filterCui !== "" ||
    filterAdresa !== "" ||
    filterAdministratie !== "" ||
    filterPlatitorTVA !== "" ||
    filterVerificareFrom !== "" ||
    filterVerificareTo !== ""

  function clearFilters() {
    setFilterDenumire("")
    setFilterTip("")
    setFilterCui("")
    setFilterAdresa("")
    setFilterAdministratie("")
    setFilterPlatitorTVA("")
    setFilterVerificareFrom("")
    setFilterVerificareTo("")
  }

  const filtered = useMemo(() => {
    return rows.filter((row) => {
      if (filterDenumire && !row.name?.toLowerCase().includes(filterDenumire.toLowerCase())) return false
      if (filterTip && row.tip !== filterTip) return false
      if (filterCui && !row.cui?.toLowerCase().includes(filterCui.toLowerCase())) return false
      if (filterAdresa && !row.adresa?.toLowerCase().includes(filterAdresa.toLowerCase())) return false
      if (filterAdministratie && row.administratie !== filterAdministratie) return false
      if (filterPlatitorTVA && row.platitorTVA !== filterPlatitorTVA) return false
      if (filterVerificareFrom && (!row.dataVerificarii || row.dataVerificarii < filterVerificareFrom)) return false
      if (filterVerificareTo && (!row.dataVerificarii || row.dataVerificarii > filterVerificareTo)) return false
      return true
    })
  }, [rows, filterDenumire, filterTip, filterCui, filterAdresa, filterAdministratie, filterPlatitorTVA, filterVerificareFrom, filterVerificareTo])

  return (
    <>
      {/* Toolbar: search + filter toggle on one row */}
      <div className="mb-4 flex items-center gap-2">
        <Input
          placeholder="Caută după numele clientului..."
          value={filterDenumire}
          onChange={(e) => setFilterDenumire(e.target.value)}
          className="max-w-sm"
        />
        <button
          type="button"
          onClick={() => setShowFilters((v) => !v)}
          className="flex items-center gap-1.5 px-3 py-2 rounded-md border border-slate-200 bg-white text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors whitespace-nowrap"
        >
          <Filter className="h-4 w-4 text-slate-400" />
          <span>Filtre</span>
          {hasFilters && (
            <span className="rounded-full bg-slate-700 px-1.5 py-0.5 text-xs font-semibold text-white">
              activ
            </span>
          )}
          {showFilters ? <ChevronUp className="h-4 w-4 text-slate-400" /> : <ChevronDown className="h-4 w-4 text-slate-400" />}
        </button>
      </div>

      {/* Collapsible filter panel */}
      {showFilters && (
        <div className="mb-4 rounded-lg border border-slate-200 bg-slate-50/60 px-3 pb-3 pt-3 space-y-3">
            {/* Row 1: Tip, CUI */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">Tip</label>
                <Select value={filterTip} onValueChange={setFilterTip}>
                  <SelectTrigger className="h-8 text-sm"><SelectValue placeholder="Toate tipurile" /></SelectTrigger>
                  <SelectContent>
                    {enums.companyTypes.map((v) => (
                      <SelectItem key={v} value={v}>{v}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">CUI</label>
                <Input
                  placeholder="Caută după CUI..."
                  value={filterCui}
                  onChange={(e) => setFilterCui(e.target.value)}
                  className="h-8 text-sm"
                />
              </div>
            </div>

            {/* Row 2: Adresa, Administratie, Platitor TVA */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">Adresă</label>
                <Input
                  placeholder="Caută după adresă..."
                  value={filterAdresa}
                  onChange={(e) => setFilterAdresa(e.target.value)}
                  className="h-8 text-sm"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">Administrație</label>
                <Select value={filterAdministratie} onValueChange={setFilterAdministratie}>
                  <SelectTrigger className="h-8 text-sm"><SelectValue placeholder="Toate" /></SelectTrigger>
                  <SelectContent>
                    {enums.administrations.map((v) => (
                      <SelectItem key={v} value={v}>{v}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">Platitor TVA</label>
                <Select value={filterPlatitorTVA} onValueChange={setFilterPlatitorTVA}>
                  <SelectTrigger className="h-8 text-sm"><SelectValue placeholder="Toate" /></SelectTrigger>
                  <SelectContent>
                    {enums.taxFrequencies.map((v) => (
                      <SelectItem key={v} value={v}>{v}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Row 3: Data verificarii interval */}
            <fieldset className="rounded border px-3 pb-3 pt-1">
              <legend className="px-1 text-xs font-semibold text-slate-600">Interval dată verificare</legend>
              <div className="grid grid-cols-2 gap-3 mt-1">
                <div className="space-y-1">
                  <label className="text-xs font-medium text-slate-600">De la</label>
                  <Input
                    type="date"
                    value={filterVerificareFrom}
                    onChange={(e) => setFilterVerificareFrom(e.target.value)}
                    className="h-8 text-sm"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-slate-600">Până la</label>
                  <Input
                    type="date"
                    value={filterVerificareTo}
                    onChange={(e) => setFilterVerificareTo(e.target.value)}
                    className="h-8 text-sm"
                  />
                </div>
              </div>
            </fieldset>

            {hasFilters && (
              <div className="flex justify-end">
                <Button variant="ghost" size="sm" onClick={clearFilters} className="text-xs h-7">
                  Șterge filtre
                </Button>
              </div>
            )}
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-lg border border-slate-200 shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full text-xs md:text-sm">
            <thead>
              <tr className="bg-slate-700">
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Nume</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Tip</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide">Useri asignați</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-100">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={3} className="px-4 py-8 text-center text-muted-foreground">
                    {hasFilters ? "Niciun client nu corespunde filtrelor aplicate." : "Niciun client găsit."}
                  </td>
                </tr>
              ) : (
                filtered.map((row) => (
                  <Fragment key={row.id}>
                    <tr
                      className="hover:bg-slate-50 transition-colors cursor-pointer"
                      onClick={() => handleRowClick(row.id)}
                    >
                      <td className="px-4 py-3 whitespace-nowrap">
                        <span className="font-semibold text-slate-800 hover:text-slate-600 hover:underline select-none">
                          {row.name}
                        </span>
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
                    </tr>
                    {expandedId === row.id && (
                      <tr className="bg-slate-50/70">
                        <td colSpan={3} className="px-6 py-4 space-y-3">
                          {/* Client details */}
                          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-x-8 gap-y-3 text-xs">
                            {row.cui && (
                              <div>
                                <div className="font-medium text-slate-500 uppercase tracking-wide text-[10px] mb-0.5">CUI</div>
                                <div className="text-slate-800">{row.cui}</div>
                              </div>
                            )}
                            {row.adresa && (
                              <div>
                                <div className="font-medium text-slate-500 uppercase tracking-wide text-[10px] mb-0.5">Adresă</div>
                                <div className="text-slate-800">{row.adresa}</div>
                              </div>
                            )}
                            {row.administratie && (
                              <div>
                                <div className="font-medium text-slate-500 uppercase tracking-wide text-[10px] mb-0.5">Administrație</div>
                                <div className="text-slate-800">{row.administratie}</div>
                              </div>
                            )}
                            {row.platitorTVA && (
                              <div>
                                <div className="font-medium text-slate-500 uppercase tracking-wide text-[10px] mb-0.5">Platitor TVA</div>
                                <div className="text-slate-800">{row.platitorTVA}</div>
                              </div>
                            )}
                            {row.dataVerificarii && (
                              <div>
                                <div className="font-medium text-slate-500 uppercase tracking-wide text-[10px] mb-0.5">Data verificării</div>
                                <div className="text-slate-800">{row.dataVerificarii}</div>
                              </div>
                            )}
                          </div>
                          {/* Contracts */}
                          {contractsCache[row.id] === undefined ? (
                            <div className="text-xs text-slate-400 border-t border-slate-200 pt-2">Se încarcă contractele…</div>
                          ) : contractsCache[row.id].length === 0 ? null : (
                            <div className="border-t border-slate-200 pt-2">
                              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-2">Contracte</div>
                              <table className="w-auto text-xs border-collapse">
                                <thead>
                                  <tr className="text-[10px] text-slate-400 uppercase tracking-wide">
                                    <th className="pr-6 pb-1 font-medium text-left">Status</th>
                                    <th className="pr-6 pb-1 font-medium text-left">Perioadă</th>
                                    <th className="pr-6 pb-1 font-medium text-right">Val. contabilitate</th>
                                    <th className="pb-1 font-medium text-right">Val. bilanț</th>
                                  </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                  {contractsCache[row.id].map((c) => {
                                    const statusColors: Record<string, string> = {
                                      ACTIVE: "bg-green-50 text-green-700 border-green-100",
                                      PENDING_SIGNATURE: "bg-yellow-50 text-yellow-700 border-yellow-100",
                                      TERMINATED: "bg-red-50 text-red-700 border-red-100",
                                    }
                                    const statusClass = statusColors[c.contractStatus ?? ""] ?? "bg-slate-100 text-slate-600 border-slate-200"
                                    return (
                                      <tr key={c.id} className="text-slate-700">
                                        <td className="pr-6 py-1">
                                          <span className={`inline-flex items-center px-2 py-0.5 rounded border text-[10px] font-medium ${statusClass}`}>
                                            {c.contractStatus ?? "—"}
                                          </span>
                                        </td>
                                        <td className="pr-6 py-1 tabular-nums">
                                          {c.contractStartDate?.slice(0, 10) ?? "—"}
                                          {c.contractEndDate ? ` → ${c.contractEndDate.slice(0, 10)}` : ""}
                                        </td>
                                        <td className="pr-6 py-1 text-right tabular-nums">{c.contractValue ?? "—"}</td>
                                        <td className="py-1 text-right tabular-nums">{c.contractBalance ?? "—"}</td>
                                      </tr>
                                    )
                                  })}
                                </tbody>
                              </table>
                            </div>
                          )}
                        </td>
                      </tr>
                    )}
                  </Fragment>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}
