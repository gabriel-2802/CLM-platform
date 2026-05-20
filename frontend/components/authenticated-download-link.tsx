"use client";

import { useAuthenticatedDownload } from "@/hooks/use-authenticated-download";
import { toast } from "sonner";

export function AuthenticatedDownloadLink({
  path,
  label,
  className,
  openInNewTab = true,
  fallbackFilename,
}: {
  path: string;
  label: string;
  className?: string;
  openInNewTab?: boolean;
  fallbackFilename?: string;
}) {
  const downloadWithAuth = useAuthenticatedDownload();

  const handleClick = () => {
    downloadWithAuth(path, { openInNewTab, fallbackFilename }).catch((err) => {
      const message = err instanceof Error ? err.message : "Descarcare esuata.";
      toast.error(message);
    });
  };

  return (
    <button type="button" className={className} onClick={handleClick}>
      {label}
    </button>
  );
}
