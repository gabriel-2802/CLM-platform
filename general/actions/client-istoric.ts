"use server"

import { Prisma, PrismaClient } from "@/lib/generated/prisma-client";
import type { $Enums } from "@/lib/generated/prisma-client";
import { unstable_noStore as noStore, revalidatePath } from "next/cache";

const prisma = new PrismaClient();

export type IstoricValues = {
  id?: number;
  anul: number;
  cifraAfaceri: number;
  inventar: boolean;
  bilantSemIun: $Enums.DaNuNuECazul;
  bilantAnual: $Enums.DaNuNuECazul;
};

function bool(v: FormDataEntryValue | null): boolean {
  return v === "on" || v === "true" || v === "1";
}

function int(v: FormDataEntryValue | null, d = 0): number {
  if (typeof v !== "string") return d;
  const n = parseInt(v, 10);
  return Number.isFinite(n) ? n : d;
}

function num(v: FormDataEntryValue | null, d = 0): number {
  if (typeof v !== "string") return d;
  const n = parseFloat(v);
  return Number.isFinite(n) ? n : d;
}

export async function getClientIstoric(clientId: number): Promise<IstoricValues | null> {
  noStore();
  const i = await prisma.istoric.findFirst({ where: { clientId }, orderBy: { anul: "desc" } });
  if (!i) return null;
  return {
    id: i.id,
    anul: i.anul,
    cifraAfaceri: i.cifraAfaceri,
    inventar: i.inventar,
    bilantSemIun: i.bilantSemIun,
    bilantAnual: i.bilantAnual,
  };
}

export async function upsertClientIstoric(clientId: number, formData: FormData): Promise<IstoricValues> {
  "use server";
  const idRaw = formData.get("id");
  const id = typeof idRaw === "string" && idRaw.trim() !== "" ? Number(idRaw) : undefined;
  const anul = int(formData.get("anul"));
  const cifraAfaceri = num(formData.get("cifraAfaceri"));
  const inventar = bool(formData.get("inventar"));
  const bilantSemIun = (formData.get("bilantSemIun") as $Enums.DaNuNuECazul | null) ?? "NU_E_CAZUL";
  const bilantAnual = (formData.get("bilantAnual") as $Enums.DaNuNuECazul | null) ?? "NU_E_CAZUL";

  if (!anul) throw new Error("Anul este obligatoriu");

  // If id is provided, prefer updating that record (handles year change correctly)
  let saved;
  try {
    if (typeof id === "number" && Number.isFinite(id)) {
      const existingById = await prisma.istoric.findUnique({ where: { id } });
      if (!existingById) {
        // fall back to composite lookup
        const existing = await prisma.istoric.findFirst({ where: { clientId, anul } });
        saved = existing
          ? await prisma.istoric.update({ where: { id: existing.id }, data: { cifraAfaceri, inventar, bilantSemIun, bilantAnual, anul } })
          : await prisma.istoric.create({ data: { clientId, anul, cifraAfaceri, inventar, bilantSemIun, bilantAnual } });
      } else {
        if (existingById.clientId !== clientId) throw new Error("Nu aveti permisiune pentru acest record");
        saved = await prisma.istoric.update({ where: { id }, data: { anul, cifraAfaceri, inventar, bilantSemIun, bilantAnual } });
      }
    } else {
      // Safer upsert: check existence then update or create
      const existing = await prisma.istoric.findFirst({ where: { clientId, anul } });
      saved = existing
        ? await prisma.istoric.update({ where: { id: existing.id }, data: { cifraAfaceri, inventar, bilantSemIun, bilantAnual } })
        : await prisma.istoric.create({ data: { clientId, anul, cifraAfaceri, inventar, bilantSemIun, bilantAnual } });
    }
  } catch (e: unknown) {
    if (e instanceof Prisma.PrismaClientKnownRequestError && e.code === "P2002") {
      // Check if this is a (clientId, anul) conflict
      const conflict = await prisma.istoric.findFirst({ where: { clientId, anul } });
      if (conflict) {
        const years = await prisma.istoric.findMany({ where: { clientId }, select: { anul: true }, orderBy: { anul: "desc" } });
        const list = years.map((y) => y.anul).join(", ");
        throw new Error(`Exista deja un istoric pentru acest an (${anul}). Ani existenti: ${list}`);
      }
      const targetMeta = e.meta?.target as string | string[] | undefined;
      const t = Array.isArray(targetMeta) ? targetMeta : typeof targetMeta === "string" ? [targetMeta] : [];
      const targetStr = t.join(", ") || "constrangere necunoscuta";
      throw new Error(`Eroare de unicitate pe ${targetStr}`);
    }
    throw e;
  }

  // Ensure the edit page gets fresh data on any server re-render/remount
  revalidatePath(`/clients/edit/${clientId}`);

  return {
    id: saved.id,
    anul: saved.anul,
    cifraAfaceri: saved.cifraAfaceri,
    inventar: saved.inventar,
    bilantSemIun: saved.bilantSemIun,
    bilantAnual: saved.bilantAnual,
  };
}

export async function getClientIstoricList(clientId: number): Promise<IstoricValues[]> {
  noStore();
  const list = await prisma.istoric.findMany({ where: { clientId }, orderBy: { anul: "desc" } });
  return list.map((i) => ({
    id: i.id,
    anul: i.anul,
    cifraAfaceri: i.cifraAfaceri,
    inventar: i.inventar,
    bilantSemIun: i.bilantSemIun,
    bilantAnual: i.bilantAnual,
  }));
}

export async function deleteClientIstoric(clientId: number, id: number): Promise<{ id: number }> {
  "use server";
  const existing = await prisma.istoric.findUnique({ where: { id } });
  if (!existing) return { id };
  if (existing.clientId !== clientId) throw new Error("Nu aveti permisiune pentru acest record");
  await prisma.istoric.delete({ where: { id } });
  revalidatePath(`/clients/edit/${clientId}`);
  return { id };
}
