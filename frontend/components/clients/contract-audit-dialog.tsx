"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Loader2, ClipboardList, ExternalLink } from "lucide-react"
import { getContractAudit, type AuditEvent, type AuditEventCategory } from "@/actions/contracts"
import { useAuthenticatedDownload } from "@/hooks/use-authenticated-download"
import { toast } from "sonner"

function formatDisplayDate(iso: string): string {
  return new Date(iso).toLocaleDateString("ro-RO", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  })
}

const CATEGORY_STYLES: Record<AuditEventCategory, { dot: string; badge: string; label: string }> = {
  created:    { dot: "bg-blue-500",   badge: "bg-blue-50 text-blue-700 ring-blue-200",    label: "Creare" },
  signed:     { dot: "bg-green-500",  badge: "bg-green-50 text-green-700 ring-green-200", label: "Semnare" },
  amended:    { dot: "bg-amber-500",  badge: "bg-amber-50 text-amber-700 ring-amber-200", label: "Modificare" },
  terminated: { dot: "bg-red-500",    badge: "bg-red-50 text-red-700 ring-red-200",       label: "Încheiere" },
}

function AuditTimeline({ events, onDownload }: { events: AuditEvent[]; onDownload: (url: string) => void }) {
  if (events.length === 0) {
    return (
      <p className="text-sm text-muted-foreground text-center py-8">
        Nu există evenimente de audit pentru acest contract.
      </p>
    )
  }

  return (
    <div className="relative pl-6">
      {/* Vertical line */}
      <div className="absolute left-[0.6875rem] top-2 bottom-2 w-px bg-slate-200" />

      <div className="space-y-6">
        {events.map((event) => {
          const style = CATEGORY_STYLES[event.category]
          return (
            <div key={event.id} className="relative flex gap-4">
              {/* Dot */}
              <div
                className={`absolute -left-6 mt-1 h-3.5 w-3.5 rounded-full ring-2 ring-white ${style.dot} flex-shrink-0`}
              />

              {/* Card */}
              <div className="flex-1 rounded-lg border bg-white px-4 py-3 shadow-sm">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <span className="text-sm font-semibold text-slate-800">{event.name}</span>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset ${style.badge}`}
                  >
                    {style.label}
                  </span>
                </div>
                <p className="mt-1 text-sm text-slate-600">{event.action}</p>
                {event.details && (
                  <p className="mt-1 text-xs text-slate-500 whitespace-pre-line">{event.details}</p>
                )}
                <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
                  <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-400">
                    <span>{formatDisplayDate(event.date)}</span>
                    <span className="font-medium text-slate-500">{event.userDisplay}</span>
                  </div>
                  {event.downloadUrl && (
                    <button
                      onClick={() => onDownload(event.downloadUrl!)}
                      className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800 transition-colors"
                    >
                      <ExternalLink className="h-3 w-3" />
                      Vizualizează
                    </button>
                  )}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export function ContractAuditDialog({
  contractId,
  clientName,
}: {
  contractId: number
  clientName?: string
}) {
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [events, setEvents] = useState<AuditEvent[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const downloadWithAuth = useAuthenticatedDownload()

  const handleOpenChange = (next: boolean) => {
    setOpen(next)
    if (!next) {
      setEvents(null)
      setError(null)
    }
  }

  const handleOpen = async () => {
    setOpen(true)
    setLoading(true)
    setError(null)
    try {
      const result = await getContractAudit(contractId)
      if (result.success) {
        setEvents(result.events)
      } else {
        setError(result.error)
      }
    } catch {
      setError("Eroare la încărcarea auditului.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <Button variant="outline" size="sm" onClick={handleOpen}>
        <ClipboardList className="mr-1.5 h-3.5 w-3.5" />
        Audit
      </Button>
      <DialogContent className="max-w-lg max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            Istoric contract{clientName ? ` - ${clientName}` : ""}
          </DialogTitle>
        </DialogHeader>

        <div className="mt-2">
          {loading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          )}
          {error && (
            <p className="text-sm text-red-600 text-center py-8">{error}</p>
          )}
          {!loading && !error && events !== null && (
            <AuditTimeline
              events={events}
              onDownload={(url) =>
                downloadWithAuth(url, { openInNewTab: true }).catch((err) =>
                  toast.error(err instanceof Error ? err.message : "Descărcare eșuată.")
                )
              }
            />
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
