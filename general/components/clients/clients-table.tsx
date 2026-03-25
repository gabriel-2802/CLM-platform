"use client";

import Link from "next/link";
import React, { useMemo, useState } from "react";
import { type Row } from "@/actions/clients";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { DataTable, type ColumnDef } from "@/components/data-table";
import ClientRow from "@/components/clients/client-row";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";

export default function ClientsTable({ rows }: { rows: Row[] }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialFormer = (searchParams.get("former") ?? "1").toString();
  const [showFormer, setShowFormer] = useState<boolean>(initialFormer === "1" || initialFormer === "true");
  const [openCabinet, setOpenCabinet] = useState(false);

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

  const columns: ColumnDef<Row>[] = [
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
          <Input type="date" defaultValue={row.original.deLa} className="h-8 px-2 py-1 text-sm w-full" />
        </div>
      ),
    },
    {
      accessorKey: "panaLa",
      header: () => <div className="w-36 min-w-[9rem]">Pana la</div>,
      enableSorting: true,
      cell: ({ row }) => (
        <div className="w-36 min-w-[9rem]">
          {row.original.panaLa ? (
            <Input type="date" defaultValue={row.original.panaLa} className="h-8 px-2 py-1 text-sm w-full" />
          ) : (
            <Button variant="destructive" size="sm">incheie</Button>
          )}
        </div>
      ),
    },
    {
      accessorKey: "tarifConta",
      header: "Tarif servicii conta",
      enableSorting: true,
      cell: ({ row }) => (
        <Input type="number" defaultValue={row.original.tarifConta} className="h-8 px-2 py-1 w-24 text-sm" />
      ),
    },
    {
      accessorKey: "tarifBilant",
      header: "Tarif bilant",
      enableSorting: true,
      cell: ({ row }) => (
        <Input type="number" defaultValue={row.original.tarifBilant} className="h-8 px-2 py-1 w-24 text-sm" />
      ),
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }) =>
        row.original.contractGen ? (
          <span className="text-blue-700 underline">
            <Link href="#">{row.original.contractGen}</Link>
          </span>
        ) : (
          <Button variant="outline" size="sm">gen.</Button>
        ),
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
  ];

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