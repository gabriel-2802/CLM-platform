"use client"

import { useState, useMemo } from "react"
import Link from "next/link"
import { Filter, ChevronDown, ChevronUp } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useEnums } from "@/hooks/use-enums"
import type { Row } from "@/actions/clients"

const TABS = [
  { label: "Detalii", value: "other" },
  { label: "Punct de lucru", value: "punct" },
  { label: "Istoric", value: "istoric" },
  { label: "Useri", value: "users" },
]

export function ClientsFilterTable({ rows }: { rows: Row[] }) {
  const enums = useEnums()

  const [showFilters, setShowFilters] = useState(false)
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
      {/* Collapsible filter panel */}
      <div className="mb-4 rounded-lg border bg-muted/30">
        <button
          type="button"
          onClick={() => setShowFilters((v) => !v)}
          className="flex w-full items-center gap-2 px-3 py-2.5 text-sm font-medium text-slate-700 hover:bg-muted/50 rounded-lg transition-colors"
        >
          <Filter className="h-4 w-4 text-slate-500" />
          <span>Aplicați filtre</span>
          {hasFilters && (
            <span className="ml-1 rounded-full bg-indigo-600 px-1.5 py-0.5 text-xs font-semibold text-white">
              activ
            </span>
          )}
          <span className="ml-auto">{showFilters ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}</span>
        </button>

        {showFilters && (
          <div className="border-t px-3 pb-3 pt-3 space-y-3">
            {/* Row 1: Denumire, Tip, CUI */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-medium text-slate-600">Denumire</label>
                <Input
                  placeholder="Caută după denumire..."
                  value={filterDenumire}
                  onChange={(e) => setFilterDenumire(e.target.value)}
                  className="h-8 text-sm"
                />
              </div>
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
      </div>

      {/* Table */}
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
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">
                    {hasFilters ? "Niciun client nu corespunde filtrelor aplicate." : "Niciun client găsit."}
                  </td>
                </tr>
              ) : (
                filtered.map((row) => (
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
    </>
  )
}
