"use client";

import { useEffect, useState } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import ClientIstoricForm, { type IstoricFormValues } from "@/components/clients/client-istoric-form";
import { toast } from "sonner";

type Props = {
  rows: IstoricFormValues[];
  onCreate: (fd: FormData) => Promise<IstoricFormValues>;
  onSave: (fd: FormData) => Promise<IstoricFormValues>;
  onDelete: (id: number) => Promise<{ id: number } | void>;
};

export default function ClientIstoricPanels({ rows, onCreate, onSave, onDelete }: Props) {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState(rows);
  useEffect(() => setItems(rows), [rows]);

  function mergeSaved(
    prev: IstoricFormValues[],
    saved: IstoricFormValues,
    originalId?: number
  ): IstoricFormValues[] {
    // Remove the original row if this was an edit
  const next = typeof originalId === "number" ? prev.filter((it) => it.id !== originalId) : [...prev];
    // Replace by id or by anul if one exists already
    const idx = next.findIndex((it) => (typeof saved.id === "number" && it.id === saved.id) || it.anul === saved.anul);
    if (idx >= 0) {
      next[idx] = saved;
    } else {
      next.unshift(saved);
    }
    // Keep sorted by year desc
    next.sort((a, b) => (b.anul ?? 0) - (a.anul ?? 0));
    return next;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button size="sm">Adauga istoric</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Adauga istoric</DialogTitle>
              <DialogDescription>Completeaza datele pentru un nou an istoric.</DialogDescription>
            </DialogHeader>
            <ClientIstoricForm
              onSubmit={async (fd) => {
                try {
                  const saved = await onCreate(fd);
                  setItems((prev) => mergeSaved(prev, saved));
                  setOpen(false);
                  toast.success("Istoric salvat");
                } catch (e: unknown) {
                  const msg = e instanceof Error ? e.message : "Eroare la creare";
                  toast.error(msg);
                }
              }}
              submitLabel="Creeaza"
            />
            <DialogFooter>
              <Button variant="secondary" onClick={() => setOpen(false)}>Inchide</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {items.length === 0 && (
        <p className="text-sm text-muted-foreground">Nu exista inca inregistrari de istoric.</p>
      )}

      {items.map((r) => {
        const key = String(r.id ?? r.anul);
        return (
        <Card key={key} className="p-4">
          <ClientIstoricForm
            initial={r}
            onSubmit={async (fd) => {
              try {
                const saved = await onSave(fd);
                setItems((prev) => mergeSaved(prev, saved, r.id));
                toast.success("Istoric salvat");
              } catch (e: unknown) {
                const msg = e instanceof Error ? e.message : "Eroare la salvare";
                toast.error(msg);
              }
            }}
            submitLabel="Salveaza"
            onDelete={r.id ? async () => {
              try {
                await onDelete(r.id!);
                setItems((prev) => prev.filter((it) => it.id !== r.id));
                toast.success("Sters");
              } catch (e: unknown) {
                const msg = e instanceof Error ? e.message : "Eroare la stergere";
                toast.error(msg);
              }
            } : undefined}
          />
        </Card>
      );})}
    </div>
  );
}
