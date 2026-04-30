#!/usr/bin/env python3
"""Lightweight static + proxy server for the CLM Swagger Hub."""

import http.server
import urllib.request
import urllib.error
import os

DOCS_DIR = os.environ.get("DOCS_DIR", "/docs")

PROXY = {
    "/proxy/contracts": "http://contracts:8081/v3/api-docs",
    "/proxy/user":      "http://user-service:8083/v3/api-docs",
    "/proxy/client":    "http://client-service:8084/v3/api-docs",
}


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DOCS_DIR, **kwargs)

    def do_GET(self):
        path = self.path.split("?")[0]
        if path in PROXY:
            target = PROXY[path]
            try:
                with urllib.request.urlopen(target, timeout=8) as resp:
                    data = resp.read()
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Content-Length", str(len(data)))
                self.end_headers()
                self.wfile.write(data)
            except Exception as exc:
                msg = str(exc).encode()
                self.send_response(502)
                self.send_header("Content-Type", "text/plain")
                self.send_header("Content-Length", str(len(msg)))
                self.end_headers()
                self.wfile.write(msg)
            return
        super().do_GET()

    def log_message(self, fmt, *args):
        pass  # suppress access logs


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8090))
    with http.server.ThreadingHTTPServer(("", port), Handler) as httpd:
        print(f"CLM Swagger Hub listening on :{port}", flush=True)
        httpd.serve_forever()
