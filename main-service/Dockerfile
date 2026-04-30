# syntax=docker/dockerfile:1

#  stage: development — hot-reload dev server ───────
FROM node:20-alpine AS development

ENV NEXT_TELEMETRY_DISABLED=1

WORKDIR /app

# copy manifests and prisma schema before source — deps layer stays cached
COPY package*.json ./
COPY prisma ./prisma
RUN npm ci

# copied here as fallback — docker compose mounts override this at runtime
COPY . .

EXPOSE 3000

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=60s \
  CMD wget -q --spider http://127.0.0.1:3000 || exit 1

CMD ["npm", "run", "dev"]

# stage: builder — compile production bundle ───────
# requires output: 'standalone' in next.config.ts for the runner stage
FROM node:20-alpine AS builder

ENV NEXT_TELEMETRY_DISABLED=1

WORKDIR /app

# copy manifests and prisma schema before source — deps layer stays cached
COPY package*.json ./
COPY prisma ./prisma
RUN --mount=type=cache,target=/root/.npm \
    npm ci

COPY . .

# build-time public env vars must be baked in at compile time for next.js
ARG NEXT_PUBLIC_CONTRACTS_API_URL
ARG NEXT_PUBLIC_NOTIFICATIONS_API_URL
ARG NEXT_PUBLIC_USER_SERVICE_URL
ENV NEXT_PUBLIC_CONTRACTS_API_URL=$NEXT_PUBLIC_CONTRACTS_API_URL
ENV NEXT_PUBLIC_NOTIFICATIONS_API_URL=$NEXT_PUBLIC_NOTIFICATIONS_API_URL
ENV NEXT_PUBLIC_USER_SERVICE_URL=$NEXT_PUBLIC_USER_SERVICE_URL

RUN --mount=type=cache,target=/root/.npm \
    npm run build

# stage: runner — minimal production image (no source, no dev deps) 
FROM node:20-alpine AS runner

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

RUN addgroup -g 1001 -S nodejs && \
    adduser  -u 1001 -S nextjs -G nodejs

WORKDIR /app

# standalone bundle embeds all required node_modules — no npm install needed at runtime
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static     ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/public           ./public

USER nextjs

EXPOSE 3000

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=60s \
  CMD wget -q --spider http://127.0.0.1:3000 || exit 1

CMD ["node", "server.js"]