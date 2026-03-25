"use client";
import * as React from "react";
import { Row } from "@tanstack/react-table";
import { TableRow, TableCell } from "@/components/ui/table";
import { flexRender } from "@tanstack/react-table";

// Generic row component that renders cells using the provided column definitions
// Extend this if you want per-row actions, hover menus, etc.
export function ClientRow<TData>({ row }: { row: Row<TData> }) {
  return (
    <TableRow data-state={row.getIsSelected() && "selected"} className="transition-colors">
      {row.getVisibleCells().map((cell) => (
        <TableCell key={cell.id} className="px-4 py-2 border-b">
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
        </TableCell>
      ))}
    </TableRow>
  );
}

export default ClientRow;
