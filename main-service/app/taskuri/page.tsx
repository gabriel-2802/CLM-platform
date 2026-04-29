import { getTaskRows, getTaskFormOptions } from "@/actions/tasks";
import TasksTable from "@/components/tasks/tasks-table";

export const dynamic = "force-dynamic";
export const revalidate = 0;

export default async function TaskuriPage() {
  const [rows, opts] = await Promise.all([getTaskRows(), getTaskFormOptions()]);
  return <TasksTable rows={rows} users={opts.users} />;
}
