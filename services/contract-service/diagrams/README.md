# Contract Service Diagrams

All diagrams are authored in [Mermaid](https://mermaid.js.org/) (`.mmd` files).

## How to Render

### Option A — Mermaid CLI
```bash
# Install once
npm install -g @mermaid-js/mermaid-cli

# Render all diagrams to PNG
for f in *.mmd; do
  mmdc -i "$f" -o "${f%.mmd}.png" -t neutral -b white
done

# Render all to SVG
for f in *.mmd; do
  mmdc -i "$f" -o "${f%.mmd}.svg" -t neutral -b white
done
```

### Option B — VS Code
Install the [Mermaid Preview](https://marketplace.visualstudio.com/items?itemName=bierner.markdown-mermaid) extension and open any `.mmd` file.

### Option C — Mermaid Live Editor
Paste the file contents at https://mermaid.live

---

## Diagram Index

| File | Title | Type |
|---|---|---|
| `er_diagram.mmd` | Full entity-relationship diagram | ER |
| `db_inheritance.mmd` | JOINED inheritance table layout | Flowchart |
| `download_strategy_pattern.mmd` | DownloadService strategy pattern | Class diagram |
| `template_upload_pipeline.mmd` | Template upload processing pipeline | Flowchart |
| `contract_generation_pipeline.mmd` | Contract generation processing pipeline | Flowchart |
| `contract_lifecycle.mmd` | Contract status state machine | State diagram |
| `docx_fill_algorithm.mmd` | DocxFiller merge-substitute-writeback algorithm | Flowchart |
| `audit_trail_overview.mmd` | Audit trail mechanisms and events | Flowchart |
