/**
 * Browser-side API URLs.
 *
 * All traffic goes through Nginx at the same origin — there is no direct
 * service access from the browser. NEXT_PUBLIC_* vars are statically inlined
 * at build time by Next.js.
 *
 * Use API_BASE_URL when you need to construct a path not covered by one of the
 * named constants (e.g. /api/enums, /api/tasks).
 */

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "https://localhost"

export const USER_API_URL =
  process.env.NEXT_PUBLIC_USER_SERVICE_URL ?? `${API_BASE_URL}/api/users`

export const CLIENT_API_URL =
  process.env.NEXT_PUBLIC_CLIENT_SERVICE_URL ?? `${API_BASE_URL}/api/clients`

export const CONTRACTS_API_URL =
  process.env.NEXT_PUBLIC_CONTRACTS_API_URL ?? `${API_BASE_URL}/api/contracts`

export const NOTIFICATIONS_API_URL =
  process.env.NEXT_PUBLIC_NOTIFICATIONS_API_URL ??
  `${API_BASE_URL}/api/notifications`
