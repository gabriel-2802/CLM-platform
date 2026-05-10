"use client"

import { useState, useEffect, useCallback } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Loader2, ChevronLeft, Plus, Check, X, Send } from "lucide-react"
import { toast } from "sonner"
import {
  getNegotiationsForContract,
  createNegotiation,
  sendNegotiation,
  acceptNegotiation,
  rejectNegotiation,
  type Negotiation,
} from "@/actions/negotiations"

type View = "list" | "new"

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "Draft",
  SENT: "Trimisă",
  ACCEPTED: "Acceptată",
  REJECTED: "Respinsă",
}

const STATUS_COLORS: Record<string, string> = {
  DRAFT:    "bg-slate-100 text-slate-700 border-slate-300",
  SENT:     "bg-blue-100  text-blue-700  border-blue-300",
  ACCEPTED: "bg-green-100 text-green-700 border-green-300",
  REJECTED: "bg-red-100   text-red-700   border-red-300",
}

const DOT_COLORS: Record<string, string> = {
  DRAFT:    "bg-slate-400",
  SENT:     "bg-blue-500",
  ACCEPTED: "bg-green-500",
  REJECTED: "bg-red-500",
}

function formatDate(iso: string | null): string {
  if (!iso) return "—"
  return new Date(iso).toLocaleDateString("ro-RO", {
    day: "2-digit", month: "short", year: "numeric",
  })
}

function formatMoney(value: number | null): string {
  if (value == null) return ""
  return new Intl.NumberFormat("ro-RO", { style: "currency", currency: "RON" }).format(value)
}

function NegotiationTimeline({
  negotiations,
  onAction,
}: {
  negotiations: Negotiation[]
  onAction: () => void
}) {
  const [busy, setBusy] = useState<Record<number, boolean>>({})

  const run = async (id: number, fn: () => Promise<{ success: boolean; error?: string }>) => {
    setBusy((b) => ({ ...b, [id]: true }))
    const res = await fn()
    if (res.success) {
      onAction()
    } else {
      toast.error(res.error ?? "Eroare necunoscută")
    }
    setBusy((b) => ({ ...b, [id]: false }))
  }

  if (negotiations.length === 0) {
    return (
      <p className="text-sm text-muted-foreground italic py-4">
        Nu există negocieri pentru acest contract.
      </p>
    )
  }

  return (
    <div className="relative pl-6 border-l-2 border-muted">
      {negotiations.map((n, idx) => (
        <div key={n.id} className={`relative pb-6 ${idx === negotiations.length - 1 ? "pb-0" : ""}`}>
          {/* dot pe linie */}
          <span
            className={`absolute -left-[0.4375rem] top-1 h-3 w-3 rounded-full border-2 border-background ${DOT_COLORS[n.status] ?? "bg-slate-400"}`}
          />

          <div className="space-y-1 pl-2">
            {/* header: dată + badge */}
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs text-muted-foreground font-medium">
                {formatDate(n.createdAt)}
              </span>
              <span
                className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[n.status] ?? ""}`}
              >
                {STATUS_LABELS[n.status] ?? n.status}
              </span>
              {n.resolvedAt && (
                <span className="text-xs text-muted-foreground">
                  rezolvată {formatDate(n.resolvedAt)}
                </span>
              )}
            </div>

            {/* creator */}
            {n.createdByUserName && (
              <p className="text-xs text-muted-foreground">
                Operată de <span className="font-medium text-slate-700">{n.createdByUserName}</span>
              </p>
            )}

            {/* propunere */}
            <div className="flex flex-wrap gap-3 text-sm">
              {n.proposedValue != null && (
                <span className="font-medium text-slate-800">
                  Preț: {formatMoney(n.proposedValue)}
                </span>
              )}
              {n.proposedEndDate && (
                <span className="text-slate-600">
                  Expirare: {formatDate(n.proposedEndDate)}
                </span>
              )}
            </div>

            {/* note */}
            {n.notes && (
              <p className="text-sm text-muted-foreground italic">"{n.notes}"</p>
            )}

            {/* acțiuni */}
            {n.status === "DRAFT" && (
              <div className="flex gap-2 pt-1">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={busy[n.id]}
                  onClick={() => run(n.id, () => sendNegotiation(n.id))}
                >
                  {busy[n.id] ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <><Send className="h-3 w-3 mr-1" />Trimite</>
                  )}
                </Button>
              </div>
            )}

            {n.status === "SENT" && (
              <div className="flex gap-2 pt-1">
                <Button
                  variant="outline"
                  size="sm"
                  className="border-green-400 text-green-700 hover:bg-green-50"
                  disabled={busy[n.id]}
                  onClick={() => run(n.id, () => acceptNegotiation(n.id))}
                >
                  {busy[n.id] ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <><Check className="h-3 w-3 mr-1" />Acceptă</>
                  )}
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  className="border-red-400 text-red-700 hover:bg-red-50"
                  disabled={busy[n.id]}
                  onClick={() => run(n.id, () => rejectNegotiation(n.id))}
                >
                  {busy[n.id] ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <><X className="h-3 w-3 mr-1" />Respinge</>
                  )}
                </Button>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}

export function NegocieriDialog({
  contractId,
  clientId,
  clientName,
  contractStatus,
}: {
  contractId: number
  clientId: number
  clientName?: string
  contractStatus?: string
}) {
  const [open, setOpen] = useState(false)
  const [view, setView] = useState<View>("list")
  const [negotiations, setNegotiations] = useState<Negotiation[]>([])
  const [loading, setLoading] = useState(false)

  // form state
  const [proposedValue, setProposedValue] = useState("")
  const [proposedEndDate, setProposedEndDate] = useState("")
  const [notes, setNotes] = useState("")
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    const data = await getNegotiationsForContract(contractId)
    setNegotiations(data)
    setLoading(false)
  }, [contractId])

  useEffect(() => {
    if (!open) return
    setView("list")
    load()
  }, [open, load])

  const lastNegotiation = negotiations[0] ?? null
  const lastDate = lastNegotiation
    ? (lastNegotiation.resolvedAt ?? lastNegotiation.createdAt)
    : null

  const isFormValid = proposedValue.trim() !== "" || proposedEndDate !== ""

  const handleCreate = async () => {
    setSaving(true)
    const res = await createNegotiation({
      contractId,
      clientId,
      proposedValue: proposedValue ? parseFloat(proposedValue) : null,
      proposedEndDate: proposedEndDate || null,
      notes: notes.trim() || null,
    })
    if (res.success) {
      toast.success("Negociere creată.")
      setProposedValue("")
      setProposedEndDate("")
      setNotes("")
      setView("list")
      load()
    } else {
      toast.error("Eroare: " + res.error)
    }
    setSaving(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
        Negocieri
      </Button>

      <DialogContent className="max-w-lg max-h-[85vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>
            {view === "list"
              ? `Negocieri - ${clientName ?? ""}`
              : "Negociere nouă"}
          </DialogTitle>
        </DialogHeader>

        {/* ── LIST ── */}
        {view === "list" && (
          <div className="flex flex-col gap-4 overflow-y-auto flex-1 min-h-0 pr-1">
            {/* data ultimei negocieri */}
            <div className="flex items-center justify-between rounded-md border bg-muted/40 px-3 py-2">
              <span className="text-sm text-muted-foreground">Ultima negociere</span>
              <span className="text-sm font-medium">
                {lastDate ? formatDate(lastDate) : "—"}
              </span>
            </div>

            {/* timeline */}
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="animate-spin h-5 w-5 text-muted-foreground" />
              </div>
            ) : (
              <NegotiationTimeline negotiations={negotiations} onAction={load} />
            )}

            {/* buton negociere nouă */}
            <div className="pt-3 border-t mt-auto">
              {contractStatus && contractStatus !== "ACTIVE" ? (
                <p className="text-xs text-muted-foreground text-center italic">
                  Negocierile pot fi inițiate doar pentru contracte active.
                </p>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full"
                  onClick={() => setView("new")}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Negociere nouă
                </Button>
              )}
            </div>
          </div>
        )}

        {/* ── FORM NEGOCIERE NOUĂ ── */}
        {view === "new" && (
          <div className="space-y-4 overflow-y-auto flex-1 min-h-0 pr-1">
            <p className="text-sm text-muted-foreground">
              Completează cel puțin prețul sau data de expirare propusă.
            </p>

            <div className="space-y-1">
              <Label>Preț propus (RON)</Label>
              <Input
                type="number"
                min="0"
                step="0.01"
                placeholder="ex: 4500.00"
                value={proposedValue}
                onChange={(e) => setProposedValue(e.target.value)}
                className="text-slate-900"
              />
            </div>

            <div className="space-y-1">
              <Label>Dată expirare propusă</Label>
              <Input
                type="date"
                value={proposedEndDate}
                onChange={(e) => setProposedEndDate(e.target.value)}
                className="text-slate-900"
              />
            </div>

            <div className="space-y-1">
              <Label>Note</Label>
              <textarea
                placeholder="Detalii despre negociere, motivul schimbării etc."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                maxLength={2000}
                rows={3}
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring resize-none text-slate-900"
              />
              <p className="text-xs text-muted-foreground text-right">
                {notes.length}/2000
              </p>
            </div>

            <DialogFooter className="pt-2">
              <Button variant="outline" onClick={() => setView("list")}>
                <ChevronLeft className="h-4 w-4 mr-1" />
                Înapoi
              </Button>
              <Button onClick={handleCreate} disabled={!isFormValid || saving}>
                {saving ? (
                  <><Loader2 className="animate-spin h-4 w-4 mr-1" />Se salvează...</>
                ) : (
                  "Salvează"
                )}
              </Button>
            </DialogFooter>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
