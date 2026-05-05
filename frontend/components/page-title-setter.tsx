"use client";

import { useEffect, useMemo } from "react";
import { usePathname } from "next/navigation";
import { usePageTitle } from "@/components/page-title-context";

type Props = {
  // Either a raw title string (e.g. "clients/edit/23" or "Clients / Edit / 23")
  // or a list of segments that will be joined with " / "
  title?: string | string[];
  // Optionally append the app name at the end of the document title
  appName?: string;
};

function toBreadcrumbSegments(source: string | string[] | undefined, fallbackPath: string): string[] {
  if (Array.isArray(source)) return source;
  const raw = source ?? fallbackPath;
  // If it's a path (e.g. "/clients/edit/23") split by "/", otherwise split by "/" in a human string
  const parts = raw.split("/").filter(Boolean);
  return parts.map((p) =>
    p
      .replace(/[-_]+/g, " ")
      .trim()
      .replace(/\s+/g, " ")
  );
}

function capitalizeWords(s: string): string {
  return s
    .split(" ")
    .map((w) => (w ? w.charAt(0).toUpperCase() + w.slice(1) : w))
    .join(" ");
}

export default function PageTitleSetter({ title, appName }: Props) {
  const pathname = usePathname();
  const { setTitle } = usePageTitle();

  const breadcrumb = useMemo(() => {
    const segs = toBreadcrumbSegments(title, pathname || "/");
    const pretty = segs.map(capitalizeWords).join(" / ");
    return pretty || "Dashboard";
  }, [title, pathname]);

  useEffect(() => {
    // Sync to context for any consumer (e.g., custom headers)
    setTitle(breadcrumb);
    // Also update the document title
    document.title = appName ? `${breadcrumb} | ${appName}` : breadcrumb;
  }, [breadcrumb, appName, setTitle]);

  return null;
}
