/**
 * Server-side service URLs.
 *
 * These resolve to internal Docker network addresses and are NEVER exposed to
 * the browser. All server actions and API routes should import from here
 * instead of reading process.env inline.
 */

export const USER_SERVICE_URL =
  process.env.USER_SERVICE_URL ?? "http://user-service:8083"

export const CLIENT_SERVICE_URL =
  process.env.CLIENT_SERVICE_URL ?? "http://client-service:8084"

export const CONTRACTS_SERVICE_URL =
  process.env.CONTRACTS_SERVICE_URL ?? "http://contracts:8081"

export const NOTIFICATIONS_SERVICE_URL =
  process.env.NOTIFICATIONS_SERVICE_URL ?? "http://notifications:8082"
