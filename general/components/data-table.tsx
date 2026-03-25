"use client";
import * as React from "react";
import {
  ColumnDef,
  ColumnFiltersState,
  type FilterFn,
  Row as TanstackRow,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
} from "@tanstack/react-table";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { usePathname, useRouter, useSearchParams } from "next/navigation";

// Re-export ColumnDef so callers can `import type { ColumnDef } from "@/components/data-table"`
export type { ColumnDef } from "@tanstack/react-table";

type DataTableProps<TData, TValue> = {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  pageSize?: number;
  rowComponent?: React.ComponentType<{ row: TanstackRow<TData> }>;
  stickyHeader?: boolean;
  leftFilters?: React.ReactNode;
  searchParamKey?: string; // when provided, persist the global search value in the URL under this key
  primaryControl?: React.ReactNode; // replaces the default search input when provided
  showGlobalSearch?: boolean; // toggles visibility of the default global search; defaults to true when primaryControl is not set
};

export function DataTable<TData, TValue>({ columns, data, pageSize = 10, rowComponent: RowComponent, stickyHeader = false, leftFilters, searchParamKey, primaryControl, showGlobalSearch = undefined }: DataTableProps<TData, TValue>) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialSearch = React.useMemo(() => (searchParamKey ? (searchParams.get(searchParamKey) ?? "") : ""), [searchParams, searchParamKey]);
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [globalFilter, setGlobalFilter] = React.useState<string>(initialSearch);

  const globalContains: FilterFn<TData> = React.useCallback((row, _columnId, filterValue) => {
    const query = String(filterValue ?? "").toLowerCase();
    if (!query) return true;
    // Search across all original row values so any column can match
    const vals = Object.values(row.original as Record<string, unknown>);
    for (const v of vals) {
      if (v === null || v === undefined) continue;
      const text = String(v).toLowerCase();
      if (text.includes(query)) return true;
    }
    return false;
  }, []);

  const table = useReactTable({
    data,
    columns,
    state: { sorting, columnFilters, globalFilter },
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    globalFilterFn: globalContains,
    initialState: { pagination: { pageIndex: 0, pageSize } },
  });

  // Persist global search to URL
  React.useEffect(() => {
    if (!searchParamKey) return;
    const params = new URLSearchParams(searchParams.toString());
    const val = table.getState().globalFilter ?? "";
    if (!val) params.delete(searchParamKey); else params.set(searchParamKey, val);
    const qs = params.toString();
    const url = qs ? `${pathname}?${qs}` : pathname;
    router.replace(url);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParamKey, table.getState().globalFilter]);

  // Sync table state if URL changes externally (back/forward)
  React.useEffect(() => {
    if (!searchParamKey) return;
    const val = searchParams.get(searchParamKey) ?? "";
    if (val !== (table.getState().globalFilter ?? "")) {
      table.setGlobalFilter(val);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, searchParamKey]);

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          {primaryControl ?? (
            (showGlobalSearch ?? true) && (
              <Input
                placeholder="Cauta (inclusiv data dd/mm/yyyy)..."
                value={table.getState().globalFilter ?? ""}
                onChange={(e) => table.setGlobalFilter(e.target.value)}
                className="w-64"
              />
            )
          )}
          {leftFilters}
        </div>
        <div className="text-xs text-muted-foreground">
          {table.getFilteredRowModel().rows.length} results
        </div>
      </div>
      <div className="overflow-hidden rounded-lg border">
        <div className="overflow-x-auto overflow-y-auto max-h-[60vh]">
          <Table className="text-xs md:text-sm min-w-full">
               <TableHeader className={stickyHeader ? "bg-muted/50 backdrop-blur supports-[backdrop-filter]:bg-muted/60 sticky top-0 z-10" : undefined}>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                      <TableHead key={header.id} className="px-3 py-2 md:px-4 border-b whitespace-nowrap text-foreground">
                    {header.isPlaceholder ? null : (
                      <div
                        className={header.column.getCanSort() ? "cursor-pointer select-none" : undefined}
                        onClick={header.column.getToggleSortingHandler()}
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {{ asc: " ▲", desc: " ▼" }[header.column.getIsSorted() as string] ?? null}
                      </div>
                    )}
                  </TableHead>
                ))}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              {table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row) => (
                  RowComponent ? (
                    <RowComponent key={row.id} row={row} />
                  ) : (
                      <TableRow
                        key={row.id}
                        data-state={row.getIsSelected() && "selected"}
                        className="transition-colors"
                      >
                      {row.getVisibleCells().map((cell) => (
                        <TableCell key={cell.id} className="px-3 py-2 md:px-4 border-b align-middle">
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </TableCell>
                      ))}
                    </TableRow>
                  )
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={columns.length} className="h-24 text-center text-muted-foreground">
                    No results.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>
      <div className="flex items-center justify-end gap-2">
        <div className="text-xs text-muted-foreground mr-auto">
          Page {table.getState().pagination.pageIndex + 1} of {table.getPageCount()}
        </div>
        <Button variant="outline" size="sm" onClick={() => table.previousPage()} disabled={!table.getCanPreviousPage()}>
          Previous
        </Button>
        <Button variant="outline" size="sm" onClick={() => table.nextPage()} disabled={!table.getCanNextPage()}>
          Next
        </Button>
      </div>
    </div>
  );
}
