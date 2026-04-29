"use client";

import { useEffect, useState } from "react";

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
  const [enums, setEnums] = useState<ClientEnums>(cached ?? EMPTY);

  useEffect(() => {
    if (cached) {
      setEnums(cached);
      return;
    }
    const url = `${process.env.NEXT_PUBLIC_CLIENT_SERVICE_URL ?? "http://localhost:8084"}/api/enums`;
    fetch(url)
      .then((r) => r.json())
      .then((data) => {
        cached = data;
        setEnums(data);
      })
      .catch(() => {});
  }, []);

  return enums;
}
