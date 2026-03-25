"use client";
import * as React from "react";
import type { SimpleUser } from "@/actions/client-users";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

type Props = {
  assigned: SimpleUser[];
  available: SimpleUser[];
  onAdd: (userId: number) => Promise<{ clientId: number; userId: number } | void>;
  onRemove: (userId: number) => Promise<{ clientId: number; userId: number } | void>;
};

export default function ClientUsersPanel({ assigned, available, onAdd, onRemove }: Props) {
  const [selected, setSelected] = React.useState<string>("");
  const [busy, setBusy] = React.useState(false);

  async function handleAdd() {
    if (!selected) return;
    setBusy(true);
    try {
      await onAdd(Number(selected));
      setSelected("");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <Select value={selected} onValueChange={setSelected}>
          <SelectTrigger className="w-60 h-9">
            <SelectValue placeholder={available.length ? "Alege user" : "Nu mai sunt useri disponibili"} />
          </SelectTrigger>
          <SelectContent>
            {available.map((u) => (
              <SelectItem key={u.id} value={String(u.id)}>
                {u.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Button size="sm" onClick={handleAdd} disabled={!selected || busy}>
          Adauga
        </Button>
      </div>
      <div>
        <h3 className="text-sm font-medium mb-2">Useri asignati</h3>
        {assigned.length ? (
          <ul className="divide-y rounded border">
            {assigned.map((u) => (
              <li key={u.id} className="flex items-center justify-between px-3 py-2">
                <span>{u.label}</span>
                <Button variant="outline" size="sm" onClick={() => onRemove(u.id)}>
                  Elimina
                </Button>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-muted-foreground">Niciun user asignat.</p>
        )}
      </div>
    </div>
  );
}
