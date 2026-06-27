"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useEnums } from "@/hooks/use-enums";

export type ClientFormValues = {
  id?: number;
  denumire: string;
  tip: string;
  cui: string;
  activa: boolean;
  dataVerificarii?: string;
  adresa?: string;
  administratie: string;
  impozit: string | null;
  platitorTVA: string;
  tvaLaIncasare: boolean | null;
  areCodTVAUE: boolean | null;
  codTVAUE?: string;
  operatiuneUE: boolean | null;
  dividende: boolean | null;
  salariati: string | null;
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
  const enums = useEnums();
  const [busy, setBusy] = useState(false);

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
  const [tip, setTip] = useState<string>(initial?.tip ?? "SRL");
  const [administratie, setAdministratie] = useState<string>(initial?.administratie ?? "SECTOR_5");
  const [impozit, setImpozit] = useState<string>(initial?.impozit ?? "MICRO_1");
  const [platitorTVA, setPlatitorTVA] = useState<string>(initial?.platitorTVA ?? "NU");
  const [salariati, setSalariati] = useState<string>(initial?.salariati ?? "NU");

  async function handleAction(data: FormData) {
    const missing: string[] = []
    if (!denumire.trim())       missing.push("Denumire")
    if (!cui.trim())            missing.push("CUI")
    if (!dataVerificarii)       missing.push("Data verificarii")
    if (!adresa.trim())         missing.push("Adresa")
    if (!tip)                   missing.push("Tip")
    if (!administratie)         missing.push("Administratie")
    if (!platitorTVA)           missing.push("Platitor TVA")
    if (missing.length > 0) {
      toast.error(`Câmpuri obligatorii necompletate: ${missing.join(", ")}`)
      return
    }
    setBusy(true);
    try {
      const res = await onSubmit(data);
      if (res && "id" in res) {
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
          router.push(`/clients/edit/${res.id}`);
        }
      }
      toast.success("Client salvat");
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : "Eroare la salvare");
    } finally {
      setBusy(false);
    }
  }

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
    setImpozit(initial?.impozit ?? "MICRO_1");
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
        <Label className="mb-2 text-indigo-800">Denumire <span className="text-red-500">*</span></Label>
        <Input name="denumire" value={denumire} onChange={(e) => setDenumire(e.target.value)} required />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Tip <span className="text-red-500">*</span></Label>
        <Select value={tip} onValueChange={setTip}>
          <SelectTrigger><SelectValue placeholder="Selecteaza tip" /></SelectTrigger>
          <SelectContent>
            {enums.companyTypes.map(v => <SelectItem key={v} value={v}>{v}</SelectItem>)}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">CUI <span className="text-red-500">*</span></Label>
        <Input name="cui" value={cui} onChange={(e) => setCui(e.target.value)} required />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Data verificarii <span className="text-red-500">*</span></Label>
        <Input type="date" name="dataVerificarii" value={dataVerificarii} onChange={(e) => setDataVerificarii(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Adresa <span className="text-red-500">*</span></Label>
        <Input name="adresa" value={adresa} onChange={(e) => setAdresa(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Administratie <span className="text-red-500">*</span></Label>
        <Select value={administratie} onValueChange={setAdministratie}>
          <SelectTrigger><SelectValue placeholder="Selecteaza administratie" /></SelectTrigger>
          <SelectContent className="max-h-60 overflow-y-auto">
            {enums.administrations.map(v => <SelectItem key={v} value={v}>{v}</SelectItem>)}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Impozit</Label>
        <Select value={impozit} onValueChange={setImpozit}>
          <SelectTrigger><SelectValue placeholder="Selecteaza impozitul" /></SelectTrigger>
          <SelectContent>
            {enums.taxTypes.map(v => <SelectItem key={v} value={v}>{v}</SelectItem>)}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Platitor TVA <span className="text-red-500">*</span></Label>
        <Select value={platitorTVA} onValueChange={setPlatitorTVA}>
          <SelectTrigger><SelectValue placeholder="Selecteaza" /></SelectTrigger>
          <SelectContent>
            {enums.taxFrequencies.map(v => <SelectItem key={v} value={v}>{v}</SelectItem>)}
          </SelectContent>
        </Select>
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Cod TVA UE</Label>
        <Input name="codTVAUE" value={codTVAUE} onChange={(e) => setCodTVAUE(e.target.value)} />
      </div>
      <div>
        <Label className="mb-2 text-indigo-800">Salariati</Label>
        <Select value={salariati} onValueChange={setSalariati}>
          <SelectTrigger><SelectValue placeholder="Selecteaza" /></SelectTrigger>
          <SelectContent>
            {enums.taxFrequencies.map(v => <SelectItem key={v} value={v}>{v}</SelectItem>)}
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
        <Button type="submit" disabled={busy}>{submitLabel}</Button>
      </div>
    </form>
  );
}
