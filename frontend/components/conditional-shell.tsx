"use client"

import * as React from "react"
import { usePathname } from "next/navigation"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { TitleProvider } from "@/components/page-title-context"

export function ConditionalShell({
  children,
  isAdmin,
  userName,
}: {
  children: React.ReactNode
  isAdmin: boolean
  userName: string
}) {
  const pathname = usePathname()
  const isAuthRoute = pathname === "/signin" || pathname === "/register"

  if (isAuthRoute) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4">
        <div className="@container/main w-full max-w-md">
          {children}
        </div>
      </div>
    )
  }

  return (
    <SidebarProvider
      style={{
        "--sidebar-width": "calc(var(--spacing) * 72)",
        "--header-height": "calc(var(--spacing) * 12)",
      } as React.CSSProperties}
    >
      <AppSidebar variant="inset" isAdmin={isAdmin} />
      <SidebarInset className="min-w-0 overflow-x-hidden">
        <TitleProvider>
          <SiteHeader userName={userName} />
          <div className="flex flex-1 flex-col min-w-0 overflow-x-hidden">
            <div className="@container/main flex flex-1 flex-col gap-2 min-w-0 overflow-x-hidden">
              <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6 min-w-0 overflow-x-hidden">
                <div className="px-4 lg:px-6 min-w-0 overflow-x-hidden">
                  {children}
                </div>
              </div>
            </div>
          </div>
        </TitleProvider>
      </SidebarInset>
    </SidebarProvider>
  )
}
