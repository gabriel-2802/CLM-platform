# Contract Service — Technical Documentation

> **Platform:** CLM (Contract Lifecycle Management)  
> **Runtime:** Java 21 · Spring Boot 4.0.5 · PostgreSQL (`clm` schema)  
> **Build tool:** Maven  
> **Document version:** 2026-05-18

---

## Table of Contents

1. [Functional Description](#1-functional-description)
2. [Service Overview](#2-service-overview)
3. [Architecture & Module Structure](#3-architecture--module-structure)
4. [Database Schema & Strategy](#4-database-schema--strategy)
   - 4.1 [Schema Isolation](#41-schema-isolation)
   - 4.2 [JOINED Inheritance — The Core Design Decision](#42-joined-inheritance--the-core-design-decision)
   - 4.3 [Entity Reference](#43-entity-reference)
   - 4.4 [Flyway Migration Strategy](#44-flyway-migration-strategy)
   - 4.5 [Indexes & Search Optimization](#45-indexes--search-optimization)
   - 4.6 [Database Triggers & Automation](#46-database-triggers--automation)
5. [Document Processing Pipeline](#5-document-processing-pipeline)
   - 5.1 [Template Upload Pipeline](#51-template-upload-pipeline)
   - 5.2 [Contract Generation Pipeline](#52-contract-generation-pipeline)
   - 5.3 [Appendix Flows](#53-appendix-flows)
6. [Parsing System — In-Depth](#6-parsing-system--in-depth)
   - 6.1 [The Placeholder Contract](#61-the-placeholder-contract)
   - 6.2 [FileParser — Extracting Structure from Documents](#62-fileparser--extracting-structure-from-documents)
   - 6.3 [PlaceholderProcessor — The Regex Engine](#63-placeholderprocessor--the-regex-engine)
   - 6.4 [DocxNormalizer — Upload-Time Canonicalization](#64-docxnormalizer--upload-time-canonicalization)
   - 6.5 [DocxFiller — Contract Generation with Format Preservation](#65-docxfiller--contract-generation-with-format-preservation)
   - 6.6 [DocxUtils — Canonical Traversal & Delta Writeback](#66-docxutils--canonical-traversal--delta-writeback)
   - 6.7 [FileUtils — Compression and Format Conversion](#67-fileutils--compression-and-format-conversion)
   - 6.8 [Parsing Challenges & Solutions](#68-parsing-challenges--solutions)
7. [DownloadService — Strategy Pattern](#7-downloadservice--strategy-pattern)
   - 7.1 [Motivation](#71-motivation)
   - 7.2 [Component Design](#72-component-design)
   - 7.3 [Provider Implementations](#73-provider-implementations)
   - 7.4 [Request Flow](#74-request-flow)
   - 7.5 [Extensibility](#75-extensibility)
8. [Audit Trails](#8-audit-trails)
   - 8.1 [Contract Lifecycle Audit Fields](#81-contract-lifecycle-audit-fields)
   - 8.2 [DocumentFieldValue — Immutable Field Audit Log](#82-documentfieldvalue--immutable-field-audit-log)
   - 8.3 [Audit Events Summary](#83-audit-events-summary)
9. [Search & Specification Pattern](#9-search--specification-pattern)
10. [Security](#10-security)
11. [Scheduled Jobs](#11-scheduled-jobs)
12. [REST API Surface](#12-rest-api-surface)
13. [Exception Handling](#13-exception-handling)
14. [Caching — Caffeine L2 Cache](#14-caching--caffeine-l2-cache)
    - 14.1 [Motivation](#141-motivation)
    - 14.2 [Cache Regions](#142-cache-regions)
    - 14.3 [Eviction Rules](#143-eviction-rules)
    - 14.4 [Configuration](#144-configuration)
    - 14.5 [Metrics & Observability](#145-metrics--observability)
    - 14.6 [Testing Strategy](#146-testing-strategy)
    - 14.7 [Test Results](#147-test-results)
15. [Configuration Reference](#15-configuration-reference)

---

## 1. Functional Description

### What the service does

The contract-service is the document management core of the CLM (Contract Lifecycle Management) platform. Its purpose is to take legal document templates authored by users — Word files with blank fields marked by sequences of dots — and transform them into fully filled, traceable, versioned contracts and appendices that are stored, signed, and managed through their entire active life.

At the most fundamental level, the service answers three questions for any document in the system: what it contains, who acted on it, and when. Every byte of every generated PDF is stored and retrievable. Every value that was ever injected into a contract field is recorded permanently and is searchable. Every lifecycle transition — generation, signing, termination — is timestamped and tied to the user who triggered it.

### Template management

The first thing a user does before creating any contract is upload a document template. A template is a DOCX or PDF file that contains the full legal text of the agreement, with blank fields represented as sequences of four or more consecutive dots (`....`). These dot sequences are the service's convention for marking where variable data should be inserted.

When a template is uploaded, the service does not simply store the file. It reads through the entire document — body text, tables, headers, and footers — and counts every placeholder it finds. Each placeholder is assigned a zero-based position index that reflects where it appears in the document's reading order. These positions become `TemplateField` records in the database, one per placeholder. At this point the fields have positions but no labels: the user has not yet told the service what each field means.

The second step is field labelling. The user assigns a human-readable label to each field — for example, `clientName`, `contractValue`, `startDate` — and optionally specifies a data type and whether the field is mandatory. The service uses a database trigger to automatically mark the template as fully mapped the moment the last required field receives a label. A template that is not fully mapped cannot be used to generate a contract; this constraint is enforced at the service layer before any document processing begins.

The service always stores templates as DOCX internally, regardless of whether the user uploaded a DOCX or a PDF. PDF uploads are converted to DOCX first using LibreOffice. The stored DOCX is also normalized at upload time: all placeholder dot sequences are rewritten to exactly four dots (`....`), regardless of how many the user originally typed. This canonical form is what makes reliable, position-consistent placeholder filling possible at generation time.

### Contract generation

Once a template is fully mapped, a user can generate a contract from it. The request supplies the template identifier, a client identifier, contract dates and financial values, and a map of field labels to the values that should be substituted into the document.

The service validates the request before touching any document: it checks that the template is fully mapped, and that every field marked as required has been given a value. If either check fails, the request is rejected immediately with a descriptive error.

If validation passes, the service fills the template. It retrieves the stored normalized DOCX, sorts the template fields by their position index, and walks through every paragraph in the document in the same canonical order that was used during upload. For each paragraph, it merges all Word run elements into a single string, applies the placeholder regex to find any dot sequences, and substitutes each one with the value mapped to that field's label. The substitution is written back into the individual Word runs in a way that preserves all per-run formatting — bold text stays bold, italic stays italic, colours remain unchanged — even when the replacement value is longer or shorter than the four dots it replaced.

After filling, the DOCX is converted to PDF using LibreOffice. The resulting PDF is compressed with GZIP and stored as the contract's unsigned document content. The contract is created in `PENDING_SIGNATURE` status, meaning it is waiting for the client to sign and return it.

Every field value that was injected into the document is also stored as an immutable `DocumentFieldValue` record. This creates a permanent, searchable log of exactly what was written into each field at the moment of generation.

### Signing and activation

When the signed document is received from the client, a user uploads it through the service. The service accepts either a DOCX or PDF and always stores the signed version as PDF, converting if necessary. Once the signed document is attached, the contract transitions to `ACTIVE` status. The timestamp and user ID of the person who performed the upload are recorded as part of the contract's audit trail.

### Appendices

Contracts can have appendices: supplementary documents that extend or modify the terms of the parent contract. The service supports two kinds of appendices. A generated appendix works exactly like a contract — it is produced by filling a template with values, rendered to PDF, and starts in `DRAFT` status until a signed version is uploaded. A direct appendix is a file that the user uploads as-is, with no template and no field filling. It goes directly to `SIGNED` status because the uploaded file is treated as the authoritative final document.

Appendices are permanently linked to their parent contract. If a contract is deleted, all its appendices are deleted with it.

### Contract lifecycle and archival

An active contract can be terminated. The user provides a termination date, a reason, and their user ID. The service validates that the contract is actually in `ACTIVE` status before applying the transition — attempting to terminate a contract that is already terminated or pending signature returns an error.

Contracts can also be set to auto-renew. This flag does not trigger any automatic action within the contract-service itself, but it is respected by the nightly archival job. Every night at midnight, the service queries for all active contracts whose end date has passed. Those with `auto_renew = false` are moved to `ARCHIVED` status in a single bulk database update. Contracts with `auto_renew = true` are left in `ACTIVE` status and are expected to be renewed through a separate business process.

### Document download

Any document managed by the service — template, unsigned contract, signed contract, unsigned appendix, signed appendix — can be downloaded in either DOCX or PDF format, subject to the rules of that document type. Generated contracts and appendices are stored as PDF; downloading them as DOCX produces a text-extracted version. Signed documents are always stored and served as PDF only. Templates are stored as DOCX and can be served in either format.

The download system uses a strategy pattern internally, with one provider class per document type. Each provider knows where to find its document and what formats are supported. The orchestrating service knows nothing about entity types and requires no changes when new document types are added.

### Search

Contracts can be searched using a combination of filters. Users can filter by status, client, creator, date range, free-text notes, and by the values that were injected into the document fields. The field-value search is particularly powerful: supplying multiple terms returns only contracts where all of the terms appear somewhere in the injected values, regardless of which field they came from. This allows users to find, for example, all contracts related to a specific company name or address, even if that information was spread across several fields.

### Reporting

The service exposes two reporting endpoints consumed by the notification microservice. One returns all active contracts whose end date falls within a configurable number of days from today, used to send expiry alerts. The other identifies active contracts where the client has not renegotiated their contract value within a configurable number of months, used to trigger renegotiation reminders.

---

## 2. Service Overview

The **contract-service** is the document backbone of the CLM platform. It is responsible for the full lifecycle of legally significant documents:

| Responsibility | Description |
|---|---|
| Template management | Upload, normalize, and store DOCX/PDF templates with extracted placeholder positions |
| Contract generation | Fill template placeholders with user-supplied values, render to PDF, persist with full audit trail |
| Appendix management | Attach supplementary documents to contracts, either generated from templates or uploaded directly |
| Document download | Serve unsigned and signed versions of any document in DOCX or PDF format |
| Contract lifecycle | Manage status transitions: PENDING_SIGNATURE → ACTIVE → TERMINATED / ARCHIVED |
| Reporting | Expose expiry and inactivity queries consumed by the notification microservice |

The service is a **standalone Spring Boot application** accessed by the frontend via an nginx reverse proxy and by internal microservices over HTTP. It exposes no business logic to the database tier — all mutation happens through JPA transactions.

---

## 3. Architecture & Module Structure

```
contract-service/
├── controllers/          REST entry points — input validation, HTTP response shaping
├── services/             Orchestration — business rules, transaction boundaries
│   ├── download/         DownloadService + Strategy pattern (providers)
│   └── utility/          Shared generation helpers (DocumentGenerationUtil)
├── repositories/         Spring Data JPA interfaces — SQL stays in JPQL/Criteria
├── models/               JPA entities — the domain model
│   └── enums/            Typed enumerations mapped to PostgreSQL ENUM types
├── dto/
│   ├── requests/         Input shapes validated with Bean Validation
│   └── responses/        Output shapes mapped with MapStruct
├── mappers/              MapStruct interfaces — entity ↔ DTO, zero reflection overhead
├── specifications/       JPA Criteria API — dynamic WHERE clause assembly
├── utils/
│   ├── docx/             DOCX-specific utilities (normalizer, filler, traversal)
│   └── file/             Format-agnostic utilities (parser, placeholder processor, file utils)
├── security/             JWT filter chain
├── config/               Spring configuration beans
├── exceptions/           Custom exception types + centralized handler
└── jobs/                 Scheduled tasks
```

**Dependency flow:** Controllers → Services → Repositories / Utils → Models. Nothing flows upward. Utilities are `@UtilityClass` (static-only) or `@Component` depending on whether they need Spring-managed configuration (e.g., `FileUtils` needs `libreoffice.path` from properties).

---

## 4. Database Schema & Strategy

### 3.1 Schema Isolation

All tables live in the dedicated `clm` PostgreSQL schema (not `public`). This provides:

- **Namespace isolation** — no risk of collisions with other microservices sharing the same database cluster
- **Permission boundary** — the service user can be granted access to `clm` only
- **Clean DROP/RECREATE semantics** — `DROP SCHEMA clm CASCADE` wipes only contract data

The schema is created as the very first statement in `V1__init_schema.sql` and all subsequent objects are qualified with `clm.`.

---

### 3.2 JOINED Inheritance — The Core Design Decision

Contracts and appendices share a large common footprint (binary content, template reference, creator information, field values, timestamps). Rather than duplicating these columns or collapsing everything into one table, the schema uses **JPA JOINED inheritance**:

```
document (base table — shared columns)
├── contract  (document_id PK/FK → document.id)
└── appendix  (document_id PK/FK → document.id)
```

**Why JOINED and not SINGLE_TABLE or TABLE_PER_CLASS?**

| Strategy | Pros | Cons |
|---|---|---|
| SINGLE_TABLE | One join to read any document | Massive NULL columns; discriminator required for all constraints |
| TABLE_PER_CLASS | Self-contained tables | UNION queries for polymorphic queries; no shared FK targets |
| **JOINED** (chosen) | Normalized; FK from `document_field_value` hits one table regardless of subtype | One extra join per subclass fetch |

The JOINED strategy was chosen because `document_field_value` needs to reference the parent entity in a single, strongly-typed FK (`document_id → document.id`). With TABLE_PER_CLASS this would require a polymorphic FK to multiple tables, which standard relational databases do not support. With SINGLE_TABLE the constraint model becomes fragile.

A `document_type` VARCHAR(31) discriminator column distinguishes `CONTRACT` from `APPENDIX` rows in the base table and is indexed for filter-only queries.

```
┌────────────────────────────────────────────┐
│               clm.document                 │
│  id (PK)  document_type  template_id  ...  │
└──────────────┬──────────────┬──────────────┘
               │              │
    ┌──────────▼───┐   ┌──────▼──────────┐
    │ clm.contract │   │  clm.appendix   │
    │ document_id  │   │  document_id    │
    │ client_id    │   │  contract_id FK │
    │ status ...   │   │  title, status  │
    └──────────────┘   └─────────────────┘
```

---

### 3.3 Entity Reference

#### `document_template` — Blueprint Storage

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `template_name` | VARCHAR(255) UNIQUE | User-facing identifier |
| `description` | VARCHAR(500) | |
| `document_format` | ENUM | Always `DOCX` after upload normalization |
| `document_content` | BYTEA | GZIP-compressed DOCX bytes |
| `field_count` | INTEGER | Count of extracted `....` sequences |
| `is_fully_mapped` | BOOLEAN | Auto-maintained by DB trigger |
| `created_at`, `updated_at` | TIMESTAMP | |

#### `template_field` — Placeholder Metadata

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `template_id` | BIGINT FK | CASCADE DELETE |
| `field_label` | VARCHAR(255) NULL | NULL until user maps it |
| `data_type` | ENUM | STRING / DATE / NUMBER / BOOLEAN / CURRENCY |
| `field_position` | INTEGER | 0-based occurrence index in document |
| `is_required` | BOOLEAN | Blocks contract generation if value missing |
| `format_pattern` | VARCHAR(255) | Validation / transformation hint |

#### `document` — Base Table (JOINED Inheritance)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `document_type` | VARCHAR(31) | `CONTRACT` or `APPENDIX` (discriminator) |
| `template_id` | BIGINT FK NULL | NULL for direct-upload documents |
| `document_format` | ENUM | PDF / DOCX |
| `document_content` | BYTEA | GZIP-compressed unsigned document |
| `signed_document_content` | BYTEA | GZIP-compressed signed PDF |
| `generated_by` | INTEGER | Legacy user ID field |
| `generated_by_mail` | VARCHAR(255) | |
| `notes` | VARCHAR(1000) | Full-text searchable |
| `created_at` | TIMESTAMP | Immutable, set on INSERT |

#### `contract` — Subclass Table

| Column | Type | Notes |
|---|---|---|
| `document_id` | BIGINT PK/FK | Joins to `document.id` |
| `client_id` | INTEGER NOT NULL | External client reference |
| `contract_status` | ENUM | PENDING_SIGNATURE → ACTIVE → TERMINATED / ARCHIVED |
| `contract_value` | NUMERIC(12,2) | |
| `contract_balance` | NUMERIC(12,2) | Tracked separately from value |
| `auto_renew` | BOOLEAN | Default FALSE |
| `contract_start_date` | DATE | |
| `contract_end_date` | DATE | Used for archival job threshold |
| `termination_date` | DATE | |
| `reasons_for_termination` | VARCHAR(1000) | |
| `generated_at` | TIMESTAMP | **Audit** — set by `@PrePersist` |
| `generated_by_user_id` | INTEGER | **Audit** |
| `terminated_at` | TIMESTAMP | **Audit** |
| `terminated_by_user_id` | INTEGER | **Audit** |
| `uploaded_signed_at` | TIMESTAMP | **Audit** |
| `uploaded_signed_by_user_id` | INTEGER | **Audit** |

#### `appendix` — Subclass Table

| Column | Type | Notes |
|---|---|---|
| `document_id` | BIGINT PK/FK | Joins to `document.id` |
| `contract_id` | BIGINT FK | Links appendix to parent contract; CASCADE DELETE |
| `title` | VARCHAR(255) | |
| `appendix_status` | ENUM | DRAFT → SIGNED |

#### `document_field_value` — Immutable Audit Log

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `document_id` | BIGINT FK | CASCADE DELETE when document is removed |
| `template_field_id` | BIGINT FK | RESTRICT — cannot delete a field while values reference it |
| `field_value` | TEXT | The raw injected string |
| `created_at` | TIMESTAMP | Immutable — never updated |

---

### 3.4 Flyway Migration Strategy

Schema evolution is managed exclusively by **Flyway**. JPA is configured with `ddl-auto=none`, meaning Hibernate never touches the schema DDL. This prevents accidental schema drift in production.

| Migration | Change |
|---|---|
| `V1__init_schema.sql` | Full baseline: all 6 tables, all enums, indexes, trigger, search functions, pg_trgm extension |
| `V2__add_auto_renewal_field.sql` | `auto_renew BOOLEAN` column on `contract` |
| `V3__add_contract_balance.sql` | `contract_balance NUMERIC(12,2)` column on `contract` |
| `V4__add_audit_fields.sql` | Six audit columns on `contract` + indexes on audit timestamps |

Each migration is **additive** — no destructive changes. The V4 audit fields were added as a separate migration after the baseline, reflecting the evolutionary nature of the audit trail requirement. The fact that they exist as a discrete migration (`V4`) shows they were a considered addition rather than a retrofitted afterthought.

---

### 3.5 Indexes & Search Optimization

The index strategy is tiered by query type:

**B-tree indexes** (equality, range, sort):
- `idx_contract_client` — `client_id` (equality filter)
- `idx_contract_status` — `contract_status` (equality filter)
- `idx_contract_validity` — `(contract_start_date, contract_end_date)` (range queries for expiry)
- `idx_contract_termination` — `termination_date DESC WHERE NOT NULL` (partial index — zero overhead for active contracts)
- `idx_contract_active` — partial index `WHERE contract_status = 'ACTIVE'` (the archival job scans only this)
- `idx_document_created_at` — `created_at DESC` (default sort order)

**GIN trigram indexes** (substring text search via `pg_trgm`):
- `idx_dt_name_lower_trgm` — `LOWER(template_name)` on `document_template`
- `idx_dt_desc_lower_trgm` — `LOWER(description)` on `document_template`
- `idx_doc_notes_lower_trgm` — `LOWER(notes)` on `document`
- `idx_dfv_value_lower_trgm` — `LOWER(field_value)` on `document_field_value`

The trigram extension (`pg_trgm`) breaks text into 3-character n-grams and stores them in a GIN (Generalized Inverted Index). This means `LIKE '%someterm%'` queries can use an index rather than doing a full sequential scan. This is critical for the label-value search path which performs correlated subqueries per search term across potentially large field value tables.

---

### 3.6 Database Triggers & Automation

#### Auto-mapping Trigger (`trg_template_field_label_mapped`)

```sql
AFTER INSERT OR UPDATE OF field_label ON clm.template_field
```

When any `template_field` row has its `field_label` set (non-null), the trigger function `fn_check_template_fully_mapped()` fires. It counts how many required fields in the parent template still have a NULL label. If the count is zero, it sets `document_template.is_fully_mapped = TRUE`.

This trigger eliminates a class of application-layer bugs: the Java service does not need to check mapping completeness after every label update — the database maintains it atomically. The trigger also handles edge cases like partial updates (setting some but not all labels) correctly.

```sql
IF v_unmapped_required = 0 THEN
    UPDATE clm.document_template
    SET    is_fully_mapped = TRUE, updated_at = NOW()
    WHERE  id = v_template_id AND is_fully_mapped = FALSE;
ELSE
    UPDATE clm.document_template
    SET    is_fully_mapped = FALSE, updated_at = NOW()
    WHERE  id = v_template_id AND is_fully_mapped = TRUE;
END IF;
```

Note the conditional `WHERE is_fully_mapped = FALSE/TRUE` — this prevents unnecessary writes when the state has not changed. The Java service also performs a final `updateMappingStatus()` check after saving field labels in batch (a belt-and-suspenders approach for correctness, because the trigger fires per-row while the Java method checks the aggregate view after all rows are saved).

---

## 5. Document Processing Pipeline

### 4.1 Template Upload Pipeline

```
User uploads file (DOCX or PDF)
        │
        ▼
Utils.detectDocumentFormat(bytes)        ← magic bytes: %PDF or PK\x03\x04
        │
        ▼
FileParser.parseTemplate(file, format)
    │   ├─ PDF: PDFTextStripper extracts raw text
    │   └─ DOCX: forEachParagraph → paragraph.getText() → newline-joined
    │   └─ PlaceholderProcessor.normalize(raw)
    │   └─ PlaceholderProcessor.findPlaceholders(normalized) → count
        │
        ▼ [if PDF uploaded]
FileUtils.convert(bytes, PDF, DOCX)      ← LibreOffice headless
        │
        ▼
DocxNormalizer.normalizePlaceholdersInDocx(docxBytes)
    │   └─ forEachParagraph → merge runs → substituteEachWithSpans → writebackSpans
    │      (every dot-sequence → exactly "...."; preserves per-run formatting)
        │
        ▼
FileUtils.compress(docxBytes)            ← GZIP
        │
        ▼
templateRepository.save(DocumentTemplate{
    documentContent = compressed DOCX,
    documentFormat  = DOCX,             ← always normalized to DOCX
    fieldCount      = N
})
        │
        ▼
For each placeholder position 0..N-1:
    templateFieldRepository.save(TemplateField{
        fieldPosition = i,
        fieldLabel    = null,           ← user maps labels separately
        isRequired    = true
    })
        │
        ▼
Return TemplateUploadResponseDTO{
    templateId,
    templateName,
    documentText with "{{fieldId}}" markers  ← shows user where each field is
}
```

The template is **always stored as DOCX** regardless of the upload format. PDF uploads are converted first. This ensures that at generation time, `DocxFiller` always receives a known, normalized DOCX — never raw PDF bytes.

---

### 4.2 Contract Generation Pipeline

```
POST /api/contracts/generate (GenContractRequest)
        │
        ▼
templateRepository.findById(request.templateId)
        │
        ▼
Check: template.isFullyMapped == true?        ← TemplateIncompleteException if not
        │
        ▼
documentGenerationUtil.validateMandatoryFields(template, mappings)
    └─ For each required TemplateField: ensure mappings contains a non-blank value
                                                ← MissingMandatoryFieldException if not
        │
        ▼
generationMapper.toContractEntity(request, template)
    └─ Sets: clientId, dates, notes, generatedByMail, generatedBy,
             contractStatus = PENDING_SIGNATURE (default)
        │
        ▼
contractRepository.save(contract)             ← persists base + subclass rows
        │
        ▼
documentGenerationUtil.buildFieldValues(contract, template, mappings)
    └─ For each TemplateField: create DocumentFieldValue{
           document       = contract,
           templateField  = field,
           fieldValue     = mappings.get(field.fieldLabel)
       }
        │
        ▼
fieldValueRepository.saveAll(fieldValues)     ← audit log written here
        │
        ▼
fillAndPersistDocument(contract, template, fieldValues):
    │
    ├─ Sort fields by fieldPosition
    ├─ buildLabelValueMap(fieldValues)         ← Map<label, value>
    ├─ FileUtils.decompress(template.documentContent)
    ├─ DocxFiller.fillDocx(templateBytes, orderedFields, labelToValue)
    │       └─ [see §5.5 for full algorithm]
    ├─ FileUtils.convert(filled, DOCX, PDF)    ← LibreOffice headless
    ├─ FileUtils.compress(pdfBytes)
    └─ contract.documentContent = compressed PDF
        │
        ▼
contractRepository.save(contract)             ← update with document content
        │
        ▼
Return ContractResponseDTO
```

The contract's `documentContent` stores the **generated, field-filled PDF** — the unsigned version. The `signedDocumentContent` field starts NULL and is populated only when the client uploads their signed copy.

---

### 4.3 Appendix Flows

Appendices have two distinct creation paths:

**Path A — Template-generated (DRAFT)**
- Same pipeline as contract generation
- Created in `AppendixStatus.DRAFT`
- Awaits `uploadSignedAppendix()` to transition to `SIGNED`

**Path B — Direct upload (immediately SIGNED)**
- No template, no field values, no placeholder filling
- Raw bytes stored as `signedDocumentContent`
- `appendixStatus = SIGNED` immediately — the uploaded file is the final document
- `documentTemplate` is NULL (supported by the nullable FK on `document.template_id`)

---

## 6. Parsing System — In-Depth

### 5.1 The Placeholder Contract

A **placeholder** is defined as any sequence of 4 or more consecutive ASCII dots. This pattern (`....`) serves as a fill-in-the-blank marker in Word documents that legal teams author manually.

The canonical regex is:

```java
Pattern.compile("\\.{4,}(?:[ \\t]*\\.+)*+")
```

This matches:
- `....` — minimum (4 dots)
- `..........` — long sequences
- `.... ....` — dot groups separated by horizontal whitespace (some Word editors insert spaces between runs of dots)

The possessive quantifier (`*+`) prevents catastrophic backtracking on pathological inputs.

At **upload time**, every placeholder is normalized to exactly `....` (4 dots). At **generation time**, the regex still matches `....` specifically. Having a canonical form eliminates all edge cases involving variable-length sequences and removes the need to count exact dot positions during filling.

---

### 5.2 FileParser — Extracting Structure from Documents

`FileParser.parseTemplate()` is the entry point for template analysis. It:

1. **Validates** the uploaded file: not null, not empty, ≤ 50 MB, valid filename
2. **Extracts raw text** depending on format:
   - **PDF**: `PDFBox PDFTextStripper.getText(document)` — linear text extraction
   - **DOCX**: `forEachParagraph(doc, p → sb.append(p.getText()).append("\n"))` — preserving paragraph boundaries as newlines
3. **Normalizes** the text via `PlaceholderProcessor.normalize()`
4. **Counts** placeholders via `PlaceholderProcessor.findPlaceholders()`

Returns a `ParsedDocument(documentText, placeholderCount)` record. The `documentText` is shown back to the user with `{{fieldId}}` markers substituted in place of dot sequences — this is how the frontend knows which field corresponds to which position in the document without needing to re-parse it.

**DOCX traversal detail**: `FileParser.extractDocx` uses `DocxUtils.forEachParagraph` — the same traversal used by `DocxNormalizer` and `DocxFiller`. This is critical: if any of the three used a different traversal order, the placeholder positions (0, 1, 2, ...) would not agree, and the wrong value would be injected into the wrong field at generation time.

---

### 5.3 PlaceholderProcessor — The Regex Engine

`PlaceholderProcessor` is a `@UtilityClass` (no state, all static methods). It provides four distinct operations:

#### `normalize(String raw)`
Converts raw document text to a consistent baseline:
- CRLF / CR → `\n`
- Unicode dot-like characters to their ASCII equivalents:
  - `U+2026 …` (HORIZONTAL ELLIPSIS) → `...` (3 dots)
  - `U+22EF ⋯` (MIDLINE ELLIPSIS) → `...` (3 dots)
  - `U+2025 ‥` (TWO DOT LEADER) → `..` (2 dots)
  - `U+FF0E ．` (FULLWIDTH FULL STOP) → `.` (1 dot)

This step is called **only at upload time** (in `FileParser` and `DocxNormalizer`). Downstream code operates on already-normalized content.

#### `findPlaceholders(String normalizedContent)`
Runs the compiled `PLACEHOLDER_PATTERN` regex and returns `List<PlaceholderRecord>` — each with its `occurrenceIndex`, `prevText` (the matched dots), and `[startOffset, endOffset)` positions in the normalized text. Used for reporting and counting.

#### `substituteEach(String content, IntFunction<String> resolver)`
Iterates all placeholder matches. For each, calls `resolver(occurrenceIndex)`. If the resolver returns non-null, replaces the placeholder; otherwise keeps the original dots. Returns `SubstitutionResult(text, filledCount)`.

Used by `TemplateService.replaceWithFieldIds()` to produce the `documentText` shown to the user after upload (each `....` is replaced by `{{fieldId}}`).

#### `substituteEachWithSpans(String content, IntFunction<String> resolver)`
The advanced variant. Identical to `substituteEach` but additionally records, for each match:
- `originalStart` / `originalEnd` — where the placeholder was in the **original** string
- `replacementLen` — length of the replacement in the **rewritten** string
- `replaced` — whether a substitution actually occurred

This span data drives the proportional run-writeback in `DocxUtils.writebackSpans`. It is the key mechanism that allows replacement text (which may be longer or shorter than `....`) to be correctly distributed back into Word's `<w:r>` XML run elements without corrupting formatting.

---

### 5.4 DocxNormalizer — Upload-Time Canonicalization

`DocxNormalizer.normalizePlaceholdersInDocx()` rewrites every dot-sequence in a DOCX file to exactly `....`. This runs **once per template upload** and never again.

**Why is this necessary?**

Word documents represent text as a sequence of `<w:r>` (run) XML elements. Each run carries formatting attributes (font, bold, italic, color). A user might type 10 dots in one paragraph, but Word might split them into multiple runs for its own reasons — e.g., `<run>.....</run><run>.....</run>` where the split happens mid-sequence. The individual run texts are valid but the split position is unpredictable.

Without normalization, the `PLACEHOLDER_PATTERN` would still match across runs (because `DocxFiller` merges run texts before regex matching), but the length of the resulting `....` would differ from the length of the original dot sequence. The span-delta arithmetic during writeback would then produce incorrect offsets, potentially writing replacement text into the wrong run.

Normalizing to exactly `....` (4 bytes) at upload time means:
- `replacementLen - 4` is the delta applied during writeback (positive for longer values, negative for shorter)
- All calculations are uniform regardless of the original dot count

**Algorithm (same as DocxFiller):**
1. For each paragraph, merge all run texts (applying `normalize()` for unicode and CRLF cleanup)
2. Call `substituteEachWithSpans(merged, i → Constants.PLACEHOLDER)` — every placeholder becomes `....`
3. Call `writebackSpans(runs, runStarts, result.text(), result.spans())` to distribute back

The normalization is **idempotent**: running it twice produces the same result, since `....` → `....` is a no-op.

---

### 5.5 DocxFiller — Contract Generation with Format Preservation

`DocxFiller.fillDocx()` is the core document generation algorithm. It takes normalized DOCX bytes, a sorted list of `TemplateField` objects, and a `Map<label, value>`, and returns filled DOCX bytes.

**Pre-conditions:**
- The DOCX was normalized via `DocxNormalizer` at upload time
- `ordered` is sorted ascending by `fieldPosition` (positions are 0-based occurrence indices)
- `labelToValue` maps each field's label to the user-supplied value

**Algorithm — three phases:**

```
Phase 1: MERGE
─────────────
For each paragraph (in canonical traversal order):
  - Collect all XWPFRun objects
  - Record the start offset of each run in the merged buffer (runStarts[i])
  - Concatenate getText(0) of each run into a single StringBuilder
  - Result: one flat string + an offset array mapping back to individual runs

Phase 2: SUBSTITUTE (with spans)
────────────────────────────────
Call PlaceholderProcessor.substituteEachWithSpans(merged, resolver)
  where resolver(localIndex) = {
      absIndex = globalIndex + localIndex;
      label    = ordered.get(absIndex).getFieldLabel();
      return   labelToValue.get(label);   // null if no value → keep dots
  }
Returns: rewritten string + List<SubstitutionSpan>

Phase 3: WRITEBACK
──────────────────
If no substitutions occurred → skip (no DOM mutation)
Else: DocxUtils.writebackSpans(runs, runStarts, rewritten, spans)
      → each run receives its slice of the rewritten string
      → run formatting (bold, italic, color...) is preserved because
         setText(text, 0) replaces only the text content, not the XML attributes

globalIndex.addAndGet(result.filledCount())
```

The `globalIndex` is an `AtomicInteger` shared across all paragraphs for thread safety within the lambda (it is passed to a `Consumer<XWPFParagraph>` that executes on the single calling thread, but Java lambdas capturing mutable state require effectively-final variables — hence the `AtomicInteger` instead of a plain `int[]`).

**Why positional (not label-based) matching?**

Placeholders in a Word document have no inherent labels — they are just dots. The label-to-placeholder binding is established by the user in the "field mapping" step after upload. Position (0-based occurrence index) is the only stable property of a placeholder. The `TemplateField.fieldPosition` captures this at upload time; the sorted `ordered` list recreates the document order at generation time.

---

### 5.6 DocxUtils — Canonical Traversal & Delta Writeback

`DocxUtils` is the single source of truth for two critical operations:

#### `forEachParagraph(XWPFDocument doc, Consumer<XWPFParagraph> consumer)`

Visits paragraphs in this fixed order:
1. Body paragraphs (`doc.getParagraphs()`)
2. Table cells (nested loop: tables → rows → cells → paragraphs)
3. Headers (`doc.getHeaderList()` → header.getParagraphs())
4. Footers (`doc.getFooterList()` → footer.getParagraphs())

This order is used by `FileParser`, `DocxNormalizer`, and `DocxFiller` — all three must see paragraphs in the same order. Adding a new zone (e.g., footnotes) requires one change here, not three.

#### `writebackSpans(List<XWPFRun>, int[] runStarts, String rewritten, List<SubstitutionSpan>)`

The delta-writeback algorithm distributes the rewritten string back into runs:

```
origPos = 0, rwPos = 0, spanIdx = 0

For each run r:
    origRunEnd = runStarts[r + 1]
    rwStart = rwPos

    Walk origPos forward to origRunEnd:
        if origPos == span[spanIdx].originalStart:
            // This run contains (or starts) a substituted placeholder
            origPos = span.originalEnd        // skip original dots
            rwPos  += span.replacementLen     // advance past replacement text
            spanIdx++
        else:
            origPos++; rwPos++               // passthrough character

    runs[r].setText(rewritten[rwStart..rwPos], 0)
```

The key insight: the run that contains the **start** of a placeholder gets the **full** replacement text. Runs that contained only the **tail** of the placeholder (in cases where dots spanned multiple runs) get empty strings. The total text is still correct because the replacement was written into the first involved run. Formatting is preserved because `setText` only changes text content — bold, italic, color, font, etc. remain on their respective `<w:rPr>` XML elements.

---

### 5.7 FileUtils — Compression and Format Conversion

#### GZIP Compression

All document binary content is stored in the database as GZIP-compressed bytes. The compression uses 64 KB buffers (`BUFFER_SIZE = 65536`):

- `compress(byte[])` → `GZIPOutputStream` → compressed bytes
- `decompress(byte[])` → `GZIPInputStream` → original bytes

DOCX files (ZIP-based internally) typically compress 10–30% further with GZIP. PDFs (already compressed internally) compress less but the overhead of uniform GZIP wrapping is preferable to detecting whether the content is already compressed.

#### Format Conversion

`FileUtils.convert(data, sourceFormat, targetFormat)` dispatches to:

**DOCX → PDF**: LibreOffice headless mode
```
1. Create temp directory
2. Write input bytes to temp/input.docx
3. Execute: libreoffice --headless --convert-to pdf --outdir tempDir tempDir/input.docx
4. Read tempDir/input.pdf
5. Delete temp directory (cleanup always runs in finally block)
```
LibreOffice is the most widely deployed open-source DOCX-to-PDF converter with near-complete fidelity for complex documents including fonts, tables, embedded images, and headers/footers. The configurable path (`libreoffice.path`) allows different deployments to use system LibreOffice, a Docker-installed version, or a local path.

**PDF → DOCX**: Text-only fallback (PDFBox + POI)
```
1. PDFBox.PDFTextStripper.getText() → raw text
2. Split by newline → paragraphs
3. Create XWPFDocument → one paragraph + one run per non-empty line
4. Return DOCX bytes
```
This conversion is lossy — it extracts text only, dropping all visual formatting. It exists to allow templates uploaded as PDF to be normalized and stored as DOCX. Generating contracts from PDF templates (after conversion) works correctly because the placeholder positions are captured from the PDF's text layer before conversion.

---

### 5.8 Parsing Challenges & Solutions

#### Challenge 1: Cross-Run Placeholder Fragmentation

**Problem**: Word's XML model splits a paragraph's text into multiple `<w:r>` runs at arbitrary boundaries. A user types `..........` but Word might serialize it as `<run>...</run><run>...</run><run>....</run>`. A naïve regex match on individual run texts would miss this sequence entirely.

**Solution**: The merge-substitute-writeback pattern. All run texts are concatenated before regex matching. The regex operates on the flat merged string. Span offsets in the merged string are then used to correctly distribute the rewritten text back to individual runs.

#### Challenge 2: Unicode Dot Variants

**Problem**: Users typing on different platforms or pasting from other documents may use Unicode characters that visually resemble dots but are not ASCII `.` (U+002E):
- `…` U+2026 HORIZONTAL ELLIPSIS
- `⋯` U+22EF MIDLINE ELLIPSIS
- `‥` U+2025 TWO DOT LEADER
- `．` U+FF0E FULLWIDTH FULL STOP

The regex `\.{4,}` would not match these.

**Solution**: `PlaceholderProcessor.normalize()` performs deterministic substitution of each Unicode variant to its ASCII equivalent before the regex is applied. This normalization runs at upload time only — by the time a document reaches generation, it is guaranteed to contain only ASCII dots.

#### Challenge 3: Placeholder Length Variability After Normalization

**Problem**: After normalization, all placeholders are exactly `....`. When a value is substituted, the resulting text is almost certainly not 4 characters. A name might be 20 characters; a contract value might be 15; an address might be 80. The run-boundary writeback must account for the delta in text length.

**Solution**: `SubstitutionSpan` records both the **original range** (`[originalStart, originalEnd)` in the merged string) and the `replacementLen` (in the rewritten string). `writebackSpans` advances `rwPos` by `replacementLen` when crossing a span, so each run slice is computed relative to the rewritten string's actual character positions, not the original positions.

#### Challenge 4: Positional Alignment Across Three Code Paths

**Problem**: `FileParser`, `DocxNormalizer`, and `DocxFiller` all iterate document paragraphs. If any one of them uses a different order (e.g., `FileParser` reads body paragraphs but not headers), placeholder positions (0, 1, 2, ...) would be assigned differently and values would be injected into wrong fields.

**Solution**: All three call `DocxUtils.forEachParagraph(doc, consumer)` — the single source of truth. Adding a new content zone (footnotes, textboxes, etc.) requires updating only `DocxUtils`, and the change propagates to all three consumers automatically.

#### Challenge 5: PDF Templates Losing Formatting

**Problem**: Users may provide PDF templates. After PDF→DOCX conversion (text-only), the visual formatting is lost.

**Solution & tradeoff**: This is accepted behavior. The contract service's PDF-to-DOCX conversion uses text extraction only. The expectation is that well-formatted DOCX templates are the primary use case. PDF upload support was added for convenience (allowing the platform to accept PDFs that users might have on hand), with the understanding that the resulting contract will be plain-text formatted. For critical formatting needs, users are guided to upload DOCX.

#### Challenge 6: LibreOffice Process Lifecycle

**Problem**: LibreOffice headless spawned as a subprocess per conversion. This is expensive (JVM startup equivalent overhead) and must not leave orphan processes or temp files on failure.

**Solution**:
- The temp directory cleanup runs in a `finally` block — it runs even if LibreOffice exits with a non-zero code or throws
- `Process.waitFor()` is used (blocking) so the JVM always waits for LibreOffice to terminate
- `InterruptedException` during `waitFor` restores the interrupt flag (`Thread.currentThread().interrupt()`) before wrapping and rethrowing

---

## 7. DownloadService — Strategy Pattern

### 6.1 Motivation

The platform supports downloading five distinct document variants:
- Template (as DOCX or PDF)
- Unsigned contract (as DOCX or PDF)
- Signed contract (PDF only)
- Unsigned appendix (as DOCX or PDF)
- Signed appendix (PDF only)

Each variant fetches from a different repository field, enforces different format constraints, and may throw different domain exceptions. Handling this with a chain of `if/else` or a `switch` statement in a single service method would:
- Couple the download service to every entity type
- Require modification every time a new document type is added
- Make unit testing of individual variants harder

The **Strategy pattern** cleanly solves this by encapsulating each variant's fetch-and-validate logic in its own class.

---

### 6.2 Component Design

```
DocumentDownloadService           ← context: knows nothing about entity types
        │
        ▼
DocumentProviderRegistry          ← index: Map<DocumentType, DocumentProvider>
        │                            auto-populated from Spring context
        ├─ DocumentType.TEMPLATE              → TemplateProvider
        ├─ DocumentType.UNSIGNED_CONTRACT     → UnsignedContractProvider
        ├─ DocumentType.SIGNED_CONTRACT       → SignedContractProvider
        ├─ DocumentType.UNSIGNED_APPENDIX     → UnsignedAppendixProvider
        └─ DocumentType.SIGNED_APPENDIX       → SignedAppendixProvider

DocumentProvider (interface)
    ├─ DocumentResult getDocument(Long documentId)
    ├─ boolean supportsFormat(DocumentFormat targetFormat)
    └─ DocumentType getDocumentType()
```

#### `DocumentResult` Record

```java
record DocumentResult(byte[] compressedContent, DocumentFormat nativeFormat) {}
```

Providers return both the compressed bytes **and** the native format in a single object. This avoids two separate repository calls — one to fetch bytes and one to fetch the format column. The `DocumentDownloadService` decompresses and optionally converts using only this record.

---

### 6.3 Provider Implementations

| Provider | DocumentType | getDocument() | supportsFormat() |
|---|---|---|---|
| `TemplateProvider` | `TEMPLATE` | `templateRepository.findById()` → returns `documentContent` | DOCX, PDF |
| `UnsignedContractProvider` | `UNSIGNED_CONTRACT` | `contractRepository.findById()` → returns `documentContent` | DOCX, PDF |
| `SignedContractProvider` | `SIGNED_CONTRACT` | `contractRepository.findById()` → returns `signedDocumentContent` | PDF only |
| `UnsignedAppendixProvider` | `UNSIGNED_APPENDIX` | `appendixRepository.findById()` → returns `documentContent` | DOCX, PDF |
| `SignedAppendixProvider` | `SIGNED_APPENDIX` | `appendixRepository.findById()` → returns `signedDocumentContent` | PDF only |

Signed providers (`SignedContractProvider`, `SignedAppendixProvider`) throw `SignedDocumentNotAvailableException` (HTTP 404) if the `signedDocumentContent` field is NULL — meaning the signed document has not yet been uploaded.

Unsigned providers return the generated document as stored. The native format for generated contracts and appendices is always PDF (they are converted from DOCX at generation time). The `supportsFormat(DOCX)` on these providers returns true because `DocumentDownloadService` will convert back: `PDF → DOCX` using the text-extraction fallback. This is intentional — a "download DOCX" for an unsigned contract gives a text-extracted version of the PDF, not the original template. For full fidelity DOCX, users should download the template separately.

---

### 6.4 Request Flow

```
GET /api/contracts/download/{contractId}/unsigned/pdf
        │
        ▼
ContractController.downloadDocument(contractId, "unsigned", "pdf")
        │
        ▼
DocumentDownloadService.downloadDocument(
    documentId   = contractId,
    targetFormat = PDF,
    documentType = UNSIGNED_CONTRACT
)
        │
        ├─ providerRegistry.getProvider(UNSIGNED_CONTRACT)
        │       → UnsignedContractProvider
        │
        ├─ provider.supportsFormat(PDF) → true
        │
        ├─ provider.getDocument(contractId)
        │       → contractRepository.findById(contractId)
        │       → DocumentResult(compressedPdfBytes, PDF)
        │
        ├─ fileUtils.decompress(compressedBytes) → rawPdfBytes
        │
        ├─ result.nativeFormat() == targetFormat (PDF == PDF) → no conversion needed
        │
        └─ return rawPdfBytes
                │
                ▼
        ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=contract-123.pdf")
            .contentType(APPLICATION_PDF)
            .body(bytes)
```

---

### 6.5 Extensibility

Adding a new document type (e.g., `COVER_LETTER`) requires:
1. Add `COVER_LETTER` to the `DocumentType` enum
2. Create `CoverLetterProvider implements DocumentProvider` annotated with `@Component`
3. No changes to `DocumentDownloadService`, `DocumentProviderRegistry`, or any controller

The registry detects duplicate registrations at startup (two providers claiming the same `DocumentType` → `IllegalStateException`), preventing silent overrides.

---

## 8. Audit Trails

The service implements two complementary audit mechanisms: **lifecycle audit fields** on the `contract` entity and an **immutable field value log** in `document_field_value`.

### 7.1 Contract Lifecycle Audit Fields

These columns were added in `V4__add_audit_fields.sql` and capture **who** performed each significant lifecycle action and **when**:

| Event | Timestamp field | User ID field |
|---|---|---|
| Contract generated | `generated_at` | `generated_by_user_id` |
| Signed document uploaded (→ ACTIVE) | `uploaded_signed_at` | `uploaded_signed_by_user_id` |
| Contract terminated (→ TERMINATED) | `terminated_at` | `terminated_by_user_id` |

**`generated_at`** is set in the `Contract` entity's `@PrePersist` hook:

```java
@PrePersist
protected void onCreate() {
    if (this.generatedAt == null) {
        this.generatedAt = LocalDateTime.now();
    }
}
```

The null-check (`if null`) means the field can be set programmatically (e.g., in tests or data imports) and the hook will not override it. For normal production use, it is always null at persist time and gets set to `NOW()`.

**`terminated_at`** and **`uploaded_signed_at`** are set in the service layer at the moment of the state transition:

```java
// In ContractService.uploadSignedContract():
contract.setUploadedSignedAt(LocalDateTime.now());
contract.setUploadedSignedByUserId(userId);

// In ContractService.terminateContract():
contract.setTerminatedByUserId(request.getUserId());
contract.setTerminatedAt(LocalDateTime.now());
```

All six audit fields are **nullable** — a contract that has never been terminated will have NULL `terminated_at` and `terminated_by_user_id`. The partial index `idx_contract_termination` (`WHERE termination_date IS NOT NULL`) means no index overhead is paid for the majority of contracts that are not terminated.

All audit fields are included in `ContractResponseDTO` and surfaced to the frontend, allowing users to see the complete history of a contract's lifecycle actions.

---

### 7.2 DocumentFieldValue — Immutable Field Audit Log

Every time a contract or appendix is generated from a template, one `document_field_value` row is created for each template field:

```java
DocumentFieldValue.builder()
    .document(contract)           // FK to the generated document
    .templateField(field)         // FK to the specific template field
    .fieldValue(mappings.get(field.getFieldLabel()))  // the injected value
    .build()
// created_at is set by the database DEFAULT NOW()
```

These records are **create-only**. There is no update or delete operation for field values in the application code. The schema enforces this:

```sql
template_field_id BIGINT NOT NULL
    REFERENCES clm.template_field (id) ON DELETE RESTRICT ON UPDATE CASCADE
```

The `ON DELETE RESTRICT` means a template field cannot be deleted while any document references it. This ensures field value records always have a valid FK target and cannot be orphaned.

The `document_id → document.id ON DELETE CASCADE` means that when a contract or appendix is deleted, all its field value rows are deleted atomically. This is correct because field values are not an independent audit log — they are part of the document's existence.

**What the field value log enables:**

1. **Reproducibility**: Given a historical contract, you can reconstruct what values were filled in, even if the template fields have been relabeled since generation.
2. **Search**: The `ContractSpecification` uses correlated `EXISTS` subqueries against `document_field_value.field_value` to find contracts where specific values (client names, CUI numbers, addresses) were injected. The GIN trigram index makes this fast.
3. **Audit disclosure**: `ContractResponseDTO` includes the full list of `DocumentFieldValueResponseDTO` entries, showing all field values as part of the contract record.

---

### 7.3 Audit Events Summary

| Event | Mechanism | Who | When |
|---|---|---|---|
| Template uploaded | `document_template.created_at` (DB DEFAULT NOW) | Implicit | Upload time |
| Template field labeled | `template_field.field_label` updated | User | Label mapping time |
| Template fully mapped | `document_template.is_fully_mapped` set by DB trigger | Database | On last label assignment |
| Contract generated | `contract.generated_at` (`@PrePersist`) + `generated_by_user_id` | Service + request | Generation time |
| Contract field values | `document_field_value` rows (create-only) | Service | Generation time |
| Signed document uploaded | `contract.uploaded_signed_at` + `uploaded_signed_by_user_id` | Service + request | Upload time |
| Contract terminated | `contract.terminated_at` + `terminated_by_user_id` | Service + request | Termination time |
| Contract archived (auto) | `contract.contract_status = ARCHIVED` | Scheduled job | Midnight daily |
| Appendix created | `document.created_at` (DB DEFAULT NOW) | Implicit | Creation time |
| Appendix signed | `appendix.appendix_status = SIGNED` | Service | Upload time |

---

## 9. Search & Specification Pattern

Contract search is built on the **JPA Specification** (Criteria API) pattern. `ContractSpecification.buildSearchSpecification(SearchRequest)` produces a composable `Specification<Contract>` from an optional set of filter criteria.

All predicates use AND logic. An empty request returns all contracts (`cb.conjunction()`).

### Predicate Groups

**Text predicates** (case-insensitive LIKE, uses trigram GIN indexes):
```java
cb.like(cb.lower(root.get("notes")), "%" + term.toLowerCase() + "%")
cb.like(cb.lower(root.get("documentTemplate").get("templateName")), ...)
cb.like(cb.lower(root.get("documentTemplate").get("description")), ...)
```

**Equality predicates** (B-tree indexes):
```java
cb.equal(root.get("contractStatus"), request.contractStatus())
cb.equal(root.get("clientId"), request.clientId())
cb.equal(root.get("generatedBy"), request.generatedBy())
```

**Date range predicates** (B-tree indexes on `created_at`):
```java
cb.greaterThanOrEqualTo(root.get("createdAt"), request.createdAfter().atStartOfDay())
cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(request.createdBefore(), LocalTime.MAX))
```

**Label value predicates** (correlated EXISTS, GIN trigram index):

For each search term in `request.labelValues()`, a correlated subquery is added:

```sql
AND EXISTS (
    SELECT 1 FROM document_field_value dfv
    WHERE dfv.document = contract
      AND LOWER(dfv.field_value) LIKE '%{term}%'
)
```

This implements **intersection semantics** — a contract matches only if it has a field value containing **each** of the supplied terms. PostgreSQL executes these EXISTS probes using the `idx_dfv_value_lower_trgm` GIN index, making multi-term searches performant even at scale.

### Fetch Join Optimization

The specification detects whether the query is a data query (vs a COUNT query for pagination):

```java
boolean isDataQuery = !Long.class.equals(query.getResultType())
        && !long.class.equals(query.getResultType());
if (isDataQuery) {
    root.fetch(FIELD_DOCUMENT_TEMPLATE, JoinType.LEFT);
    query.distinct(true);
}
```

The LEFT fetch join eagerly loads `documentTemplate` in the same SQL query that fetches contracts, avoiding N+1 queries when the response includes template names/descriptions. The `distinct(true)` prevents row duplication from the join.

---

## 10. Security

### JWT Authentication

All endpoints except OpenAPI docs and actuator are protected by JWT Bearer token authentication.

**`JwtAuthenticationFilter`** (servlet filter):
1. Extracts the `Authorization: Bearer <token>` header
2. Validates via `JwtTokenProvider.validateToken(token)` (HMAC-SHA256 signature check)
3. Extracts claims and populates `SecurityContextHolder`
4. If no valid token, the request proceeds unauthenticated — downstream method security or explicit endpoint restrictions reject unauthorized access

**`JwtTokenProvider`**: Wraps JJWT 0.12.3. Requires `jwt.secret` ≥ 32 characters to produce a cryptographically adequate HMAC key.

**`SecurityConfig`**: Stateless session (`SessionCreationPolicy.STATELESS`) — no server-side session storage. CORS is permissive (all origins) for the current development configuration.

**Authentication entry point**: `JwtAuthenticationEntryPoint` returns HTTP 401 for requests that reach protected resources without a valid token.

---

## 11. Scheduled Jobs

### `ContractArchiveJob`

```java
@Scheduled(cron = "${job.archive-contracts.cron:0 0 0 * * *}")
public void archiveExpiredContracts() {
    int count = contractRepository.archiveExpiredContracts(
            ContractStatus.ARCHIVED,
            ContractStatus.ACTIVE,
            LocalDate.now()
    );
    log.info("Archived {} expired contract(s)", count);
}
```

- Default schedule: `0 0 0 * * *` — midnight every day
- Configurable via `job.archive-contracts.cron` property
- Single bulk `UPDATE` statement (no individual entity loads):
  ```sql
  UPDATE clm.contract
  SET contract_status = :archived
  WHERE contract_status = :active
    AND auto_renew = false
    AND contract_end_date < :today
  ```
- Uses the `idx_contract_active` partial index (`WHERE contract_status = 'ACTIVE'`) — the scan is limited to only active contracts, not the full table
- Contracts with `auto_renew = true` are excluded — these are expected to be renewed before their end date by a separate business process

---

### `ContractTerminationJob`

```java
@Scheduled(cron = "${job.process-termination.cron:0 0 0 * * *}")
public void processTerminationDueContracts() {
    int count = contractRepository.processTerminationDueContracts(
            ContractStatus.TERMINATED,
            ContractStatus.TERMINATION_DUE,
            LocalDate.now()
    );
    log.info("Processed {} termination-due contract(s) to TERMINATED", count);
}
```

- Default schedule: `0 0 0 * * *` — midnight every day
- Configurable via `job.process-termination.cron` property
- Single bulk `UPDATE` statement (no individual entity loads):
  ```sql
  UPDATE clm.contract
  SET contract_status = :terminated
  WHERE contract_status = :terminationDue
    AND termination_date = :today
  ```
- Automatically transitions contracts with `TERMINATION_DUE` status to `TERMINATED` when their `termination_date` equals the current date
- No in-memory processing — all work is performed in a direct database UPDATE
- Runs in parallel with `ContractArchiveJob` thanks to the `ThreadPoolTaskScheduler` with 5 concurrent workers

---

## 12. REST API Surface

### `ContractController` — `/api/contracts`

| Method | Path | Description | Request | Response |
|---|---|---|---|---|
| POST | `/generate` | Generate contract from template | `GenContractRequest` | `201 ContractResponseDTO` |
| POST | `/{id}/upload-signed` | Upload signed document → ACTIVE | `multipart/form-data` | `200 ContractResponseDTO` |
| PUT | `/terminate/{id}` | Terminate ACTIVE contract | `ContractTerminationRequest` | `204` |
| PUT | `/{id}/toggle-auto-renew` | Toggle auto-renewal | — | `200 ContractResponseDTO` |
| GET | `/all` | List all (paginated) | `?page=0&size=20` | `200 Page<ContractResponseDTO>` |
| POST | `/search` | Dynamic search | `SearchRequest` | `200 Page<ContractResponseDTO>` |
| GET | `/download/{id}/{type}/{format}` | Download document | path params | `200 bytes` |
| GET | `/report/expiring` | Contracts expiring within N days | `?days=30` | `200 List<ContractResponseDTO>` |
| GET | `/report/inactive-clients` | Clients with no rate change in M months | `?months=6` | `200 List<ContractResponseDTO>` |

### `TemplateController` — `/api/templates`

| Method | Path | Description | Request | Response |
|---|---|---|---|---|
| POST | `/upload` | Upload template, extract fields | `multipart/form-data` | `201 TemplateUploadResponseDTO` |
| GET | `/` | List all templates (paginated) | `?page=0&size=20` | `200 Page<TemplateResponseDTO>` |
| GET | `/{id}` | Get template with fields | — | `200 TemplateResponseDTO` |
| PUT | `/{id}/labels` | Batch-update field labels | `FieldMappingRequest` | `200 List<TemplateFieldResponseDTO>` |
| DELETE | `/{id}` | Delete template | — | `204` |
| GET | `/download/{id}/{format}` | Download template | path params | `200 bytes` |

### `AppendixController` — `/api/appendices`

| Method | Path | Description | Request | Response |
|---|---|---|---|---|
| POST | `/generate` | Generate appendix from template | `GenAppendixRequest` | `201 AppendixResponseDTO` |
| POST | `/upload` | Upload non-fillable appendix | `multipart/form-data` | `201 AppendixResponseDTO` |
| POST | `/{id}/upload-signed` | Upload signed appendix → SIGNED | `multipart/form-data` | `200 AppendixResponseDTO` |
| GET | `/contract/{contractId}` | List appendices for a contract | — | `200 List<AppendixResponseDTO>` |
| DELETE | `/{id}` | Delete appendix | — | `204` |
| GET | `/download/{id}/{type}/{format}` | Download appendix | path params | `200 bytes` |

---

## 13. Exception Handling

All exceptions are centralized in `GlobalExceptionHandler` (`@RestControllerAdvice`). Responses follow RFC 7807 `ProblemDetail` structure:

```json
{
  "type":   "https://api.clm.demo/errors/resource-not-found",
  "title":  "Resource Not Found",
  "status": 404,
  "detail": "Contract not found: 42",
  "timestamp": "2026-05-07T14:23:00.000Z"
}
```

| Exception | HTTP Status | When |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `TemplateIncompleteException` | 422 | Template not fully mapped, contract generation blocked |
| `MissingMandatoryFieldException` | 400 | Required field has no value in generation request |
| `ContractGenerationFailException` | 422 | Document filling or PDF conversion failed |
| `FileConversionException` | 422 | GZIP / LibreOffice conversion error |
| `DuplicateTemplateNameException` | 400 | Template name already exists |
| `InvalidContractStateException` | 409 | State transition not allowed (e.g., terminate non-ACTIVE) |
| `InvalidAppendixStateException` | 409 | Appendix already SIGNED |
| `SignedDocumentNotAvailableException` | 404 | Signed document not yet uploaded |
| `UnsupportedFileException` | 415 | Unrecognized file format |
| `TemplateUploadException` | 422 | Template parse/normalize failure |
| `TemplateFieldOwnershipException` | 400 | Field does not belong to the specified template |

---

## 14. Caching — Caffeine L2 Cache

### 14.1 Motivation

Two entity types in the contract-service carry significant binary payloads that make individual DB round-trips expensive:

| Entity | Binary field | Typical compressed size |
|---|---|---|
| `DocumentTemplate` | `document_content` (GZIP-compressed DOCX) | 20 – 500 KB |
| `Contract` | `document_content` + `signedDocumentContent` (GZIP-compressed PDFs) | 50 KB – several MB |

These entities are also **read-heavy relative to their mutation rate**: templates are uploaded once and read on every contract or appendix generation; contracts are read for display, download, and search far more often than they are modified. Hibernate's built-in first-level (session-scoped) cache already avoids redundant loads *within* a single transaction, but provides no benefit across requests.

A **service-level L2 cache** backed by Caffeine keeps the last-fetched entity in JVM heap memory. Subsequent reads for the same ID bypass the database entirely, eliminating the network round-trip, PostgreSQL query execution, JDBC deserialization, and Hibernate hydration overhead.

---

### 14.2 Cache Regions

Two named caches are defined in `CacheConfig` and shared via the `CacheNames` constants class:

| Cache name | Constant | Covers | Max entries | TTL (after write) | Rationale |
|---|---|---|---|---|---|
| `templates` | `CacheNames.TEMPLATES` | `TemplateResponseDTO` keyed by `Long templateId` | 200 | 1 hour | Templates are rarely updated; 200 is sufficient for any realistic deployment |
| `contracts` | `CacheNames.CONTRACTS` | `ContractResponseDTO` keyed by `Long contractId` | 1 000 | 10 minutes | Contracts change more often (status transitions, renegotiations); shorter TTL keeps stale risk low |

Both caches are configured with `recordStats()`, which enables Caffeine's internal hit/miss/eviction counters and exposes them via Micrometer (see §14.5).

**What is cached**: the **DTO** produced by the service method (`TemplateResponseDTO`, `ContractResponseDTO`) — not the JPA entity. This avoids holding open lazy-load references in cache memory and means cached objects are fully serializable plain Java records/classes.

**What is NOT cached**: list queries (`getAll`, `search`, `getAllTemplates`), report queries, or any write that changes state. Caching mutable collection results would require invalidating on every mutation — the hit rate would be negligible and the consistency risk high.

---

### 14.3 Eviction Rules

Spring's `@CacheEvict` annotations ensure the cache never serves a stale entry after a mutation. The complete eviction matrix is:

#### `templates` cache

| Method | Annotation | Key evicted | Why |
|---|---|---|---|
| `TemplateService.getTemplate(id)` | `@Cacheable` | — | Populates cache on miss |
| `TemplateService.deleteTemplate(id)` | `@CacheEvict` | `#templateId` | Template no longer exists |
| `TemplateService.updateFieldLabels(request)` | `@CacheEvict` | `#request.templateId` | `isFullyMapped` flag and field labels change |

#### `contracts` cache

| Method | Annotation | Key evicted | Why |
|---|---|---|---|
| `ContractService.getById(id)` | `@Cacheable` | — | Populates cache on miss |
| `ContractService.uploadSignedContract(id, …)` | `@CacheEvict` | `#contractId` | Status changes to ACTIVE; signed content added |
| `ContractService.terminateContract(id, …)` | `@CacheEvict` | `#contractId` | Status changes to TERMINATED; audit fields set |
| `ContractService.toggleAutoRenewal(id)` | `@CacheEvict` | `#contractId` | `autoRenew` flag flips |
| `ContractService.renegotiateContract(id, …)` | `@CacheEvict` | `#contractId` | `contractValue` and/or `contractEndDate` change |
| `ContractService.updateContractTerms(id, …)` | `@CacheEvict` | `#contractId` | Multiple fields can change |

`@CacheEvict` uses the default `beforeInvocation = false` — eviction happens **after** the method returns successfully. If the method throws an exception (e.g., contract not found, invalid state), the cache entry is preserved, which is correct: a failed mutation leaves the entity unchanged.

The scheduled archival job (`ContractArchiveJob`) performs a bulk `UPDATE` in SQL that bypasses the service layer entirely. Because it changes `contract_status` in bulk without calling `getById` or any eviction-annotated method, archived contracts may remain in the `contracts` cache with status `ACTIVE` until their TTL (10 minutes) expires. This is an accepted tradeoff: archival runs at midnight, no users are querying those contracts at that moment, and the TTL guarantees consistency is restored within 10 minutes.

---

### 14.4 Configuration

**`CacheConfig.java`** (`clm.demo.config`) — defines the `CacheManager` bean using `SimpleCacheManager` with per-region `CaffeineCache` instances:

```java
@Bean
public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(List.of(
            new CaffeineCache(CacheNames.TEMPLATES,
                    Caffeine.newBuilder()
                            .maximumSize(200)
                            .expireAfterWrite(Duration.ofHours(1))
                            .recordStats()
                            .build()),
            new CaffeineCache(CacheNames.CONTRACTS,
                    Caffeine.newBuilder()
                            .maximumSize(1_000)
                            .expireAfterWrite(Duration.ofMinutes(10))
                            .recordStats()
                            .build())
    ));
    return manager;
}
```

`SimpleCacheManager` (rather than `CaffeineCacheManager`) is used because the latter applies a **single** Caffeine spec to all caches, while the two regions here have intentionally different TTL and size settings.

**`application.yaml`** declares the cache type to prevent Spring Boot's auto-configuration from falling back to `NoOpCacheManager` if the `CacheManager` bean is not found early in startup:

```yaml
spring:
  cache:
    type: caffeine
```

**`DemoApplication.java`** carries `@EnableCaching` to activate the Spring Cache AOP proxy infrastructure. Placing it on the main class (rather than on a `@Configuration` class) ensures caching is active for the entire application context, including integration test contexts that import the full application.

---

### 14.5 Metrics & Observability

Caffeine's `recordStats()` mode exposes the following per-region counters. With `micrometer-registry-prometheus` on the classpath (already a production dependency), these are scraped by Prometheus and visible in Grafana:

| Micrometer metric | Description |
|---|---|
| `cache.gets{name="templates",result="hit"}` | Template cache hits |
| `cache.gets{name="templates",result="miss"}` | Template cache misses |
| `cache.puts{name="templates"}` | Entries written to the templates cache |
| `cache.evictions{name="templates"}` | Entries evicted (by TTL or explicit `@CacheEvict`) |
| `cache.gets{name="contracts",result="hit"}` | Contract cache hits |
| `cache.gets{name="contracts",result="miss"}` | Contract cache misses |

A **hit rate** (`hits / (hits + misses)`) above 80% indicates the cache is providing meaningful benefit. Low hit rates on `contracts` may indicate the TTL is too short for the access pattern, or that the client is predominantly doing list/search operations rather than individual `getById` calls.

Access cache statistics at runtime:

```
GET /actuator/metrics/cache.gets?tag=name:contracts
GET /actuator/metrics/cache.gets?tag=name:templates
```

---

### 14.6 Testing Strategy

The cache is validated by two independent test suites that prove orthogonal properties.

#### In-vitro suite — `CachePerformanceTest`

**Purpose:** verify correctness and latency in complete isolation — no database, no Flyway, no network.

**Approach:** a minimal Spring context loads only `TemplateService`, `ContractService`, and `CacheConfig`. All repositories are Mockito mocks. A mock `PlatformTransactionManager` satisfies the `@Transactional` AOP interceptor. Each mock repository answer sleeps for 50 ms before returning, simulating a real DB round-trip.

**Correctness proof:** after N calls to the same cached method, `verify(repository, times(1)).findById(id)` confirms the repository was called exactly once — all subsequent calls were served from Caffeine. After a mutating operation, `verify(repository, times(3)).findById(id)` confirms the entry was re-fetched (the mutating method loads once internally, plus one load after eviction).

**Latency proof:** wall-clock time is measured with `System.currentTimeMillis()` before and after each call. The cold call must be ≥ 50 ms; the warm call must be < 10 ms.

#### Real-DB integration suite — `CacheRealDbTest`

**Purpose:** verify that the cache behaves correctly end-to-end against a live PostgreSQL instance with the full schema and Hibernate stack.

**Approach:** `@SpringBootTest(webEnvironment = NONE)` with `@ActiveProfiles("test")` loads the complete application context. The datasource points to a short-lived PostgreSQL container started externally by the Makefile target `test-cache-real` (port 5434) and removed after the test run — whether it passes or fails. No Testcontainers library is involved; container lifecycle is managed entirely by the Makefile.

```bash
make test-cache-real   # start postgres → run test → remove container
```

**Correctness proof:** Caffeine's built-in stats (`recordStats()` is enabled in `CacheConfig`) expose `missCount()` and `hitCount()` as authoritative counters. Because the counters are cumulative (they do not reset when `cache.clear()` is called), each test snapshots the counters in `@BeforeEach` and asserts on the **delta** since the snapshot. Absence of a cache entry is checked via `cache.asMap().containsKey(key)` — which does not register as a hit or miss — to avoid polluting the stats being asserted.

**Latency proof:** `System.nanoTime()` measurements assert directionally (`warmNs < coldNs`) rather than against an absolute threshold, because PostgreSQL round-trip times vary with Docker overhead and host hardware.

**Eviction correctness:** after every mutating operation, `asMap().containsKey(key) == false` is asserted before any subsequent read, proving no stale value remains in the cache.

---

### 14.7 Test Results

#### In-vitro results (`CachePerformanceTest`) — 12 tests, all passing

| Cache region | Cold call | Warm call | Speedup |
|---|---|---|---|
| `templates` | 55 ms | 1 ms | **55×** |
| `contracts` | 52 ms | 0 ms | **52×** |

*Cold latency reflects the 50 ms artificial sleep injected by the mock. Warm latency is pure Caffeine in-process lookup overhead.*

**Correctness scenarios verified:**

| Scenario | Assertion |
|---|---|
| Repeated reads (same ID) | Repository called **exactly once** regardless of call count |
| `deleteTemplate` | Entry evicted — next `getTemplate` re-fetches from repository |
| `updateFieldLabels` | Entry evicted — next `getTemplate` re-fetches from repository |
| `renegotiateContract` | Entry evicted — next `getById` re-fetches from repository |
| `updateContractTerms` | Entry evicted — next `getById` re-fetches from repository |
| `terminateContract` | Entry evicted — next `getById` re-fetches from repository |
| `toggleAutoRenewal` | Entry evicted — next `getById` re-fetches from repository |
| N distinct IDs | Exactly N repository fetches for any number of repeated reads across all IDs |

#### Real-DB results (`CacheRealDbTest`) — 12 tests, all passing

Measured against a live PostgreSQL 16 container with full Flyway schema, JPA/Hibernate stack, and HikariCP connection pool.

| Cache region | Cold call (real DB) | Warm call (Caffeine) | Speedup |
|---|---|---|---|
| `templates` | ~2.4 ms | ~62 µs | **~38×** |
| `contracts` | ~3.7 ms | ~66 µs | **~56×** |

*Cold latency includes HikariCP checkout, Hibernate JOINED-inheritance join query, and ResultSet mapping. Warm latency is pure in-heap Caffeine lookup (~62–66 µs).*

**Caffeine stats verified per test (delta since `@BeforeEach` snapshot):**

| Scenario | `missCount` delta | `hitCount` delta |
|---|---|---|
| Second read is a cache hit | 1 | 1 |
| N repeated reads | 1 | N − 1 |
| Eviction after `deleteTemplate` | 1 (initial load only) | — |
| Eviction after `updateFieldLabels` | 2 (load + re-fetch post-eviction) | 0 |
| Eviction after `renegotiateContract` | 2 | 1 |
| Eviction after `terminateContract` | 2 | 0 |
| Eviction after `updateContractTerms` | 2 | 0 |
| Eviction after `toggleAutoRenewal` | 2 | 0 |

---

## 15. Configuration Reference

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | — | PostgreSQL JDBC URL |
| `spring.datasource.username` | — | DB username |
| `spring.datasource.password` | — | DB password |
| `spring.jpa.hibernate.ddl-auto` | `none` | Flyway manages all DDL |
| `spring.jpa.show-sql` | `false` | Enable for development debugging |
| `libreoffice.path` | `libreoffice` | Absolute path or command name for LibreOffice binary |
| `jwt.secret` | — | HMAC-SHA256 key, minimum 32 characters |
| `job.archive-contracts.cron` | `0 0 0 * * *` | Cron expression for contract archival job |
| `spring.cache.type` | `caffeine` | Cache provider; must be `caffeine` to activate `CacheConfig` |
| `spring.datasource.hikari.maximum-pool-size` | `10` | Max concurrent DB connections |
| `spring.datasource.hikari.connection-timeout` | `30000` | Connection wait timeout (ms) |

---

*Documentation generated from source analysis of the `contract-service` module. For schema details see also `DATABASE_DOCUMENTATION.md` and the migration files under `src/main/resources/db/migration/`.*
