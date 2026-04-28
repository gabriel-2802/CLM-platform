"use server"

import { PrismaClient } from "@/lib/generated/prisma-client";
import type { $Enums } from "@/lib/generated/prisma-client";
import { unstable_noStore as noStore, revalidatePath } from "next/cache";

const prisma = new PrismaClient();

export type PunctDeLucruValues = {
  id?: number;
  denumire: string;
  deLa: string; // YYYY-MM-DD
  panaLa?: string; // YYYY-MM-DD | undefined
  administratie: $Enums.Administratie;
  registruUC: boolean;
  salariati: number;
  cui?: string;
  casaDeMarcat: boolean;
};

function bool(v: FormDataEntryValue | null): boolean {
  return v === "on" || v === "true" || v === "1";
}

function str(v: FormDataEntryValue | null | undefined): string | undefined {
  return typeof v === "string" && v.trim() !== "" ? v : undefined;
}

function int(v: FormDataEntryValue | null, d = 0): number {
  if (typeof v !== "string") return d;
  const n = parseInt(v, 10);
  return Number.isFinite(n) ? n : d;
}

function toISODate(d: Date | string | null | undefined): string | undefined {
  if (!d) return undefined;
  const dt = typeof d === "string" ? new Date(d) : d;
  if (Number.isNaN(dt.getTime())) return undefined;
  return dt.toISOString().slice(0, 10);
}

export async function getClientPunctDeLucru(clientId: number): Promise<PunctDeLucruValues | null> {
  noStore();
  // Load the latest punct de lucru for this client (by id desc)
  const p = await prisma.punctDeLucru.findFirst({ where: { clientId }, orderBy: { id: "desc" } });
  if (!p) return null;
  return {
    id: p.id,
    denumire: p.denumire,
    deLa: toISODate(p.deLa)!,
    panaLa: toISODate(p.panaLa) ?? undefined,
    administratie: p.administratie,
    registruUC: p.registruUC,
    salariati: p.salariati,
    cui: p.cui ?? undefined,
    casaDeMarcat: p.casaDeMarcat,
  };
}

export async function getClientPuncteDeLucru(clientId: number): Promise<PunctDeLucruValues[]> {
  noStore();
  const list = await prisma.punctDeLucru.findMany({ where: { clientId }, orderBy: { id: "asc" } });
  return list.map((p) => ({
    id: p.id,
    denumire: p.denumire,
    deLa: toISODate(p.deLa)!,
    panaLa: toISODate(p.panaLa) ?? undefined,
    administratie: p.administratie,
    registruUC: p.registruUC,
    salariati: p.salariati,
    cui: p.cui ?? undefined,
    casaDeMarcat: p.casaDeMarcat,
  }));
}

export async function upsertClientPunctDeLucru(clientId: number, formData: FormData): Promise<PunctDeLucruValues> {
  "use server";
  const id = int(formData.get("id"), 0);
  const denumire = (formData.get("denumire") as string)?.trim() || "";
  const deLa = formData.get("deLa") as string | null;
  const panaLa = str(formData.get("panaLa"));
  const administratie = formData.get("administratie") as $Enums.Administratie | null;
  const registruUC = bool(formData.get("registruUC"));
  const salariati = int(formData.get("salariati"));
  const cui = str(formData.get("cui"));
  const casaDeMarcat = bool(formData.get("casaDeMarcat"));

  if (!denumire) throw new Error("Denumire este obligatorie");
  if (!deLa) throw new Error("Data 'deLa' este obligatorie");
  const adm = administratie ?? "SECTOR_1"; // default sensible value

  if (id > 0) {
    // Ensure it belongs to this client
    const existing = await prisma.punctDeLucru.findUnique({ where: { id } });
    if (existing && existing.clientId === clientId) {
        const updated = await prisma.punctDeLucru.update({
        where: { id },
        data: {
          denumire,
          deLa: new Date(deLa),
          panaLa: panaLa ? new Date(panaLa) : null,
          administratie: adm,
          registruUC,
          salariati,
          cui: cui ?? null,
          casaDeMarcat,
        },
      });
        revalidatePath(`/clients/edit/${clientId}`);
      return {
          id: updated.id,
          denumire: updated.denumire,
          deLa: toISODate(updated.deLa)!,
          panaLa: toISODate(updated.panaLa) ?? undefined,
          administratie: updated.administratie,
          registruUC: updated.registruUC,
          salariati: updated.salariati,
          cui: updated.cui ?? undefined,
          casaDeMarcat: updated.casaDeMarcat,
      };
    }
  }

  // If not updating, create a new one
  const created = await prisma.punctDeLucru.create({
    data: {
      clientId,
      denumire,
      deLa: new Date(deLa),
      panaLa: panaLa ? new Date(panaLa) : null,
      administratie: adm,
      registruUC,
      salariati,
      cui: cui ?? null,
      casaDeMarcat,
    },
  });
    revalidatePath(`/clients/edit/${clientId}`);
  return {
      id: created.id,
      denumire: created.denumire,
      deLa: toISODate(created.deLa)!,
      panaLa: toISODate(created.panaLa) ?? undefined,
      administratie: created.administratie,
      registruUC: created.registruUC,
      salariati: created.salariati,
      cui: created.cui ?? undefined,
      casaDeMarcat: created.casaDeMarcat,
  };
}

export async function deleteClientPunctDeLucru(clientId: number, id: number): Promise<{ id: number }> {
  "use server";
  const existing = await prisma.punctDeLucru.findUnique({ where: { id } });
  if (!existing) return { id };
  if (existing.clientId !== clientId) throw new Error("Nu aveti permisiune pentru acest record");
  await prisma.punctDeLucru.delete({ where: { id } });
    revalidatePath(`/clients/edit/${clientId}`);
  return { id };
}
