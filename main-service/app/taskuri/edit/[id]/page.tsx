import { notFound } from "next/navigation";
import BackButton from "../../../../components/ui/back-button";
import { getTask, updateTask, createTask, getTaskFormOptions } from "@/actions/tasks";
import TaskForm from "@/components/tasks/task-form";
import PageTitleSetter from "@/components/page-title-setter";

export const dynamic = "force-dynamic";

export default async function EditTaskPage({ params }: { params: Promise<{ id: string }> }) {
	const { id: idParam } = await params;
	const isCreate = idParam === "new" || idParam === "nou";
	const idNum = Number(idParam);
	const options = await getTaskFormOptions();
	let task = null as Awaited<ReturnType<typeof getTask>> | null;
	if (!isCreate) {
		if (Number.isNaN(idNum)) return notFound();
		task = await getTask(idNum);
		if (!task) return notFound();
	}

	async function onSubmit(fd: FormData) {
		"use server";
		if (isCreate) {
			return await createTask(fd);
		} else {
			return await updateTask(idNum, fd);
		}
	}

	return (
		<div className="p-6 space-y-4">
			<BackButton className="-mt-2" />
			<PageTitleSetter title={`Taskuri / ${isCreate ? "Nou" : task?.title || "Edit"}`} />
			<h1 className="text-2xl font-semibold">{isCreate ? "Task nou" : `Editeaza: ${task?.title}`}</h1>
			<TaskForm initial={task ?? undefined} options={options} onSubmit={onSubmit} submitLabel={isCreate ? "Creeaza" : "Salveaza"} />
		</div>
	);
}