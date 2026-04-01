# CLM Platform - Database Documentation

## Overview

The CLM (Contract Lifecycle Management) Platform uses a **PostgreSQL relational database** to manage contract templates, generated contracts, and their associated field values. The system is designed to:

- **Store contract templates** (document blueprints with placeholders)
- **Extract and map template fields** (placeholders) for data injection
- **Generate contracts** by merging templates with client-specific data
- **Maintain audit trails** of all injected field values
- **Track contract lifecycle** (PENDING_SIGNATURE → ACTIVE → TERMINATED/ARCHIVED)
- **Support advanced search** with trigram indexing and database functions

**Key Characteristics:**
- **Database**: PostgreSQL 12+
- **Schema**: `contracts` (isolated namespace)
- **Tables**: 4 core tables with comprehensive indexing
- **Data Volume**: Supports BYTEA storage for document binary content
- **Migration Tool**: Flyway (versioned migrations, V1-V6)
- **ORM**: Hibernate JPA (Spring Data JPA with Lombok annotations)
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
└── Schema: contracts
    ├── contract_template (core table)
    ├── template_field (child of contract_template)
    ├── generated_contract (core table)
    └── contract_field_value (audit trail)
```

### Key Design Principles

1. **Isolated Schema**: All tables exist in the `contracts` schema to allow multi-tenancy/separation from other microservices
2. **BYTEA Storage**: Document binaries stored directly in the database for transactional consistency
3. **Lazy Loading**: Relationships use `FetchType.LAZY` by default to optimize performance
4. **Foreign Keys**: Enforce referential integrity with CASCADE/RESTRICT delete rules
5. **Audit Trail**: Timestamps on all records for compliance and debugging

---

## Entity-Relationship Diagram

```
┌─────────────────────────┐
│   CONTRACT_TEMPLATE     │  (Blueprint)
├─────────────────────────┤
│ id (LONG)                 │
│ template_name (UNIQUE)  │
│ description             │
│ document_format (ENUM)  │
│ document_content (BYTEA)│
│ field_count             │
│ is_fully_mapped         │
│ created_at              │
│ updated_at              │
└──────────┬──────────────┘
           │ (1:N)
           │
    ┌──────▼──────────────────┐
    │   TEMPLATE_FIELD        │  (Placeholders)
    ├─────────────────────────┤
    │ id (PK)                 │
    │ template_id (FK)        │
    │ field_label             │
    │ data_type (ENUM)        │
    │ placeholder_text        │
    │ field_position          │
    │ is_required             │
    │ format_pattern          │
    │ created_at              │
    └─────────────────────────┘

┌─────────────────────────┐
│  GENERATED_CONTRACT     │  (Final Filled Document)
├─────────────────────────┤
│ id (PK)                 │
│ template_id (FK)        │◄───────┐
│ client_id (External)    │        │
│ contract_status (ENUM)  │        │
│ generated_by (External) │        │
│ generated_by_mail       │        │
│ document_content (BYTEA)│        │
│ contract_value          │        │
│ contract_start_date     │        │
│ contract_end_date       │        │
│ notes                   │        │
│ created_at              │        │
└──────────┬──────────────┘        │
           │ (1:N)                 │
           │        ┌──────────────┘
    ┌──────▼──────────────────┐
    │ CONTRACT_FIELD_VALUE    │  (Audit Trail)
    ├─────────────────────────┤
    │ id (PK)                 │
    │ generated_contract_id   │
    │ template_field_id (FK)  │
    │ field_value (TEXT)      │
    │ created_at              │
    └─────────────────────────┘
```

---

## Tables Reference

### 1. CONTRACT_TEMPLATE
**Purpose**: Stores contract document blueprints/templates

**Table Name**: `contracts.contract_template`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier, auto-incremented |
| `template_name` | VARCHAR(255) | NOT NULL, UNIQUE | Human-readable template name (e.g., "Enterprise NDA v2") |
| `description` | VARCHAR(500) | | Administrative notes/metadata about template purpose |
| `document_format` | document_format_enum | NOT NULL | Format type: PDF, DOCX, or ZIP |
| `document_content` | BYTEA | NOT NULL | Raw binary content of the template file |
| `field_count` | INTEGER | NOT NULL, DEFAULT 0 | Count of placeholders found in document |
| `is_fully_mapped` | BOOLEAN | NOT NULL, DEFAULT false | True when ALL required fields have labels assigned |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Template upload/creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last modification timestamp |

**Indexes**:
- `idx_contract_template_name` on `template_name` (lookup by name)
- `idx_contract_template_created` on `created_at DESC` (recent templates)

---

### 2. TEMPLATE_FIELD
**Purpose**: Stores individual placeholders extracted from templates

**Table Name**: `contracts.template_field`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `template_id` | BIGINT | NOT NULL, FK | References `contract_template(id)` with CASCADE delete |
| `field_label` | VARCHAR(255) | | User-friendly field name (e.g., "Client Name"). NULL until mapped. |
| `data_type` | data_type_enum | NOT NULL, DEFAULT 'STRING' | Expected data type: STRING, DATE, NUMBER, BOOLEAN, CURRENCY |
| `field_position` | INTEGER | | Zero-based index of field occurrence in document |
| `is_required` | BOOLEAN | NOT NULL, DEFAULT true | If true, generation fails without a value |
| `format_pattern` | VARCHAR(255) | | Validation/formatting pattern (e.g., "dd/MM/yyyy", "#,##0.00") |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Field creation timestamp |

**Indexes**:
- `idx_template_field_template_position` on `(template_id, field_position)`
- `idx_template_field_template` on `template_id`

---

### 3. GENERATED_CONTRACT
**Purpose**: Stores final filled/generated contracts for clients

**Table Name**: `contracts.generated_contract`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `template_id` | BIGINT | NOT NULL, FK | References `contract_template(id)` with RESTRICT delete |
| `client_id` | INTEGER | NOT NULL | Foreign reference to client (different schema/service) |
| `contract_status` | contract_status_enum | NOT NULL, DEFAULT 'PENDING_SIGNATURE' | Lifecycle: PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED |
| `generated_by` | INTEGER | | User ID of the person who generated the contract |
| `generated_by_mail` | VARCHAR(255) | | Email address of the generating user |
| `document_content` | BYTEA | NOT NULL | Binary content of the filled/unsigned document |
| `signed_document_content` | BYTEA | | Binary content of the signed document (populated after signing) |
| `contract_value` | NUMERIC(12,2) | | Monetary value for reporting (e.g., 50000.00) |
| `contract_start_date` | DATE | | Contract validity start date |
| `contract_end_date` | DATE | | Contract validity end date |
| `notes` | VARCHAR(1000) | | Additional contextual notes |
| `termination_date` | DATE | | Date when contract was terminated (NULL if active) |
| `reasons_for_termination` | VARCHAR(1000) | NOT NULL, DEFAULT '' | Reasons for termination (empty string if not terminated) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Generation timestamp |

**Indexes**:
- `idx_generated_contract_template_client` on `(template_id, client_id)` (find contracts for specific template+client)
- `idx_generated_contract_validity` on `(contract_start_date, contract_end_date)` (date range queries)
- `idx_generated_contract_created_at` on `created_at DESC` (recent contracts)
- `idx_generated_contract_template` on `template_id`
- `idx_generated_contract_client` on `client_id`
- `idx_generated_contract_status` on `contract_status`

---

### 4. CONTRACT_FIELD_VALUE
**Purpose**: Audit trail of field values injected into generated contracts

**Table Name**: `contracts.contract_field_value`

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `generated_contract_id` | BIGINT | NOT NULL, FK | References `generated_contract(id)` with CASCADE delete |
| `template_field_id` | BIGINT | NOT NULL, FK | References `template_field(id)` with RESTRICT delete |
| `field_value` | TEXT | NOT NULL | The actual value injected into the document |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Value insertion timestamp |

**Indexes**:
- `idx_contract_field_value_contract` on `generated_contract_id` (find all fields in a contract)
- `idx_contract_field_value_field` on `template_field_id`
- `idx_contract_field_value_contract_field` on `(generated_contract_id, template_field_id)` (specific field lookup)

**Audit Trail Use Case**: When a contract is generated, for each field injected, a record is created here. This allows you to:
- Trace exactly what values were used at generation time
- Detect if a field value was modified after generation
- Meet compliance/auditing requirements

---

## Indexes

### Index Strategy

Indexes are created to optimize:
1. **Lookup by ID** (Primary Key)
2. **Lookup by Business Key** (template_name)
3. **Filtering** (client_id, template_id, status, contract_status)
4. **Date Range Queries** (contract_start_date, contract_end_date, created_at)
5. **Substring Search** (GIN trigram indexes for case-insensitive LIKE patterns)
6. **Composite Queries** (template+client, status+client combinations)

### Complete Index Reference

**Primary Indexes (V1)**:

| Table | Index Name | Columns | Type | Purpose |
|-------|------------|---------|------|---------|
| `contract_template` | PRIMARY | `id` | Unique | Primary key |
| `contract_template` | idx_contract_template_name | `template_name` | Unique | Lookup by name |
| `contract_template` | idx_contract_template_created | `created_at DESC` | Regular | Recent templates |
| `template_field` | PRIMARY | `id` | Unique | Primary key |
| `template_field` | idx_template_field_template | `template_id` | Regular | Find fields in template |
| `template_field` | idx_template_field_template_position | `template_id, field_position` | Composite | Lookup field by position |
| `generated_contract` | PRIMARY | `id` | Unique | Primary key |
| `generated_contract` | idx_generated_contract_template_client | `template_id, client_id` | Composite | Template+Client lookup |
| `generated_contract` | idx_generated_contract_validity | `contract_start_date, contract_end_date` | Composite | Date range queries |
| `generated_contract` | idx_generated_contract_created_at | `created_at DESC` | Regular | Recent contracts |
| `contract_field_value` | PRIMARY | `id` | Unique | Primary key |
| `contract_field_value` | idx_contract_field_value_contract | `generated_contract_id` | Regular | Find values in contract |
| `contract_field_value` | idx_contract_field_value_field | `template_field_id` | Regular | Find usage of field |

**Search Indexes (V2-V4)**:

| Table | Index Name | Columns | Type | Purpose |
|-------|------------|---------|------|---------|
| `generated_contract` | idx_generated_contract_status | `contract_status` | Regular | Filter by status |
| `generated_contract` | idx_generated_contract_client_id | `client_id` | Regular | Filter by client |
| `generated_contract` | idx_generated_contract_generated_by | `generated_by` | Regular | Filter by generator |
| `generated_contract` | idx_generated_contract_created_date_range | `created_at DESC` | Regular | Date range queries |
| `contract_template` | idx_ct_name_lower_trgm | `lower(template_name)` | GIN trigram | Substring search in names |
| `contract_template` | idx_ct_desc_lower_trgm | `lower(description)` | GIN trigram | Substring search in descriptions |
| `generated_contract` | idx_gc_notes_lower_trgm | `lower(notes)` | GIN trigram | Substring search in notes |
| `contract_field_value` | idx_cfv_field_value_lower_trgm | `lower(field_value)` | GIN trigram | Substring search in field values |

**Removed Indexes (V6)**:
- `idx_generated_contract_status_client` - Composite index not used in dynamic filtering
- `idx_contract_template_name_status` - Composite index not used in dynamic filtering
- `idx_contract_field_value_contract_field` - Composite index not used in dynamic filtering
- `idx_generated_contract_search_composite` - Composite index not used in dynamic filtering
- `idx_generated_contract_notes_btree` - Replaced by GIN trigram index
- `idx_contract_field_value_field_value_btree` - Replaced by GIN trigram index
| `generated_contract` | PRIMARY | `id` | Unique | Primary key |
| `generated_contract` | idx_generated_contract_template | `template_id` | Regular | Find contracts for template |
| `generated_contract` | idx_generated_contract_client | `client_id` | Regular | Find contracts for client |
| `generated_contract` | idx_generated_contract_status | `contract_status` | Regular | Filter by status |
| `generated_contract` | idx_generated_contract_template_client | `template_id, client_id` | Composite | Template+Client lookup |
| `generated_contract` | idx_generated_contract_validity | `contract_start_date, contract_end_date` | Composite | Date range queries |
| `generated_contract` | idx_generated_contract_created_at | `created_at DESC` | Regular | Recent contracts |
| `generated_contract` | idx_generated_contract_termination_date | `termination_date DESC WHERE termination_date IS NOT NULL` | Partial | Terminated contracts |
| `generated_contract` | idx_generated_contract_signed | `contract_status WHERE contract_status = 'ACTIVE'` | Partial | Active/signed contracts |
| `contract_field_value` | PRIMARY | `id` | Unique | Primary key |
| `contract_field_value` | idx_contract_field_value_contract | `generated_contract_id` | Regular | Find values in contract |
| `contract_field_value` | idx_contract_field_value_field | `template_field_id` | Regular | Find usage of field |

### Index Maintenance

**View Index Statistics**:
```sql
SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'contracts';
```

**Check Index Usage**:
```sql
SELECT relname, idx_scan, idx_tup_read, idx_tup_fetch 
FROM pg_stat_user_indexes 
WHERE schemaname = 'contracts'
ORDER BY idx_scan DESC;
```

**Reindex if Fragmented**:
```sql
REINDEX TABLE contracts.contract_template;
```

---

## Database Migrations

All schema changes are managed via Flyway version-controlled migrations in `src/main/resources/db/migration/`:

| Migration | Version | Purpose | Status |
|-----------|---------|---------|--------|
| `V1__init_schema.sql` | 1 | Create 4 core tables with indexes, enums, and auto-mapping trigger | ACTIVE |
| `V2__optimize_search_indexes.sql` | 2 | Add single-column and composite indexes for search optimization | ACTIVE |
| `V3__add_search_functions.sql` | 3 | Add database functions for label value intersection and advanced search | ACTIVE |
| `V4__trigram_search_indexes.sql` | 4 | Enable pg_trgm extension and create GIN trigram indexes for substring search | ACTIVE |
| `V5__remove_placeholder_text_column.sql` | 5 | Remove `placeholder_text` column (normalized via regex matching) | ACTIVE |
| `V6__cleanup_unused_indexes.sql` | 6 | Remove unused composite indexes from V2 | ACTIVE |

---

## Constraints & Referential Integrity

### Foreign Keys

| Table | Column | References | Delete Action | Update Action | Description |
|-------|--------|-----------|---------------|---------------|-------------|
| `template_field` | `template_id` | `contract_template(id)` | CASCADE | CASCADE | Delete field if template deleted |
| `generated_contract` | `template_id` | `contract_template(id)` | RESTRICT | CASCADE | Prevent deleting templates with contracts |
| `contract_field_value` | `generated_contract_id` | `generated_contract(id)` | CASCADE | CASCADE | Delete field values if contract deleted |
| `contract_field_value` | `template_field_id` | `template_field(id)` | RESTRICT | CASCADE | Prevent deleting fields with values |

### Unique Constraints

| Table | Columns | Description |
|-------|---------|-------------|
| `contract_template` | `template_name` | Only one template per name |

### Check Constraints

| Table | Condition | Description |
|-------|-----------|-------------|
| `contract_template` | `field_count >= 0` | (Implicit via INTEGER type) |

### NOT NULL Constraints

**Critical (Always required)**:
- `contract_template.template_name`
- `contract_template.document_format`
- `contract_template.document_content`
- `contract_template.field_count`
- `contract_template.is_fully_mapped`
- `template_field.template_id`
- `template_field.data_type`
- `template_field.is_required`
- `generated_contract.template_id`
- `generated_contract.client_id`
- `generated_contract.contract_status`
- `generated_contract.document_content`
- `contract_field_value.generated_contract_id`
- `contract_field_value.template_field_id`
- `contract_field_value.field_value`

**Optional**:
- `contract_template.description`
- `template_field.field_label` (NULL = unmapped)
- `template_field.field_position`
- `template_field.format_pattern`
- `generated_contract.generated_by`
- `generated_contract.generated_by_mail`
- `generated_contract.contract_value`
- `generated_contract.contract_start_date`
- `generated_contract.contract_end_date`
- `generated_contract.notes`
- `generated_contract.termination_date`

---

## Database Triggers

### Purpose

Triggers enforce business logic and maintain data consistency at the database level, ensuring correctness even with direct SQL operations.

### 1. trg_template_field_label_mapped

**Table**: `template_field`  
**Event**: AFTER INSERT OR UPDATE OF field_label  
**Scope**: FOR EACH ROW

**Function**: `fn_check_template_fully_mapped()`

```sql
CREATE OR REPLACE FUNCTION contracts.fn_check_template_fully_mapped()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_template_id       BIGINT;
    v_unmapped_required INTEGER;
BEGIN
    -- Only act when field_label changes to a non-null value
    IF NEW.field_label IS NULL THEN
        RETURN NEW;
    END IF;

    v_template_id := NEW.template_id;

    -- Count required fields that still have no label
    SELECT COUNT(*)
    INTO   v_unmapped_required
    FROM   contracts.template_field
    WHERE  template_id  = v_template_id
      AND  is_required  = TRUE
      AND  field_label  IS NULL;

    IF v_unmapped_required = 0 THEN
        -- Every required field has a label → mark template as fully mapped
        UPDATE contracts.contract_template
        SET    is_fully_mapped = TRUE,
               updated_at      = NOW()
        WHERE  id = v_template_id
          AND  is_fully_mapped = FALSE;
    ELSE
        -- At least one required field still unmapped → ensure flag is FALSE
        UPDATE contracts.contract_template
        SET    is_fully_mapped = FALSE,
               updated_at      = NOW()
        WHERE  id = v_template_id
          AND  is_fully_mapped = TRUE;
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
INSERT INTO contracts.template_field (template_id, field_position, data_type, is_required)
VALUES (1, 0, 'STRING', true);
-- Trigger fires: counts 3 unmapped required fields
-- Result: is_fully_mapped stays FALSE (or is set to FALSE if new)

-- Map the first field
UPDATE contracts.template_field 
SET field_label = 'Client Name' 
WHERE id = 1;
-- Trigger fires: counts 2 unmapped required fields remaining
-- Result: is_fully_mapped = FALSE

-- Map remaining two fields
UPDATE contracts.template_field SET field_label = 'Address' WHERE id = 2;
UPDATE contracts.template_field SET field_label = 'Email' WHERE id = 3;
-- Trigger fires on second update: counts 0 unmapped required fields
-- Result: is_fully_mapped = TRUE (template is now ready for generation)
```

**Optimization**:
- Only activates when `field_label` is changed (not for other column updates)
- Uses `v_unmapped_required = 0` check to avoid expensive full query scans
- Created as AFTER trigger to use NEW values (already persisted)

---

## Database Functions (Search & Analytics)

Database functions created in V3 to optimize search operations:

### 1. fn_find_contracts_by_label_values

**Purpose**: Find contracts matching ALL label values (intersection-based search)

**Signature**:
```sql
fn_find_contracts_by_label_values(
    p_label_values TEXT[],
    p_case_insensitive BOOLEAN DEFAULT TRUE
) RETURNS TABLE (contract_id BIGINT)
```

**Use Case**: Search for contracts where field values contain all specified search terms
- Supports case-insensitive substring matching
- Returns only contracts where ALL search terms are found in some field value

**Example**:
```sql
SELECT * FROM contracts.fn_find_contracts_by_label_values(
    ARRAY['John', 'USD'],  -- Find contracts with both "John" and "USD"
    TRUE
);
```

---

### 2. fn_get_contract_search_summary

**Purpose**: Retrieve contract details with all field values aggregated

**Signature**:
```sql
fn_get_contract_search_summary(p_contract_id BIGINT)
RETURNS TABLE (
    contract_id BIGINT,
    template_name VARCHAR,
    client_id INTEGER,
    contract_status VARCHAR,
    field_count BIGINT,
    field_values_csv TEXT,
    created_at TIMESTAMP
)
```

**Use Case**: Get a complete summary of a contract including all injected field values

**Example**:
```sql
SELECT * FROM contracts.fn_get_contract_search_summary(123);
-- Returns: contract details with all field values as comma-separated string
```

---

### 3. fn_search_contracts_advanced

**Purpose**: Advanced search with multiple optional filters

**Signature**:
```sql
fn_search_contracts_advanced(
    p_client_id INTEGER DEFAULT NULL,
    p_contract_status VARCHAR DEFAULT NULL,
    p_generated_by INTEGER DEFAULT NULL,
    p_created_after DATE DEFAULT NULL,
    p_created_before DATE DEFAULT NULL,
    p_notes_search VARCHAR DEFAULT NULL
) RETURNS TABLE (contract_id BIGINT, score FLOAT)
```

**Use Case**: Complex contract search with dynamic optional filters

**Example**:
```sql
SELECT * FROM contracts.fn_search_contracts_advanced(
    p_client_id => 42,
    p_contract_status => 'ACTIVE',
    p_created_after => '2024-01-01'
);
-- Returns: contracts for client 42, active status, created after Jan 1 2024
```

---

---

## Enum Types

### 1. ContractStatus

**Database Type**: `contracts.contract_status_enum`  
**Java Enum**: `clm.demo.models.enums.ContractStatus`  
**Table**: `generated_contract.contract_status`

| Value | Description | Lifecycle Stage |
|-------|-------------|-----------------|
| `PENDING_SIGNATURE` | Contract generated but not yet signed | Initial state |
| `ACTIVE` | Contract signed and currently in effect | Active/operational |
| `TERMINATED` | Contract ended prematurely (before natural end date) | End state |
| `ARCHIVED` | Contract completed or expired naturally | End state |

**State Transitions**:
```
PENDING_SIGNATURE → ACTIVE (when signed)
         ↓
      ACTIVE → TERMINATED (if terminated early)
         ↓
      ACTIVE → ARCHIVED (if contract completes naturally)
      TERMINATED → ARCHIVED (can archive terminated contracts)
```

---

### 2. DocumentFormat

**Database Type**: `contracts.document_format_enum`  
**Java Enum**: `clm.demo.models.enums.DocumentFormat`  
**Table**: `contract_template.document_format`

| Value | Description | Processing Strategy |
|-------|-------------|-------------------|
| `PDF` | Adobe PDF document format | Using PDFBox library |
| `DOCX` | Microsoft Word document format | Using Apache POI library |

**Processing Notes**:
- Format determines which parsing/generation library is used
- ZIP archives containing multiple formats are also supported (extracted to PDF/DOCX internally)
- Document binary is stored in `document_content` BYTEA column

---

### 3. DataType

**Database Type**: `contracts.data_type_enum`  
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
INSERT INTO contracts.template_field 
(template_id, field_label, data_type, format_pattern, is_required)
VALUES (1, 'Contract Amount', 'CURRENCY', '$#,##0.00', true);

-- Contract end date with custom format
INSERT INTO contracts.template_field 
(template_id, field_label, data_type, format_pattern, is_required)
VALUES (1, 'End Date', 'DATE', 'dd MMMM yyyy', true);
```

---

### 1. Template Entity

**Package**: `clm.demo.models`  
**File**: `Template.java`  
**ORM Table**: `contracts.contract_template`

```java
@Entity
@Table(name = "contract_template", schema = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    
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
    
    @OneToMany(mappedBy = "contractTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> templateFields = new ArrayList<>();
}
```

**Annotations Explained**:
- `@Entity`: Maps to database table
- `@Table(name = "contract_template", schema = "contracts")`: Specifies table and schema
- `@Data`: Lombok generates getters/setters/equals/hashCode
- `@Builder`: Provides builder pattern construction
- `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`: Maps enum to PostgreSQL ENUM type
- `@Lob + @JdbcTypeCode(VARBINARY)`: Maps byte[] to BYTEA
- `@CreationTimestamp`: Auto-set on insert (Hibernate)
- `@UpdateTimestamp`: Auto-set on update (Hibernate)
- `@OneToMany(mappedBy = "contractTemplate")`: Relationship mapped on child side

**Enums**:
- `DocumentFormat`: PDF, DOCX

**Relationships**:
- `1:N` → `TemplateField` (one template has many fields)

---

### 2. TemplateField Entity

**Package**: `clm.demo.models`  
**File**: `TemplateField.java`  
**ORM Table**: `contracts.template_field`

```java
@Entity
@Table(name = "template_field", schema = "contracts", indexes = {
    @Index(name = "idx_template_field_template_position", columnList = "template_id, field_position")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template contractTemplate;
    
    @Column(name = "field_label", length = 255)
    @Builder.Default
    private String fieldLabel = null;
    
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

**Enums**:
- `DataType`: STRING, DATE, NUMBER, BOOLEAN, CURRENCY

**Relationships**:
- `N:1` → `Template` (lazy loaded)

**Key Fields**:
- `fieldLabel`: NULL initially, set during mapping phase. When all required fields have labels, template becomes "fully mapped"
- `formatPattern`: For dates use "dd/MM/yyyy", for numbers use "#,##0.00"
- `field_position`: Zero-based index tracking the order of appearance in the document

---

### 3. Contract Entity (Generated Contract)

**Package**: `clm.demo.models`  
**File**: `Contract.java`  
**ORM Table**: `contracts.generated_contract`

```java
@Entity
@Table(name = "generated_contract", schema = "contracts", indexes = {
    @Index(name = "idx_generated_contract_template_client", columnList = "template_id, client_id"),
    @Index(name = "idx_generated_contract_validity", columnList = "contract_start_date, contract_end_date"),
    @Index(name = "idx_generated_contract_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template contractTemplate;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contract_status", nullable = false)
    @Builder.Default
    private ContractStatus contractStatus = ContractStatus.PENDING_SIGNATURE;
    
    @Column(name = "generated_by")
    private Integer generatedBy;
    
    @Column(name = "generated_by_mail", length = 255)
    private String generatedByMail;
    
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "document_content", nullable = false)
    private byte[] documentContent;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "signed_document_content")
    private byte[] signedDocument;
    
    @Column(name = "contract_value", precision = 12, scale = 2)
    private BigDecimal contractValue;
    
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;
    
    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    @Column(name = "reasons_for_termination", length = 1000)
    @Builder.Default
    private String reasonsForTermination = "";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<ContractFieldValue> fieldValues = new ArrayList<>();
}
```

**Enums**:
- `ContractStatus`: PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED

**Key Design**:
- `clientId`: Raw Integer (no JPA relationship) - client is in different schema
- `generatedBy`: User ID from external service
- `generatedByMail`: Email address for notifications
- `documentContent`: Final filled unsigned document stored as BYTEA
- `signedDocument`: Signed version of the document (set when status = ACTIVE)
- `terminationDate`: Date when contract was terminated (NULL if active)
- `reasonsForTermination`: Reason for early termination (empty string if not terminated)

**Relationships**:
- `N:1` → `Template` (lazy loaded)
- `1:N` → `ContractFieldValue` (audit trail with batch loading for performance)

---

### 4. ContractFieldValue Entity

**Package**: `clm.demo.models`  
**File**: `ContractFieldValue.java`  
**ORM Table**: `contracts.contract_field_value`

```java
@Entity
@Table(name = "contract_field_value", schema = "contracts", indexes = {
    @Index(name = "idx_contract_field_value_contract", columnList = "generated_contract_id"),
    @Index(name = "idx_contract_field_value_field", columnList = "template_field_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractFieldValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_contract_id", nullable = false)
    private Contract contract;
    
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

**Purpose**: Audit trail - record exactly what value was injected for each field at generation time

**Relationships**:
- `N:1` → `Contract` (lazy loaded)
- `N:1` → `TemplateField` (lazy loaded)

---
