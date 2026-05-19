import { NextRequest, NextResponse } from "next/server"
import { getServerSession } from "next-auth"
import { authOptions } from "@/lib/auth/config"
import { clientServiceFetch } from "@/lib/client-service-fetch"

export const dynamic = "force-dynamic"

export async function GET(request: NextRequest) {
  const session = await getServerSession(authOptions)
  if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const res = await clientServiceFetch("/api/clients?request.page=0&request.size=1000", { cache: "no-store" })
  if (!res.ok) return NextResponse.json([], { status: 200 })

  const data = await res.json()
  const clients: any[] = data.content ?? data ?? []

  return NextResponse.json(
    clients.map((c: any) => ({ id: c.id, name: c.name })).sort((a: any, b: any) => a.name.localeCompare(b.name)),
    { headers: { "Cache-Control": "no-store" } }
  )
}
