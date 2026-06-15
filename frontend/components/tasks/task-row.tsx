"use client";
import * as React from "react";
import Link from "next/link";
import { Row } from "@tanstack/react-table";
import { TableRow, TableCell } from "@/components/ui/table";
import { flexRender } from "@tanstack/react-table";
import type { TaskRow as TRow } from "@/actions/tasks";
import { markTaskDone } from "@/actions/tasks";

export default function TaskRowComponent<TData>({ row }: { row: Row<TData> }) {
	const t = row.original as unknown as TRow;
	const [pending, setPending] = React.useState(false);

	return (
		<TableRow data-state={row.getIsSelected() && "selected"}>
			{row.getVisibleCells().map((cell) => (
				<TableCell key={cell.id} className="px-3 py-2 md:px-4 border-b align-middle">
					{cell.column.id === "title" ? (
						<Link href={`/taskuri/edit/${t.id}`} className="font-medium text-slate-800 hover:underline hover:text-slate-600">
							{t.title}
						</Link>
					) : cell.column.id === "done" ? (
						t.done ? (
							<span className="text-green-600 text-xs font-medium">✔ Finalizat</span>
						) : (
							<button
								disabled={pending}
								onClick={async () => {
									setPending(true);
									await markTaskDone(t.id);
									setPending(false);
								}}
								className="px-2.5 py-1 text-xs font-medium rounded border border-slate-200 text-slate-600 hover:bg-slate-100 transition-colors disabled:opacity-50"
							>
								{pending ? "..." : "Finalizează"}
							</button>
						)
					) : (
						flexRender(cell.column.columnDef.cell, cell.getContext())
					)}
				</TableCell>
			))}
		</TableRow>
	);
}
