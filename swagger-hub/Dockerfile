# syntax=docker/dockerfile:1

FROM python:3.13-alpine

RUN adduser -D -u 1001 appuser

WORKDIR /app

COPY server.py index.html ./

ENV DOCS_DIR=/app
ENV PORT=8090

USER appuser

EXPOSE 8090

CMD ["python", "/app/server.py"]
