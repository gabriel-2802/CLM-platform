"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { $Enums } from "@/lib/generated/prisma-client";

export type DetaliiFormValues = {
  registruUC: boolean;
  registruEvFiscala: $Enums.DaNuNuECazul;
  ofSpalareBani: boolean;
  regulamentOrdineInterioara: boolean;
  manualPoliticiContabile: boolean;
  adresaRevisal: boolean;
  parolaITM?: string;
  depunereDeclaratiiOnline: boolean;
  accesDosarFiscal: $Enums.DaNuNuECazul;
};

type Props = {
  initial?: Partial<DetaliiFormValues>;
  onSubmit: (formData: FormData) => Promise<DetaliiFormValues | void>;
  submitLabel?: string;
};

export default function ClientDetaliiForm({ initial, onSubmit, submitLabel = "Save" }: Props) {
  const [busy, setBusy] = useState(false);
  const [parolaITM, setParolaITM] = useState<string>(initial?.parolaITM ?? "");

  async function handleAction(data: FormData) {
    setBusy(true);
    try {
      const saved = await onSubmit(data);
      if (saved) {
        setRegistruEvFiscala(saved.registruEvFiscala);
        setAccesDosarFiscal(saved.accesDosarFiscal);
        setRegistruUC(saved.registruUC);
        setOfSpalareBani(saved.ofSpalareBani);
        setRegulamentOrdineInterioara(saved.regulamentOrdineInterioara);
        setManualPoliticiContabile(saved.manualPoliticiContabile);
        setAdresaRevisal(saved.adresaRevisal);
        setDepunereDeclaratiiOnline(saved.depunereDeclaratiiOnline);
        setParolaITM(saved.parolaITM ?? "");
      }
      toast.success("Detalii salvate");
      // Stay on the page without refreshing so the user-entered values remain visible
    } finally {
      setBusy(false);
    }
  }

  const [registruEvFiscala, setRegistruEvFiscala] = useState<$Enums.DaNuNuECazul>(initial?.registruEvFiscala ?? "NU_E_CAZUL");
  const [accesDosarFiscal, setAccesDosarFiscal] = useState<$Enums.DaNuNuECazul>(initial?.accesDosarFiscal ?? "NU_E_CAZUL");
  // Controlled booleans
  const [registruUC, setRegistruUC] = useState<boolean>(initial?.registruUC ?? false);
  const [ofSpalareBani, setOfSpalareBani] = useState<boolean>(initial?.ofSpalareBani ?? false);
  const [regulamentOrdineInterioara, setRegulamentOrdineInterioara] = useState<boolean>(initial?.regulamentOrdineInterioara ?? false);
  const [manualPoliticiContabile, setManualPoliticiContabile] = useState<boolean>(initial?.manualPoliticiContabile ?? false);
  const [adresaRevisal, setAdresaRevisal] = useState<boolean>(initial?.adresaRevisal ?? false);
  const [depunereDeclaratiiOnline, setDepunereDeclaratiiOnline] = useState<boolean>(initial?.depunereDeclaratiiOnline ?? false);

  // Note: we intentionally avoid resyncing from `initial` after save to prevent values
  // from snapping back due to a route re-render. The form updates from the saved payload.
  // However, if the server re-renders and provides fresh `initial`, we resync here.
  useEffect(() => {
    if (!initial) return;
    setRegistruEvFiscala((initial.registruEvFiscala as $Enums.DaNuNuECazul) ?? "NU_E_CAZUL");
    setAccesDosarFiscal((initial.accesDosarFiscal as $Enums.DaNuNuECazul) ?? "NU_E_CAZUL");
    setRegistruUC(!!initial.registruUC);
    setOfSpalareBani(!!initial.ofSpalareBani);
    setRegulamentOrdineInterioara(!!initial.regulamentOrdineInterioara);
    setManualPoliticiContabile(!!initial.manualPoliticiContabile);
    setAdresaRevisal(!!initial.adresaRevisal);
    setDepunereDeclaratiiOnline(!!initial.depunereDeclaratiiOnline);
    setParolaITM(initial.parolaITM ?? "");
  }, [initial]);

  return (
    <form action={handleAction} className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Top controls: selects + text input */}
      <div>
        <Label className="mb-2 text-purple-800">Registru ev. fiscala</Label>
        <input type="hidden" name="registruEvFiscala" value={registruEvFiscala} />
        <Select value={registruEvFiscala} onValueChange={(v: $Enums.DaNuNuECazul) => setRegistruEvFiscala(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {(["DA","NU","NU_E_CAZUL"] as $Enums.DaNuNuECazul[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-purple-800">Acces dosar fiscal</Label>
        <input type="hidden" name="accesDosarFiscal" value={accesDosarFiscal} />
        <Select value={accesDosarFiscal} onValueChange={(v: $Enums.DaNuNuECazul) => setAccesDosarFiscal(v)}>
          <SelectTrigger><SelectValue placeholder="Alege" /></SelectTrigger>
          <SelectContent>
            {(["DA","NU","NU_E_CAZUL"] as $Enums.DaNuNuECazul[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-purple-800">Parola ITM</Label>
        <Input name="parolaITM" value={parolaITM} onChange={(e) => setParolaITM(e.target.value)} />
      </div>

      {/* Checkbox group at the very bottom */}
      <div className="md:col-span-2 mt-2">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {/* Hidden inputs so booleans are submitted with form */}
          <input type="hidden" name="registruUC" value={registruUC ? "true" : "false"} />
          <input type="hidden" name="ofSpalareBani" value={ofSpalareBani ? "true" : "false"} />
          <input type="hidden" name="regulamentOrdineInterioara" value={regulamentOrdineInterioara ? "true" : "false"} />
          <input type="hidden" name="manualPoliticiContabile" value={manualPoliticiContabile ? "true" : "false"} />
          <input type="hidden" name="adresaRevisal" value={adresaRevisal ? "true" : "false"} />
          <input type="hidden" name="depunereDeclaratiiOnline" value={depunereDeclaratiiOnline ? "true" : "false"} />

          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={registruUC} onCheckedChange={(v) => setRegistruUC(!!v)} />
            <span>Registru UC</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={ofSpalareBani} onCheckedChange={(v) => setOfSpalareBani(!!v)} />
            <span>Of spalare bani</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={regulamentOrdineInterioara} onCheckedChange={(v) => setRegulamentOrdineInterioara(!!v)} />
            <span>Regulament OI</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={manualPoliticiContabile} onCheckedChange={(v) => setManualPoliticiContabile(!!v)} />
            <span>Manual politici contabile</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={adresaRevisal} onCheckedChange={(v) => setAdresaRevisal(!!v)} />
            <span>Adresa Revisal</span>
          </label>
          <label className="inline-flex items-center gap-2 text-purple-800">
            <Checkbox checked={depunereDeclaratiiOnline} onCheckedChange={(v) => setDepunereDeclaratiiOnline(!!v)} />
            <span>Depunere declaratii online</span>
          </label>
        </div>
      </div>

      <div className="md:col-span-2 flex justify-end gap-2 mt-4">
        <Button type="submit" disabled={busy}>{submitLabel}</Button>
      </div>
    </form>
  );
}
