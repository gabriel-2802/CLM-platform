"use server"

import { PrismaClient } from "@/lib/generated/prisma-client"
import type { $Enums, Prisma } from "@/lib/generated/prisma-client"
import { unstable_noStore as noStore, revalidatePath } from "next/cache"

export type Row = {
  id: number
  name: string
  tip: string
  deLa?: string
  panaLa?: string
  users?: string[]
  tarifConta?: number
  tarifBilant?: number
  contractGen?: string
  contractSemnat?: string
  probleme?: string[]
}

const prisma = new PrismaClient()

const toISODate = (d?: Date | null) => (d ? new Date(d).toISOString().slice(0, 10) : undefined)

export async function getClientRows(): Promise<Row[]> {
  noStore();
  const clients = await prisma.client.findMany({
    orderBy: { denumire: "asc" },
    include: {
      detalii: true,
      puncteDeLucru: { select: { deLa: true, panaLa: true } },
      users: { select: { user: { select: { id: true, name: true, email: true } } } },
    },
  })

  return clients.map((c) => {
    const earliestDeLa = c.puncteDeLucru
      .map((p) => p.deLa)
      .filter(Boolean)
      .sort((a, b) => (a && b ? a.getTime() - b.getTime() : 0))[0]

    const latestPanaLa = c.puncteDeLucru
      .map((p) => p.panaLa)
      .filter((d): d is Date => Boolean(d))
      .sort((a, b) => a.getTime() - b.getTime())
      .at(-1)

    const problems: string[] = []
    if (c.detalii) {
      if (!c.detalii.manualPoliticiContabile) problems.push("Manual pol. contabile")
      if (!c.detalii.regulamentOrdineInterioara) problems.push("Regulament OI")
      if (!c.detalii.ofSpalareBani) problems.push("Of spalare bani")
      if (!c.detalii.registruUC) problems.push("Registru UC")
    }

    return {
      id: c.id,
      name: c.denumire,
      tip: c.tip,
      deLa: toISODate(earliestDeLa ?? c.dataVerificarii),
      panaLa: toISODate(latestPanaLa ?? null),
      users: c.users
        ? c.users
            .map((uc) => uc.user.name || uc.user.email)
            .filter((s): s is string => !!s)
            .sort((a, b) => a.localeCompare(b))
        : undefined,
      tarifConta: undefined,
      tarifBilant: undefined,
      contractGen: undefined,
      contractSemnat: undefined,
      probleme: problems.length ? problems : undefined,
    }
  })
}

export type ClientDetails = {
  id: number
  name: string
  tip: $Enums.Tip
  deLa?: string
  panaLa?: string
  probleme?: string[]
  // Editable fields for form
  denumire: string
  cui: string
  activa: boolean
  dataVerificarii?: string
  adresa?: string
  administratie: $Enums.Administratie
  impozit: $Enums.Impozit | null
  platitorTVA: $Enums.DaLunarTrim
  tvaLaIncasare: boolean | null
  areCodTVAUE: boolean | null
  codTVAUE?: string
  operatiuneUE: boolean | null
  dividende: boolean | null
  salariati: $Enums.DaLunarTrim | null
  casaDeMarcat: boolean | null
  dataExpSediuSocial?: string
  dataExpMandatAdmin?: string
  dataCertificatFiscal?: string
  dataFisaPlatitor?: string
  dataVectFiscal?: string
  detalii?: {
    manualPoliticiContabile: boolean
    regulamentOrdineInterioara: boolean
    ofSpalareBani: boolean
    registruUC: boolean
  }
}

export async function getClient(id: number): Promise<ClientDetails | null> {
  noStore();
  const c = await prisma.client.findUnique({
    where: { id },
    include: {
      detalii: true,
      puncteDeLucru: { select: { deLa: true, panaLa: true } },
    },
  })
  if (!c) return null

  const earliestDeLa = c.puncteDeLucru
    .map((p) => p.deLa)
    .filter(Boolean)
    .sort((a, b) => (a && b ? a.getTime() - b.getTime() : 0))[0]

  const latestPanaLa = c.puncteDeLucru
    .map((p) => p.panaLa)
    .filter((d): d is Date => Boolean(d))
    .sort((a, b) => a.getTime() - b.getTime())
    .at(-1)

  const problems: string[] = []
  if (c.detalii) {
    if (!c.detalii.manualPoliticiContabile) problems.push("Manual pol. contabile")
    if (!c.detalii.regulamentOrdineInterioara) problems.push("Regulament OI")
    if (!c.detalii.ofSpalareBani) problems.push("Of spalare bani")
    if (!c.detalii.registruUC) problems.push("Registru UC")
  }

  return {
    id: c.id,
    name: c.denumire,
    tip: c.tip,
    deLa: toISODate(earliestDeLa ?? c.dataVerificarii),
    panaLa: toISODate(latestPanaLa ?? null),
    denumire: c.denumire,
    cui: c.cui,
    activa: c.activa,
    dataVerificarii: toISODate(c.dataVerificarii ?? null),
    adresa: c.adresa ?? undefined,
    administratie: c.administratie,
    impozit: c.impozit,
    platitorTVA: c.platitorTVA,
    tvaLaIncasare: c.tvaLaIncasare,
    areCodTVAUE: c.areCodTVAUE,
    codTVAUE: c.codTVAUE ?? undefined,
    operatiuneUE: c.operatiuneUE,
    dividende: c.dividende,
    salariati: c.salariati,
    casaDeMarcat: c.casaDeMarcat,
    dataExpSediuSocial: toISODate(c.dataExpSediuSocial ?? null),
    dataExpMandatAdmin: toISODate(c.dataExpMandatAdmin ?? null),
    dataCertificatFiscal: toISODate(c.dataCertificatFiscal ?? null),
    dataFisaPlatitor: toISODate(c.dataFisaPlatitor ?? null),
    dataVectFiscal: toISODate(c.dataVectFiscal ?? null),
    probleme: problems.length ? problems : undefined,
    detalii: c.detalii
      ? {
          manualPoliticiContabile: !!c.detalii.manualPoliticiContabile,
          regulamentOrdineInterioara: !!c.detalii.regulamentOrdineInterioara,
          ofSpalareBani: !!c.detalii.ofSpalareBani,
          registruUC: !!c.detalii.registruUC,
        }
      : undefined,
  }
}

// =============== Mutations ===============

function bool(v: FormDataEntryValue | null): boolean {
  return v === "on" || v === "true" || v === "1";
}

function str(v: FormDataEntryValue | null | undefined): string | undefined {
  return typeof v === "string" && v.trim() !== "" ? v : undefined;
}

function dateStr(v: FormDataEntryValue | null): Date | null {
  if (typeof v !== "string" || !v) return null;
  const d = new Date(v);
  return isNaN(d.getTime()) ? null : d;
}

export async function createClient(formData: FormData) {
  "use server";
  const denumire = formData.get("denumire") as string;
  const tip = formData.get("tip") as $Enums.Tip;
  const cui = formData.get("cui") as string;
  const activa = bool(formData.get("activa"));
  const dataVerificarii = dateStr(formData.get("dataVerificarii"));
  const adresa = str(formData.get("adresa"));
  const administratie = formData.get("administratie") as $Enums.Administratie;

  const impozit = formData.get("impozit") as $Enums.Impozit;
  const platitorTVA = formData.get("platitorTVA") as $Enums.DaLunarTrim;
  const tvaLaIncasare = bool(formData.get("tvaLaIncasare"));
  const areCodTVAUE = bool(formData.get("areCodTVAUE"));
  const codTVAUE = str(formData.get("codTVAUE"));
  const operatiuneUE = bool(formData.get("operatiuneUE"));
  const dividende = bool(formData.get("dividende"));
  const salariati = formData.get("salariati") as $Enums.DaLunarTrim;
  const casaDeMarcat = bool(formData.get("casaDeMarcat"));

  const dataExpSediuSocial = dateStr(formData.get("dataExpSediuSocial"));
  const dataExpMandatAdmin = dateStr(formData.get("dataExpMandatAdmin"));
  const dataCertificatFiscal = dateStr(formData.get("dataCertificatFiscal"));
  const dataFisaPlatitor = dateStr(formData.get("dataFisaPlatitor"));
  const dataVectFiscal = dateStr(formData.get("dataVectFiscal"));

  if (!denumire || !tip || !cui || !administratie || !impozit || !platitorTVA || !salariati) {
    throw new Error("Missing required fields");
  }

  const created = await prisma.client.create({
    data: {
      denumire,
      tip,
      cui,
      activa,
      dataVerificarii: dataVerificarii ?? undefined,
      adresa,
      administratie,
      impozit,
      platitorTVA,
      tvaLaIncasare,
      areCodTVAUE,
      codTVAUE,
      operatiuneUE,
      dividende,
      salariati,
      casaDeMarcat,
      dataExpSediuSocial: dataExpSediuSocial ?? undefined,
      dataExpMandatAdmin: dataExpMandatAdmin ?? undefined,
      dataCertificatFiscal: dataCertificatFiscal ?? undefined,
      dataFisaPlatitor: dataFisaPlatitor ?? undefined,
      dataVectFiscal: dataVectFiscal ?? undefined,
    },
    select: { id: true },
  });

  return { id: created.id };
}

export async function updateClient(id: number, formData: FormData) {
  "use server";
  const denumire = str(formData.get("denumire"));
  const tip = str(formData.get("tip")) as $Enums.Tip | undefined;
  const cui = str(formData.get("cui"));
  const activaMaybe = formData.get("activa");
  const dataVerificarii = dateStr(formData.get("dataVerificarii"));
  const adresa = str(formData.get("adresa"));
  const administratie = str(formData.get("administratie")) as $Enums.Administratie | undefined;

  const impozit = str(formData.get("impozit")) as $Enums.Impozit | undefined;
  const platitorTVA = str(formData.get("platitorTVA")) as $Enums.DaLunarTrim | undefined;
  const tvaLaIncasareMaybe = formData.get("tvaLaIncasare");
  const areCodTVAUEMaybe = formData.get("areCodTVAUE");
  const codTVAUE = str(formData.get("codTVAUE"));
  const operatiuneUEMaybe = formData.get("operatiuneUE");
  const dividendeMaybe = formData.get("dividende");
  const salariati = str(formData.get("salariati")) as $Enums.DaLunarTrim | undefined;
  const casaDeMarcatMaybe = formData.get("casaDeMarcat");

  const dataExpSediuSocial = dateStr(formData.get("dataExpSediuSocial"));
  const dataExpMandatAdmin = dateStr(formData.get("dataExpMandatAdmin"));
  const dataCertificatFiscal = dateStr(formData.get("dataCertificatFiscal"));
  const dataFisaPlatitor = dateStr(formData.get("dataFisaPlatitor"));
  const dataVectFiscal = dateStr(formData.get("dataVectFiscal"));

  const data: Partial<Prisma.ClientUpdateInput> = {};
  if (denumire !== undefined) data.denumire = denumire;
  if (tip !== undefined) data.tip = tip;
  if (cui !== undefined) data.cui = cui;
  if (activaMaybe !== null) data.activa = bool(activaMaybe);
  if (dataVerificarii !== null) data.dataVerificarii = dataVerificarii ?? undefined;
  if (adresa !== undefined) data.adresa = adresa;
  if (administratie !== undefined) data.administratie = administratie;
  if (impozit !== undefined) data.impozit = impozit;
  if (platitorTVA !== undefined) data.platitorTVA = platitorTVA;
  if (tvaLaIncasareMaybe !== null) data.tvaLaIncasare = bool(tvaLaIncasareMaybe);
  if (areCodTVAUEMaybe !== null) data.areCodTVAUE = bool(areCodTVAUEMaybe);
  if (codTVAUE !== undefined) data.codTVAUE = codTVAUE;
  if (operatiuneUEMaybe !== null) data.operatiuneUE = bool(operatiuneUEMaybe);
  if (dividendeMaybe !== null) data.dividende = bool(dividendeMaybe);
  if (salariati !== undefined) data.salariati = salariati;
  if (casaDeMarcatMaybe !== null) data.casaDeMarcat = bool(casaDeMarcatMaybe);
  if (dataExpSediuSocial !== null) data.dataExpSediuSocial = dataExpSediuSocial ?? undefined;
  if (dataExpMandatAdmin !== null) data.dataExpMandatAdmin = dataExpMandatAdmin ?? undefined;
  if (dataCertificatFiscal !== null) data.dataCertificatFiscal = dataCertificatFiscal ?? undefined;
  if (dataFisaPlatitor !== null) data.dataFisaPlatitor = dataFisaPlatitor ?? undefined;
  if (dataVectFiscal !== null) data.dataVectFiscal = dataVectFiscal ?? undefined;

  const updated = await prisma.client.update({
    where: { id },
    data,
    select: {
      id: true,
      denumire: true,
      tip: true,
      cui: true,
      activa: true,
      dataVerificarii: true,
      adresa: true,
      administratie: true,
      impozit: true,
      platitorTVA: true,
      tvaLaIncasare: true,
      areCodTVAUE: true,
      codTVAUE: true,
      operatiuneUE: true,
      dividende: true,
      salariati: true,
      casaDeMarcat: true,
      dataExpSediuSocial: true,
      dataExpMandatAdmin: true,
      dataCertificatFiscal: true,
      dataFisaPlatitor: true,
      dataVectFiscal: true,
    },
  });

  // Ensure fresh data if the page remounts after this action
  revalidatePath(`/clients/edit/${id}`)

  return {
    id: updated.id,
    denumire: updated.denumire,
    tip: updated.tip,
    cui: updated.cui,
    activa: updated.activa,
    dataVerificarii: toISODate(updated.dataVerificarii ?? null),
    adresa: updated.adresa ?? undefined,
    administratie: updated.administratie,
    impozit: updated.impozit,
    platitorTVA: updated.platitorTVA,
    tvaLaIncasare: updated.tvaLaIncasare,
    areCodTVAUE: updated.areCodTVAUE,
    codTVAUE: updated.codTVAUE ?? undefined,
    operatiuneUE: updated.operatiuneUE,
    dividende: updated.dividende,
    salariati: updated.salariati,
    casaDeMarcat: updated.casaDeMarcat,
    dataExpSediuSocial: toISODate(updated.dataExpSediuSocial ?? null),
    dataExpMandatAdmin: toISODate(updated.dataExpMandatAdmin ?? null),
    dataCertificatFiscal: toISODate(updated.dataCertificatFiscal ?? null),
    dataFisaPlatitor: toISODate(updated.dataFisaPlatitor ?? null),
    dataVectFiscal: toISODate(updated.dataVectFiscal ?? null),
  };
}
