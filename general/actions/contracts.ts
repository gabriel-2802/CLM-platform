"use server"

import { revalidatePath } from "next/cache";
import { contractsFetch } from "@/lib/auth/contracts-fetch";

export async function generateContract(payload: any) {
  try {
    const res = await contractsFetch("/api/contracts/generate", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
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
