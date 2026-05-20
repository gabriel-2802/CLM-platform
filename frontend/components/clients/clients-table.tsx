"use client";

import Link from "next/link";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { Download } from "lucide-react";
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

const STATUS_LABELS: Record<string, string> = {
  PENDING_SIGNATURE: "In asteptare",
  ACTIVE: "Activ",
  TERMINATED: "Incetat",
  ARCHIVED: "Arhivat",
};

const STATUS_BADGE: Record<string, string> = {
  PENDING_SIGNATURE: "bg-amber-100 text-amber-800",
  ACTIVE: "bg-green-100 text-green-800",
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
          Contract Info
        </Button>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Contract Info - {clientName}</DialogTitle>
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

  const [rowEdits, setRowEdits] = useState<Record<number, RowEditFields>>(() => {
    const init: Record<number, RowEditFields> = {};
    rows.forEach((r) => {
      init[r.id] = {
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

  const data = useMemo(() => (showFormer ? rows : rows.filter((r) => !r.panaLa)), [rows, showFormer]);

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
      header: "Contract Info",
      enableSorting: false,
      cell: ({ row }) => {
        if (!row.original.contractId) {
          return <span className="text-muted-foreground text-xs">—</span>
        }
        const status = row.original.contractStatus
        const contractSigned = status === "ACTIVE" || status === "TERMINATED" || Boolean(row.original.contractSemnat)
        return (
          <ClientDetailsDialog
              clientId={row.original.id}
              clientName={row.original.name ?? ""}
              deLa={getEdit(row.original.id, "deLa")}
              panaLa={getEdit(row.original.id, "panaLa")}
              tarifConta={getEdit(row.original.id, "tarifConta")}
              tarifBilant={getEdit(row.original.id, "tarifBilant")}
              onEdit={setEdit}
              readOnly={contractSigned}
          />
        )
      },
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }) => {
        const id = row.original.id;
        const enrichedClient = {
          ...row.original,
          deLa: getEdit(id, "deLa"),
          panaLa: getEdit(id, "panaLa"),
          tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
          tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
        };

        const status = row.original.contractStatus ?? "";
        return row.original.contractId ? (
            <div className="flex items-center gap-2">
              <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap ${STATUS_BADGE[status] ?? "bg-slate-100 text-slate-600"}`}>
                {STATUS_LABELS[status] ?? "Generat"}
              </span>
              <button
                  className="text-slate-400 hover:text-slate-700 transition-colors"
                  title="Descarca nesemnat"
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
                <Download className="w-3.5 h-3.5" />
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
        const hasSigned = status === "ACTIVE" || status === "TERMINATED" || Boolean(row.original.contractSemnat);

        if (!contractId) {
          return <span className="text-muted-foreground">—</span>;
        }

        if (hasSigned) {
          return (
              <button
                  className="inline-flex items-center gap-1.5 text-xs text-slate-600 hover:text-slate-900 transition-colors"
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
                semnat
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
      id: "autoRenew",
      header: "Auto-renew",
      enableSorting: false,
      cell: ({ row }) => {
        const contractId = row.original.contractId;
        const status = row.original.contractStatus;
        if (!contractId) {
          return <span className="text-muted-foreground">—</span>;
        }

        const isBusy = Boolean(togglingAutoRenew[contractId]);
        return (
            <Checkbox
                checked={Boolean(row.original.autoRenew)}
                onCheckedChange={() => handleToggleAutoRenew(contractId)}
                disabled={isBusy || status === "TERMINATED" || status === "ARCHIVED"}
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
        const id = row.original.id;
        const enrichedClient = {
          ...row.original,
          deLa: getEdit(id, "deLa"),
          panaLa: getEdit(id, "panaLa"),
          tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
          tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
        };
        return <ActeAditionaleDialog contractId={contractId} client={enrichedClient} onUpdateDetails={(vals) => {
          setEdit(id, "deLa", vals.deLa)
          setEdit(id, "panaLa", vals.panaLa)
          setEdit(id, "tarifConta", vals.tarifConta)
          setEdit(id, "tarifBilant", vals.tarifBilant)
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

        if (status === "TERMINATED") {
          const date = row.original.contractEndDate;
          return <span className="text-xs text-red-700">{date ?? "—"}</span>;
        }
        if (status === "ARCHIVED") {
          return <span className="text-muted-foreground">—</span>;
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