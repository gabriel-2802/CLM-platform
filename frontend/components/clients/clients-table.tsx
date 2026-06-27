"use client";

import Link from "next/link";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { Download, Filter, ChevronDown, ChevronUp, FileText, FileType } from "lucide-react";
import { type Row } from "@/actions/clients";
import { type Row as TanstackRow } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { DataTable, type ColumnDef } from "@/components/data-table";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";
import { GenerateContractModal } from "@/components/clients/generate-contract-modal";
import { ActeAditionaleDialog } from "@/components/clients/acte-aditionale-dialog";
import { NegocieriDialog } from "@/components/clients/negocieri-dialog";
import { ContractAuditDialog } from "@/components/clients/contract-audit-dialog";
import { terminateContract, uploadSignedContract } from "@/actions/contracts";
import { toast } from "sonner";
import { useAuthenticatedDownload } from "@/hooks/use-authenticated-download";

type RowEditFields = { deLa: string; panaLa: string; tarifConta: string; tarifBilant: string }

function formatDisplayDate(iso?: string | null): string {
  if (!iso) return "—"
  return new Date(iso).toLocaleDateString("ro-RO", { day: "2-digit", month: "2-digit", year: "numeric" })
}

const STATUS_LABELS: Record<string, string> = {
  PENDING_SIGNATURE: "In asteptarea semnaturii",
  ACTIVE: "Activ",
  TERMINATION_DUE: "In curs de incheiere",
  TERMINATED: "Incetat",
  ARCHIVED: "Arhivat",
};

const STATUS_BADGE: Record<string, string> = {
  PENDING_SIGNATURE: "bg-amber-100 text-amber-800",
  ACTIVE: "bg-green-100 text-green-800",
  TERMINATION_DUE: "bg-orange-100 text-orange-800",
  TERMINATED: "bg-red-100 text-red-700",
  ARCHIVED: "bg-slate-100 text-slate-400",
};

function SignedContractUploadDialog({
  contractId,
  onUploaded,
}: {
  contractId: number;
  onUploaded: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleUpload = async () => {
    if (!file) return;
    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    const res = await uploadSignedContract(contractId, formData);
    if (res.success) {
      toast.success("Contract semnat incarcat.");
      setOpen(false);
      setFile(null);
      onUploaded();
    } else {
      toast.error("Eroare la incarcare: " + res.error);
    }
    setIsUploading(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
        Incarca semnat
      </Button>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Incarca contract semnat</DialogTitle>
        </DialogHeader>
        <div className="space-y-2">
          <Label>Fisier (PDF/DOCX)</Label>
          <Input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>
            Anuleaza
          </Button>
          <Button onClick={handleUpload} disabled={!file || isUploading}>
            {isUploading ? "Se incarca..." : "Incarca"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function TerminateContractDialog({
  contractId,
  onTerminated,
}: {
  contractId: number;
  onTerminated: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [terminationDate, setTerminationDate] = useState("");
  const [reasons, setReasons] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleTerminate = async () => {
    if (!terminationDate) return;
    setIsSubmitting(true);
    const res = await terminateContract(contractId, terminationDate, reasons);
    if (res.success) {
      toast.success("Contract inchis.");
      setOpen(false);
      setTerminationDate("");
      setReasons("");
      onTerminated();
    } else {
      toast.error("Eroare la inchidere: " + res.error);
    }
    setIsSubmitting(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="outline" size="sm" className="border-red-200 bg-red-50 text-red-700 hover:bg-red-100 hover:text-red-800" onClick={() => setOpen(true)}>
        Încheie
      </Button>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Incheie contract</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>Data incetare</Label>
            <Input
              type="date"
              value={terminationDate}
              onChange={(e) => setTerminationDate(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label>Motive</Label>
            <Input
              placeholder="Motive incetare..."
              value={reasons}
              onChange={(e) => setReasons(e.target.value)}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>
            Anuleaza
          </Button>
          <Button onClick={handleTerminate} disabled={!terminationDate || isSubmitting}>
            {isSubmitting ? "Se inchide..." : "Incheie"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function DownloadGeneratedDialog({
  contractId,
  onDownload,
}: {
  contractId: number;
  onDownload: (format: "pdf" | "docx") => void;
}) {
  const [open, setOpen] = useState(false);
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button variant="outline" size="sm" onClick={() => setOpen(true)} className="inline-flex items-center gap-1.5 text-xs text-blue-600 border-blue-200 hover:text-blue-800">
        <Download className="w-3 h-3" />
        Descarca generat
      </Button>
      <DialogContent className="max-w-xs">
        <DialogHeader>
          <DialogTitle>Alege formatul</DialogTitle>
        </DialogHeader>
        <div className="flex gap-3 py-2">
          <Button
            variant="outline"
            className="flex-1 flex flex-col gap-1.5 h-auto py-4"
            onClick={() => { onDownload("pdf"); setOpen(false); }}
          >
            <FileText className="w-6 h-6 text-red-500" />
            <span className="text-sm font-medium">PDF</span>
          </Button>
          <Button
            variant="outline"
            className="flex-1 flex flex-col gap-1.5 h-auto py-4"
            onClick={() => { onDownload("docx"); setOpen(false); }}
          >
            <FileType className="w-6 h-6 text-blue-500" />
            <span className="text-sm font-medium">DOCX</span>
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default function ClientsTable({ rows, headerExtra }: { rows: Row[]; headerExtra?: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const downloadWithAuth = useAuthenticatedDownload();
  const initialFormer = (searchParams.get("former") ?? "1").toString();
  const [showFormer, setShowFormer] = useState<boolean>(initialFormer === "1" || initialFormer === "true");
  const [openCabinet, setOpenCabinet] = useState(false);

  const [showFilters, setShowFilters] = useState(false)
  const [filterFirma, setFilterFirma] = useState("")
  const [filterStartFrom, setFilterStartFrom] = useState("")
  const [filterStartTo, setFilterStartTo] = useState("")
  const [filterEndFrom, setFilterEndFrom] = useState("")
  const [filterEndTo, setFilterEndTo] = useState("")
  const [filterStatus, setFilterStatus] = useState("")

  const todayStr = useMemo(() => new Date().toLocaleDateString("sv-SE"), [])

  const hasFilters = filterFirma || filterStartFrom || filterStartTo || filterEndFrom || filterEndTo || filterStatus
  const clearFilters = () => {
    setFilterFirma(""); setFilterStartFrom(""); setFilterStartTo("")
    setFilterEndFrom(""); setFilterEndTo(""); setFilterStatus("")
  }

  const [rowEdits, setRowEdits] = useState<Record<number, RowEditFields>>(() => {
    const init: Record<number, RowEditFields> = {};
    rows.forEach((r) => {
      const key = r.contractId ?? r.id;
      init[key] = {
        deLa: r.contractStartDate ?? r.deLa ?? "",
        panaLa: r.contractEndDate ?? r.panaLa ?? "",
        tarifConta: r.contractValue != null ? String(r.contractValue) : (r.tarifConta != null ? String(r.tarifConta) : ""),
        tarifBilant: r.tarifBilant != null ? String(r.tarifBilant) : "",
      };
    });
    return init;
  });

  const rowEditsRef = useRef(rowEdits);
  rowEditsRef.current = rowEdits;

  const getEdit = useCallback((id: number, key: keyof RowEditFields): string => {
    return rowEditsRef.current[id]?.[key] ?? "";
  }, []);

  const setEdit = useCallback((id: number, key: keyof RowEditFields, val: string) => {
    setRowEdits((prev) => ({
      ...prev,
      [id]: { ...(prev[id] ?? { deLa: "", panaLa: "", tarifConta: "", tarifBilant: "" }), [key]: val },
    }));
  }, []);

  React.useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("former", showFormer ? "1" : "0");
    const qs = params.toString();
    const url = qs ? `${pathname}?${qs}` : pathname;
    router.replace(url);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showFormer]);

  React.useEffect(() => {
    const f = (searchParams.get("former") ?? "1").toString();
    const next = f === "1" || f === "true";
    if (next !== showFormer) setShowFormer(next);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const data = useMemo(() => {
    let result = showFormer ? rows : rows.filter((r) => !r.panaLa)

    if (filterFirma.trim()) {
      const q = filterFirma.trim().toLowerCase()
      result = result.filter((r) => r.name?.toLowerCase().includes(q))
    }
    if (filterStartFrom) {
      result = result.filter((r) => r.contractStartDate != null && r.contractStartDate >= filterStartFrom)
    }
    if (filterStartTo) {
      result = result.filter((r) => r.contractStartDate != null && r.contractStartDate <= filterStartTo)
    }
    if (filterEndFrom) {
      result = result.filter((r) => {
        const d = r.contractEndDate ?? r.panaLa
        return d != null && d >= filterEndFrom
      })
    }
    if (filterEndTo) {
      result = result.filter((r) => {
        const d = r.contractEndDate ?? r.panaLa
        return d != null && d <= filterEndTo
      })
    }
    if (filterStatus) {
      result = result.filter((r) => {
        const s = r.contractStatus ?? ""
        const td = r.terminationDate
        const isIncetat = s === "TERMINATED" || (td != null && td <= todayStr)
        const isTerminationDue = !isIncetat && (s === "TERMINATION_DUE" || td != null)
        switch (filterStatus) {
          case "ACTIVE":            return s === "ACTIVE" && !isIncetat && !isTerminationDue
          case "TERMINATION_DUE":  return isTerminationDue
          case "TERMINATED":       return isIncetat
          case "PENDING_SIGNATURE": return s === "PENDING_SIGNATURE"
          default:                 return true
        }
      })
    }

    return result
  }, [rows, showFormer, filterFirma, filterStartFrom, filterStartTo, filterEndFrom, filterEndTo, filterStatus, todayStr]);

  const columns = useMemo<ColumnDef<Row>[]>(() => [
    {
      accessorKey: "name",
      header: "Firma",
      enableSorting: true,
      cell: ({ row }) => (
        <Link
          href={`/clients/edit/${row.original.id}`}
          onClick={(e) => e.stopPropagation()}
          className="font-semibold text-slate-800 hover:underline cursor-pointer whitespace-nowrap"
        >
          {row.original.name}
        </Link>
      ),
    },
    {
      accessorKey: "tip",
      header: "Tip",
      enableSorting: true,
      cell: ({ row }) => (
        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-600">
          {row.original.tip || "—"}
        </span>
      ),
    },
    {
      id: "status",
      header: "Status",
      enableSorting: false,
      cell: ({ row }) => {
        const r = row.original;
        if (!r.contractId) return <span className="text-muted-foreground text-xs">Fără contract</span>;
        const status = r.contractStatus ?? "";
        const td = r.terminationDate;
        const isTerminated = status === "TERMINATED" || (td != null && td <= todayStr);
        const isTerminating = !isTerminated && (Boolean(td) || status === "TERMINATION_DUE");
        if (isTerminated) return <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-700">Incetat</span>;
        if (isTerminating) return <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">In curs de incheiere</span>;
        return (
          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap ${STATUS_BADGE[status] ?? "bg-slate-100 text-slate-600"}`}>
            {STATUS_LABELS[status] ?? "Generat"}
          </span>
        );
      },
    },
    {
      id: "acteAditionale",
      header: "Acte aditionale",
      enableSorting: false,
      cell: ({ row }) => {
        const r = row.original;
        if (!r.contractId) return <span className="text-muted-foreground text-xs">—</span>;
        const id = r.contractId;
        const enrichedClient = {
          ...r,
          deLa: getEdit(id, "deLa"),
          panaLa: getEdit(id, "panaLa"),
          tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
          tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
        };
        return (
          <ActeAditionaleDialog
            contractId={id}
            client={enrichedClient}
            onUpdateDetails={(vals) => {
              setEdit(id, "deLa", vals.effectiveDate || vals.panaLa)
              setEdit(id, "panaLa", vals.panaLa)
              setEdit(id, "tarifConta", vals.tarifConta)
              setEdit(id, "tarifBilant", vals.tarifBilant)
            }}
          />
        );
      },
    },
    {
      id: "incheie",
      header: "Incheie",
      enableSorting: false,
      cell: ({ row }) => {
        const r = row.original;
        if (!r.contractId) return <span className="text-muted-foreground text-xs">—</span>;
        const status = r.contractStatus ?? "";
        if (status === "ARCHIVED") return <span className="text-muted-foreground text-xs">—</span>;
        const hasTermination = status === "TERMINATION_DUE" || status === "TERMINATED" || Boolean(r.terminationDate);
        if (hasTermination || status !== "ACTIVE") {
          return (
            <span title={hasTermination ? "A fost deja inregistrata o cerere de incheiere" : "Nu poate fi incetat un contract care nu a fost incarcat semnat"}>
              <Button variant="outline" size="sm" className="border-red-200 bg-red-50 text-red-300 pointer-events-none" tabIndex={-1} aria-disabled="true">
                Încheie
              </Button>
            </span>
          );
        }
        return (
          <TerminateContractDialog
            contractId={r.contractId}
            onTerminated={() => router.refresh()}
          />
        );
      },
    },
  ], [todayStr, getEdit, setEdit, router]);

  const expandedRowContent = useCallback((row: TanstackRow<Row>) => {
    const r = row.original;
    const id = r.contractId ?? r.id;
    const status = r.contractStatus ?? "";
    const terminationDate = r.terminationDate;
    const td = new Date().toLocaleDateString("sv-SE");
    const isTerminated = status === "TERMINATED" || (terminationDate != null && terminationDate <= td);
    const isTerminating = !isTerminated && (Boolean(terminationDate) || status === "TERMINATION_DUE");
    const hasSigned = status === "ACTIVE" || status === "TERMINATION_DUE" || status === "TERMINATED" || Boolean(r.contractSemnat);

    const enrichedClient = {
      ...r,
      deLa: getEdit(id, "deLa"),
      panaLa: getEdit(id, "panaLa"),
      tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
      tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
    };

    if (!r.contractId) {
      return (
        <div className="flex items-center gap-4">
          <span className="text-sm text-slate-500">Niciun contract generat.</span>
          <GenerateContractModal client={enrichedClient} />
        </div>
      );
    }

    return (
      <div className="space-y-3">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-x-8 gap-y-2 text-xs">
          {(getEdit(id, "deLa") || getEdit(id, "panaLa")) && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Perioadă</div>
              <div className="text-slate-700">
                {formatDisplayDate(getEdit(id, "deLa"))} → {formatDisplayDate(getEdit(id, "panaLa"))}
              </div>
            </div>
          )}
          {getEdit(id, "tarifConta") && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Tarif contabilitate</div>
              <div className="text-slate-700">{getEdit(id, "tarifConta")}</div>
            </div>
          )}
          {getEdit(id, "tarifBilant") && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Tarif bilanț</div>
              <div className="text-slate-700">{getEdit(id, "tarifBilant")}</div>
            </div>
          )}
          {isTerminated && terminationDate && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Data încetare</div>
              <div className="text-red-700">{formatDisplayDate(terminationDate)}</div>
            </div>
          )}
          {isTerminating && terminationDate && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Urmează să înceteze</div>
              <div className="text-orange-700">{formatDisplayDate(terminationDate)}</div>
            </div>
          )}
          {r.users && r.users.length > 0 && (
            <div>
              <div className="text-[10px] font-medium text-slate-400 uppercase tracking-wide mb-0.5">Useri</div>
              <div className="text-slate-700">{r.users.join(", ")}</div>
            </div>
          )}
        </div>

        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-slate-200">
          <DownloadGeneratedDialog
            contractId={r.contractId}
            onDownload={(fmt) =>
              downloadWithAuth(
                `/api/contracts/download/${r.contractId}/unsigned/${fmt}`,
                { openInNewTab: true, fallbackFilename: `contract-${r.contractId}.${fmt}` }
              ).catch((err) => toast.error(err instanceof Error ? err.message : "Descarcare esuata."))
            }
          />

          {hasSigned ? (
            <button
              className="inline-flex items-center gap-1.5 text-xs border border-slate-200 rounded-md px-3 py-1.5 hover:bg-slate-50 transition-colors text-blue-600 hover:text-blue-800"
              onClick={() =>
                downloadWithAuth(`/api/contracts/download/${r.contractId}/signed/pdf`, {
                  openInNewTab: true,
                  fallbackFilename: `contract-${r.contractId}.pdf`,
                }).catch((err) => {
                  toast.error(err instanceof Error ? err.message : "Descarcare esuata.");
                })
              }
            >
              <Download className="w-3 h-3" />
              Descarca semnat
            </button>
          ) : (
            status !== "TERMINATED" && status !== "ARCHIVED" && (
              <SignedContractUploadDialog
                contractId={r.contractId}
                onUploaded={() => router.refresh()}
              />
            )
          )}

          <NegocieriDialog
            contractId={r.contractId}
            clientId={r.id}
            clientName={r.name ?? undefined}
            contractStatus={r.contractStatus ?? undefined}
          />

          <ContractAuditDialog
            contractId={r.contractId}
            clientName={r.name ?? undefined}
          />
        </div>
      </div>
    );
  }, [getEdit, setEdit, downloadWithAuth, router]);

  return (
    <div className="p-6 space-y-4">
      <div>
        <div className="mb-4 flex flex-wrap items-center gap-3">
          <label className="inline-flex items-center gap-2 text-sm">
            <Checkbox
              checked={showFormer}
              onCheckedChange={(v) => setShowFormer(Boolean(v))}
            />
            Afiseaza fostii clienti
          </label>
          <div className="ml-auto flex gap-2">
            {headerExtra}

            <Button variant="outline" size="sm" onClick={() => setOpenCabinet(true)}>
              <span className="i-edit">✎</span> date cabinet
            </Button>
          </div>
        </div>

        <div className="mb-4 rounded-lg border bg-muted/30">
          <button
            className="w-full flex items-center gap-2 px-3 py-2.5 text-sm font-medium text-slate-700 hover:bg-muted/60 transition-colors rounded-lg"
            onClick={() => setShowFilters((v) => !v)}
          >
            <Filter className="h-4 w-4 text-slate-500" />
            Aplicați filtre
            {hasFilters && (
              <span className="ml-1 inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">
                activ
              </span>
            )}
            <span className="ml-auto">
              {showFilters ? <ChevronUp className="h-4 w-4 text-slate-400" /> : <ChevronDown className="h-4 w-4 text-slate-400" />}
            </span>
          </button>
          {showFilters && (
            <div className="px-3 pb-3 space-y-3">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">Firma</Label>
                  <Input
                    placeholder="Cauta firma..."
                    value={filterFirma}
                    onChange={(e) => setFilterFirma(e.target.value)}
                    className="h-8 text-sm"
                  />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">Status contract</Label>
                  <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="w-full h-8 text-sm rounded-md border border-input bg-background px-2 text-slate-900"
                  >
                    <option value="">Toate statusurile</option>
                    <option value="ACTIVE">Activ</option>
                    <option value="PENDING_SIGNATURE">Generat, neincărcat semnat</option>
                    <option value="TERMINATION_DUE">Urmează să fie încetat</option>
                    <option value="TERMINATED">Încetat</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <fieldset className="rounded-md border px-3 pb-3 pt-1">
                  <legend className="text-xs text-muted-foreground px-1">Interval data de început</legend>
                  <div className="grid grid-cols-2 gap-2">
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">De la</Label>
                      <Input type="date" value={filterStartFrom} onChange={(e) => setFilterStartFrom(e.target.value)} className="h-8 text-sm" />
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">Până la</Label>
                      <Input type="date" value={filterStartTo} onChange={(e) => setFilterStartTo(e.target.value)} className="h-8 text-sm" />
                    </div>
                  </div>
                </fieldset>
                <fieldset className="rounded-md border px-3 pb-3 pt-1">
                  <legend className="text-xs text-muted-foreground px-1">Interval data de încheiere</legend>
                  <div className="grid grid-cols-2 gap-2">
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">De la</Label>
                      <Input type="date" value={filterEndFrom} onChange={(e) => setFilterEndFrom(e.target.value)} className="h-8 text-sm" />
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">Până la</Label>
                      <Input type="date" value={filterEndTo} onChange={(e) => setFilterEndTo(e.target.value)} className="h-8 text-sm" />
                    </div>
                  </div>
                </fieldset>
              </div>
              {hasFilters && (
                <div className="flex justify-end">
                  <Button variant="ghost" size="sm" className="h-7 text-xs text-muted-foreground" onClick={clearFilters}>
                    Șterge filtre
                  </Button>
                </div>
              )}
            </div>
          )}
        </div>

        <DataTable
          columns={columns}
          data={data}
          pageSize={10}
          expandedRowContent={expandedRowContent}
          stickyHeader
          showGlobalSearch={false}
        />
      </div>

      <Dialog open={openCabinet} onOpenChange={setOpenCabinet}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Editeaza datele cabinetului de contabilitate</DialogTitle>
          </DialogHeader>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label className="mb-1 block">Denumire</Label>
              <Input defaultValue="Voitto Tethys SRL" />
            </div>
            <div>
              <Label className="mb-1 block">Reprezentant</Label>
              <Input defaultValue="Rosescu Elena" />
            </div>
            <div>
              <Label className="mb-1 block">CUI</Label>
              <Input defaultValue="RO12345678" />
            </div>
            <div>
              <Label className="mb-1 block">Nr Reg Com</Label>
              <Input defaultValue="J40/1234/2009" />
            </div>
            <div>
              <Label className="mb-1 block">Nr Autorizatie</Label>
              <Input defaultValue="0011108/2016" />
            </div>
            <div className="md:col-span-2">
              <Label className="mb-1 block">Adresa</Label>
              <Input defaultValue="Str. Dumitru Ruse nr 17, sector 5, Bucuresti" />
            </div>
            <div>
              <Label className="mb-1 block">Banca</Label>
              <Input defaultValue="ING" />
            </div>
            <div className="md:col-span-2">
              <Label className="mb-1 block">IBAN</Label>
              <Input defaultValue="RO33INGB0000999912345678" />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpenCabinet(false)}>Cancel</Button>
            <Button onClick={() => setOpenCabinet(false)}>Save</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
