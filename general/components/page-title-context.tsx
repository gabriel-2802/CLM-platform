"use client";
import { createContext, useContext, useState } from "react";

type TitleCtx = {
  title: string | null;
  setTitle: (t: string | null) => void;
};

const Ctx = createContext<TitleCtx | null>(null);

export function TitleProvider({ children }: { children: React.ReactNode }) {
  const [title, setTitle] = useState<string | null>(null);
  return <Ctx.Provider value={{ title, setTitle }}>{children}</Ctx.Provider>;
}

export function usePageTitle() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("usePageTitle must be used within TitleProvider");
  return ctx;
}
