"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { $Enums } from "@/lib/generated/prisma-client";

export type ClientFormValues = {
  id?: number;
  denumire: string;
  tip: $Enums.Tip;
  cui: string;
  activa: boolean;
  dataVerificarii?: string;
  adresa?: string;
  administratie: $Enums.Administratie;
  // Allow nullable impozit to align with server type; default will be applied if null
  impozit: $Enums.Impozit | null;
  platitorTVA: $Enums.DaLunarTrim;
  tvaLaIncasare: boolean | null;
  areCodTVAUE: boolean | null;
  codTVAUE?: string;
  operatiuneUE: boolean | null;
  dividende: boolean | null;
  salariati: $Enums.DaLunarTrim | null;
  casaDeMarcat: boolean | null;
  dataExpSediuSocial?: string;
  dataExpMandatAdmin?: string;
  dataCertificatFiscal?: string;
  dataFisaPlatitor?: string;
  dataVectFiscal?: string;
};

type Props = {
  initial?: Partial<ClientFormValues>;
  onSubmit: (formData: FormData) => Promise<
    | { id: number }
    | (Pick<
        ClientFormValues,
        | "denumire"
        | "tip"
        | "cui"
        | "activa"
        | "dataVerificarii"
        | "adresa"
        | "administratie"
        | "impozit"
        | "platitorTVA"
        | "tvaLaIncasare"
        | "areCodTVAUE"
        | "codTVAUE"
        | "operatiuneUE"
        | "dividende"
        | "salariati"
        | "casaDeMarcat"
        | "dataExpSediuSocial"
        | "dataExpMandatAdmin"
        | "dataCertificatFiscal"
        | "dataFisaPlatitor"
        | "dataVectFiscal"
      > & { id: number })
  >;
  submitLabel?: string;
};

export default function ClientForm({ initial, onSubmit, submitLabel = "Save" }: Props) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  // Controlled state for all fields to reflect saved values instantly
  const [denumire, setDenumire] = useState(initial?.denumire ?? "");
  const [cui, setCui] = useState(initial?.cui ?? "");
  const [dataVerificarii, setDataVerificarii] = useState(initial?.dataVerificarii ?? "");
  const [adresa, setAdresa] = useState(initial?.adresa ?? "");
  const [codTVAUE, setCodTVAUE] = useState(initial?.codTVAUE ?? "");
  const [dataExpSediuSocial, setDataExpSediuSocial] = useState(initial?.dataExpSediuSocial ?? "");
  const [dataExpMandatAdmin, setDataExpMandatAdmin] = useState(initial?.dataExpMandatAdmin ?? "");
  const [dataCertificatFiscal, setDataCertificatFiscal] = useState(initial?.dataCertificatFiscal ?? "");
  const [dataFisaPlatitor, setDataFisaPlatitor] = useState(initial?.dataFisaPlatitor ?? "");
  const [dataVectFiscal, setDataVectFiscal] = useState(initial?.dataVectFiscal ?? "");
  const [activa, setActiva] = useState<boolean>(initial?.activa ?? true);
  const [tvaLaIncasare, setTvaLaIncasare] = useState<boolean>(initial?.tvaLaIncasare ?? false);
  const [areCodTVAUE, setAreCodTVAUE] = useState<boolean>(initial?.areCodTVAUE ?? false);
  const [operatiuneUE, setOperatiuneUE] = useState<boolean>(initial?.operatiuneUE ?? false);
  const [dividende, setDividende] = useState<boolean>(initial?.dividende ?? false);
  const [casaDeMarcat, setCasaDeMarcat] = useState<boolean>(initial?.casaDeMarcat ?? false);

  async function handleAction(data: FormData) {
    setBusy(true);
    try {
      const res = await onSubmit(data);
      if (res && "id" in res) {
        // If server returned full updated client fields (on update), sync them locally
        if ("denumire" in res) {
          const u = res as ClientFormValues & { id: number };
          setDenumire(u.denumire ?? "");
          setCui(u.cui ?? "");
          setDataVerificarii(u.dataVerificarii ?? "");
          setAdresa(u.adresa ?? "");
          setCodTVAUE(u.codTVAUE ?? "");
          setDataExpSediuSocial(u.dataExpSediuSocial ?? "");
          setDataExpMandatAdmin(u.dataExpMandatAdmin ?? "");
          setDataCertificatFiscal(u.dataCertificatFiscal ?? "");
          setDataFisaPlatitor(u.dataFisaPlatitor ?? "");
          setDataVectFiscal(u.dataVectFiscal ?? "");
          setTip(u.tip ?? tip);
          setAdministratie(u.administratie ?? administratie);
          setImpozit(u.impozit ?? impozit);
          setPlatitorTVA(u.platitorTVA ?? platitorTVA);
          setSalariati(u.salariati ?? salariati);
          setActiva(u.activa ?? activa);
          setTvaLaIncasare(u.tvaLaIncasare ?? tvaLaIncasare);
          setAreCodTVAUE(u.areCodTVAUE ?? areCodTVAUE);
          setOperatiuneUE(u.operatiuneUE ?? operatiuneUE);
          setDividende(u.dividende ?? dividende);
          setCasaDeMarcat(u.casaDeMarcat ?? casaDeMarcat);
        } else {
          // On create, navigate to the new client's edit page
          router.push(`/clients/edit/${res.id}`);
        }
      }
      toast.success("Client salvat");
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : "Eroare la salvare";
      toast.error(msg);
    } finally {
      setBusy(false);
    }
  }

  // Local state mirrors select components to submit real values via hidden inputs
  const [tip, setTip] = useState<$Enums.Tip>(initial?.tip ?? "SRL");
  const [administratie, setAdministratie] = useState<$Enums.Administratie>(initial?.administratie ?? "SECTOR_5");
  const [impozit, setImpozit] = useState<$Enums.Impozit>((initial?.impozit ?? "MICRO_1") as $Enums.Impozit);
  const [platitorTVA, setPlatitorTVA] = useState<$Enums.DaLunarTrim>(initial?.platitorTVA ?? "NU");
  const [salariati, setSalariati] = useState<$Enums.DaLunarTrim>(initial?.salariati ?? "NU");

  // Sync when initial changes (navigation or server-provided fresh data)
  useEffect(() => {
    setDenumire(initial?.denumire ?? "");
    setCui(initial?.cui ?? "");
    setDataVerificarii(initial?.dataVerificarii ?? "");
    setAdresa(initial?.adresa ?? "");
    setCodTVAUE(initial?.codTVAUE ?? "");
    setDataExpSediuSocial(initial?.dataExpSediuSocial ?? "");
    setDataExpMandatAdmin(initial?.dataExpMandatAdmin ?? "");
    setDataCertificatFiscal(initial?.dataCertificatFiscal ?? "");
    setDataFisaPlatitor(initial?.dataFisaPlatitor ?? "");
    setDataVectFiscal(initial?.dataVectFiscal ?? "");
    setTip(initial?.tip ?? "SRL");
    setAdministratie(initial?.administratie ?? "SECTOR_5");
  setImpozit((initial?.impozit ?? "MICRO_1") as $Enums.Impozit);
    setPlatitorTVA(initial?.platitorTVA ?? "NU");
    setSalariati(initial?.salariati ?? "NU");
    setActiva(initial?.activa ?? true);
    setTvaLaIncasare(initial?.tvaLaIncasare ?? false);
    setAreCodTVAUE(initial?.areCodTVAUE ?? false);
    setOperatiuneUE(initial?.operatiuneUE ?? false);
    setDividende(initial?.dividende ?? false);
    setCasaDeMarcat(initial?.casaDeMarcat ?? false);
  }, [initial]);

  return (
    <form action={handleAction} className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <input type="hidden" name="tip" value={tip} />
      <input type="hidden" name="administratie" value={administratie} />
      <input type="hidden" name="impozit" value={impozit} />
      <input type="hidden" name="platitorTVA" value={platitorTVA} />
      <input type="hidden" name="salariati" value={salariati} />
      <div>
        <Label className="mb-2 text-indigo-800">Denumire</Label>
  <Input name="denumire" value={denumire} onChange={(e) => setDenumire(e.target.value)} required />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Tip</Label>
        <Select value={tip} onValueChange={(v: $Enums.Tip) => setTip(v)}>
          <SelectTrigger><SelectValue placeholder="Selecteaza tip" /></SelectTrigger>
          <SelectContent>
            {(["SRL","PFA","II","ASOC"] as $Enums.Tip[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">CUI</Label>
  <Input name="cui" value={cui} onChange={(e) => setCui(e.target.value)} required />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data verificarii</Label>
  <Input type="date" name="dataVerificarii" value={dataVerificarii} onChange={(e) => setDataVerificarii(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Adresa</Label>
  <Input name="adresa" value={adresa} onChange={(e) => setAdresa(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Administratie</Label>
        <Select value={administratie} onValueChange={(v: $Enums.Administratie) => setAdministratie(v)}>
          <SelectTrigger><SelectValue placeholder="Selecteaza administratie" /></SelectTrigger>
          <SelectContent>
            {(["SECTOR_1","SECTOR_2","SECTOR_3","SECTOR_4","SECTOR_5","SECTOR_6","ILFOV","BUFTEA","BRAGADIRU","VRANCEA"] as $Enums.Administratie[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Impozit</Label>
        <Select value={impozit} onValueChange={(v: $Enums.Impozit) => setImpozit(v)}>
          <SelectTrigger><SelectValue placeholder="Selecteaza impozitul" /></SelectTrigger>
          <SelectContent>
            {(["MICRO_1","MICRO_3","PROFIT"] as $Enums.Impozit[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Platitor TVA</Label>
        <Select value={platitorTVA} onValueChange={(v: $Enums.DaLunarTrim) => setPlatitorTVA(v)}>
          <SelectTrigger><SelectValue placeholder="Selecteaza" /></SelectTrigger>
          <SelectContent>
            {(["DA_LUNAR","DA_TRIM","NU"] as $Enums.DaLunarTrim[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Cod TVA UE</Label>
  <Input name="codTVAUE" value={codTVAUE} onChange={(e) => setCodTVAUE(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Salariati</Label>
        <Select value={salariati} onValueChange={(v: $Enums.DaLunarTrim) => setSalariati(v)}>
          <SelectTrigger><SelectValue placeholder="Selecteaza" /></SelectTrigger>
          <SelectContent>
            {(["DA_LUNAR","DA_TRIM","NU"] as $Enums.DaLunarTrim[]).map(v => (
              <SelectItem key={v} value={v}>{v}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      
      <div>
        <Label className="mb-2 text-indigo-800">Data exp. sediu social</Label>
  <Input type="date" name="dataExpSediuSocial" value={dataExpSediuSocial} onChange={(e) => setDataExpSediuSocial(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data exp. mandat admin</Label>
  <Input type="date" name="dataExpMandatAdmin" value={dataExpMandatAdmin} onChange={(e) => setDataExpMandatAdmin(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data certificat fiscal</Label>
  <Input type="date" name="dataCertificatFiscal" value={dataCertificatFiscal} onChange={(e) => setDataCertificatFiscal(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data fisa platitor</Label>
  <Input type="date" name="dataFisaPlatitor" value={dataFisaPlatitor} onChange={(e) => setDataFisaPlatitor(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data vect. fiscal</Label>
  <Input type="date" name="dataVectFiscal" value={dataVectFiscal} onChange={(e) => setDataVectFiscal(e.target.value)} />
      </div>

      {/* Checkbox group moved to the very bottom; use hidden inputs so unchecked values submit as false */}
      <div className="md:col-span-2 mt-2">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          <input type="hidden" name="activa" value={activa ? "true" : "false"} />
          <input type="hidden" name="tvaLaIncasare" value={tvaLaIncasare ? "true" : "false"} />
          <input type="hidden" name="areCodTVAUE" value={areCodTVAUE ? "true" : "false"} />
          <input type="hidden" name="operatiuneUE" value={operatiuneUE ? "true" : "false"} />
          <input type="hidden" name="dividende" value={dividende ? "true" : "false"} />
          <input type="hidden" name="casaDeMarcat" value={casaDeMarcat ? "true" : "false"} />

          <label className="inline-flex items-center gap-2"><Checkbox checked={activa} onCheckedChange={(v) => setActiva(!!v)} /><span>Activa</span></label>
          <label className="inline-flex items-center gap-2"><Checkbox checked={tvaLaIncasare} onCheckedChange={(v) => setTvaLaIncasare(!!v)} /><span>TVA la incasare</span></label>
          <label className="inline-flex items-center gap-2"><Checkbox checked={areCodTVAUE} onCheckedChange={(v) => setAreCodTVAUE(!!v)} /><span>Are cod TVA UE</span></label>
          <label className="inline-flex items-center gap-2"><Checkbox checked={operatiuneUE} onCheckedChange={(v) => setOperatiuneUE(!!v)} /><span>Operatiune UE</span></label>
          <label className="inline-flex items-center gap-2"><Checkbox checked={dividende} onCheckedChange={(v) => setDividende(!!v)} /><span>Dividende</span></label>
          <label className="inline-flex items-center gap-2"><Checkbox checked={casaDeMarcat} onCheckedChange={(v) => setCasaDeMarcat(!!v)} /><span>Casa de marcat</span></label>
        </div>
      </div>

      <div className="md:col-span-2 flex justify-end gap-2 mt-4">
        <Button type="submit" disabled={busy} className="bg-indigo-800 text-white hover:bg-indigo-700">{submitLabel}</Button>
      </div>
    </form>
  );
}
