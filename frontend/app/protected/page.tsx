import { requireUser } from "@/lib/auth";

export default async function ProtectedPage() {
  const user = await requireUser();
  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">Protected</h1>
      <p>Welcome, {user.email}!</p>
    </div>
  );
}
