"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";
import { getSession } from "@/lib/auth";
import { getUsers } from "@/lib/user-service-client";

export type SituatieRow = {
  clientId: number;
  firma: string;
  tip: string;
  data: string;
  dateTs: number;
  user?: string;
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
  const dd = String(d.getDate()).padStart(2, "0")
  const mm = String(d.getMonth() + 1).padStart(2, "0")
  return `${dd}/${mm}/${d.getFullYear()}`
}

type StageKey = "avemActe" | "introdusActe" | "verificareLuna" | "generatDeclaratii" | "depusDeclaratii" | "lunaPrintata"

const STAGE_PATTERNS: Record<StageKey, string[]> = {
  avemActe: ["avem acte"],
  introdusActe: ["introdus acte", "introdusacte", "introducere acte"],
  verificareLuna: ["verificare luna", "verificat luna", "verificare acte", "verificat acte"],
  generatDeclaratii: ["generat declaratii", "generare declaratii", "generare declara"],
  depusDeclaratii: ["depus declaratii", "depunere declaratii", "depunere declara"],
  lunaPrintata: ["luna printata", "printat luna", "printare luna"],
}

function matchStage(title: string): StageKey[] {
  const t = title.toLowerCase()
  return (Object.keys(STAGE_PATTERNS) as StageKey[]).filter((k) =>
    STAGE_PATTERNS[k].some((p) => t.includes(p))
  )
}

export async function getSituatieRows(): Promise<SituatieRow[]> {
  const session = await getSession()
  const token = (session?.user as any)?.serviceToken ?? ""

  const [tasksRes, allUsers] = await Promise.all([
    clientServiceFetch("/api/tasks", { cache: "no-store" }),
    getUsers(token),
  ])

  if (!tasksRes.ok) return []

  const tasks: any[] = await tasksRes.json()
  const userMap = new Map(allUsers.map((u: any) => [u.id, u]))

  type Key = string
  const byClientMonth = new Map<Key, SituatieRow>()

  for (const t of tasks) {
    if (!t.date) continue
    const d = new Date(t.date)
    const yyyy = d.getFullYear()
    const mm = d.getMonth()
    const monthStart = new Date(yyyy, mm, 1, 0, 0, 0, 0)
    const key = `${t.clientId}-${yyyy}-${mm}`
    const existing = byClientMonth.get(key)

    const taskUser = userMap.get(t.userId) as any
    const label = taskUser ? (taskUser.name || taskUser.email) : undefined

    const base: SituatieRow = existing ?? {
      clientId: t.clientId,
      firma: t.clientName ?? `Client ${t.clientId}`,
      tip: t.clientType ?? "",
      data: toDMY(monthStart),
      dateTs: monthStart.getTime(),
      user: undefined,
      avemActe: false, avemActeUser: undefined, avemActeNotes: undefined, avemActeHasTask: false, avemActeTaskId: undefined,
      introdusActe: false, introdusActeUser: undefined, introdusActeNotes: undefined, introdusActeHasTask: false, introdusActeTaskId: undefined,
      verificareLuna: false, verificareLunaUser: undefined, verificareLunaNotes: undefined, verificareLunaHasTask: false, verificareLunaTaskId: undefined,
      generatDeclaratii: false, generatDeclaratiiUser: undefined, generatDeclaratiiNotes: undefined, generatDeclaratiiHasTask: false, generatDeclaratiiTaskId: undefined,
      depusDeclaratii: false, depusDeclaratiiUser: undefined, depusDeclaratiiNotes: undefined, depusDeclaratiiHasTask: false, depusDeclaratiiTaskId: undefined,
      lunaPrintata: false, lunaPrintataUser: undefined, lunaPrintataNotes: undefined, lunaPrintataHasTask: false, lunaPrintataTaskId: undefined,
    }
    if (!existing) byClientMonth.set(key, base)

    if (t.title) {
      if (!base.user && label) base.user = label
      for (const s of matchStage(t.title)) {
        if (s === "avemActe") {
          base.avemActeHasTask = true
          if (base.avemActeTaskId === undefined) base.avemActeTaskId = t.id
          if (t.done) base.avemActe = true
          base.avemActeUser = label ?? base.avemActeUser
          if (!base.avemActeNotes && t.notes) base.avemActeNotes = t.notes
        } else if (s === "introdusActe") {
          base.introdusActeHasTask = true
          if (base.introdusActeTaskId === undefined) base.introdusActeTaskId = t.id
          if (t.done) base.introdusActe = true
          base.introdusActeUser = label ?? base.introdusActeUser
          if (!base.introdusActeNotes && t.notes) base.introdusActeNotes = t.notes
        } else if (s === "verificareLuna") {
          base.verificareLunaHasTask = true
          if (base.verificareLunaTaskId === undefined) base.verificareLunaTaskId = t.id
          if (t.done) base.verificareLuna = true
          base.verificareLunaUser = label ?? base.verificareLunaUser
          if (!base.verificareLunaNotes && t.notes) base.verificareLunaNotes = t.notes
        } else if (s === "generatDeclaratii") {
          base.generatDeclaratiiHasTask = true
          if (base.generatDeclaratiiTaskId === undefined) base.generatDeclaratiiTaskId = t.id
          if (t.done) base.generatDeclaratii = true
          base.generatDeclaratiiUser = label ?? base.generatDeclaratiiUser
          if (!base.generatDeclaratiiNotes && t.notes) base.generatDeclaratiiNotes = t.notes
        } else if (s === "depusDeclaratii") {
          base.depusDeclaratiiHasTask = true
          if (base.depusDeclaratiiTaskId === undefined) base.depusDeclaratiiTaskId = t.id
          if (t.done) base.depusDeclaratii = true
          base.depusDeclaratiiUser = label ?? base.depusDeclaratiiUser
          if (!base.depusDeclaratiiNotes && t.notes) base.depusDeclaratiiNotes = t.notes
        } else if (s === "lunaPrintata") {
          base.lunaPrintataHasTask = true
          if (base.lunaPrintataTaskId === undefined) base.lunaPrintataTaskId = t.id
          if (t.done) base.lunaPrintata = true
          base.lunaPrintataUser = label ?? base.lunaPrintataUser
          if (!base.lunaPrintataNotes && t.notes) base.lunaPrintataNotes = t.notes
        }
      }
    }
  }

  return Array.from(byClientMonth.values()).sort((a, b) => {
    const nf = a.firma.localeCompare(b.firma, "ro")
    if (nf !== 0) return nf
    return b.dateTs - a.dateTs
  })
}
