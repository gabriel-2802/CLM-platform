
FROM nginx:1.27-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf      /etc/nginx/nginx.conf
COPY conf.d/clm.conf /etc/nginx/conf.d/clm.conf

EXPOSE 80 443

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:80 || exit 1