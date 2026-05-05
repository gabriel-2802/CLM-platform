import ClientForm from "@/components/clients/client-form";
import { createClient } from "@/actions/clients";

export default function NewClientPage() {
  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Client nou</h1>
      <ClientForm onSubmit={createClient} submitLabel="Creeaza" />
    </div>
  )
}
