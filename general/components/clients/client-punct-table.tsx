"use client";

import { useState } from "react";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { $Enums } from "@/lib/generated/prisma-client";
import type { PunctDeLucruValues } from "@/actions/client-punct";

type RowProps = {
  initial: PunctDeLucruValues;
  onSubmit: (fd: FormData) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
};

function PunctRow({ initial, onSubmit, onDelete }: RowProps) {
  const [busy, setBusy] = useState(false);
  const [administratie, setAdministratie] = useState<$Enums.Administratie>(initial.administratie);

  async function handleAction(fd: FormData) {
    setBusy(true);
    try {
      await onSubmit(fd);
    } finally {
      setBusy(false);
    }
  }

  return (
    <tr className="border-b align-top">
      <td className="p-2">
        <form action={handleAction} className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          <input type="hidden" name="id" value={String(initial.id)} />

          <div>
            <Label className="mb-1 text-purple-800">Denumire</Label>
            <Input name="denumire" defaultValue={initial.denumire} />
          </div>
          <div>
            <Label className="mb-1 text-purple-800">Administratie</Label>
            <input type="hidden" name="administratie" value={administratie} />
            <Select value={administratie} onValueChange={(v: $Enums.Administratie) => setAdministratie(v)}>
              <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
              <SelectContent>
                {["SECTOR_1","SECTOR_2","SECTOR_3","SECTOR_4","SECTOR_5","SECTOR_6","ILFOV","BUFTEA","BRAGADIRU","VRANCEA"].map((v) => (
                  <SelectItem key={v} value={v}>{v}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div>
            <Label className="mb-1 text-purple-800">De la</Label>
            <Input type="date" name="deLa" defaultValue={initial.deLa} />
          </div>
          <div>
            <Label className="mb-1 text-purple-800">Pana la</Label>
            <Input type="date" name="panaLa" defaultValue={initial.panaLa ?? ""} />
          </div>

          <div>
            <Label className="mb-1 text-purple-800">CUI</Label>
            <Input name="cui" defaultValue={initial.cui ?? ""} />
          </div>
          <div>
            <Label className="mb-1 text-purple-800">Salariati</Label>
            <Input type="number" name="salariati" defaultValue={initial.salariati} min={0} />
          </div>

          <div className="sm:col-span-2 grid grid-cols-2 gap-2 mt-1">
            <label className="inline-flex items-center gap-2 text-purple-800"><Checkbox name="registruUC" defaultChecked={initial.registruUC} /><span>Registru UC</span></label>
            <label className="inline-flex items-center gap-2 text-purple-800"><Checkbox name="casaDeMarcat" defaultChecked={initial.casaDeMarcat} /><span>Casa de marcat</span></label>
          </div>

          <div className="sm:col-span-2 flex justify-between mt-2">
            <Button type="button" variant="destructive" size="sm" disabled={busy} onClick={async () => { await onDelete(initial.id!); }}>Sterge</Button>
            <Button type="submit" disabled={busy} size="sm">Salveaza</Button>
          </div>
        </form>
      </td>
    </tr>
  );
}

type Props = {
  rows: PunctDeLucruValues[];
  onSubmitRow: (fd: FormData) => Promise<void>;
  onCreateRow: (fd: FormData) => Promise<void>;
  onDeleteRow: (id: number) => Promise<void>;
};

export default function ClientPunctTable({ rows, onSubmitRow, onCreateRow, onDeleteRow }: Props) {
  return (
    <div className="w-full overflow-x-auto">
      <table className="w-full text-sm border rounded-md">
        <thead>
          <tr className="bg-muted/50">
            <th className="text-left p-2">Puncte de lucru</th>
          </tr>
        </thead>
        <tbody>
          {/* Add new blank row */}
          <tr className="border-b align-top">
            <td className="p-2">
              <AddNewRow onCreate={onCreateRow} />
            </td>
          </tr>
          {rows.length === 0 ? (
            <tr>
              <td className="p-2 text-muted-foreground">Nu exista puncte de lucru.</td>
            </tr>
          ) : (
            rows.map((r) => (
              <PunctRow key={r.id} initial={r} onSubmit={onSubmitRow} onDelete={onDeleteRow} />
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

function AddNewRow({ onCreate }: { onCreate: (fd: FormData) => Promise<void> }) {
  const [busy, setBusy] = useState(false);
  const [administratie, setAdministratie] = useState<$Enums.Administratie>("SECTOR_1");

  async function handleAction(fd: FormData) {
    setBusy(true);
    try {
      await onCreate(fd);
      // Stay inline, no refresh
    } finally {
      setBusy(false);
    }
  }

  return (
    <form action={handleAction} className="grid grid-cols-1 sm:grid-cols-2 gap-2">
      <div>
        <Label className="mb-1 text-purple-800">Denumire</Label>
        <Input name="denumire" required />
      </div>
      <div>
        <Label className="mb-1 text-purple-800">Administratie</Label>
        <input type="hidden" name="administratie" value={administratie} />
        <Select value={administratie} onValueChange={(v: $Enums.Administratie) => setAdministratie(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {["SECTOR_1","SECTOR_2","SECTOR_3","SECTOR_4","SECTOR_5","SECTOR_6","ILFOV","BUFTEA","BRAGADIRU","VRANCEA"].map((v) => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-1 text-purple-800">De la</Label>
        <Input type="date" name="deLa" required />
      </div>
      <div>
        <Label className="mb-1 text-purple-800">Pana la</Label>
        <Input type="date" name="panaLa" />
      </div>
      <div>
        <Label className="mb-1 text-purple-800">CUI</Label>
        <Input name="cui" />
      </div>
      <div>
        <Label className="mb-1 text-purple-800">Salariati</Label>
        <Input type="number" name="salariati" defaultValue={0} min={0} />
      </div>
      <div className="sm:col-span-2 grid grid-cols-2 gap-2 mt-1">
        <label className="inline-flex items-center gap-2 text-purple-800"><Checkbox name="registruUC" /><span>Registru UC</span></label>
        <label className="inline-flex items-center gap-2 text-purple-800"><Checkbox name="casaDeMarcat" /><span>Casa de marcat</span></label>
      </div>
      <div className="sm:col-span-2 flex justify-end mt-2">
        <Button type="submit" disabled={busy} size="sm">Adauga</Button>
      </div>
    </form>
  );
}
