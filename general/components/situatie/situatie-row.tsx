"use client";
import * as React from "react";
import { Row } from "@tanstack/react-table";
import { TableRow, TableCell } from "@/components/ui/table";
import { flexRender } from "@tanstack/react-table";
import type { SituatieRow } from "@/actions/situatie";

export default function SituatieRowComponent<TData>({ row }: { row: Row<TData> }) {
  const s = row.original as unknown as SituatieRow;
  const stageCols = new Set([
    "avemActe",
    "introdusActe",
    "verificareLuna",
    "generatDeclaratii",
    "depusDeclaratii",
    "lunaPrintata",
  ]);

  return (
    <TableRow data-state={row.getIsSelected() && "selected"}>
      {row.getVisibleCells().map((cell) => {
        const colId = cell.column.id;
        let extra = "";
        if (stageCols.has(colId)) {
          let val = false;
          let hasTask = true;
          if (colId === "avemActe") { val = s.avemActe; hasTask = !!s.avemActeHasTask; }
          else if (colId === "introdusActe") { val = s.introdusActe; hasTask = !!s.introdusActeHasTask; }
          else if (colId === "verificareLuna") { val = s.verificareLuna; hasTask = !!s.verificareLunaHasTask; }
          else if (colId === "generatDeclaratii") { val = s.generatDeclaratii; hasTask = !!s.generatDeclaratiiHasTask; }
          else if (colId === "depusDeclaratii") { val = s.depusDeclaratii; hasTask = !!s.depusDeclaratiiHasTask; }
          else if (colId === "lunaPrintata") { val = s.lunaPrintata; hasTask = !!s.lunaPrintataHasTask; }
          if (!hasTask) extra = " bg-blue-100 hover:bg-blue-50";
          else extra = val ? " bg-green-100 hover:bg-green-50" : " bg-red-100 hover:bg-red-50";
        }
        return (
          <TableCell key={cell.id} className={`px-3 py-2 md:px-4 border-b align-middle${extra}`}>
            {flexRender(cell.column.columnDef.cell, cell.getContext())}
          </TableCell>
        );
      })}
    </TableRow>
  );
}
