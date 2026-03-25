import { getSituatieRows } from "@/actions/situatie";
import SituatieTable from "@/components/situatie/situatie-table";

export default async function SituatiePage() {
  const rows = await getSituatieRows();
  return <SituatieTable rows={rows} />;
}
