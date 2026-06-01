"use client";

import Link from "next/link";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { Download, Filter, ChevronDown, ChevronUp } from "lucide-react";
import { type Row, getClientRows } from "@/actions/clients";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { DataTable, type ColumnDef } from "@/components/data-table";
import ClientRow from "@/components/clients/client-row";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";
import { GenerateContractModal } from "@/components/clients/generate-contract-modal";
import { ActeAditionaleDialog } from "@/components/clients/acte-aditionale-dialog";
import { NegocieriDialog } from "@/components/clients/negocieri-dialog";
import { terminateContract, toggleAutoRenewal, uploadSignedContract } from "@/actions/contracts";
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

// Defined outside ClientsTable so its identity is stable across parent re-renders.
function ClientDetailsDialog({
                               clientId,
                               clientName,
                               deLa,
                               panaLa,
                               tarifConta,
                               tarifBilant,
                               onEdit,
                               readOnly,
                             }: {
  clientId: number
  clientName: string
  deLa: string
  panaLa: string
  tarifConta: string
  tarifBilant: string
  onEdit: (id: number, key: keyof RowEditFields, val: string) => void
  readOnly?: boolean
}) {
  const [open, setOpen] = useState(false)
  return (
      <Dialog open={open} onOpenChange={setOpen}>
        <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
          Detalii contract
        </Button>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Detalii contract - {clientName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            {readOnly && (
              <p className="text-xs text-muted-foreground">
                Valorile pot fi modificate doar prin încărcarea unui act adițional semnat.
              </p>
            )}
            <div className="space-y-1">
              <Label>De la {!readOnly && <span className="text-red-500">*</span>}</Label>
              <Input
                  type="date"
                  value={deLa}
                  onChange={(e) => onEdit(clientId, "deLa", e.target.value)}
                  className="text-slate-900"
                  disabled={readOnly}
              />
            </div>
            <div className="space-y-1">
              <Label>Pana la {!readOnly && <span className="text-red-500">*</span>}</Label>
              <Input
                  type="date"
                  value={panaLa}
                  onChange={(e) => onEdit(clientId, "panaLa", e.target.value)}
                  className="text-slate-900"
                  disabled={readOnly}
              />
            </div>
            <div className="space-y-1">
              <Label>Tarif servicii conta {!readOnly && <span className="text-red-500">*</span>}</Label>
              <Input
                  type="number"
                  value={tarifConta}
                  onChange={(e) => onEdit(clientId, "tarifConta", e.target.value)}
                  className="text-slate-900"
                  disabled={readOnly}
              />
            </div>
            <div className="space-y-1">
              <Label>Tarif bilant {!readOnly && <span className="text-red-500">*</span>}</Label>
              <Input
                  type="number"
                  value={tarifBilant}
                  onChange={(e) => onEdit(clientId, "tarifBilant", e.target.value)}
                  className="text-slate-900"
                  disabled={readOnly}
              />
            </div>
          </div>
          <DialogFooter>
            <Button onClick={() => setOpen(false)}>Gata</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
  )
}

export default function ClientsTable({ rows, headerExtra }: { rows: Row[]; headerExtra?: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const downloadWithAuth = useAuthenticatedDownload();
  const initialFormer = (searchParams.get("former") ?? "1").toString();
  const [showFormer, setShowFormer] = useState<boolean>(initialFormer === "1" || initialFormer === "true");
  const [openCabinet, setOpenCabinet] = useState(false);
  const [togglingAutoRenew, setTogglingAutoRenew] = useState<Record<number, boolean>>({});

  const [showFilters, setShowFilters] = useState(false)
  const [filterFirma, setFilterFirma] = useState("")
  const [filterUser, setFilterUser] = useState("")
  const [filterStartFrom, setFilterStartFrom] = useState("")
  const [filterStartTo, setFilterStartTo] = useState("")
  const [filterEndFrom, setFilterEndFrom] = useState("")
  const [filterEndTo, setFilterEndTo] = useState("")
  const [filterStatus, setFilterStatus] = useState("")

  const todayStr = useMemo(() => new Date().toLocaleDateString("sv-SE"), [])

  const hasFilters = filterFirma || filterUser || filterStartFrom || filterStartTo || filterEndFrom || filterEndTo || filterStatus
  const clearFilters = () => {
    setFilterFirma(""); setFilterUser(""); setFilterStartFrom(""); setFilterStartTo("")
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

  // Ref keeps rowEdits always fresh for the stable getEdit callback below
  const rowEditsRef = useRef(rowEdits);
  rowEditsRef.current = rowEdits;

  // Stable identity — safe to include in useMemo([]) for columns
  const getEdit = useCallback((id: number, key: keyof RowEditFields): string => {
    return rowEditsRef.current[id]?.[key] ?? "";
  }, []);

  const setEdit = useCallback((id: number, key: keyof RowEditFields, val: string) => {
    setRowEdits((prev) => ({
      ...prev,
      [id]: { ...(prev[id] ?? { deLa: "", panaLa: "", tarifConta: "", tarifBilant: "" }), [key]: val },
    }));
  }, []);

  const handleToggleAutoRenew = useCallback(
      async (contractId: number) => {
        setTogglingAutoRenew((prev) => ({ ...prev, [contractId]: true }));
        const res = await toggleAutoRenewal(contractId);
        if (res.success) {
          toast.success("Auto-renew actualizat.");
          router.refresh();
        } else {
          toast.error("Eroare auto-renew: " + res.error);
        }
        setTogglingAutoRenew((prev) => ({ ...prev, [contractId]: false }));
      },
      [router]
  );

  // Persist "former" checkbox in URL (?former=1|0)
  React.useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("former", showFormer ? "1" : "0");
    const qs = params.toString();
    const url = qs ? `${pathname}?${qs}` : pathname;
    router.replace(url);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showFormer]);

  // Sync state on back/forward
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
    if (filterUser.trim()) {
      const q = filterUser.trim().toLowerCase()
      result = result.filter((r) => r.users?.some((u) => u.toLowerCase().includes(q)))
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
  }, [rows, showFormer, filterFirma, filterUser, filterStartFrom, filterStartTo, filterEndFrom, filterEndTo, filterStatus, todayStr]);

  // Memoized with stable deps so TanStack Table never sees a new columns array —
  // this prevents cells from remounting on every rowEdits state change.
  const columns = useMemo<ColumnDef<Row>[]>(() => [
    {
      accessorKey: "name",
      header: "Firma",
      enableSorting: true,
      cell: ({ row }) => (
          <Link href={`/clients/edit/${row.original.id}`} className="font-semibold text-slate-800 hover:underline cursor-pointer whitespace-nowrap">
            {row.original.name}
          </Link>
      ),
    },
    { accessorKey: "tip", header: "Tip", enableSorting: true },
    {
      id: "users",
      header: "Useri",
      enableSorting: false,
      cell: ({ row }) =>
          row.original.users && row.original.users.length ? (
              <div className="max-w-[16rem] truncate" title={row.original.users.join(", ")}>{row.original.users.join(", ")}</div>
          ) : (
              <span className="text-muted-foreground">—</span>
          ),
    },
    {
      id: "detalii",
      header: "Detalii contract",
      enableSorting: false,
      cell: ({ row }) => {
        if (!row.original.contractId) {
          return <span className="text-muted-foreground text-xs">—</span>
        }
        return (
          <ClientDetailsDialog
              clientId={row.original.contractId ?? row.original.id}
              clientName={row.original.name ?? ""}
              deLa={getEdit(row.original.contractId ?? row.original.id, "deLa")}
              panaLa={getEdit(row.original.contractId ?? row.original.id, "panaLa")}
              tarifConta={getEdit(row.original.contractId ?? row.original.id, "tarifConta")}
              tarifBilant={getEdit(row.original.contractId ?? row.original.id, "tarifBilant")}
              onEdit={setEdit}
              readOnly={true}
          />
        )
      },
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }) => {
        const id = row.original.contractId ?? row.original.id;
        const enrichedClient = {
          ...row.original,
          deLa: getEdit(id, "deLa"),
          panaLa: getEdit(id, "panaLa"),
          tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
          tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
        };

        const status = row.original.contractStatus ?? "";
        const terminationDate = row.original.terminationDate
        const todayStr = new Date().toLocaleDateString("sv-SE") // "YYYY-MM-DD" in local time
        const isTerminated = status === "TERMINATED" || (terminationDate != null && terminationDate <= todayStr)
        const isTerminating = !isTerminated && (Boolean(terminationDate) || status === "TERMINATION_DUE")
        return row.original.contractId ? (
            <div className="flex flex-col gap-1.5">
              {isTerminated ? (
                <span className="text-xs text-red-700 font-medium">
                  Contractul a fost incetat la data de {formatDisplayDate(terminationDate ?? row.original.contractEndDate)}
                </span>
              ) : isTerminating ? (
                <span className="text-xs text-orange-600 font-medium">
                  Contractul urmeaza sa fie incheiat la data de {formatDisplayDate(terminationDate ?? row.original.contractEndDate)}
                </span>
              ) : (
                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap ${STATUS_BADGE[status] ?? "bg-slate-100 text-slate-600"}`}>
                  {STATUS_LABELS[status] ?? "Generat"}
                </span>
              )}
              <button
                  className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800 transition-colors"
                  onClick={() =>
                    downloadWithAuth(
                      `/api/contracts/download/${row.original.contractId}/unsigned/pdf`,
                      { openInNewTab: true, fallbackFilename: `contract-${row.original.contractId}.pdf` }
                    ).catch((err) => {
                      const message = err instanceof Error ? err.message : "Descarcare esuata.";
                      toast.error(message);
                    })
                  }
              >
                <Download className="w-3 h-3" />
                Descarca contract generat
              </button>
            </div>
        ) : (
            <GenerateContractModal client={enrichedClient} />
        );
      },
    },
    {
      accessorKey: "contractSemnat",
      header: "Contract semnat",
      enableSorting: false,
      cell: ({ row }) => {
        const contractId = row.original.contractId;
        const status = row.original.contractStatus;
        const hasSigned = status === "ACTIVE" || status === "TERMINATION_DUE" || status === "TERMINATED" || Boolean(row.original.contractSemnat);

        if (!contractId) {
          return <span className="text-muted-foreground">—</span>;
        }

        if (hasSigned) {
          return (
              <button
                  className="inline-flex items-center gap-1.5 text-xs text-blue-600 hover:text-blue-800 transition-colors"
                  title="Descarca semnat"
                  onClick={() =>
                    downloadWithAuth(`/api/contracts/download/${contractId}/signed/pdf`, {
                      openInNewTab: true,
                      fallbackFilename: `contract-${contractId}.pdf`,
                    }).catch((err) => {
                      const message = err instanceof Error ? err.message : "Descarcare esuata.";
                      toast.error(message);
                    })
                  }
              >
                <Download className="w-3.5 h-3.5" />
                Descarca semnat
              </button>
          );
        }

        if (status === "TERMINATED" || status === "ARCHIVED") {
          return <span className="text-muted-foreground">—</span>;
        }

        return (
            <SignedContractUploadDialog
                contractId={contractId}
                onUploaded={() => router.refresh()}
            />
        );
      },
    },
    {
      id: "acteAditionale",
      header: "Acte aditionale",
      enableSorting: false,
      cell: ({ row }) => {
        const contractId = row.original.contractId;
        if (!contractId) return <span className="text-muted-foreground">—</span>;
        const enrichedClient = {
          ...row.original,
          deLa: getEdit(contractId, "deLa"),
          panaLa: getEdit(contractId, "panaLa"),
          tarifConta: getEdit(contractId, "tarifConta") ? parseFloat(getEdit(contractId, "tarifConta")) : undefined,
          tarifBilant: getEdit(contractId, "tarifBilant") ? parseFloat(getEdit(contractId, "tarifBilant")) : undefined,
        };
        return <ActeAditionaleDialog contractId={contractId} client={enrichedClient} onUpdateDetails={(vals) => {
          setEdit(contractId, "deLa", vals.effectiveDate || vals.panaLa)
          setEdit(contractId, "panaLa", vals.panaLa)
          setEdit(contractId, "tarifConta", vals.tarifConta)
          setEdit(contractId, "tarifBilant", vals.tarifBilant)
        }} />;
      },
    },
    {
      id: "negocieri",
      header: "Negocieri",
      enableSorting: false,
      cell: ({ row }) => {
        const contractId = row.original.contractId;
        if (!contractId) return <span className="text-muted-foreground">—</span>;
        return (
          <NegocieriDialog
            contractId={contractId}
            clientId={row.original.id}
            clientName={row.original.name ?? undefined}
            contractStatus={row.original.contractStatus ?? undefined}
          />
        );
      },
    },
    {
      id: "incheie",
      header: "Incheie",
      enableSorting: false,
      cell: ({ row }) => {
        const contractId = row.original.contractId;
        const status = row.original.contractStatus;
        if (!contractId) {
          return <span className="text-muted-foreground">—</span>;
        }

        if (status === "ARCHIVED") {
          return <span className="text-muted-foreground">—</span>;
        }

        const hasTermination = status === "TERMINATION_DUE" || status === "TERMINATED" || Boolean(row.original.terminationDate)
        if (hasTermination) {
          return (
            <span title="A fost deja inregistrata o cerere de incheiere a contractului">
              <Button
                variant="outline"
                size="sm"
                className="border-red-200 bg-red-50 text-red-300 pointer-events-none"
                tabIndex={-1}
                aria-disabled="true"
              >
                Încheie
              </Button>
            </span>
          )
        }

        if (status !== "ACTIVE") {
          return (
            <span title="Nu poate fi incetat un contract care nu a fost incarcat semnat">
              <Button
                variant="outline"
                size="sm"
                className="border-red-200 bg-red-50 text-red-300 pointer-events-none"
                tabIndex={-1}
                aria-disabled="true"
              >
                Încheie
              </Button>
            </span>
          )
        }

        return (
            <TerminateContractDialog
                contractId={contractId}
                onTerminated={() => router.refresh()}
            />
        );
      },
    },
  ], [getEdit, setEdit]);

  return (
      <div className="p-6 space-y-4">
        <div>
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

            {/* Filtre */}
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
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
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
                  <Label className="text-xs text-muted-foreground">User</Label>
                  <Input
                    placeholder="Cauta user..."
                    value={filterUser}
                    onChange={(e) => setFilterUser(e.target.value)}
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
                rowComponent={ClientRow}
                stickyHeader
                searchParamKey="q"
            />
          </div>
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