"use client";

import { useEffect, useState } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import ClientPunctForm, { type PunctFormValues } from "@/components/clients/client-punct-form";
import { toast } from "sonner";

type Props = {
  rows: PunctFormValues[];
  onCreate: (fd: FormData) => Promise<PunctFormValues>;
  onSave: (fd: FormData) => Promise<PunctFormValues>;
  onDelete: (id: number) => Promise<{ id: number } | void>;
};

export default function ClientPunctPanels({ rows, onCreate, onSave, onDelete }: Props) {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState(rows);

  // If the server revalidates and provides fresh rows, keep local list in sync
  useEffect(() => {
    setItems(rows);
  }, [rows]);

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button size="sm">Adauga punct de lucru</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Adauga punct de lucru</DialogTitle>
              <DialogDescription>Completeaza detaliile pentru noul punct de lucru.</DialogDescription>
            </DialogHeader>
            <ClientPunctForm
              onSubmit={async (fd) => {
                try {
                  const saved = await onCreate(fd);
                  setItems((prev) => [...prev, saved]);
                  setOpen(false);
                  toast.success("Punct de lucru creat");
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
        <p className="text-sm text-muted-foreground">Nu exista puncte de lucru.</p>
      )}

      {items.map((r) => (
        <Card key={r.id} className="p-4">
          <ClientPunctForm
            initial={r}
            onSubmit={async (fd) => {
              try {
                const saved = await onSave(fd);
                if (saved && saved.id) {
                  setItems((prev) => prev.map((it) => (it.id === saved.id ? saved : it)));
                }
                toast.success("Salvat");
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
      ))}
    </div>
  );
}
