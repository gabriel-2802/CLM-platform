"use server";

import { PrismaClient } from "@/lib/generated/prisma-client";
import type { Prisma } from "@/lib/generated/prisma-client";
import { getSession } from "@/lib/auth";
import { unstable_noStore as noStore } from "next/cache";

const prisma = new PrismaClient();

export type SituatieRow = {
  clientId: number;
  firma: string;
  tip: string;
  data: string; // dd/mm/yyyy (first day of month)
  dateTs: number; // timestamp for sorting
  user?: string; // first user found among tasks for that month
  avemActe: boolean;
  avemActeUser?: string;
  avemActeNotes?: string;
  avemActeHasTask?: boolean;
  avemActeTaskId?: number;
  introdusActe: boolean;
  introdusActeUser?: string;
  introdusActeNotes?: string;
  introdusActeHasTask?: boolean;
  introdusActeTaskId?: number;
  verificareLuna: boolean;
  verificareLunaUser?: string;
  verificareLunaNotes?: string;
  verificareLunaHasTask?: boolean;
  verificareLunaTaskId?: number;
  generatDeclaratii: boolean;
  generatDeclaratiiUser?: string;
  generatDeclaratiiNotes?: string;
  generatDeclaratiiHasTask?: boolean;
  generatDeclaratiiTaskId?: number;
  depusDeclaratii: boolean;
  depusDeclaratiiUser?: string;
  depusDeclaratiiNotes?: string;
  depusDeclaratiiHasTask?: boolean;
  depusDeclaratiiTaskId?: number;
  lunaPrintata: boolean;
  lunaPrintataUser?: string;
  lunaPrintataNotes?: string;
  lunaPrintataHasTask?: boolean;
  lunaPrintataTaskId?: number;
};

const toDMY = (d: Date) => {
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yy = d.getFullYear();
  return `${dd}/${mm}/${yy}`;
};

type StageKey = "avemActe" | "introdusActe" | "verificareLuna" | "generatDeclaratii" | "depusDeclaratii" | "lunaPrintata";
const STAGE_PATTERNS: Record<StageKey, string[]> = {
  avemActe: ["avem acte"],
  introdusActe: ["introdus acte", "introdusacte", "introducere acte"],
  verificareLuna: ["verificare luna", "verificat luna", "verificare acte", "verificat acte"],
  generatDeclaratii: ["generat declaratii", "generare declaratii", "generare declara"],
  depusDeclaratii: ["depus declaratii", "depunere declaratii", "depunere declara"],
  lunaPrintata: ["luna printata", "printat luna", "printare luna"],
};

function matchStage(title: string): StageKey[] {
  const t = title.toLowerCase();
  const hits: (keyof typeof STAGE_PATTERNS)[] = [];
  (Object.keys(STAGE_PATTERNS) as (keyof typeof STAGE_PATTERNS)[]).forEach((k) => {
    if (STAGE_PATTERNS[k].some((p) => t.includes(p))) hits.push(k);
  });
  return hits;
}

export async function getSituatieRows(): Promise<SituatieRow[]> {
  noStore();
  const session = await getSession();
  const role = (session?.user as unknown as { role?: string })?.role;
  const currentUserId = (session?.user as unknown as { id?: string })?.id;

  // Build task-level permission where (consistent with tasks list)
  const taskPermission: Prisma.TaskWhereInput =
    role === "ADMIN" || role === "MANAGER"
      ? {}
      : currentUserId
      ? { userId: Number(currentUserId) }
      : { id: -1 };

  // Fetch all tasks (permission-filtered), across all dates
  const tasks = await prisma.task.findMany({
    where: {
      ...taskPermission,
    },
    include: { client: { select: { id: true, denumire: true, tip: true } }, user: { select: { name: true, email: true } } },
    orderBy: [{ clientId: "asc" }, { date: "asc" }],
  });

  // Group by (clientId, year, month)
  type Key = string; // `${clientId}-${yyyy}-${mm}`
  const byClientMonth = new Map<Key, SituatieRow>();
  for (const t of tasks) {
    if (!t.date) continue;
    const d = new Date(t.date);
    const yyyy = d.getFullYear();
    const mm = d.getMonth(); // 0-based
    const monthStart = new Date(yyyy, mm, 1, 0, 0, 0, 0);
    const key = `${t.clientId}-${yyyy}-${mm}`;
    const existing = byClientMonth.get(key);
    const base: SituatieRow =
      existing ?? {
        clientId: t.clientId,
        firma: t.client?.denumire ?? `Client ${t.clientId}`,
        tip: String(t.client?.tip ?? ""),
        data: toDMY(monthStart),
        dateTs: monthStart.getTime(),
        user: undefined,
        avemActe: false,
        avemActeUser: undefined,
        avemActeNotes: undefined,
        avemActeHasTask: false,
        avemActeTaskId: undefined,
        introdusActe: false,
        introdusActeUser: undefined,
        introdusActeNotes: undefined,
        introdusActeHasTask: false,
        introdusActeTaskId: undefined,
        verificareLuna: false,
        verificareLunaUser: undefined,
        verificareLunaNotes: undefined,
        verificareLunaHasTask: false,
        verificareLunaTaskId: undefined,
        generatDeclaratii: false,
        generatDeclaratiiUser: undefined,
        generatDeclaratiiNotes: undefined,
        generatDeclaratiiHasTask: false,
        generatDeclaratiiTaskId: undefined,
        depusDeclaratii: false,
        depusDeclaratiiUser: undefined,
        depusDeclaratiiNotes: undefined,
        depusDeclaratiiHasTask: false,
        depusDeclaratiiTaskId: undefined,
        lunaPrintata: false,
        lunaPrintataUser: undefined,
        lunaPrintataNotes: undefined,
        lunaPrintataHasTask: false,
        lunaPrintataTaskId: undefined,
      };
    if (!existing) byClientMonth.set(key, base);

    if (t.title) {
      const label = t.user?.name || t.user?.email || undefined;
      if (!base.user && label) {
        base.user = label; // capture first user encountered for this month
      }
      const stages = matchStage(t.title);
      for (const s of stages) {
        if (s === "avemActe") {
          base.avemActeHasTask = true;
          if (base.avemActeTaskId === undefined) base.avemActeTaskId = t.id;
          if (t.done) base.avemActe = true;
          base.avemActeUser = label ?? base.avemActeUser;
          if (!base.avemActeNotes && t.notes) base.avemActeNotes = t.notes;
        } else if (s === "introdusActe") {
          base.introdusActeHasTask = true;
          if (base.introdusActeTaskId === undefined) base.introdusActeTaskId = t.id;
          if (t.done) base.introdusActe = true;
          base.introdusActeUser = label ?? base.introdusActeUser;
          if (!base.introdusActeNotes && t.notes) base.introdusActeNotes = t.notes;
        } else if (s === "verificareLuna") {
          base.verificareLunaHasTask = true;
          if (base.verificareLunaTaskId === undefined) base.verificareLunaTaskId = t.id;
          if (t.done) base.verificareLuna = true;
          base.verificareLunaUser = label ?? base.verificareLunaUser;
          if (!base.verificareLunaNotes && t.notes) base.verificareLunaNotes = t.notes;
        } else if (s === "generatDeclaratii") {
          base.generatDeclaratiiHasTask = true;
          if (base.generatDeclaratiiTaskId === undefined) base.generatDeclaratiiTaskId = t.id;
          if (t.done) base.generatDeclaratii = true;
          base.generatDeclaratiiUser = label ?? base.generatDeclaratiiUser;
          if (!base.generatDeclaratiiNotes && t.notes) base.generatDeclaratiiNotes = t.notes;
        } else if (s === "depusDeclaratii") {
          base.depusDeclaratiiHasTask = true;
          if (base.depusDeclaratiiTaskId === undefined) base.depusDeclaratiiTaskId = t.id;
          if (t.done) base.depusDeclaratii = true;
          base.depusDeclaratiiUser = label ?? base.depusDeclaratiiUser;
          if (!base.depusDeclaratiiNotes && t.notes) base.depusDeclaratiiNotes = t.notes;
        } else if (s === "lunaPrintata") {
          base.lunaPrintataHasTask = true;
          if (base.lunaPrintataTaskId === undefined) base.lunaPrintataTaskId = t.id;
          if (t.done) base.lunaPrintata = true;
          base.lunaPrintataUser = label ?? base.lunaPrintataUser;
          if (!base.lunaPrintataNotes && t.notes) base.lunaPrintataNotes = t.notes;
        }
      }
    }
  }

  // Sort by firma then date descending
  return Array.from(byClientMonth.values()).sort((a, b) => {
    const nf = a.firma.localeCompare(b.firma, "ro");
    if (nf !== 0) return nf;
    return b.dateTs - a.dateTs;
  });
}
