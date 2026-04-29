"use server"

import { revalidatePath } from "next/cache";
import { contractsFetch } from "@/lib/auth/contracts-fetch";
import { getSession } from "@/lib/auth";

export type GenerateContractPayload = {
  templateId: number;
  clientId: number;
  startDate: string;
  endDate: string;
  mappings: Record<string, string>;
  autoRenew?: boolean;
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

    const backendFormData = new FormData();
    backendFormData.append("file", file);

    const res = await contractsFetch(`/api/contracts/${contractId}/upload-signed`, {
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
    return { success: true, data };
  } catch (error) {
    console.error("Error uploading signed contract:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}

export async function terminateContract(contractId: number, terminationDate: string, reasons?: string) {
  try {
    const res = await contractsFetch(`/api/contracts/terminate/${contractId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ terminationDate, reasons: reasons || "" }),
    });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Failed to terminate contract:", errorText);
      return { success: false, error: errorText };
    }

    revalidatePath("/clients", "page");
    revalidatePath("/clienti", "page");
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
    return { success: true, data };
  } catch (error) {
    console.error("Error toggling auto-renew:", error);
    return { success: false, error: "Network error occurred connecting to backend" };
  }
}
