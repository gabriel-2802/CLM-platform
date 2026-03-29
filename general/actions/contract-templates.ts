"use server"

import { PrismaClient } from "@/lib/generated/prisma-client"
import { writeFile, mkdir } from "fs/promises"
import { join } from "path"
import { revalidatePath } from "next/cache"

const prisma = new PrismaClient()

export async function uploadTemplate(formData: FormData) {
  const file = formData.get("file") as File
  const name = formData.get("name") as string

  if (!file || !name) {
    throw new Error("File and name are required")
  }

  const bytes = await file.arrayBuffer()
  const buffer = Buffer.from(bytes)

  const uploadDir = join(process.cwd(), "public", "templates")
  await mkdir(uploadDir, { recursive: true })

  const fileName = `${Date.now()}-${file.name.replace(/[^a-zA-Z0-9.\-_]/g, '')}`
  const filePath = join(uploadDir, fileName)

  await writeFile(filePath, buffer)

  await prisma.contractTemplate.create({
    data: {
      name,
      fileName: file.name,
      filePath: `/api/templates/${fileName}`
    }
  })

  revalidatePath("/contract-templates")
}

export async function deleteTemplate(id: number) {
  await prisma.contractTemplate.delete({ where: { id } })
  revalidatePath("/contract-templates")
}

export async function getTemplates() {
  return await prisma.contractTemplate.findMany({
    orderBy: { createdAt: "desc" }
  });
}
