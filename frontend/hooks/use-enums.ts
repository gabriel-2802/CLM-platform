"use client";

import { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { API_BASE_URL } from "@/lib/config/public";

export type ClientEnums = {
  companyTypes: string[];
  taxTypes: string[];
  taxFrequencies: string[];
  yesNoNa: string[];
  administrations: string[];
};

const EMPTY: ClientEnums = {
  companyTypes: [],
  taxTypes: [],
  taxFrequencies: [],
  yesNoNa: [],
  administrations: [],
};

let cached: ClientEnums | null = null;

export function useEnums(): ClientEnums {
  const { data: session } = useSession();
  const [enums, setEnums] = useState<ClientEnums>(cached ?? EMPTY);

  useEffect(() => {
    if (cached) {
      setEnums(cached);
      return;
    }
    const token = (session?.user as any)?.serviceToken as string | undefined;
    if (!token) return;
    fetch(`${API_BASE_URL}/api/enums`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((r) => r.json())
      .then((data) => {
        cached = data;
        setEnums(data);
      })
      .catch(() => {});
  }, [session]);

  return enums;
}
