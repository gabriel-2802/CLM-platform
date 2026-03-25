"use client";

import { useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger, DialogDescription } from "@/components/ui/dialog";
import { Trash2 } from "lucide-react";
import type { $Enums } from "@/lib/generated/prisma-client";

export type PunctFormValues = {
  id?: number;
  denumire: string;
  deLa: string; // YYYY-MM-DD
  panaLa?: string;
  administratie: $Enums.Administratie;
  registruUC: boolean;
  salariati: number;
  cui?: string;
  casaDeMarcat: boolean;
};

type Props = {
  initial?: Partial<PunctFormValues>;
  onSubmit: (formData: FormData) => Promise<PunctFormValues | void>;
  submitLabel?: string;
  onDelete?: () => Promise<void>;
};

export default function ClientPunctForm({ initial, onSubmit, submitLabel = "Save", onDelete }: Props) {
  const [busy, setBusy] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  // Controlled state for all fields
  const [denumire, setDenumire] = useState(initial?.denumire ?? "");
  const [administratie, setAdministratie] = useState<$Enums.Administratie>(initial?.administratie ?? "SECTOR_1");
  const [deLa, setDeLa] = useState(initial?.deLa ?? "");
  const [panaLa, setPanaLa] = useState(initial?.panaLa ?? "");
  const [cui, setCui] = useState(initial?.cui ?? "");
  const [salariati, setSalariati] = useState<number>(typeof initial?.salariati === "number" ? initial!.salariati : 0);
  const [registruUC, setRegistruUC] = useState<boolean>(initial?.registruUC ?? false);
  const [casaDeMarcat, setCasaDeMarcat] = useState<boolean>(initial?.casaDeMarcat ?? false);
  const registruUCRef = useRef(registruUC);
  const casaDeMarcatRef = useRef(casaDeMarcat);

  // Resync from fresh server-provided `initial` (like Detalii form) when parent updates the row
  useEffect(() => {
    if (!initial) return;
    if (typeof initial.denumire === "string") setDenumire(initial.denumire);
    if (initial.administratie) setAdministratie(initial.administratie);
    if (typeof initial.deLa === "string") setDeLa(initial.deLa);
    setPanaLa(initial.panaLa ?? "");
    setCui(initial.cui ?? "");
    if (typeof initial.salariati === "number") setSalariati(initial.salariati);
    const reg = !!initial.registruUC;
    const cdm = !!initial.casaDeMarcat;
    setRegistruUC(reg);
    setCasaDeMarcat(cdm);
    registruUCRef.current = reg;
    casaDeMarcatRef.current = cdm;
  }, [initial]);

  // Note: we avoid resyncing blindly from initial to prevent overwriting the just-saved local state

  async function handleAction(fd: FormData) {
    setBusy(true);
    try {
      // Guarantee latest controlled values reach the server (avoid hidden input race conditions)
      fd.set("administratie", administratie);
  fd.set("registruUC", registruUCRef.current ? "true" : "false");
  fd.set("casaDeMarcat", casaDeMarcatRef.current ? "true" : "false");
      fd.set("salariati", String(salariati ?? 0));
  fd.set("denumire", denumire);
  if (deLa) fd.set("deLa", deLa);
  if (panaLa !== undefined) fd.set("panaLa", panaLa);
  if (cui !== undefined) fd.set("cui", cui);
      const saved = await onSubmit(fd);
      if (saved) {
        // Sync all local state from the saved payload to reflect values instantly
        if (typeof saved.denumire === "string") setDenumire(saved.denumire);
        if (saved.administratie) setAdministratie(saved.administratie);
        if (typeof saved.deLa === "string") setDeLa(saved.deLa);
        setPanaLa(saved.panaLa ?? "");
        setCui(saved.cui ?? "");
        if (typeof saved.salariati === "number") setSalariati(saved.salariati);
        if (typeof saved.registruUC === "boolean") setRegistruUC(saved.registruUC);
        if (typeof saved.casaDeMarcat === "boolean") setCasaDeMarcat(saved.casaDeMarcat);
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <form action={handleAction} className="grid grid-cols-1 md:grid-cols-2 gap-4">
  {initial?.id ? <input type="hidden" name="id" value={String(initial.id)} /> : null}

      <div>
        <Label className="mb-2 text-purple-800">Denumire</Label>
        <Input name="denumire" value={denumire} onChange={(e) => setDenumire(e.target.value)} required />
      </div>

      <div>
        <Label className="mb-2 text-purple-800">Administratie</Label>
        <input type="hidden" name="administratie" value={administratie} />
        <Select value={administratie} onValueChange={(v: $Enums.Administratie) => setAdministratie(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {(["SECTOR_1","SECTOR_2","SECTOR_3","SECTOR_4","SECTOR_5","SECTOR_6","ILFOV","BUFTEA","BRAGADIRU","VRANCEA"] as $Enums.Administratie[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div>
        <Label className="mb-2 text-purple-800">De la</Label>
        <Input type="date" name="deLa" value={deLa} onChange={(e) => setDeLa(e.target.value)} required />
      </div>
      <div>
        <Label className="mb-2 text-purple-800">Pana la</Label>
        <Input type="date" name="panaLa" value={panaLa} onChange={(e) => setPanaLa(e.target.value)} />
      </div>

      <div>
        <Label className="mb-2 text-purple-800">CUI</Label>
        <Input name="cui" value={cui} onChange={(e) => setCui(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-purple-800">Salariati</Label>
        <Input type="number" name="salariati" value={salariati} onChange={(e) => setSalariati(Number(e.target.value))} min={0} />
      </div>

      {/* Checkbox group at the bottom */}
      <div className="md:col-span-2 mt-2">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {/* Hidden inputs ensure values are submitted since Radix Checkbox isn't a native input */}
          <input type="hidden" name="registruUC" value={registruUC ? "true" : "false"} />
          <input type="hidden" name="casaDeMarcat" value={casaDeMarcat ? "true" : "false"} />
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox
              checked={registruUC}
              onCheckedChange={(v) => {
                const b = v === true;
                setRegistruUC(b);
                registruUCRef.current = b;
              }}
            />
            <span>Registru UC</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox
              checked={casaDeMarcat}
              onCheckedChange={(v) => {
                const b = v === true;
                setCasaDeMarcat(b);
                casaDeMarcatRef.current = b;
              }}
            />
            <span>Casa de marcat</span>
          </label>
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
                <DialogDescription>Esti sigur ca vrei sa stergi acest punct de lucru? Actiunea nu poate fi anulata.</DialogDescription>
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
