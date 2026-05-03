const USER_SERVICE_URL = process.env.USER_SERVICE_URL!;

export interface ServiceUser {
  id: number;
  email: string;
  name: string | null;
  enabled: boolean;
  roles: string[];
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: ServiceUser;
}

export type AppRole = "USER" | "ADMIN" | "MANAGER";

export function primaryRole(user: ServiceUser): AppRole {
  const roles = user.roles.map((r) => r.replace("ROLE_", ""));
  if (roles.includes("ADMIN")) return "ADMIN";
  if (roles.includes("MANAGER")) return "MANAGER";
  return "USER";
}

export function normalizeUser(u: ServiceUser) {
  return {
    id: u.id,
    email: u.email,
    name: u.name,
    enabled: u.enabled,
    rol: primaryRole(u),
    createdAt: u.createdAt,
  };
}

export async function loginUser(
  email: string,
  password: string
): Promise<AuthResponse | null> {
  try {
    const res = await fetch(`${USER_SERVICE_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
      cache: "no-store",
    });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export async function registerUser(data: {
  email: string;
  password: string;
  name: string;
  adminCode?: string;
}): Promise<{ status: number; body: unknown }> {
  const res = await fetch(`${USER_SERVICE_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
    cache: "no-store",
  });
  return { status: res.status, body: await res.json() };
}

export async function getUsers(token: string | null | undefined): Promise<ServiceUser[]> {
  if (!token) return [];
  try {
    const res = await fetch(`${USER_SERVICE_URL}/api/users`, {
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    if (!res.ok) return [];
    const data = await res.json();
    return Array.isArray(data) ? data : (data.content ?? []);
  } catch {
    return [];
  }
}

export async function getUserById(
  id: number,
  token: string | null | undefined
): Promise<ServiceUser | null> {
  if (!token) return null;
  try {
    const res = await fetch(`${USER_SERVICE_URL}/api/users/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export async function updateUser(
  id: number,
  data: { email: string; name?: string | null; role?: string },
  token: string
): Promise<{ status: number; body: unknown }> {
  const res = await fetch(`${USER_SERVICE_URL}/api/users/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
    cache: "no-store",
  });
  return { status: res.status, body: await res.json() };
}

export async function deleteUser(id: number, token: string): Promise<number> {
  const res = await fetch(`${USER_SERVICE_URL}/api/users/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });
  return res.status;
}

export async function resetPassword(
  id: number,
  password: string,
  token: string
): Promise<{ status: number; body: unknown }> {
  const res = await fetch(`${USER_SERVICE_URL}/api/users/${id}/password`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ password }),
    cache: "no-store",
  });
  return { status: res.status, body: await res.json() };
}
