"use client";
import * as React from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { ChevronLeft } from "lucide-react";

type Props = {
  className?: string;
  label?: string;
};

export default function BackButton({ className, label = "Înapoi" }: Props) {
  const router = useRouter();
  return (
    <div className={className}>
      <Button
        variant="outline"
        size="sm"
        className="h-8 px-2 gap-1 text-xs"
        onClick={() => router.back()}
        aria-label="Inapoi"
      >
        <ChevronLeft className="h-4 w-4" />
        {label}
      </Button>
    </div>
  );
}
