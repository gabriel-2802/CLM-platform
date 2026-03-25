"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Trash2 } from "lucide-react";
import type { $Enums } from "@/lib/generated/prisma-client";

export type IstoricFormValues = {
  id?: number;
  anul: number;
  cifraAfaceri: number;
  inventar: boolean;
  bilantSemIun: $Enums.DaNuNuECazul;
  bilantAnual: $Enums.DaNuNuECazul;
};

type Props = {
  initial?: Partial<IstoricFormValues>;
  onSubmit: (formData: FormData) => Promise<IstoricFormValues | void>;
  submitLabel?: string;
  onDelete?: () => Promise<void>;
};

export default function ClientIstoricForm({ initial, onSubmit, submitLabel = "Save", onDelete }: Props) {
  const [busy, setBusy] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [anul, setAnul] = useState<number>(initial?.anul ?? new Date().getFullYear());
  const [cifraAfaceri, setCifraAfaceri] = useState<number>(initial?.cifraAfaceri ?? 0);
  const [inventar, setInventar] = useState<boolean>(initial?.inventar ?? false);
  const [bilantSemIun, setBilantSemIun] = useState<$Enums.DaNuNuECazul>(initial?.bilantSemIun ?? "NU_E_CAZUL");
  const [bilantAnual, setBilantAnual] = useState<$Enums.DaNuNuECazul>(initial?.bilantAnual ?? "NU_E_CAZUL");

  // Resync from server-provided fresh props on any remount
  useEffect(() => {
    setAnul(initial?.anul ?? new Date().getFullYear());
    setCifraAfaceri(initial?.cifraAfaceri ?? 0);
    setInventar(initial?.inventar ?? false);
    setBilantSemIun((initial?.bilantSemIun as $Enums.DaNuNuECazul) ?? "NU_E_CAZUL");
    setBilantAnual((initial?.bilantAnual as $Enums.DaNuNuECazul) ?? "NU_E_CAZUL");
  }, [initial]);

  async function handleAction(fd: FormData) {
    setBusy(true);
    try {
      // Ensure latest controlled values are submitted
      fd.set("anul", String(anul));
      fd.set("cifraAfaceri", String(cifraAfaceri ?? 0));
      fd.set("bilantSemIun", bilantSemIun);
      fd.set("bilantAnual", bilantAnual);
      fd.set("inventar", inventar ? "true" : "false");
      const saved = await onSubmit(fd);
      if (saved) {
        setAnul(saved.anul);
        setCifraAfaceri(saved.cifraAfaceri);
        setInventar(saved.inventar);
        setBilantSemIun(saved.bilantSemIun);
        setBilantAnual(saved.bilantAnual);
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <form action={handleAction} className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {typeof initial?.id === "number" ? (
        <input type="hidden" name="id" value={String(initial.id)} />
      ) : null}
      <div>
        <Label className="mb-2 text-purple-800">Anul</Label>
        <Input type="number" name="anul" value={anul} onChange={(e) => setAnul(parseInt(e.target.value || "0", 10) || 0)} required />
      </div>
      <div>
        <Label className="mb-2 text-purple-800">Cifra de afaceri</Label>
        <Input type="number" step="0.01" name="cifraAfaceri" value={cifraAfaceri} onChange={(e) => setCifraAfaceri(parseFloat(e.target.value || "0") || 0)} />
      </div>

      <div>
        <Label className="mb-2 text-purple-800">Bilant semestrial iunie</Label>
        <input type="hidden" name="bilantSemIun" value={bilantSemIun} />
        <Select value={bilantSemIun} onValueChange={(v: $Enums.DaNuNuECazul) => setBilantSemIun(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {(["DA","NU","NU_E_CAZUL"] as $Enums.DaNuNuECazul[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div>
        <Label className="mb-2 text-purple-800">Bilant anual</Label>
        <input type="hidden" name="bilantAnual" value={bilantAnual} />
        <Select value={bilantAnual} onValueChange={(v: $Enums.DaNuNuECazul) => setBilantAnual(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {(["DA","NU","NU_E_CAZUL"] as $Enums.DaNuNuECazul[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Checkbox at the bottom */}
      <div className="md:col-span-2 mt-2">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          <input type="hidden" name="inventar" value={inventar ? "true" : "false"} />
          <label className="inline-flex items-center gap-2 text-purple-800"><Checkbox checked={inventar} onCheckedChange={(v) => setInventar(!!v)} /><span>Inventar</span></label>
        </div>
      </div>

      <div className="md:col-span-2 flex justify-between gap-2 mt-4">
        <Button type="submit" disabled={busy}>{submitLabel}</Button>
        {onDelete ? (
          <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
            <DialogTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                className="text-red-600 hover:text-red-700 hover:bg-red-50 p-2"
                disabled={busy}
                aria-label="Sterge"
                title="Sterge"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Confirma stergerea</DialogTitle>
                <DialogDescription>Esti sigur ca vrei sa stergi acest istoric? Actiunea nu poate fi anulata.</DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <Button variant="secondary" type="button" onClick={() => setConfirmOpen(false)} disabled={busy}>Anuleaza</Button>
                <Button
                  variant="destructive"
                  type="button"
                  disabled={busy}
                  onClick={async () => {
                    setBusy(true);
                    try {
                      await onDelete();
                      setConfirmOpen(false);
                    } finally {
                      setBusy(false);
                    }
                  }}
                >
                  Sterge
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        ) : <span />}
      </div>
    </form>
  );
}
