"use client";
import React from "react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

export interface FirmaAutocompleteProps {
  firms: string[];
  value: string;
  onChange: (val: string) => void;
  placeholder?: string;
  className?: string;
}

// Simple client-side autocomplete for selecting a single firma.
// Shows a popover with filtered firms based on substring match.
export default function FirmaAutocomplete({ firms, value, onChange, placeholder = "Toate firmele", className }: FirmaAutocompleteProps) {
  const [query, setQuery] = React.useState<string>(value);
  const [open, setOpen] = React.useState(false);
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const inputRef = React.useRef<HTMLInputElement | null>(null);
  const listRef = React.useRef<HTMLUListElement | null>(null);
  const normalizedFirms = React.useMemo(() => firms.sort((a, b) => a.localeCompare(b, "ro")), [firms]);

  React.useEffect(() => {
    // Sync external value to internal query; avoid adding 'query' to dependencies intentionally.
    setQuery((prev) => (prev === value ? prev : value));
  }, [value]);

  const filtered = React.useMemo(() => {
    if (!query) return normalizedFirms;
    const q = query.toLowerCase();
    return normalizedFirms.filter((f) => f.toLowerCase().includes(q));
  }, [normalizedFirms, query]);

  const selectFirma = (firma: string) => {
    onChange(firma);
    setQuery(firma);
    setOpen(false);
  };

  const clear = () => {
    onChange("");
    setQuery("");
    setOpen(false);
    inputRef.current?.focus();
  };

  React.useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (!containerRef.current || containerRef.current.contains(e.target as Node)) return;
      setOpen(false);
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const onKeyDown: React.KeyboardEventHandler<HTMLInputElement> = (e) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setOpen(true);
      const first = listRef.current?.querySelector("li");
      (first as HTMLElement | null)?.focus();
    } else if (e.key === "Enter") {
      // If exact match exists, select it, else select first filtered.
      const exact = filtered.find((f) => f.toLowerCase() === query.toLowerCase());
      if (exact) {
        selectFirma(exact);
      } else if (filtered.length) {
        selectFirma(filtered[0]);
      }
    } else if (e.key === "Escape") {
      setOpen(false);
    }
  };

  return (
    <div ref={containerRef} className={cn("relative w-64", className)}>
      <div className="flex gap-2 items-center">
        <Input
          ref={inputRef}
            value={query}
          placeholder={placeholder}
          onChange={(e) => {
            setQuery(e.target.value);
            setOpen(true);
          }}
          onFocus={() => setOpen(true)}
          onKeyDown={onKeyDown}
          className="h-8 text-sm"
          aria-autocomplete="list"
          aria-expanded={open}
          aria-controls="firma-autocomplete-list"
        />
        {value && (
          <button
            type="button"
            onClick={clear}
            className="h-8 px-2 rounded border text-xs hover:bg-muted"
            aria-label="Reseteaza firma"
          >
            Reset
          </button>
        )}
      </div>
      {open && filtered.length > 0 && (
        <ul
          id="firma-autocomplete-list"
          ref={listRef}
          className="absolute z-50 mt-1 max-h-64 w-full overflow-auto rounded border bg-popover text-popover-foreground shadow-sm"
          role="listbox"
        >
          {filtered.map((f) => (
            <li
              key={f}
              tabIndex={0}
              role="option"
              aria-selected={value === f}
              onClick={() => selectFirma(f)}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  selectFirma(f);
                }
              }}
              className={cn(
                "cursor-pointer select-none px-3 py-1.5 text-xs",
                value === f ? "bg-primary/10" : "hover:bg-muted"
              )}
            >
              {f}
            </li>
          ))}
        </ul>
      )}
      {open && filtered.length === 0 && (
        <div className="absolute z-50 mt-1 w-full rounded border bg-popover p-2 text-xs text-muted-foreground shadow-sm">
          Nicio firma gasita
        </div>
      )}
    </div>
  );
}
