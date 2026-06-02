// ── Next.js API routes (browser-facing) ──────────────────────────────────────

export const API = {
  auth: {
    nextauth: "/api/auth",
    register: "/api/register",
  },

  users: {
    list:          "/api/users",
    byId:          (id: number | string) => `/api/users/${id}`,
    resetPassword: (id: number | string) => `/api/users/${id}/reset-password`,
  },

  templates: {
    downloadDocx: (id: number | string) => `/api/templates/download/${id}/docx`,
    downloadPdf:  (id: number | string) => `/api/templates/download/${id}/pdf`,
  },

  contracts: {
    downloadPdf: (id: number | string, type: "unsigned" | "signed" = "unsigned") =>
      `/api/contracts/download/${id}/${type}/pdf`,
  },

  tasks: {
    generate:              "/api/tasks/generate",
    generateConditional:   "/api/tasks/generateConditional",
    generateWithRules:     "/api/tasks/generateWithRules",
  },
} as const;

// ── User service (server-side, http://user-service:8083) ──────────────────────

export const USER_SERVICE = {
  auth: {
    login:    "/api/auth/login",
    register: "/api/auth/register",
  },

  users: {
    list:     "/api/users",
    byId:     (id: number | string) => `/api/users/${id}`,
    password: (id: number | string) => `/api/users/${id}/password`,
  },
} as const;

// ── Client service (server-side, http://client-service:8084) ─────────────────

export const CLIENT_SERVICE = {
  clients: {
    list:           "/api/clients",
    byId:           (id: number | string) => `/api/clients/${id}`,
    users:          (clientId: number | string) => `/api/clients/${clientId}/users`,
    userById:       (clientId: number | string, userId: number | string) =>
                      `/api/clients/${clientId}/users/${userId}`,
    details:        (clientId: number | string) => `/api/clients/${clientId}/details`,
    histories:      (clientId: number | string) => `/api/clients/${clientId}/histories`,
    historyByYear:  (clientId: number | string, year: number | string) =>
                      `/api/clients/${clientId}/histories/${year}`,
    workPoints:     (clientId: number | string) => `/api/clients/${clientId}/work-points`,
    workPointById:  (clientId: number | string, id: number | string) =>
                      `/api/clients/${clientId}/work-points/${id}`,
    templateFields: "/api/clients/template-fields",
  },

  tasks: {
    list:               "/api/tasks",
    byId:               (id: number | string) => `/api/tasks/${id}`,
    generateWithRules:  "/api/tasks/generate-with-rules",
  },

  enums: {
    list: "/api/enums",
  },
} as const;

// ── Contracts service (server-side, http://contracts:8081) ───────────────────

export const CONTRACTS_SERVICE = {
  contracts: {
    search:       "/api/contracts/search",
    generate:     "/api/contracts/generate",
    terminate:    (id: number | string) => `/api/contracts/terminate/${id}`,
    autoRenew:    (id: number | string) => `/api/contracts/${id}/toggle-auto-renew`,
    uploadSigned: (id: number | string) => `/api/contracts/${id}/upload-signed`,
    downloadPdf:  (id: number | string, type: "unsigned" | "signed") =>
            `/api/contracts/download/${id}/${type}/pdf`,
    detailed:     (id: number | string) => `/api/contracts/${id}/detailed`,
  },

  templates: {
    list:          "/api/templates",
    byId:          (id: number | string) => `/api/templates/${id}`,
    upload:        "/api/templates/upload",
    labels:        (id: number | string) => `/api/templates/${id}/labels`,
    downloadDocx:  (id: number | string) => `/api/templates/download/${id}/docx`,
    downloadPdf:   (id: number | string) => `/api/templates/download/${id}/pdf`,
  },
} as const;
