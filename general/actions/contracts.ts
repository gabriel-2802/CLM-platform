"use server"

import { revalidatePath } from "next/cache";

const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8080";

export async function generateContract(payload: any) {
  try {
    const res = await fetch(`${API_BASE_URL}/api/contracts/generate`, {
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
