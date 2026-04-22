# CLM Platform - Database Documentation

## Overview

The CLM (Contract Lifecycle Management) Platform uses a **PostgreSQL relational database** to manage document templates, generated contracts, appendices, and their associated field values. The system is designed to:

- **Store document templates** (document blueprints with placeholders)
- **Extract and map template fields** (placeholders) for data injection
- **Generate contracts** by merging templates with client-specific data
- **Attach appendices** to contracts (both fillable and direct-upload)
- **Maintain audit trails** of all injected field values
- **Track lifecycle state** for both contracts and appendices
- **Support advanced search** with trigram indexing and database functions

**Key Characteristics:**
- **Database**: PostgreSQL 12+
- **Schema**: `clm` (isolated namespace)
- **Tables**: 6 core tables with comprehensive indexing
- **Inheritance**: JOINED-table inheritance (`document` → `contract`, `appendix`)
- **Data Volume**: Supports BYTEA storage for document binary content
- **Migration Tool**: Flyway (single baseline migration, V1)
- **ORM**: Hibernate JPA (Spring Data JPA with Lombok `@SuperBuilder`)
- **Search**: pg_trgm extension with GIN indexes for efficient substring searches


### Environment Variables

For production environments, configure these environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/clm_platform` | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | `org.postgresql.Driver` | PostgreSQL JDBC driver class |
| `SPRING_DATASOURCE_USERNAME` | `clm_user` | Database user account |
| `SPRING_DATASOURCE_PASSWORD` | `clm_password` | Database user password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `none` | Hibernate DDL strategy (`none` = no auto-generation) |
| `SPRING_JPA_SHOW_SQL` | `false` | Enable SQL logging for debugging |

### Connection Pool Configuration

Spring Boot uses HikariCP by default. Key connection pool settings:

- **Maximum Pool Size**: 10 (default)
- **Minimum Idle**: 2 (default)
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

To customize, add to `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

---

## Schema Structure

### Database & Schema Hierarchy

```
PostgreSQL Database: clm_platform
└── Schema: clm
    ├── document_template    (blueprint table)
    ├── template_field       (child of document_template)
    ├── document             (JOINED-inheritance base table)
    ├── contract             (subclass of document)
    ├── appendix             (subclass of document — linked to contract)
    └── document_field_value (audit trail for any document)
```

### Key Design Principles

1. **Isolated Schema**: All tables exist in the `clm` schema for multi-tenancy separation
2. **JOINED Inheritance**: `document` is the shared base; `contract` and `appendix` are subclass tables joined on `document_id`
3. **BYTEA Storage**: Document binaries stored directly in the database for transactional consistency
4. **Lazy Loading**: Relationships use `FetchType.LAZY` by default to optimize performance
5. **Foreign Keys**: Referential integrity enforced with CASCADE/RESTRICT delete rules
6. **Audit Trail**: Timestamps on all records; field values are immutable audit records

---

## Entity-Relationship Diagram

```
┌─────────────────────────────┐
│      DOCUMENT_TEMPLATE      │  (Blueprint)
├─────────────────────────────┤
│ id (PK, BIGSERIAL)          │
│ template_name (UNIQUE)      │
│ description                 │
│ document_format (ENUM)      │
│ document_content (BYTEA)    │
│ field_count                 │
│ is_fully_mapped             │
│ created_at                  │
│ updated_at                  │
└────┬────────────────────────┘
     │ (1:N)
     │
┌────▼────────────────────────┐
│      TEMPLATE_FIELD         │  (Placeholders)
├─────────────────────────────┤
│ id (PK)                     │
│ template_id (FK → doc_tmpl) │
│ field_label                 │
│ data_type (ENUM)            │
│ field_position              │
│ is_required                 │
│ format_pattern              │
└─────────────────────────────┘

┌─────────────────────────────┐
│         DOCUMENT            │  (JOINED base — shared columns)
├─────────────────────────────┤
│ id (PK, BIGSERIAL)          │
│ document_type (discriminator│
│ template_id (FK, nullable)  │◄── NULL = direct-upload (non-fillable)
│ document_format (ENUM)      │
│ document_content (BYTEA)    │
│ signed_document_content     │
│ generated_by                │
│ generated_by_mail           │
│ notes                       │
│ created_at                  │
└──┬──────────────────────────┘
   │
   ├─────────────────────────────────────────────────┐
   │ (1:1 JOINED, document_type='CONTRACT')           │ (1:1 JOINED, document_type='APPENDIX')
   │                                                  │
┌──▼──────────────────────────┐       ┌──────────────▼──────────────┐
│         CONTRACT            │       │          APPENDIX            │
├─────────────────────────────┤       ├─────────────────────────────┤
│ document_id (PK, FK→doc)    │◄──┐   │ document_id (PK, FK→doc)    │
│ client_id                   │   │   │ contract_id (FK→contract)   │──┘
│ contract_status (ENUM)      │   └───│ title                       │
│ contract_value              │       │ appendix_status (ENUM)      │
│ contract_start_date         │       └─────────────────────────────┘
│ contract_end_date           │
│ termination_date            │
│ reasons_for_termination     │
└─────────────────────────────┘

┌─────────────────────────────┐
│     DOCUMENT_FIELD_VALUE    │  (Audit Trail — for any document)
├─────────────────────────────┤
│ id (PK)                     │
│ document_id (FK → document) │
│ template_field_id (FK)      │
│ field_value (TEXT)          │
│ created_at                  │
└─────────────────────────────┘
```

---

## Tables Reference

### 1. DOCUMENT_TEMPLATE
**Purpose**: Stores document blueprints/templates used for filling contracts and appendices

**Table Name**: `clm.document_template`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier, auto-incremented |
| `template_name` | VARCHAR(255) | NOT NULL, UNIQUE | Human-readable template name (e.g., "Enterprise NDA v2") |
| `description` | VARCHAR(500) | | Administrative notes/metadata about template purpose |
| `document_format` | document_format_enum | NOT NULL | Format type: PDF or DOCX |
| `document_content` | BYTEA | NOT NULL | Raw (gzip-compressed) binary content of the template file |
| `field_count` | INTEGER | NOT NULL, DEFAULT 0 | Count of placeholders found in the document |
| `is_fully_mapped` | BOOLEAN | NOT NULL, DEFAULT false | True when ALL required fields have labels assigned |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Template upload/creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last modification timestamp (managed by trigger) |

**Indexes**:
- `idx_document_template_name` on `template_name` (lookup by name)
- `idx_document_template_created` on `created_at DESC` (recent templates)
- `idx_dt_name_lower_trgm` GIN trigram on `LOWER(template_name)` (substring search)
- `idx_dt_desc_lower_trgm` GIN trigram on `LOWER(description)` (substring search)

---

### 2. TEMPLATE_FIELD
**Purpose**: Stores individual placeholders extracted from a template

**Table Name**: `clm.template_field`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `template_id` | BIGINT | NOT NULL, FK | References `document_template(id)` with CASCADE delete |
| `field_label` | VARCHAR(255) | | User-friendly field name (e.g., "Client Name"). NULL until mapped. |
| `data_type` | data_type_enum | NOT NULL, DEFAULT 'STRING' | Expected data type: STRING, DATE, NUMBER, BOOLEAN, CURRENCY |
| `field_position` | INTEGER | | Zero-based index of field occurrence in the document |
| `is_required` | BOOLEAN | NOT NULL, DEFAULT true | If true, generation fails without a value |
| `format_pattern` | VARCHAR(255) | | Validation/formatting pattern (e.g., "dd/MM/yyyy", "#,##0.00") |

**Indexes**:
- `idx_template_field_template` on `template_id` (find fields in template)
- `idx_template_field_template_position` on `(template_id, field_position)` (ordered field lookup)

---

### 3. DOCUMENT
**Purpose**: JOINED-inheritance base table — holds columns shared by all document types

**Table Name**: `clm.document`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier, auto-incremented |
| `document_type` | VARCHAR(31) | NOT NULL | Discriminator: `CONTRACT` or `APPENDIX` |
| `template_id` | BIGINT | FK, nullable | References `document_template(id)` RESTRICT. NULL = non-fillable (direct upload) |
| `document_format` | document_format_enum | | Format of the stored document (PDF or DOCX) |
| `document_content` | BYTEA | | Gzip-compressed binary of the unsigned/filled document |
| `signed_document_content` | BYTEA | | Gzip-compressed binary of the signed document |
| `generated_by` | INTEGER | | User ID of the creator (external auth service reference) |
| `generated_by_mail` | VARCHAR(255) | | Email of the creator |
| `notes` | VARCHAR(1000) | | Additional contextual notes |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |

**Indexes**:
- `idx_document_template` on `template_id` (find documents from template)
- `idx_document_type` on `document_type` (filter by type)
- `idx_document_created_at` on `created_at DESC` (recent documents)
- `idx_doc_notes_lower_trgm` GIN trigram on `LOWER(notes)` (substring search)

---

### 4. CONTRACT
**Purpose**: JOINED subclass of `document` — adds contract-specific columns

**Table Name**: `clm.contract`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `document_id` | BIGINT | PRIMARY KEY, FK → document(id) CASCADE | Shared PK with base table |
| `client_id` | INTEGER | NOT NULL | Foreign reference to client (external service) |
| `contract_status` | contract_status_enum | NOT NULL, DEFAULT 'PENDING_SIGNATURE' | Lifecycle: PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED |
| `contract_value` | NUMERIC(12,2) | | Monetary value for reporting (e.g., 50000.00) |
| `contract_start_date` | DATE | | Contract validity start date |
| `contract_end_date` | DATE | | Contract validity end date |
| `termination_date` | DATE | | Date when contract was terminated (NULL if not terminated) |
| `reasons_for_termination` | VARCHAR(1000) | NOT NULL, DEFAULT '' | Reasons for early termination (empty string if not terminated) |

**Indexes**:
- `idx_contract_client` on `client_id`
- `idx_contract_status` on `contract_status`
- `idx_contract_template_client` on `(document_id, client_id)` (composite lookup)
- `idx_contract_validity` on `(contract_start_date, contract_end_date)`
- `idx_contract_termination` on `termination_date DESC WHERE termination_date IS NOT NULL` (partial)
- `idx_contract_active` on `contract_status WHERE contract_status = 'ACTIVE'` (partial)

---

### 5. APPENDIX
**Purpose**: JOINED subclass of `document` — auxiliary files attached to a contract

**Table Name**: `clm.appendix`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `document_id` | BIGINT | PRIMARY KEY, FK → document(id) CASCADE | Shared PK with base table |
| `contract_id` | BIGINT | NOT NULL, FK → contract(document_id) CASCADE | Parent contract; appendix is deleted if contract is deleted |
| `title` | VARCHAR(255) | NOT NULL | Human-readable title of the appendix |
| `appendix_status` | appendix_status_enum | NOT NULL, DEFAULT 'DRAFT' | Lifecycle: DRAFT, SIGNED |

**Indexes**:
- `idx_appendix_contract` on `contract_id` (find all appendices for a contract)
- `idx_appendix_status` on `appendix_status`

**Fillability**: An appendix is considered fillable when `document.template_id IS NOT NULL` (generated from a template). A `NULL` `template_id` means the appendix was uploaded directly (no placeholder injection).

---

### 6. DOCUMENT_FIELD_VALUE
**Purpose**: Audit trail of field values injected into any document

**Table Name**: `clm.document_field_value`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `document_id` | BIGINT | NOT NULL, FK → document(id) CASCADE | The document this value belongs to |
| `template_field_id` | BIGINT | NOT NULL, FK → template_field(id) RESTRICT | The field definition this value satisfies |
| `field_value` | TEXT | NOT NULL | The actual value injected into the document |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Value insertion timestamp |

**Indexes**:
- `idx_document_field_value_document` on `document_id` (find all values in a document)
- `idx_document_field_value_field` on `template_field_id` (find usage of a field)
- `idx_dfv_value_lower_trgm` GIN trigram on `LOWER(field_value)` (substring search)

**Audit Trail Use Case**: When a document is generated, one record is created per injected field value. This allows you to:
- Trace exactly what values were used at generation time
- Meet compliance and auditing requirements
- Search across all documents by injected value content

---

## Indexes

### Index Strategy

Indexes are created to optimize:
1. **Lookup by ID** (Primary Key)
2. **Lookup by Business Key** (`template_name`)
3. **Filtering** (`client_id`, `template_id`, `contract_status`, `appendix_status`)
4. **Date Range Queries** (`contract_start_date`, `contract_end_date`, `created_at`)
5. **Substring Search** (GIN trigram indexes for case-insensitive LIKE patterns)
6. **Partial Indexes** (terminated contracts, active contracts — reduce index footprint)

### Complete Index Reference

| Table | Index Name | Columns | Type | Purpose |
|-------|------------|---------|------|---------|
| `document_template` | PRIMARY | `id` | Unique | Primary key |
| `document_template` | idx_document_template_name | `template_name` | Regular | Lookup by name |
| `document_template` | idx_document_template_created | `created_at DESC` | Regular | Recent templates |
| `document_template` | idx_dt_name_lower_trgm | `lower(template_name)` | GIN trigram | Substring name search |
| `document_template` | idx_dt_desc_lower_trgm | `lower(description)` | GIN trigram | Substring description search |
| `template_field` | PRIMARY | `id` | Unique | Primary key |
| `template_field` | idx_template_field_template | `template_id` | Regular | Find fields in template |
| `template_field` | idx_template_field_template_position | `template_id, field_position` | Composite | Ordered field lookup |
| `document` | PRIMARY | `id` | Unique | Primary key |
| `document` | idx_document_template | `template_id` | Regular | Documents from template |
| `document` | idx_document_type | `document_type` | Regular | Filter by type |
| `document` | idx_document_created_at | `created_at DESC` | Regular | Recent documents |
| `document` | idx_doc_notes_lower_trgm | `lower(notes)` | GIN trigram | Substring notes search |
| `contract` | PRIMARY | `document_id` | Unique | Primary key (shared with document) |
| `contract` | idx_contract_client | `client_id` | Regular | Find contracts for client |
| `contract` | idx_contract_status | `contract_status` | Regular | Filter by status |
| `contract` | idx_contract_template_client | `document_id, client_id` | Composite | Template+Client lookup |
| `contract` | idx_contract_validity | `contract_start_date, contract_end_date` | Composite | Date range queries |
| `contract` | idx_contract_termination | `termination_date DESC WHERE NOT NULL` | Partial | Terminated contracts |
| `contract` | idx_contract_active | `contract_status WHERE ACTIVE` | Partial | Active contracts only |
| `appendix` | PRIMARY | `document_id` | Unique | Primary key (shared with document) |
| `appendix` | idx_appendix_contract | `contract_id` | Regular | Appendices for a contract |
| `appendix` | idx_appendix_status | `appendix_status` | Regular | Filter by status |
| `document_field_value` | PRIMARY | `id` | Unique | Primary key |
| `document_field_value` | idx_document_field_value_document | `document_id` | Regular | Values in a document |
| `document_field_value` | idx_document_field_value_field | `template_field_id` | Regular | Usage of a field |
| `document_field_value` | idx_dfv_value_lower_trgm | `lower(field_value)` | GIN trigram | Substring value search |

### Index Maintenance

**View Index Statistics**:
```sql
SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'clm';
```

**Check Index Usage**:
```sql
SELECT relname, idx_scan, idx_tup_read, idx_tup_fetch 
FROM pg_stat_user_indexes 
WHERE schemaname = 'clm'
ORDER BY idx_scan DESC;
```

**Reindex if Fragmented**:
```sql
REINDEX TABLE clm.document_template;
```

---

## Database Migrations

All schema changes are managed via Flyway version-controlled migrations in `src/main/resources/db/migration/`:

| Migration | Version | Purpose | Status |
|-----------|---------|---------|--------|
| `V1__init_schema.sql` | 1 | Create all 6 tables, enums, indexes, auto-mapping trigger, search functions, and trigram indexes | ACTIVE |

The entire schema is bootstrapped in a single baseline migration. All tables, indexes, triggers, functions, and extensions are defined here.

---

## Constraints & Referential Integrity

### Foreign Keys

| Table | Column | References | Delete Action | Description |
|-------|--------|-----------|---------------|-------------|
| `template_field` | `template_id` | `document_template(id)` | CASCADE | Delete fields if template deleted |
| `document` | `template_id` | `document_template(id)` | RESTRICT | Prevent deleting templates with documents |
| `contract` | `document_id` | `document(id)` | CASCADE | Delete contract row if base document deleted |
| `appendix` | `document_id` | `document(id)` | CASCADE | Delete appendix row if base document deleted |
| `appendix` | `contract_id` | `contract(document_id)` | CASCADE | Delete appendix when its parent contract is deleted |
| `document_field_value` | `document_id` | `document(id)` | CASCADE | Delete field values when document deleted |
| `document_field_value` | `template_field_id` | `template_field(id)` | RESTRICT | Prevent deleting fields with recorded values |

### Unique Constraints

| Table | Columns | Description |
|-------|---------|-------------|
| `document_template` | `template_name` | Only one template per name |

### NOT NULL Constraints

**Critical (Always required)**:
- `document_template.template_name`
- `document_template.document_format`
- `document_template.document_content`
- `document_template.field_count`
- `document_template.is_fully_mapped`
- `template_field.template_id`
- `template_field.data_type`
- `template_field.is_required`
- `document.document_type`
- `document.created_at`
- `contract.document_id`
- `contract.client_id`
- `contract.contract_status`
- `contract.reasons_for_termination`
- `appendix.document_id`
- `appendix.contract_id`
- `appendix.title`
- `appendix.appendix_status`
- `document_field_value.document_id`
- `document_field_value.template_field_id`
- `document_field_value.field_value`

**Optional**:
- `document_template.description`
- `document.template_id` (NULL = direct upload, non-fillable)
- `document.document_format`
- `document.document_content`
- `document.signed_document_content`
- `document.generated_by`
- `document.generated_by_mail`
- `document.notes`
- `template_field.field_label` (NULL = unmapped)
- `template_field.field_position`
- `template_field.format_pattern`
- `contract.contract_value`
- `contract.contract_start_date`
- `contract.contract_end_date`
- `contract.termination_date`

---

## Database Triggers

### Purpose

Triggers enforce business logic at the database level, ensuring correctness even with direct SQL operations.

### 1. trg_template_field_label_mapped

**Table**: `clm.template_field`  
**Event**: AFTER INSERT OR UPDATE OF `field_label`  
**Scope**: FOR EACH ROW

**Function**: `clm.fn_check_template_fully_mapped()`

```sql
CREATE OR REPLACE FUNCTION clm.fn_check_template_fully_mapped()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_template_id       BIGINT;
    v_unmapped_required INTEGER;
BEGIN
    IF NEW.field_label IS NULL THEN
        RETURN NEW;
    END IF;

    v_template_id := NEW.template_id;

    SELECT COUNT(*)
    INTO   v_unmapped_required
    FROM   clm.template_field
    WHERE  template_id = v_template_id
      AND  is_required = TRUE
      AND  field_label IS NULL;

    IF v_unmapped_required = 0 THEN
        UPDATE clm.document_template
        SET    is_fully_mapped = TRUE, updated_at = NOW()
        WHERE  id = v_template_id AND is_fully_mapped = FALSE;
    ELSE
        UPDATE clm.document_template
        SET    is_fully_mapped = FALSE, updated_at = NOW()
        WHERE  id = v_template_id AND is_fully_mapped = TRUE;
    END IF;

    RETURN NEW;
END;
$$;
```

**Behavior**:
- Fires when a `field_label` is set (INSERT or UPDATE with non-NULL value)
- Counts required fields in the template that still lack a label
- If ALL required fields now have labels → sets `is_fully_mapped = TRUE`
- If ANY required field still lacks a label → sets `is_fully_mapped = FALSE`
- Only updates template if the flag actually changes (avoids unnecessary writes)

**Example**:
```sql
-- Template has 3 required fields, none mapped yet
INSERT INTO clm.template_field (template_id, field_position, data_type, is_required)
VALUES (1, 0, 'STRING', true);
-- Trigger fires: 3 unmapped required fields → is_fully_mapped stays FALSE

UPDATE clm.template_field SET field_label = 'Client Name' WHERE id = 1;
-- Trigger fires: 2 unmapped required fields remaining → is_fully_mapped = FALSE

UPDATE clm.template_field SET field_label = 'Address' WHERE id = 2;
UPDATE clm.template_field SET field_label = 'Email'   WHERE id = 3;
-- Trigger fires on last update: 0 unmapped required fields → is_fully_mapped = TRUE
```

---

## Database Functions (Search & Analytics)

### 1. fn_find_documents_by_label_values

**Purpose**: Find documents matching ALL given label values (intersection-based search)

**Signature**:
```sql
clm.fn_find_documents_by_label_values(
    p_label_values     TEXT[],
    p_case_insensitive BOOLEAN DEFAULT TRUE
) RETURNS TABLE (document_id BIGINT)
```

**Use Case**: Search for contracts/appendices where field values contain all specified search terms. Supports case-insensitive substring matching. Returns only documents where ALL search terms appear in some field value.

**Example**:
```sql
SELECT * FROM clm.fn_find_documents_by_label_values(
    ARRAY['John', 'USD'],  -- Find documents with both "John" and "USD"
    TRUE
);
```

---

### 2. fn_search_contracts_advanced

**Purpose**: Advanced contract search with multiple optional filters

**Signature**:
```sql
clm.fn_search_contracts_advanced(
    p_client_id       INTEGER DEFAULT NULL,
    p_contract_status VARCHAR DEFAULT NULL,
    p_generated_by    INTEGER DEFAULT NULL,
    p_created_after   DATE    DEFAULT NULL,
    p_created_before  DATE    DEFAULT NULL,
    p_notes_search    VARCHAR DEFAULT NULL
) RETURNS TABLE (contract_id BIGINT, score FLOAT)
```

**Use Case**: Complex contract search with dynamic optional filters. JOINs `document` and `contract` tables internally.

**Example**:
```sql
SELECT * FROM clm.fn_search_contracts_advanced(
    p_client_id       => 42,
    p_contract_status => 'ACTIVE',
    p_created_after   => '2024-01-01'
);
-- Returns contracts for client 42, active status, created after Jan 1 2024
```

---

## Enum Types

### 1. ContractStatus

**Database Type**: `clm.contract_status_enum`  
**Java Enum**: `clm.demo.models.enums.ContractStatus`  
**Table**: `contract.contract_status`

| Value | Description | Lifecycle Stage |
|-------|-------------|-----------------|
| `PENDING_SIGNATURE` | Contract generated but not yet signed | Initial state |
| `ACTIVE` | Contract signed and currently in effect | Active/operational |
| `TERMINATED` | Contract ended prematurely | End state |
| `ARCHIVED` | Contract completed or expired naturally | End state |

**State Transitions**:
```
PENDING_SIGNATURE → ACTIVE (when signed)
ACTIVE → TERMINATED (early termination)
ACTIVE → ARCHIVED (natural completion)
TERMINATED → ARCHIVED (archive terminated contracts)
```

---

### 2. AppendixStatus

**Database Type**: `clm.appendix_status_enum`  
**Java Enum**: `clm.demo.models.enums.AppendixStatus`  
**Table**: `appendix.appendix_status`

| Value | Description | Lifecycle Stage |
|-------|-------------|-----------------|
| `DRAFT` | Appendix was **generated from a template** — awaiting a signed copy | Initial state (generated only) |
| `SIGNED` | Appendix is fully signed — either uploaded directly, or had a signed copy attached | Terminal state |

**State Transitions**:
```
Generated appendix:  DRAFT → SIGNED (via POST /api/appendices/{id}/upload-signed)
Direct upload:       created directly as SIGNED (no separate signing step)
```

> **Rule**: Directly uploaded appendices (`POST /api/appendices/upload`) skip `DRAFT` entirely and are persisted as `SIGNED` immediately. Only template-generated appendices (`POST /api/appendices/generate`) start as `DRAFT`.

---

### 3. DocumentFormat

**Database Type**: `clm.document_format_enum`  
**Java Enum**: `clm.demo.models.enums.DocumentFormat`  
**Used by**: `document_template.document_format`, `document.document_format`

| Value | Description | Processing Strategy |
|-------|-------------|-------------------|
| `PDF` | Adobe PDF document format | PDFBox library |
| `DOCX` | Microsoft Word document format | Apache POI library |

**Processing Notes**:
- Format is auto-detected from file magic bytes on direct upload (no format parameter needed)
- All generated/filled appendices are converted to PDF after template filling
- Document binary is stored gzip-compressed in `document_content` BYTEA column

---

### 4. DataType

**Database Type**: `clm.data_type_enum`  
**Java Enum**: `clm.demo.models.enums.DataType`  
**Table**: `template_field.data_type`

| Value | Description | Default Format Pattern | Example |
|-------|-------------|----------------------|---------|
| `STRING` | Text/string values | (none) | "John Doe" |
| `DATE` | Calendar dates | `dd/MM/yyyy` | "01/15/2024" |
| `NUMBER` | Numeric values | `#,##0.00` | "1,234.56" |
| `BOOLEAN` | True/False values | (none) | "true", "Yes" |
| `CURRENCY` | Monetary values | `$#,##0.00` | "$1,234.56" |

**Format Pattern Syntax**:
- **Dates**: Java `DateTimeFormatter` syntax (e.g., `dd/MM/yyyy`, `yyyy-MM-dd`)
- **Numbers**: Java `DecimalFormat` syntax (e.g., `#,##0.00`, `0.00%`)

**Example Field Configuration**:
```sql
-- Amount field with currency format
INSERT INTO clm.template_field (template_id, field_label, data_type, format_pattern, is_required)
VALUES (1, 'Contract Amount', 'CURRENCY', '$#,##0.00', true);

-- Contract end date with custom format
INSERT INTO clm.template_field (template_id, field_label, data_type, format_pattern, is_required)
VALUES (1, 'End Date', 'DATE', 'dd MMMM yyyy', true);
```

---

## JPA Entity Reference

### 1. Document (Abstract Base)

**Package**: `clm.demo.models`  
**File**: `Document.java`  
**ORM Table**: `clm.document`

```java
@Entity
@Table(name = "document", schema = "clm")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "document_type", discriminatorType = DiscriminatorType.STRING)
@Getter @Setter @NoArgsConstructor @SuperBuilder
public abstract class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private DocumentTemplate documentTemplate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_format")
    private DocumentFormat documentFormat;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "document_content")
    private byte[] documentContent;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "signed_document_content")
    private byte[] signedDocumentContent;

    private Integer generatedBy;
    private String generatedByMail;
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<DocumentFieldValue> fieldValues = new ArrayList<>();
}
```

**Key Annotations**:
- `@Inheritance(JOINED)`: Subclass columns stored in separate tables, joined on PK
- `@DiscriminatorColumn`: JPA writes `CONTRACT` or `APPENDIX` into `document_type`
- `@SuperBuilder`: Required for `@Builder` across inheritance hierarchies (Lombok)
- `@BatchSize(size = 50)`: Prevents N+1 when loading `fieldValues`

---

### 2. Contract Entity

**Package**: `clm.demo.models`  
**File**: `Contract.java`  
**ORM Tables**: `clm.document` + `clm.contract` (JOINED)

```java
@Entity
@Table(name = "contract", schema = "clm")
@DiscriminatorValue("CONTRACT")
@PrimaryKeyJoinColumn(name = "document_id")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
@EqualsAndHashCode(callSuper = false) @ToString(callSuper = false)
public class Contract extends Document {

    private Integer clientId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contract_status")
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING_SIGNATURE;

    @Column(name = "contract_value", precision = 12, scale = 2)
    private BigDecimal contractValue;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private LocalDate terminationDate;

    @Builder.Default
    private String reasonsForTermination = "";

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Appendix> appendices = new ArrayList<>();
}
```

**Key Design**:
- `clientId`: Raw Integer — client lives in a different service/schema
- `@PrimaryKeyJoinColumn(name = "document_id")`: Links to `document.id` via `contract.document_id`
- `@EqualsAndHashCode(callSuper = false)`: Avoids Hibernate proxy issues with inherited equals

---

### 3. Appendix Entity

**Package**: `clm.demo.models`  
**File**: `Appendix.java`  
**ORM Tables**: `clm.document` + `clm.appendix` (JOINED)

```java
@Entity
@Table(name = "appendix", schema = "clm")
@DiscriminatorValue("APPENDIX")
@PrimaryKeyJoinColumn(name = "document_id")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
@EqualsAndHashCode(callSuper = false) @ToString(callSuper = false)
public class Appendix extends Document {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "appendix_status")
    @Builder.Default
    private AppendixStatus appendixStatus = AppendixStatus.DRAFT;
}
```

**Key Design**:
- `contract`: Many-to-one relationship; appendix is always linked to exactly one contract
- `title`: Required user-facing name for the appendix
- Fillable appendix: `documentTemplate != null` (generated from template with field injection)
- Non-fillable appendix: `documentTemplate == null` (direct file upload)

---

### 4. DocumentTemplate Entity

**Package**: `clm.demo.models`  
**File**: `DocumentTemplate.java`  
**ORM Table**: `clm.document_template`

```java
@Entity
@Table(name = "document_template", schema = "clm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"documentContent", "templateFields"})
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, unique = true, length = 255)
    private String templateName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_format", nullable = false)
    private DocumentFormat documentFormat;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "document_content", nullable = false)
    private byte[] documentContent;

    @Column(name = "field_count", nullable = false)
    @Builder.Default
    private Integer fieldCount = 0;

    @Column(name = "is_fully_mapped", nullable = false)
    @Builder.Default
    private Boolean isFullyMapped = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "documentTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> templateFields = new ArrayList<>();
}
```

**Relationships**:
- `1:N` → `TemplateField` (one template has many fields, cascade delete)
- Referenced by `document.template_id` (RESTRICT delete — cannot delete a template that has documents)

---

### 5. TemplateField Entity

**Package**: `clm.demo.models`  
**File**: `TemplateField.java`  
**ORM Table**: `clm.template_field`

```java
@Entity
@Table(name = "template_field", schema = "clm", indexes = {
    @Index(name = "idx_template_field_template_position", columnList = "template_id, field_position")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"documentTemplate"})
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate documentTemplate;

    @Column(name = "field_label", length = 255)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "data_type", nullable = false)
    @Builder.Default
    private DataType dataType = DataType.STRING;

    @Column(name = "field_position")
    private Integer fieldPosition;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "format_pattern", length = 255)
    private String formatPattern;
}
```

**Key Fields**:
- `fieldLabel`: NULL initially; set during the mapping phase. The trigger fires and updates `is_fully_mapped` on the parent template
- `fieldPosition`: Zero-based order of appearance in the document — used to fill fields in sequence

---

### 6. DocumentFieldValue Entity

**Package**: `clm.demo.models`  
**File**: `DocumentFieldValue.java`  
**ORM Table**: `clm.document_field_value`

```java
@Entity
@Table(name = "document_field_value", schema = "clm", indexes = {
    @Index(name = "idx_document_field_value_document", columnList = "document_id"),
    @Index(name = "idx_document_field_value_field",    columnList = "template_field_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"document", "templateField"})
public class DocumentFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id", nullable = false)
    private TemplateField templateField;

    @Column(name = "field_value", nullable = false, columnDefinition = "TEXT")
    private String fieldValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Purpose**: Immutable audit record — one row per field value injected into a document at generation time.

**Relationships**:
- `N:1` → `Document` (base class — works for both contracts and appendices)
- `N:1` → `TemplateField` (lazy loaded)
