"use server"

import { PrismaClient } from "@/lib/generated/prisma-client"
import type { $Enums } from "@/lib/generated/prisma-client"
import { unstable_noStore as noStore, revalidatePath } from "next/cache"

const prisma = new PrismaClient()

export type DetaliiValues = {
  registruUC: boolean
  registruEvFiscala: $Enums.DaNuNuECazul
  ofSpalareBani: boolean
  regulamentOrdineInterioara: boolean
  manualPoliticiContabile: boolean
  adresaRevisal: boolean
  parolaITM?: string
  depunereDeclaratiiOnline: boolean
  accesDosarFiscal: $Enums.DaNuNuECazul
}

function bool(v: FormDataEntryValue | null): boolean {
  return v === "on" || v === "true" || v === "1";
}

function str(v: FormDataEntryValue | null | undefined): string | undefined {
  return typeof v === "string" && v.trim() !== "" ? v : undefined;
}

export async function getClientDetalii(clientId: number): Promise<DetaliiValues | null> {
  // Ensure fresh data on every render (avoid RSC cache returning stale values after mutations)
  noStore();
  const d = await prisma.detalii.findUnique({ where: { clientId } });
  if (!d) return null;
  return {
    registruUC: d.registruUC,
    registruEvFiscala: d.registruEvFiscala,
    ofSpalareBani: d.ofSpalareBani,
    regulamentOrdineInterioara: d.regulamentOrdineInterioara,
    manualPoliticiContabile: d.manualPoliticiContabile,
    adresaRevisal: d.adresaRevisal,
    parolaITM: d.parolaITM ?? undefined,
    depunereDeclaratiiOnline: d.depunereDeclaratiiOnline,
    accesDosarFiscal: d.accesDosarFiscal,
  }
}

export async function upsertClientDetalii(clientId: number, formData: FormData): Promise<DetaliiValues> {
  "use server";
  const registruUC = bool(formData.get("registruUC"));
  const registruEvFiscala = formData.get("registruEvFiscala") as $Enums.DaNuNuECazul | null;
  const ofSpalareBani = bool(formData.get("ofSpalareBani"));
  const regulamentOrdineInterioara = bool(formData.get("regulamentOrdineInterioara"));
  const manualPoliticiContabile = bool(formData.get("manualPoliticiContabile"));
  const adresaRevisal = bool(formData.get("adresaRevisal"));
  const parolaITM = str(formData.get("parolaITM"));
  const depunereDeclaratiiOnline = bool(formData.get("depunereDeclaratiiOnline"));
  const accesDosarFiscal = formData.get("accesDosarFiscal") as $Enums.DaNuNuECazul | null;

  // Provide sane defaults for required enum fields if missing
  const regEv = registruEvFiscala ?? "NU_E_CAZUL";
  const acces = accesDosarFiscal ?? "NU_E_CAZUL";

  const saved = await prisma.detalii.upsert({
    where: { clientId },
    create: {
      clientId,
      registruUC,
      registruEvFiscala: regEv,
      ofSpalareBani,
      regulamentOrdineInterioara,
      manualPoliticiContabile,
      adresaRevisal,
      parolaITM,
      depunereDeclaratiiOnline,
      accesDosarFiscal: acces,
    },
    update: {
      registruUC,
      registruEvFiscala: regEv,
      ofSpalareBani,
      regulamentOrdineInterioara,
      manualPoliticiContabile,
      adresaRevisal,
      parolaITM,
      depunereDeclaratiiOnline,
      accesDosarFiscal: acces,
    },
  })

  // Ensure the edit page re-fetches fresh data if it remounts after this action
  revalidatePath(`/clients/edit/${clientId}`)

  return {
    registruUC: saved.registruUC,
    registruEvFiscala: saved.registruEvFiscala,
    ofSpalareBani: saved.ofSpalareBani,
    regulamentOrdineInterioara: saved.regulamentOrdineInterioara,
    manualPoliticiContabile: saved.manualPoliticiContabile,
    adresaRevisal: saved.adresaRevisal,
    parolaITM: saved.parolaITM ?? undefined,
    depunereDeclaratiiOnline: saved.depunereDeclaratiiOnline,
    accesDosarFiscal: saved.accesDosarFiscal,
  }
}
