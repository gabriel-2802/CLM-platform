"use client";
import * as React from "react";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ChevronLeft, ChevronRight } from "lucide-react";

type Props = {
  value?: string; // YYYY-MM
  onChange: (val: string | undefined) => void;
  buttonClassName?: string;
};

export default function MonthPicker({ value, onChange, buttonClassName }: Props) {
  const [open, setOpen] = React.useState(false);
  const parsed = React.useMemo(() => {
    if (!value) return undefined as Date | undefined;
    const [yy, mm] = value.split("-");
    const y = Number(yy);
    const m = Number(mm) - 1;
    if (Number.isNaN(y) || Number.isNaN(m)) return undefined;
    return new Date(y, m, 1);
  }, [value]);

  const [year, setYear] = React.useState<number>(() => parsed?.getFullYear() ?? new Date().getFullYear());
  React.useEffect(() => {
    if (parsed) setYear(parsed.getFullYear());
  }, [parsed]);

  const label = React.useMemo(() => {
    if (!parsed) return "Alege luna";
    return parsed.toLocaleDateString(undefined, { year: "numeric", month: "long" });
  }, [parsed]);

  const monthNames = React.useMemo(() => {
    return Array.from({ length: 12 }, (_v, i) =>
      new Date(2000, i, 1).toLocaleDateString(undefined, { month: "short" })
    );
  }, []);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" className={buttonClassName} aria-label="Alege luna">
          {label}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[18rem] p-3" align="start">
        <div className="flex items-center justify-between mb-2">
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setYear((y) => y - 1)} aria-label="Anul anterior">
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="text-sm font-medium">{year}</div>
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setYear((y) => y + 1)} aria-label="Anul urmator">
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
        <div className="grid grid-cols-3 gap-2">
          {monthNames.map((name, idx) => {
            const isSelected = parsed && parsed.getFullYear() === year && parsed.getMonth() === idx;
            return (
              <Button
                key={idx}
                variant={isSelected ? "default" : "outline"}
                className="h-8"
                onClick={() => {
                  const m = String(idx + 1).padStart(2, "0");
                  onChange(`${year}-${m}`);
                  setOpen(false);
                }}
              >
                {name}
              </Button>
            );
          })}
        </div>
        {value && (
          <div className="mt-3 text-right">
            <Button variant="ghost" size="sm" className="h-8" onClick={() => onChange(undefined)}>
              Reset
            </Button>
          </div>
        )}
      </PopoverContent>
    </Popover>
  );
}
