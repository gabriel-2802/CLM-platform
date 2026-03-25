"use client";

import Link from "next/link";
import React, { useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { DataTable, type ColumnDef } from "@/components/data-table";
import TaskRowComponent from "@/components/tasks/task-row";
import type { TaskRow } from "@/actions/tasks";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import MonthPicker from "@/components/month-picker";
import { usePathname, useRouter, useSearchParams } from "next/navigation";

type UserOpt = { id: number; label: string };

export default function TasksTable({ rows, users }: { rows: TaskRow[]; users: UserOpt[] }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialUser = searchParams.get("user") ?? "all";
  const initialStatus = searchParams.get("status") ?? "all";
  const initialMonth = searchParams.get("month") ?? "";
  const [userFilter, setUserFilter] = useState<string>(initialUser);
  const [statusFilter, setStatusFilter] = useState<string>(initialStatus);
  const [month, setMonth] = useState<string>(initialMonth); // YYYY-MM

  // Keep URL in sync when filters change
  React.useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    if (userFilter === "all") params.delete("user"); else params.set("user", userFilter);
    if (statusFilter === "all") params.delete("status"); else params.set("status", statusFilter);
    if (!month) params.delete("month"); else params.set("month", month);
    const qs = params.toString();
    const url = qs ? `${pathname}?${qs}` : pathname;
    router.replace(url);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userFilter, statusFilter, month]);

  // Update state if user navigates via back/forward and params change externally
  React.useEffect(() => {
    const u = searchParams.get("user") ?? "all";
    const s = searchParams.get("status") ?? "all";
    const m = searchParams.get("month") ?? "";
    if (u !== userFilter) setUserFilter(u);
    if (s !== statusFilter) setStatusFilter(s);
    if (m !== month) setMonth(m);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const columns: ColumnDef<TaskRow>[] = useMemo(
    () => [
      {
        accessorKey: "title",
        header: "Titlu",
        enableSorting: true,
        cell: ({ row }) => (
          <span className="text-indigo-700 underline">
            <Link href={`/taskuri/edit/${row.original.id}`}>{row.original.title}</Link>
          </span>
        ),
      },
      {
        accessorKey: "date",
        header: "Data",
        enableSorting: true,
        cell: ({ row }) => {
          const ts = row.original.dateTs;
          if (!ts) return "";
          const d = new Date(ts);
          const mm = String(d.getMonth() + 1).padStart(2, "0");
          const yy = d.getFullYear();
          return `${mm}/${yy}`;
        },
        sortingFn: (a, b) => {
          const at = (a.original as TaskRow).dateTs ?? 0;
          const bt = (b.original as TaskRow).dateTs ?? 0;
          return at === bt ? 0 : at < bt ? -1 : 1;
        },
      },
      { accessorKey: "user", header: "User", enableSorting: true },
      { accessorKey: "client", header: "Client", enableSorting: true },
      { accessorKey: "objective", header: "Obiectiv", enableSorting: false },
      { accessorKey: "blocked", header: "Blocata", enableSorting: false },
      { accessorKey: "done", header: "Finalizat", enableSorting: true },
    ],
    []
  );

  const filtered = useMemo(() => {
    let out = rows;
    if (userFilter !== "all") {
      const uid = Number(userFilter);
      out = out.filter((r) => r.userId === uid);
    }
    if (statusFilter === "finalizat") {
      out = out.filter((r) => r.done === true);
    } else if (statusFilter === "nefinalizat") {
      out = out.filter((r) => r.done === false);
    }
    if (month) {
      const [yy, mm] = month.split("-");
      const y = Number(yy);
      const m = Number(mm) - 1; // 0-based
      if (!Number.isNaN(y) && !Number.isNaN(m)) {
        const start = new Date(y, m, 1).getTime();
        const end = new Date(y, m + 1, 1).getTime();
        out = out.filter((r) => (r.dateTs ?? 0) >= start && (r.dateTs ?? 0) < end);
      }
    }
    return out;
  }, [rows, userFilter, statusFilter, month]);

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center gap-2">
        <h1 className="text-2xl font-semibold">Taskuri</h1>
        <Button asChild variant="outline" size="sm" className="ml-auto">
          <Link href="/taskuri/edit/new">Task nou</Link>
        </Button>
      </div>
      <DataTable
        columns={columns}
        data={filtered}
        pageSize={10}
        rowComponent={TaskRowComponent}
        stickyHeader
        primaryControl={
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Luna</span>
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
        leftFilters={
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">User</span>
              <Select value={userFilter} onValueChange={setUserFilter}>
                <SelectTrigger className="w-56 h-8">
                  <SelectValue placeholder="Filtreaza dupa user" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Toti</SelectItem>
                  {users.map((u) => (
                    <SelectItem key={u.id} value={String(u.id)}>
                      {u.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Status</span>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-44 h-8">
                  <SelectValue placeholder="Filtreaza dupa status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Toate</SelectItem>
                  <SelectItem value="finalizat">Finalizat</SelectItem>
                  <SelectItem value="nefinalizat">Nefinalizat</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        }
        searchParamKey="q"
      />
    </div>
  );
}
