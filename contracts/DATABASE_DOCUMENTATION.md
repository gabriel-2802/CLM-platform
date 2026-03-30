# CLM Platform - Database Documentation

## Overview

The CLM (Contract Lifecycle Management) Platform uses a **PostgreSQL relational database** to manage contract templates, generated contracts, and their associated field values. The system is designed to:

- **Store contract templates** (document blueprints with placeholders)
- **Extract and map template fields** (placeholders) for data injection
- **Generate contracts** by merging templates with client-specific data
- **Maintain audit trails** of all injected field values
- **Track contract lifecycle** (GENERATED → SIGNED → ARCHIVED/VOID)

**Key Characteristics:**
- **Database**: PostgreSQL
- **Schema**: `contracts` (isolated namespace)
- **Tables**: 4 core tables + audit trail
- **Data Volume**: Supports BYTEA storage for document binary content
- **Migration Tool**: Flyway (versioned migrations)
- **ORM**: Hibernate JPA (Spring Data JPA)


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
| `document_format` | document_format_enum | NOT NULL | Format type: PDF or DOCX |
| `document_content` | BYTEA | NOT NULL | Raw binary content of the template file |
| `field_count` | INTEGER | NOT NULL, DEFAULT 0 | Count of placeholders found in document |
| `is_fully_mapped` | BOOLEAN | NOT NULL, DEFAULT false | True when ALL fields have labels assigned |
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
| `data_type` | data_type_enum | NOT NULL, DEFAULT 'STRING' | Expected data type: STRING, DATE, NUMBER, BOOLEAN, CURRENCY, ENUM |
| `placeholder_text` | VARCHAR(255) | | The actual placeholder pattern found (e.g., "......") |
| `field_position` | INTEGER | | Zero-based index of field occurrence in document |
| `is_required` | BOOLEAN | NOT NULL, DEFAULT true | If true, generation fails without a value |
| `format_pattern` | VARCHAR(255) | | Validation/formatting pattern (e.g., "dd/MM/yyyy", "#,##0.00") |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | Field creation timestamp |

**Indexes**:
- `idx_template_field_template_position` on `(template_id, field_position)`
- `idx_template_field_template_name` on `(template_id, field_label)`
- `idx_template_field_template` on `template_id`
- `idx_template_field_label_null` on `template_id WHERE field_label IS NULL` (optimization for trigger)

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
| `generated_by_mail` | VARCHAR(255) | | Email address of the generating user (added in V4) |
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
3. **Filtering** (client_id, template_id, status)
4. **Date Range Queries** (contract_start_date, contract_end_date)
5. **Sorting** (created_at DESC)
6. **Conditional Queries** (field_label IS NULL)

### Complete Index Reference

| Table | Index Name | Columns | Type | Purpose |
|-------|------------|---------|------|---------|
| `contract_template` | PRIMARY | `id` | Unique | Primary key |
| `contract_template` | idx_contract_template_name | `template_name` | Unique | Lookup by name |
| `contract_template` | idx_contract_template_created | `created_at DESC` | Regular | Recent templates |
| `template_field` | PRIMARY | `id` | Unique | Primary key |
| `template_field` | idx_template_field_template | `template_id` | Regular | Find fields in template |
| `template_field` | idx_template_field_template_position | `template_id, field_position` | Composite | Lookup field by position |
| `template_field` | idx_template_field_template_name | `template_id, field_label` | Composite | Find field by label |
| `template_field` | idx_template_field_label_null | `template_id WHERE field_label IS NULL` | Partial | Unmapped fields |
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
| `contract_field_value` | idx_contract_field_value_contract_field | `generated_contract_id, template_field_id` | Composite | Specific field in contract |

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
| `V0__Reset.sql` | 0 | Initial schema reset | BASELINE |
| `V1__Initial_schema.sql` | 1 | Create 4 core tables with indexes | ACTIVE |
| `V2__Fix_fully_mapped_column.sql` | 2 | Rename `fully_mapped` to `is_fully_mapped` | ACTIVE |
| `V3__Add_fully_mapped_trigger.sql` | 3 | Add trigger for auto-updating fully_mapped status | ACTIVE |
| `V4__Add_generated_by_mail_column.sql` | 4 | Add email tracking for contract generation | ACTIVE |
| `V5__Add_termination_fields.sql` | 5 | Add termination tracking (terminationDate, reasonsForTermination) | ACTIVE |
| `V6__Update_contract_status_enum_and_add_signed_document.sql` | 6 | Update status enum and add signed document support | ACTIVE |

### Contract Status Enum Updates

**V1 (Original)**:
- `GENERATED`: Contract generated but not signed
- `SIGNED`: Contract signed by client
- `ARCHIVED`: Contract completed or expired
- `VOID`: Contract cancelled/terminated

**V6 (Current)**:
- `PENDING_SIGNATURE`: Contract generated and awaiting signature (replaces GENERATED)
- `ACTIVE`: Contract signed and in effect (replaces SIGNED)
- `TERMINATED`: Contract ended prematurely (replaces VOID)
- `ARCHIVED`: Contract completed or expired (unchanged)

### Recent Column Additions

**V5 - Termination Tracking**:
- `terminationDate`: DATE, nullable - records when a contract was terminated
- `reasonsForTermination`: VARCHAR(1000), NOT NULL DEFAULT '' - documents termination reason

**V6 - Signed Document Support**:
- `signed_document_content`: BYTEA, nullable - stores the digitally signed version of the contract
- Populated when contract status changes to ACTIVE
- Used for audit trail and client delivery

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
- `template_field.placeholder_text`
- `template_field.field_position`
- `template_field.format_pattern`
- `generated_contract.generated_by`
- `generated_contract.generated_by_mail`
- `generated_contract.contract_value`
- `generated_contract.contract_start_date`
- `generated_contract.contract_end_date`
- `generated_contract.notes`

---

## Database Triggers

### Purpose

Triggers enforce business logic and maintain data consistency at the database level, ensuring correctness even with direct SQL operations.

### 1. contract_template_updated_at_trigger

**Table**: `contract_template`  
**Event**: BEFORE UPDATE  
**Scope**: FOR EACH ROW

**Function**: `update_updated_at_column()`

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Behavior**: Automatically sets `updated_at` to current timestamp whenever the template is modified

**Example**:
```sql
UPDATE contracts.contract_template 
SET description = 'Updated description' 
WHERE id = 1;
-- Result: updated_at is automatically set to NOW()
```

---

### 2. template_field_update_trigger

**Table**: `template_field`  
**Event**: AFTER UPDATE  
**Scope**: FOR EACH ROW  
**Condition**: `OLD.field_label IS DISTINCT FROM NEW.field_label`

**Function**: `update_template_fully_mapped_status()`

```sql
CREATE OR REPLACE FUNCTION update_template_fully_mapped_status()
RETURNS TRIGGER AS $$
DECLARE
    template_id BIGINT;
    all_mapped BOOLEAN;
BEGIN
    template_id := NEW.template_id;
    
    all_mapped := NOT EXISTS (
        SELECT 1 FROM contracts.template_field
        WHERE template_id = template_id
        AND field_label IS NULL
    );
    
    UPDATE contracts.contract_template
    SET is_fully_mapped = all_mapped,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = template_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Behavior**: When a field's label is updated, check if ALL fields in the template now have labels, and update the template's `is_fully_mapped` status

**Example**:
```sql
-- Template has 3 fields, 2 with labels, 1 without
UPDATE contracts.template_field 
SET field_label = 'Client Email' 
WHERE id = 5;
-- Trigger fires: Checks that all 3 fields now have labels
-- Result: contract_template.is_fully_mapped = true
```

---

### 3. template_field_insert_trigger

**Table**: `template_field`  
**Event**: AFTER INSERT  
**Scope**: FOR EACH ROW

**Function**: `update_template_fully_mapped_status()` (same as above)

**Behavior**: When a new field is inserted (though typically without a label initially), update the template's `is_fully_mapped` status for consistency

**Example**:
```sql
INSERT INTO contracts.template_field (template_id, placeholder_text, field_position, data_type)
VALUES (1, '......', 3, 'STRING');
-- Trigger fires: Rechecks template's fully_mapped status
-- Result: is_fully_mapped updated (likely to false since new field has no label)
```

---

### Trigger Optimization

**Index for Trigger Performance**:
```sql
CREATE INDEX idx_template_field_label_null
ON contracts.template_field(template_id)
WHERE field_label IS NULL;
```

This partial index helps the `NOT EXISTS` query in the trigger function execute efficiently by only indexing null labels.

---

## JPA Entity Models

### 1. ContractTemplate Entity

**Package**: `clm.demo.models`  
**File**: `ContractTemplate.java`  
**ORM Table**: `contracts.contract_template`

```java
@Entity
@Table(name = "contract_template", schema = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_name", nullable = false, unique = true, length = 255)
    private String templateName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_format", nullable = false, length = 10)
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
    
    @OneToMany(mappedBy = "contractTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();
}
```

**Annotations Explained**:
- `@Entity`: Maps to database table
- `@Table(name = "contract_template", schema = "contracts")`: Specifies table and schema
- `@Data`: Lombok generates getters/setters/equals/hashCode
- `@Builder`: Provides builder pattern construction
- `@Lob + @JdbcTypeCode(VARBINARY)`: Maps byte[] to BYTEA
- `@CreationTimestamp`: Auto-set on insert (Hibernate)
- `@UpdateTimestamp`: Auto-set on update (Hibernate)
- `@OneToMany(mappedBy = "contractTemplate")`: Relationship mapped on child side

**Enums**:
- `DocumentFormat`: PDF, DOCX

**Relationships**:
- `1:N` → `TemplateField` (one template has many fields)
- `1:N` → `Contract` (one template generates many contracts)

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
    private ContractTemplate contractTemplate;
    
    @Column(name = "field_label", length = 255)
    @Builder.Default
    private String fieldLabel = null;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 50)
    @Builder.Default
    private DataType dataType = DataType.STRING;
    
    @Column(name = "placeholder_text", length = 255)
    private String placeholderText;
    
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
- `DataType`: STRING, DATE, NUMBER, BOOLEAN, CURRENCY, ENUM

**Relationships**:
- `N:1` → `ContractTemplate` (lazy loaded)

**Key Fields**:
- `fieldLabel`: NULL initially, set during mapping phase. When all fields have labels, template becomes "fully mapped"
- `formatPattern`: For dates use "dd/MM/yyyy", for numbers use "#,##0.00"

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
    private ContractTemplate contractTemplate;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 50)
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

    /** The signed version of the document (populated when contract is signed). */
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
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
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

**Relationships**:
- `N:1` → `ContractTemplate` (lazy loaded)
- `1:N` → `ContractFieldValue` (audit trail)

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
