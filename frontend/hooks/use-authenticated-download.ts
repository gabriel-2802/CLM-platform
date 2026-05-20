"use client";

import { useCallback } from "react";
import { useSession } from "next-auth/react";
import { API_BASE_URL } from "@/lib/config/public";

type DownloadOptions = {
  openInNewTab?: boolean;
  fallbackFilename?: string;
};

const FILENAME_RE = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i;

function getFilenameFromHeader(header: string | null, fallback: string): string {
  if (!header) return fallback;
  const match = header.match(FILENAME_RE);
  if (!match) return fallback;
  const raw = match[1] ?? match[2];
  if (!raw) return fallback;
  try {
    return decodeURIComponent(raw.replace(/\+/g, " "));
  } catch {
    return raw;
  }
}

export function useAuthenticatedDownload() {
  const { data: session } = useSession();
  const token = (session?.user as { serviceToken?: string } | undefined)?.serviceToken;

  return useCallback(
    async (path: string, options: DownloadOptions = {}) => {
      if (!token) {
        throw new Error("Autentificare lipsa.");
      }

      const url = path.startsWith("http") ? path : `${API_BASE_URL}${path}`;
      const response = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` },
        cache: "no-store",
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Descarcare esuata (${response.status}).`);
      }

      const fallbackName = options.fallbackFilename ?? "download";
      const filename = getFilenameFromHeader(
        response.headers.get("content-disposition"),
        fallbackName
      );

      const blob = await response.blob();
      const objectUrl = URL.createObjectURL(blob);

      if (options.openInNewTab) {
        window.open(objectUrl, "_blank", "noopener,noreferrer");
      } else {
        const anchor = document.createElement("a");
        anchor.href = objectUrl;
        anchor.download = filename;
        anchor.click();
      }

      setTimeout(() => URL.revokeObjectURL(objectUrl), 15000);
    },
    [token]
  );
}
