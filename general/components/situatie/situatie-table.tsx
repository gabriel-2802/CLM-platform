"use client";
import * as React from "react";
import { DataTable, type ColumnDef } from "@/components/data-table";
import type { SituatieRow } from "@/actions/situatie";
import MonthPicker from "@/components/month-picker";
// Removed Select in favor of custom autocomplete
import FirmaAutocomplete from "@/components/situatie/firma-autocomplete";
import SituatieRowComponent from "@/components/situatie/situatie-row";
import { usePathname, useRouter, useSearchParams } from "next/navigation";

export default function SituatieTable({ rows }: { rows: SituatieRow[] }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialMonth = searchParams.get("month") ?? "";
  const initialFirma = searchParams.get("firma") ?? "";
  const [month, setMonth] = React.useState<string>(initialMonth); // format YYYY-MM
  const columns = React.useMemo<ColumnDef<SituatieRow>[]>(
    () => [
      { accessorKey: "firma", header: "Firma", enableSorting: true },
  { accessorKey: "user", header: "User", enableSorting: false, cell: ({ row }) => (<span>{row.original.user ?? ""}</span>) },
      {
        accessorKey: "data",
        header: "Data",
        enableSorting: true,
        cell: ({ row }) => {
          const ts = row.original.dateTs;
          const d = new Date(ts);
          const mm = String(d.getMonth() + 1).padStart(2, "0");
          const yyyy = d.getFullYear();
          return <span>{mm}/{yyyy}</span>;
        },
        sortingFn: (a, b) => {
          const at = (a.original as SituatieRow).dateTs ?? 0;
          const bt = (b.original as SituatieRow).dateTs ?? 0;
          return at === bt ? 0 : at < bt ? -1 : 1;
        },
      },
      { accessorKey: "tip", header: "Tip", enableSorting: true },
      {
        accessorKey: "avemActe",
        header: "Avem acte",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.avemActeHasTask ? null : row.original.avemActeTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.avemActeTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.avemActeNotes ?? (row.original.avemActe ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.avemActeNotes ?? (row.original.avemActe ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.avemActeNotes ?? (row.original.avemActe ? "Finalizat" : "Ne-finalizat") }>
                {row.original.avemActeNotes ?? (row.original.avemActe ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
      {
        accessorKey: "introdusActe",
        header: "Introdus acte",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.introdusActeHasTask ? null : row.original.introdusActeTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.introdusActeTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.introdusActeNotes ?? (row.original.introdusActe ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.introdusActeNotes ?? (row.original.introdusActe ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.introdusActeNotes ?? (row.original.introdusActe ? "Finalizat" : "Ne-finalizat") }>
                {row.original.introdusActeNotes ?? (row.original.introdusActe ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
      {
        accessorKey: "verificareLuna",
        header: "Verificat acte",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.verificareLunaHasTask ? null : row.original.verificareLunaTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.verificareLunaTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.verificareLunaNotes ?? (row.original.verificareLuna ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.verificareLunaNotes ?? (row.original.verificareLuna ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.verificareLunaNotes ?? (row.original.verificareLuna ? "Finalizat" : "Ne-finalizat") }>
                {row.original.verificareLunaNotes ?? (row.original.verificareLuna ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
      {
        accessorKey: "generatDeclaratii",
        header: "Generat declaratii",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.generatDeclaratiiHasTask ? null : row.original.generatDeclaratiiTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.generatDeclaratiiTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.generatDeclaratiiNotes ?? (row.original.generatDeclaratii ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.generatDeclaratiiNotes ?? (row.original.generatDeclaratii ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.generatDeclaratiiNotes ?? (row.original.generatDeclaratii ? "Finalizat" : "Ne-finalizat") }>
                {row.original.generatDeclaratiiNotes ?? (row.original.generatDeclaratii ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
      {
        accessorKey: "depusDeclaratii",
        header: "Depus declaratii",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.depusDeclaratiiHasTask ? null : row.original.depusDeclaratiiTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.depusDeclaratiiTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.depusDeclaratiiNotes ?? (row.original.depusDeclaratii ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.depusDeclaratiiNotes ?? (row.original.depusDeclaratii ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.depusDeclaratiiNotes ?? (row.original.depusDeclaratii ? "Finalizat" : "Ne-finalizat") }>
                {row.original.depusDeclaratiiNotes ?? (row.original.depusDeclaratii ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
      {
        accessorKey: "lunaPrintata",
        header: "Luna printata",
        cell: ({ row }) => (
          <div className="flex items-center">
            {!row.original.lunaPrintataHasTask ? null : row.original.lunaPrintataTaskId ? (
              <a
                href={`/taskuri/edit/${row.original.lunaPrintataTaskId}`}
                className="text-xs font-medium underline"
                title={row.original.lunaPrintataNotes ?? (row.original.lunaPrintata ? "Finalizat" : "Ne-finalizat")}
              >
                {row.original.lunaPrintataNotes ?? (row.original.lunaPrintata ? "DA" : "NO")}
              </a>
            ) : (
              <span className="text-xs font-medium truncate" title={row.original.lunaPrintataNotes ?? (row.original.lunaPrintata ? "Finalizat" : "Ne-finalizat") }>
                {row.original.lunaPrintataNotes ?? (row.original.lunaPrintata ? "DA" : "NO")}
              </span>
            )}
          </div>
        ),
      },
    ],
    []
  );

  const [firma, setFirma] = React.useState<string>(initialFirma);
  // Sync URL when month or firma changes
  React.useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    if (!month) params.delete("month"); else params.set("month", month);
    if (!firma) params.delete("firma"); else params.set("firma", firma);
    const qs = params.toString();
    const url = qs ? `${pathname}?${qs}` : pathname;
    router.replace(url);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month, firma]);
  // Restore state when URL changes via navigation
  React.useEffect(() => {
    const m = searchParams.get("month") ?? "";
    if (m !== month) setMonth(m);
    const f = searchParams.get("firma") ?? "";
    if (f !== firma) setFirma(f);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);
  const filtered = React.useMemo(() => {
    let out = rows;
    if (month) {
      const [yy, mm] = month.split("-");
      const y = Number(yy);
      const m = Number(mm) - 1; // 0-based
      if (!Number.isNaN(y) && !Number.isNaN(m)) {
        const start = new Date(y, m, 1).getTime();
        const end = new Date(y, m + 1, 1).getTime();
        out = out.filter((r) => r.dateTs >= start && r.dateTs < end);
      }
    }
    if (firma) {
      out = out.filter((r) => r.firma === firma);
    }
    return out;
  }, [rows, month, firma]);

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Situatie</h1>
      <DataTable
        columns={columns}
        data={filtered}
        pageSize={20}
        stickyHeader
        searchParamKey="q"
        rowComponent={SituatieRowComponent}
        primaryControl={
          <div className="flex items-center gap-2">
            <label className="text-sm text-muted-foreground">Firma</label>
            <FirmaAutocomplete
              firms={Array.from(new Set(rows.map((r) => r.firma)))}
              value={firma}
              onChange={(v) => setFirma(v)}
            />
          </div>
        }
        leftFilters={
          <div className="flex items-center gap-2">
            <label className="text-sm text-muted-foreground">Luna</label>
            <MonthPicker value={month || undefined} onChange={(val) => setMonth(val ?? "")} buttonClassName="h-8" />
            {month && (
              <button
                type="button"
                onClick={() => setMonth("")}
                className="h-8 px-3 rounded border text-sm hover:bg-muted"
              >
                Reset
              </button>
            )}
          </div>
        }
      />
    </div>
  );
}
