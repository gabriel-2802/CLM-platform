"use client";
import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { useMemo } from "react";
import { usePathname } from "next/navigation";

function prettify(segment: string) {
  const s = segment.replace(/[-_]/g, " ").trim();
  return s.charAt(0).toUpperCase() + s.slice(1);
}

export function SiteHeader({ userName }: { userName?: string }) {
  const pathname = usePathname();
  const segments = useMemo(() => (pathname || "/").split("/").filter(Boolean), [pathname]);

  return (
    <header className="flex h-(--header-height) shrink-0 items-center gap-2 border-b transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-(--header-height)">
      <div className="flex w-full items-center gap-1 px-4 lg:gap-2 lg:px-6">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mx-2 data-[orientation=vertical]:h-4" />
        <h1 className="text-sm text-blue-800 flex items-center flex-wrap">
          {segments.length === 0 ? (
            <span>Dashboard</span>
          ) : (
            segments.map((seg, idx) => {
              const label = prettify(seg);
              const isFirst = idx === 0;
              const isLast = idx === segments.length - 1;
              // middle segments are not links; only first is linked back to index
              return (
                <span key={`${seg}-${idx}`} className="flex items-center">
                  {idx > 0 && <span className="mx-1 text-muted-foreground">/</span>}
                  {isFirst ? (
                    <Link href={`/${segments[0]}`} className="text-primary underline-offset-2 hover:underline">
                      {label}
                    </Link>
                  ) : isLast ? (
                    <span className="text-foreground">{label}</span>
                  ) : (
                    <span className="text-muted-foreground">{label}</span>
                  )}
                </span>
              );
            })
          )}
        </h1>
        <div className="ml-auto flex items-center gap-3 text-sm text-muted-foreground">
          {userName ? <span>Salutare, <span className="font-medium text-foreground">{userName}</span></span> : null}
        </div>
      </div>
    </header>
  );
}
