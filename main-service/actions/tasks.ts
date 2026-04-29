"use server";

import { clientServiceFetch } from "@/lib/client-service-fetch";
import { getSession } from "@/lib/auth";
import { getUsers, type ServiceUser } from "@/lib/user-service-client";

export type TaskRow = {
  id: number;
  title: string;
  date?: string;
  dateTs?: number;
  done: boolean;
  user?: string;
  userId?: number;
  client?: string;
  objective?: string;
  blocked?: string;
};

export type TaskDetails = {
  id: number;
  title: string;
  date?: string;
  done: boolean;
  notes?: string;
  blocked?: string;
  objective?: string;
  userId: number;
  clientId: number;
};

type SessionUserWithServiceToken = {
  serviceToken?: string;
};

type TaskApiResponse = {
  id: number;
  title: string;
  date?: string | null;
  done?: boolean;
  notes?: string | null;
  blocked?: string | null;
  objective?: string | null;
  userId: number;
  clientId: number;
  clientName?: string | null;
};

type ClientApiResponse = {
  id: number;
  denumire?: string | null;
  name?: string | null;
};

type ClientsApiResponse = {
  content?: ClientApiResponse[];
};

type TaskMutationBody = {
  title?: string;
  date?: string;
  done?: boolean;
  notes?: string | null;
  blocked?: string | null;
  objective?: string | null;
  userId?: number;
  clientId?: number;
};

function toDMY(d?: string | null): string | undefined {
  if (!d) return undefined
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return undefined
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  return `${dd}/${mm}/${dt.getFullYear()}`
}

function dateToLocalDateTime(value: FormDataEntryValue | null): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new Error("Data este obligatorie")
  }

  const match = value.match(/^(\d{2})\/(\d{2})\/(\d{4})$/)
  if (!match) {
    throw new Error("Data trebuie aleasa in formatul dd/mm/yyyy")
  }

  const day = Number(match[1])
  const month = Number(match[2])
  const year = Number(match[3])
  const parsed = new Date(year, month - 1, day)
  if (
    parsed.getFullYear() !== year ||
    parsed.getMonth() !== month - 1 ||
    parsed.getDate() !== day
  ) {
    throw new Error("Data selectata nu este valida")
  }

  return `${match[3]}-${match[2]}-${match[1]}T00:00:00`
}

function requiredString(value: FormDataEntryValue | null, field: string): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new Error(`${field} este obligatoriu`)
  }
  return value.trim()
}

function requiredNumber(value: FormDataEntryValue | null, field: string): number {
  const number = Number(value)
  if (!Number.isInteger(number) || number <= 0) {
    throw new Error(`${field} este obligatoriu`)
  }
  return number
}

function optionalString(value: FormDataEntryValue | null): string | null {
  if (typeof value !== "string" || !value.trim()) return null
  return value.trim()
}

function userLabel(user: ServiceUser): string {
  if (!user.name?.trim()) return user.email
  return `${user.name} (${user.email})`
}

export async function getTaskRows(): Promise<TaskRow[]> {
  const session = await getSession()
  const token = (session?.user as unknown as SessionUserWithServiceToken | undefined)?.serviceToken ?? ""

  const [res, allUsers] = await Promise.all([
    clientServiceFetch("/api/tasks", { cache: "no-store" }),
    getUsers(token),
  ])

  if (!res.ok) return []

  const tasks = (await res.json()) as TaskApiResponse[]
  const userMap = new Map<number, ServiceUser>(allUsers.map((u) => [u.id, u]))

  return (tasks ?? []).map((t) => {
    const u = userMap.get(t.userId)
    return {
      id: t.id,
      title: t.title,
      date: toDMY(t.date),
      dateTs: t.date ? new Date(t.date).getTime() : undefined,
      done: !!t.done,
      user: u ? userLabel(u) : undefined,
      userId: t.userId,
      client: t.clientName ?? undefined,
      objective: t.objective ?? undefined,
      blocked: t.blocked ?? undefined,
    }
  })
}

export async function getTask(id: number): Promise<TaskDetails | null> {
  const res = await clientServiceFetch(`/api/tasks/${id}`, { cache: "no-store" })
  if (!res.ok) return null
  const t = (await res.json()) as TaskApiResponse
  return {
    id: t.id,
    title: t.title,
    date: toDMY(t.date),
    done: !!t.done,
    notes: t.notes ?? undefined,
    blocked: t.blocked ?? undefined,
    objective: t.objective ?? undefined,
    userId: t.userId,
    clientId: t.clientId,
  }
}

export async function createTask(formData: FormData) {
  const body = {
    title: requiredString(formData.get("title"), "Titlul"),
    date: dateToLocalDateTime(formData.get("date")),
    done: formData.get("done") === "on" || formData.get("done") === "true",
    notes: optionalString(formData.get("notes")),
    blocked: optionalString(formData.get("blocked")),
    objective: optionalString(formData.get("objective")),
    userId: requiredNumber(formData.get("userId"), "Userul"),
    clientId: requiredNumber(formData.get("clientId"), "Clientul"),
  }

  const res = await clientServiceFetch("/api/tasks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to create task: ${err}`)
  }

  const created = await res.json()
  return { id: created.id }
}

export async function updateTask(id: number, formData: FormData) {
  const body: TaskMutationBody = {}

  const dateFd = formData.get("date")
  if (dateFd) {
    body.date = dateToLocalDateTime(dateFd)
  }

  const title = formData.get("title")
  if (typeof title === "string" && title.trim()) body.title = title.trim()
  if (formData.get("done") !== null) body.done = formData.get("done") === "on" || formData.get("done") === "true"
  if (formData.get("notes") !== null) body.notes = optionalString(formData.get("notes"))
  if (formData.get("blocked") !== null) body.blocked = optionalString(formData.get("blocked"))
  if (formData.get("objective") !== null) body.objective = optionalString(formData.get("objective"))
  if (formData.get("userId")) body.userId = Number(formData.get("userId"))
  if (formData.get("clientId")) body.clientId = Number(formData.get("clientId"))

  const res = await clientServiceFetch(`/api/tasks/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const err = await res.text()
    throw new Error(`Failed to update task: ${err}`)
  }

  const t = (await res.json()) as TaskApiResponse
  return {
    id: t.id,
    title: t.title,
    date: toDMY(t.date),
    done: !!t.done,
    notes: t.notes ?? undefined,
    blocked: t.blocked ?? undefined,
    objective: t.objective ?? undefined,
    userId: t.userId,
    clientId: t.clientId,
  }
}

export async function deleteTask(id: number) {
  const res = await clientServiceFetch(`/api/tasks/${id}`, { method: "DELETE" })
  if (!res.ok && res.status !== 404) throw new Error("Failed to delete task")
  return { id }
}

export async function getTaskFormOptions() {
  const session = await getSession()
  const token = (session?.user as unknown as SessionUserWithServiceToken | undefined)?.serviceToken ?? ""

  const [usersRes, clientsRes] = await Promise.all([
    getUsers(token),
    clientServiceFetch("/api/clients?size=1000&page=0&sort=denumire,asc", { cache: "no-store" }),
  ])

  const clientsData = clientsRes.ok ? ((await clientsRes.json()) as ClientsApiResponse | ClientApiResponse[]) : { content: [] }
  const clients = Array.isArray(clientsData) ? clientsData : clientsData.content ?? []

  return {
    users: usersRes.map((u) => ({ id: u.id, label: userLabel(u) })),
    clients: clients.map((c) => ({ id: c.id, label: c.denumire ?? c.name ?? String(c.id) })),
  }
}
