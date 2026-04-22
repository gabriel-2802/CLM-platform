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

type RowEditFields = { deLa: string; panaLa: string; tarifConta: string; tarifBilant: string }

// Defined outside ClientsTable so its identity is stable across parent re-renders.
// Uses local state for display (prevents focus loss) and calls onCommit to sync parent.
function EditableTarifCell({
  id,
  fieldKey,
  initialValue,
  onCommit,
}: {
  id: number
  fieldKey: keyof RowEditFields
  initialValue: string
  onCommit: (id: number, key: keyof RowEditFields, val: string) => void
}) {
  const [val, setVal] = useState(initialValue)
  const commitRef = useRef(onCommit)
  commitRef.current = onCommit
  return (
    <Input
      type="number"
      value={val}
      onChange={(e) => {
        setVal(e.target.value)
        commitRef.current(id, fieldKey, e.target.value)
      }}
      className="h-8 px-2 py-1 w-24 text-sm"
    />
  )
}

export default function ClientsTable({ rows }: { rows: Row[] }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialFormer = (searchParams.get("former") ?? "1").toString();
  const [showFormer, setShowFormer] = useState<boolean>(initialFormer === "1" || initialFormer === "true");
  const [openCabinet, setOpenCabinet] = useState(false);

  const [rowEdits, setRowEdits] = useState<Record<number, RowEditFields>>(() => {
    const init: Record<number, RowEditFields> = {};
    rows.forEach((r) => {
      init[r.id] = {
        deLa: r.deLa ?? "",
        panaLa: r.panaLa ?? "",
        tarifConta: r.tarifConta != null ? String(r.tarifConta) : "",
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
        <span className="text-primary underline">
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
      accessorKey: "deLa",
      header: () => <div className="w-36 min-w-[9rem]">De la</div>,
      enableSorting: true,
      cell: ({ row }) => (
        <div className="w-36 min-w-[9rem]">
          <Input
            type="date"
            value={getEdit(row.original.id, "deLa")}
            onChange={(e) => setEdit(row.original.id, "deLa", e.target.value)}
            className="h-8 px-2 py-1 text-sm w-full"
          />
        </div>
      ),
    },
    {
      accessorKey: "panaLa",
      header: () => <div className="w-36 min-w-[9rem]">Pana la</div>,
      enableSorting: true,
      cell: ({ row }) => (
        <div className="w-36 min-w-[9rem]">
          <Input
            type="date"
            value={getEdit(row.original.id, "panaLa")}
            onChange={(e) => setEdit(row.original.id, "panaLa", e.target.value)}
            className="h-8 px-2 py-1 text-sm w-full"
          />
        </div>
      ),
    },
    {
      id: "incheie",
      header: "Incheie",
      enableSorting: false,
      cell: () => (
        <Button variant="destructive" size="sm">
          incheie
        </Button>
      ),
    },
    {
      accessorKey: "tarifConta",
      header: "Tarif servicii conta",
      enableSorting: true,
      cell: ({ row }) => (
        <EditableTarifCell
          id={row.original.id}
          fieldKey="tarifConta"
          initialValue={row.original.tarifConta != null ? String(row.original.tarifConta) : ""}
          onCommit={setEdit}
        />
      ),
    },
    {
      accessorKey: "tarifBilant",
      header: "Tarif bilant",
      enableSorting: true,
      cell: ({ row }) => (
        <EditableTarifCell
          id={row.original.id}
          fieldKey="tarifBilant"
          initialValue={row.original.tarifBilant != null ? String(row.original.tarifBilant) : ""}
          onCommit={setEdit}
        />
      ),
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }) => {
        const id = row.original.id;
        const canGenerate =
          !!getEdit(id, "deLa") &&
          !!getEdit(id, "panaLa") &&
          !!getEdit(id, "tarifConta") &&
          !!getEdit(id, "tarifBilant");

        const enrichedClient = {
          ...row.original,
          deLa: getEdit(id, "deLa"),
          panaLa: getEdit(id, "panaLa"),
          tarifConta: getEdit(id, "tarifConta") ? parseFloat(getEdit(id, "tarifConta")) : undefined,
          tarifBilant: getEdit(id, "tarifBilant") ? parseFloat(getEdit(id, "tarifBilant")) : undefined,
        };

        return (
          <div className="flex flex-col gap-1">
            {row.original.contractId ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
                  window.open(
                    `${baseUrl}/api/contracts/download/${row.original.contractId}/unsigned/pdf`,
                    "_blank"
                  );
                }}
              >
                Vizualizează contract
              </Button>
            ) : canGenerate ? (
              <GenerateContractModal client={enrichedClient} />
            ) : (
              <Button
                variant="outline"
                size="sm"
                disabled
                title="Completați câmpurile De la, Până la, Tarif servicii conta și Tarif bilanț"
              >
                gen.
              </Button>
            )}
          </div>
        );
      },
    },
    {
      accessorKey: "contractSemnat",
      header: "Contract semnat",
      enableSorting: false,
      cell: ({ row }) =>
        row.original.contractSemnat ? (
          <span className="text-blue-700 underline">
            <Link href="#">{row.original.contractSemnat}</Link>
          </span>
        ) : (
          <Button variant="destructive" size="sm">incarca</Button>
        ),
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
  // eslint-disable-next-line react-hooks/exhaustive-deps
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
