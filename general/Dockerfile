# ─────────────────────────────────────────────────────────────────────────────
# Stage: development — hot-reload dev server
# ─────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS development

ENV NEXT_TELEMETRY_DISABLED=1

WORKDIR /app

COPY package*.json ./
COPY prisma ./prisma
RUN npm ci

COPY . .

EXPOSE 3000

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=60s \
  CMD wget -q --spider http://127.0.0.1:3000 || exit 1

CMD ["npm", "run", "dev"]

# ─────────────────────────────────────────────────────────────────────────────
# Stage: builder — compile production bundle
# Requires `output: 'standalone'` in next.config.ts for the runner stage.
# ─────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

ENV NEXT_TELEMETRY_DISABLED=1

WORKDIR /app

COPY package*.json ./
COPY prisma ./prisma
RUN npm ci

COPY . .

RUN npm run build

# ─────────────────────────────────────────────────────────────────────────────
# Stage: runner — minimal production image (no source, no dev deps)
# ─────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS runner

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

RUN addgroup -g 1001 -S nodejs && \
    adduser  -u 1001 -S nextjs -G nodejs

WORKDIR /app

# Standalone bundle embeds all required node_modules — no npm install needed at runtime
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static   ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/public          ./public

USER nextjs

EXPOSE 3000

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=60s \
  CMD wget -q --spider http://127.0.0.1:3000 || exit 1

CMD ["node", "server.js"]
