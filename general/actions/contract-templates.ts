"use server"

import { revalidatePath } from "next/cache"

const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8080";

export async function uploadTemplate(formData: FormData) {
  const file = formData.get("file") as File
  const name = formData.get("name") as string

  if (!file || !name) {
    throw new Error("File and name are required")
  }

  const backendFormData = new FormData();
  backendFormData.append("file", file);
  backendFormData.append("templateName", name);

  const res = await fetch(`${API_BASE_URL}/api/templates/upload`, {
    method: "POST",
    body: backendFormData,
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Failed to upload template: ${err}`);
  }

  revalidatePath("/contract-templates")
}

export async function deleteTemplate(id: number) {
  const res = await fetch(`${API_BASE_URL}/api/templates/${id}`, {
    method: "DELETE"
  });

  if (!res.ok) {
    throw new Error("Failed to delete template");
  }

  revalidatePath("/contract-templates")
}

export async function getTemplates() {
  const res = await fetch(`${API_BASE_URL}/api/templates?page=0&size=50`, {
    cache: "no-store",
  });

  if (!res.ok) {
    console.error("Failed to fetch templates", await res.text());
    return [];
  }

  const data = await res.json();
  const content = data.content || data || [];

  return content.map((t: any) => ({
    id: t.templateId,
    name: t.templateName,
    createdAt: t.createdAt,
    fullyMapped: t.fullyMapped,
    fieldCount: t.fieldCount
  }));
}

export async function getTemplateById(id: number) {
  const res = await fetch(`${API_BASE_URL}/api/templates/${id}`, {
    cache: "no-store",
  });

  if (!res.ok) {
    if (res.status === 404) return null;
    throw new Error("Failed to fetch template by id");
  }

  return res.json();
}

export async function updateTemplateMappings(templateId: number, mappings: { fieldId: number, fieldLabel: string }[]) {
  const payload = {
    templateId,
    mappings: mappings.map(m => ({
      fieldId: m.fieldId,
      fieldLabel: m.fieldLabel,
      dataType: "STRING",
      isRequired: true,
      formatPattern: ""
    }))
  };

  const res = await fetch(`${API_BASE_URL}/api/templates/${templateId}/labels`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const errorText = await res.text();
    console.error("Failed to update mappings:", errorText);
    return { success: false, error: errorText };
  }

  revalidatePath("/contract-templates");
  return { success: true };
}
