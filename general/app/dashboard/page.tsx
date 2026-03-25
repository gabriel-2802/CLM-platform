import { ChartAreaInteractive } from "@/components/chart-area-interactive"
import { FullDataTable as DataTable } from "@/components/dashboard/full-data-table"
import { SectionCards } from "@/components/section-cards"

import data from "./data.json"

export default function Page() {
  return (
    <div className="flex flex-col gap-4">
      <SectionCards />
      <ChartAreaInteractive />
      <DataTable data={data} />
    </div>
  )
}
