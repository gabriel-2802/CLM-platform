"use client";
import { useMemo, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { DataTable, type ColumnDef } from "@/components/data-table";
import ClientRow from "@/components/clients/client-row";

type Row = {
  name: string;
  tip: string;
  deLa?: string;
  panaLa?: string;
  tarifConta?: number;
  tarifBilant?: number;
  contractGen?: string;
  contractSemnat?: string;
  probleme?: string[];
};

const rows: Row[] = [
  {
    name: "Alexandru Olteanu",
    tip: "PFA",
    deLa: "2017-07-01",
    tarifConta: 150,
    tarifBilant: 100,
    contractGen: "14 din 01/08/2009",
    probleme: ["Registru UC", "Manual pol. contabile"],
  },
  {
    name: "Atolpi",
    tip: "SRL",
    deLa: "2017-07-01",
    tarifConta: 150,
    tarifBilant: 100,
    contractGen: "15 din 01/08/2011",
    contractSemnat: "un fisier.pdf",
  },
  {
    name: "FIA Integra",
    tip: "SRL",
    deLa: "2017-07-01",
    tarifConta: 150,
    tarifBilant: 100,
    contractGen: "16 din 01/08/2011",
    contractSemnat: "fia.pdf",
  },
  {
    name: "Zara",
    tip: "SRL",
    deLa: "2017-07-01",
    panaLa: "2017-07-01",
    tarifConta: 150,
    tarifBilant: 100,
  },
];

export default function ClientsPage() {
  const [showFormer, setShowFormer] = useState(true);
  const [openCabinet, setOpenCabinet] = useState(false);
  const data = useMemo(() => (showFormer ? rows : rows.filter((r) => !r.panaLa)), [showFormer]);

  const columns: ColumnDef<Row>[] = [
    {
      accessorKey: "name",
      header: "Firma",
      enableSorting: true,
      cell: ({ row }: { row: { original: Row } }) => (
        <span className="text-primary underline">
          <Link href="#">{row.original.name}</Link>
        </span>
      ),
    },
    { accessorKey: "tip", header: "Tip", enableSorting: true },
    {
      accessorKey: "deLa",
      header: () => <div className="w-36 min-w-[9rem]">De la</div>,
      enableSorting: true,
      cell: ({ row }: { row: { original: Row } }) => (
        <div className="w-36 min-w-[9rem]">
          <Input type="date" defaultValue={row.original.deLa} className="h-8 px-2 py-1 text-sm w-full" />
        </div>
      ),
    },
    {
      accessorKey: "panaLa",
      header: () => <div className="w-36 min-w-[9rem]">Pana la</div>,
      enableSorting: true,
      cell: ({ row }: { row: { original: Row } }) => (
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
      cell: ({ row }: { row: { original: Row } }) => (
        <Input type="number" defaultValue={row.original.tarifConta} className="h-8 px-2 py-1 w-24 text-sm" />
      ),
    },
    {
      accessorKey: "tarifBilant",
      header: "Tarif bilant",
      enableSorting: true,
      cell: ({ row }: { row: { original: Row } }) => (
        <Input type="number" defaultValue={row.original.tarifBilant} className="h-8 px-2 py-1 w-24 text-sm" />
      ),
    },
    {
      accessorKey: "contractGen",
      header: "Contract generat",
      enableSorting: false,
      cell: ({ row }: { row: { original: Row } }) =>
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
      cell: ({ row }: { row: { original: Row } }) =>
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
      cell: ({ row }: { row: { original: Row } }) =>
        row.original.probleme ? (
          <ul className="list-disc pl-4 space-y-1">
            {row.original.probleme.map((p) => (
              <li key={p}>{p as string}</li>
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
                onCheckedChange={(v: boolean | "indeterminate") => setShowFormer(Boolean(v))}
              />
              Afiseaza fostii clienti
            </label>
            <div className="ml-auto flex gap-2">
              <Button variant="outline" size="sm">
                <span className="i-plus">+</span> client nou
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

