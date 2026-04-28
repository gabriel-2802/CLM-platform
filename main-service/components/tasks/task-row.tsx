"use client";
import * as React from "react";
import Link from "next/link";
import { Row } from "@tanstack/react-table";
import { TableRow, TableCell } from "@/components/ui/table";
import { flexRender } from "@tanstack/react-table";
import type { TaskRow as TRow } from "@/actions/tasks";

export default function TaskRowComponent<TData>({ row }: { row: Row<TData> }) {
	const t = row.original as unknown as TRow;
	return (
		<TableRow
			data-state={row.getIsSelected() && "selected"}
			className={t.done ? undefined : "bg-red-100 hover:bg-red-50"}
		>
			{row.getVisibleCells().map((cell) => (
				<TableCell key={cell.id} className="px-3 py-2 md:px-4 border-b align-middle">
					{cell.column.id === "title"
						? (
								<Link href={`/taskuri/edit/${t.id}`} className="underline text-indigo-700">
									{t.title}
								</Link>
							)
						: cell.column.id === "done"
						? (t.done ? "✔" : "")
						: flexRender(cell.column.columnDef.cell, cell.getContext())}
				</TableCell>
			))}
		</TableRow>
	);
}