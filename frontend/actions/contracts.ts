"use server"

import { revalidatePath } from "next/cache";
import { contractsFetch } from "@/lib/auth/contracts-fetch";
import { getSession } from "@/lib/auth";
import { getUsers } from "@/lib/user-service-client";

export type GenerateContractPayload = {
  templateId: number;
  clientId: number;
  startDate: string;
  endDate: string;
  mappings: Record<string, string>;
  autoRenew?: boolean;
  contractBalance: number;
  value?: number | null;
  notes?: string | null;
};

type SessionUser = {
  id?: string;
  email?: string | null;
};

export type ContractLookup = {
  id: number;
  clientId: number;
  contractStatus?: string | null;
  contractValue?: number | null;
  contractBalance?: number | null;
  contractStartDate?: string | null;
  contractEndDate?: string | null;
  autoRenew?: boolean | null;
  createdAt?: string | null;
};

export async function getContractByClientId(clientId: number): Promise<ContractLookup | null> {
  try {
    const res = await contractsFetch("/api/contracts/search", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ clientId, page: 0, size: 1 }),
    });

    if (res.status === 204) return null;
    if (!res.ok) {
      console.error("Failed to search contracts:", await res.text());
      return null;
    }

    const data = (await res.json()) as ContractLookup[];
    return data[0] ?? null;
  } catch (error) {
    console.error("Error searching contracts:", error);
    return null;
  }
}

export async function generateContract(payload: GenerateContractPayload) {
  try {
    const session = await getSession();
    const user = session?.user as SessionUser | undefined;
    const userId = Number(user?.id);

    if (!Number.isInteger(userId) || userId <= 0 || !user?.email) {
      return { success: false, error: "Utilizator neautentificat sau sesiune incompleta" };
    }

    const requestBody = {
      ...payload,
      userId,
      userMail: user.email,
      autoRenew: payload.autoRenew ?? true,
    };

    const res = await contractsFetch("/api/contracts/generate", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Failed to generate contract:", errorText);
      return { success: false, error: errorText };
    }

    const data = await res.json();
    revalidatePath("/clients", "page");
    revalidatePath("/clienti", "page");
    revalidatePath("/contracts", "page");
    return { success: true, data };
  } catch (error) {
    console.error("Error generating contract:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}

export async function uploadSignedContract(contractId: number, formData: FormData) {
  try {
    const file = formData.get("file") as File | null;
    if (!file) {
      return { success: false, error: "Fisier lipsa" };
    }

    const session = await getSession();
    const userId = Number(session?.user?.id);
    if (!Number.isInteger(userId) || userId <= 0) {
      return { success: false, error: "Utilizator neautentificat" };
    }

    const backendFormData = new FormData();
    backendFormData.append("file", file);

    const res = await contractsFetch(`/api/contracts/${contractId}/upload-signed?userId=${userId}`, {
      method: "POST",
      body: backendFormData,
    });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Failed to upload signed contract:", errorText);
      return { success: false, error: errorText };
    }

    const data = await res.json();
    revalidatePath("/clients", "page");
    revalidatePath("/clienti", "page");
    revalidatePath("/contracts", "page");
    return { success: true, data };
  } catch (error) {
    console.error("Error uploading signed contract:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}

export async function terminateContract(contractId: number, terminationDate: string, reasons?: string) {
  try {
    const session = await getSession();
    const userId = Number(session?.user?.id);

    const res = await contractsFetch(`/api/contracts/terminate/${contractId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ terminationDate, reasons: reasons || "", userId: userId > 0 ? userId : null }),
    });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Failed to terminate contract:", errorText);
      return { success: false, error: errorText };
    }

    revalidatePath("/clients", "page");
    revalidatePath("/clienti", "page");
    revalidatePath("/contracts", "page");
    return { success: true };
  } catch (error) {
    console.error("Error terminating contract:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}

export async function toggleAutoRenewal(contractId: number) {
  try {
    const res = await contractsFetch(`/api/contracts/${contractId}/toggle-auto-renew`, {
      method: "PUT",
    });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Failed to toggle auto-renew:", errorText);
      return { success: false, error: errorText };
    }

    const data = await res.json();
    revalidatePath("/clients", "page");
    revalidatePath("/clienti", "page");
    revalidatePath("/contracts", "page");
    return { success: true, data };
  } catch (error) {
    console.error("Error toggling auto-renew:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}

export type AuditEventCategory = "created" | "signed" | "amended" | "terminated"

export type AuditEvent = {
  id: string
  name: string
  action: string
  details?: string
  date: string
  userDisplay: string
  category: AuditEventCategory
  downloadUrl?: string
}

export async function getContractAudit(
  contractId: number
): Promise<{ success: true; events: AuditEvent[] } | { success: false; error: string }> {
  try {
    const session = await getSession()
    const token = (session?.user as any)?.serviceToken ?? ""

    const [detailedRes, allUsers] = await Promise.all([
      contractsFetch(`/api/contracts/${contractId}/detailed`, { cache: "no-store" }),
      getUsers(token),
    ])

    if (!detailedRes.ok) {
      return { success: false, error: "Nu s-au putut incarca datele contractului." }
    }

    const contract = await detailedRes.json()
    const userMap = new Map<number, string>(
      allUsers.map((u: any) => [u.id, u.name || u.email] as [number, string])
    )
    const resolveUser = (id?: number | null) =>
      id != null ? (userMap.get(id) ?? `User #${id}`) : "Sistem"

    const events: AuditEvent[] = []

    if (contract.generatedAt) {
      events.push({
        id: "created",
        name: "Contract generat",
        action: "Generare contract din șablon",
        date: contract.generatedAt,
        userDisplay: resolveUser(contract.generatedByUser),
        category: "created",
        downloadUrl: `/api/contracts/download/${contractId}/unsigned/pdf`,
      })
    }

    if (contract.uploadedSignedAt) {
      events.push({
        id: "signed",
        name: "Contract semnat",
        action: "Încărcare contract semnat",
        date: contract.uploadedSignedAt,
        userDisplay: resolveUser(contract.uploadedSignedByUser),
        category: "signed",
        downloadUrl: `/api/contracts/download/${contractId}/signed/pdf`,
      })
    }

    for (const appendix of contract.appendices ?? []) {
      if (appendix.uploadedSignedAt) {
        events.push({
          id: `appendix-signed-${appendix.id}`,
          name: "Act adițional semnat",
          action: `Încărcare act adițional semnat: ${appendix.title ?? `Act adițional #${appendix.id}`}`,
          date: appendix.uploadedSignedAt,
          userDisplay: resolveUser(appendix.uploadedSignedByUser),
          category: "amended",
          downloadUrl: `/api/appendices/download/${appendix.id}/signed/pdf`,
        })
      } else if (appendix.generatedAt) {
        events.push({
          id: `appendix-gen-${appendix.id}`,
          name: "Act adițional generat",
          action: `Generare act adițional: ${appendix.title ?? `Act adițional #${appendix.id}`}`,
          date: appendix.generatedAt,
          userDisplay: resolveUser(appendix.generatedByUser),
          category: "amended",
          downloadUrl: `/api/appendices/download/${appendix.id}/unsigned/pdf`,
        })
      }
    }

    if (contract.terminatedAt) {
      const reasons = contract.reasonsForTermination?.trim()
      const effectiveDate = contract.terminationDate
        ? new Date(contract.terminationDate).toLocaleDateString("ro-RO", { day: "2-digit", month: "2-digit", year: "numeric" })
        : null
      events.push({
        id: "terminated",
        name: "Contract încetat",
        action: effectiveDate ? `Încheiere contract - Data intrării în vigoare: ${effectiveDate}` : "Încheiere contract",
        details: reasons || undefined,
        date: contract.terminatedAt,
        userDisplay: resolveUser(contract.terminatedByUserId),
        category: "terminated",
      })
    }

    events.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())

    return { success: true, events }
  } catch (error) {
    console.error("Error fetching contract audit:", error)
    return { success: false, error: "Eroare la încărcarea auditului." }
  }
}
