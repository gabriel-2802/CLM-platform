# syntax=docker/dockerfile:1

FROM nginx:1.27-alpine

# remove default config — replaced by clm.conf below
RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf      /etc/nginx/nginx.conf
COPY conf.d/clm.conf /etc/nginx/conf.d/clm.conf

# certs are mounted at runtime via docker compose volume — not baked into the
# image so the private key never ends up in a layer or in version control
EXPOSE 80 443

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:80 || exit 1