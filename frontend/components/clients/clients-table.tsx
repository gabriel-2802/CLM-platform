"use client";

import Link from "next/link";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { type Row } from "@/actions/clients";
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
import { terminateContract, toggleAutoRenewal, uploadSignedContract } from "@/actions/contracts";
import { toast } from "sonner";

type RowEditFields = { deLa: string; panaLa: string; tarifConta: string; tarifBilant: string }

const STATUS_LABELS: Record<string, string> = {
  PENDING_SIGNATURE: "In asteptare",
  ACTIVE: "Activ",
  TERMINATED: "Incetat",
  ARCHIVED: "Arhivat",
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
        <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
          incarca
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
        <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
          incheie
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
                             }: {
  clientId: number
  clientName: string
  deLa: string
  panaLa: string
  tarifConta: string
  tarifBilant: string
  onEdit: (id: number, key: keyof RowEditFields, val: string) => void
}) {
  const [open, setOpen] = useState(false)
  return (
      <Dialog open={open} onOpenChange={setOpen}>
        <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
          Detalii
        </Button>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Detalii - {clientName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1">
              <Label>De la</Label>
              <Input
                  type="date"
                  value={deLa}
                  onChange={(e) => onEdit(clientId, "deLa", e.target.value)}
                  className="text-slate-900"
              />
            </div>
            <div className="space-y-1">
              <Label>Pana la</Label>
              <Input
                  type="date"
                  value={panaLa}
                  onChange={(e) => onEdit(clientId, "panaLa", e.target.value)}
                  className="text-slate-900"
              />
            </div>
            <div className="space-y-1">
              <Label>Tarif servicii conta</Label>
              <Input
                  type="number"
                  value={tarifConta}
                  onChange={(e) => onEdit(clientId, "tarifConta", e.target.value)}
                  className="text-slate-900"
              />
            </div>
            <div className="space-y-1">
              <Label>Tarif bilant</Label>
              <Input
                  type="number"
                  value={tarifBilant}
                  onChange={(e) => onEdit(clientId, "tarifBilant", e.target.value)}
                  className="text-slate-900"
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

export default function ClientsTable({ rows }: { rows: Row[] }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
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
          <span className="text-slate-900 underline">
          <Link href={`/clients/edit/${row.original.id}`}>{row.original.name}</Link>
        </span>
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
      header: "Detalii",
      enableSorting: false,
      cell: ({ row }) => (
          <ClientDetailsDialog
              clientId={row.original.id}
              clientName={row.original.name ?? ""}
              deLa={getEdit(row.original.id, "deLa")}
              panaLa={getEdit(row.original.id, "panaLa")}
              tarifConta={getEdit(row.original.id, "tarifConta")}
              tarifBilant={getEdit(row.original.id, "tarifBilant")}
              onEdit={setEdit}
          />
      ),
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }) => {
        const id = row.original.id;
        const deLa = getEdit(id, "deLa");
        const panaLa = getEdit(id, "panaLa");
        const tarifConta = getEdit(id, "tarifConta");
        const tarifBilant = getEdit(id, "tarifBilant");
        const allFilled = !!(deLa && panaLa && tarifConta && tarifBilant);
        const enrichedClient = {
          ...row.original,
          deLa,
          panaLa,
          tarifConta: tarifConta ? parseFloat(tarifConta) : undefined,
          tarifBilant: tarifBilant ? parseFloat(tarifBilant) : undefined,
        };

        return (
            <div className="flex flex-col gap-1">
              {row.original.contractId ? (
                  <>
                    <Button
                        variant="secondary"
                        size="sm"
                        disabled
                        className="bg-slate-300 text-slate-900 disabled:opacity-100"
                    >
                      {STATUS_LABELS[row.original.contractStatus ?? ""] ?? "Generat"}
                    </Button>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          window.open(
                              `/api/contracts/download/${row.original.contractId}?type=unsigned`,
                              "_blank"
                          );
                        }}
                    >
                      Descarca nesemnat
                    </Button>
                  </>
              ) : (
                  <GenerateContractModal client={enrichedClient} disabled={!allFilled} />
              )}
            </div>
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
              <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    window.open(
                        `/api/contracts/download/${contractId}?type=signed`,
                        "_blank"
                    );
                  }}
              >
                Descarca semnat
              </Button>
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
        return <ActeAditionaleDialog contractId={contractId} client={enrichedClient} />;
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

        if (status === "TERMINATED" || status === "ARCHIVED") {
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
    {
      id: "probleme",
      header: "Probleme",
      enableSorting: false,
      cell: ({ row }) =>
          row.original.probleme ? (
              <ul className="list-disc pl-4 space-y-1">
                {row.original.probleme.map((p) => (
                    <li key={p}>{p}</li>
                ))}
              </ul>
          ) : null,
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
                <Button asChild variant="outline" size="sm">
                  <Link href="/clients/new"><span className="i-plus">+</span> client nou</Link>
                </Button>
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