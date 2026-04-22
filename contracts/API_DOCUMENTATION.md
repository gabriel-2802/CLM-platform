# CLM Platform - API Reference

> **Base URL:** `/api`  
> **Content-Type:** `application/json` unless noted  
> **Pagination defaults:** `page=0`, `size=20`

---

## Response Format

### Success `2xx`

```json
{
  "templateId": 42,
  "templateName": "Enterprise NDA",
  "fieldCount": 8,
  "fullyMapped": true,
  "fields": [
    {
      "id": 101,
      "fieldLabel": "Client Name",
      "dataType": "STRING",
      "isRequired": true
    }
  ]
}
```

### Error `4xx` / `5xx`

```json
{
  "status": 400,
  "message": "Template upload failed",
  "details": "File cannot be empty",
  "timestamp": "2026-03-30T10:30:00"
}
```

### Error Code Reference

| Exception | Status | Message |
|-----------|--------|---------|
| `IllegalArgumentException`, `EmptyFileNameException` | `400` | Invalid request / Template upload failed |
| `TemplateFieldOwnershipException`, `MissingMandatoryFieldException` | `400` | Field does not belong to template / Missing required field mappings |
| `ResourceNotFoundException` | `404` | Resource not found |
| `UnsupportedFileException` | `415` | Unsupported file format |
| `FileConversionException`, `TemplateUploadException` | `422` | Document conversion failed / Template upload failed |
| `InvalidContractStateException`, `SignedDocumentNotAvailableException` | `409` | Invalid contract state / Signed document not available |
| `InvalidAppendixStateException` | `409` | Appendix is already SIGNED or in an invalid state for the operation |
| `ContractGenerationFailException`, generic `Exception` | `500` | Contract generation failed / Internal server error |

---

## Template Endpoints

### `POST /api/templates/upload`

Upload a new template. PDFs are converted to DOCX internally. Placeholders are normalised to four dots and replaced with `{{fieldId}}` tokens.

**Request** — `multipart/form-data`

| Field | Required | Notes |
|-------|----------|-------|
| `file` | YES | DOCX or PDF |
| `templateName` | YES | Must be unique — duplicates raise `DuplicateTemplateNameException` |
| `description` | NO | Optional admin note |

**Response** — `201 Created`

```json
{
  "templateId": 7,
  "templateName": "Enterprise NDA",
  "documentText": "This Agreement between {{101}} and {{102}}..."
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Empty file or duplicate template name |
| `415` | File is not DOCX or PDF |
| `422` | Document parse / conversion failure |
| `500` | Unexpected server error |

---

### `GET /api/templates`

List all templates, ordered by `createdAt DESC`.

**Request**

| Query Param | Required | Default | Notes |
|-------------|----------|---------|-------|
| `page` | NO | `0` | Zero-based page index |
| `size` | NO | `20` | Items per page |

```
GET /api/templates?page=0&size=20
```

**Response** — `200 OK`

```json
[
  {
    "templateId": 7,
    "templateName": "Enterprise NDA",
    "description": "For new enterprise clients",
    "fieldCount": 4,
    "fullyMapped": false,
    "createdAt": "2026-03-01T09:00:00",
    "updatedAt": "2026-03-15T14:22:00",
    "fields": [
      {
        "id": 101,
        "fieldLabel": null,
        "dataType": "STRING",
        "fieldPosition": 1,
        "isRequired": true,
        "formatPattern": null
      }
    ]
  }
]
```

> Returns `204 No Content` when the page is empty.

---

### `GET /api/templates/{templateId}`

Retrieve a single template by ID.

**Request**

| Path Param | Required | Example |
|------------|----------|---------|
| `templateId` | YES | `7` |

```
GET /api/templates/7
```

**Response** — `200 OK`

```json
{
  "templateId": 7,
  "templateName": "Enterprise NDA",
  "description": "For new enterprise clients",
  "fieldCount": 4,
  "fullyMapped": true,
  "createdAt": "2026-03-01T09:00:00",
  "updatedAt": "2026-03-15T14:22:00",
  "fields": [
    {
      "id": 101,
      "fieldLabel": "Client Name",
      "dataType": "STRING",
      "fieldPosition": 1,
      "isRequired": true,
      "formatPattern": null
    }
  ]
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `404` | Template not found |

---

### `PUT /api/templates/{templateId}/labels`

Map or update field labels on a template. The `templateId` in the request body must match the path parameter.

**Request** — `application/json`

| Path Param | Required | Example |
|------------|----------|---------|
| `templateId` | YES | `7` |

**Body fields (`mappings` array)**

| Field | Required | Default | Notes |
|-------|----------|---------|-------|
| `fieldId` | YES | — | Existing `TemplateField` ID |
| `fieldLabel` | YES | — | Unique per template |
| `dataType` | NO | `STRING` | `STRING`, `DATE`, `NUMBER`, `BOOLEAN`, `CURRENCY` |
| `isRequired` | NO | `true` | |
| `formatPattern` | NO | `null` | Date: `DateTimeFormatter` syntax · Number: `DecimalFormat` syntax |

```json
{
  "templateId": 7,
  "mappings": [
    {
      "fieldId": 101,
      "fieldLabel": "Client Name",
      "dataType": "STRING",
      "isRequired": true,
      "formatPattern": null
    },
    {
      "fieldId": 102,
      "fieldLabel": "Start Date",
      "dataType": "DATE",
      "isRequired": true,
      "formatPattern": "yyyy-MM-dd"
    }
  ]
}
```

**Response** — `200 OK`

```json
[
  {
    "id": 101,
    "fieldLabel": "Client Name",
    "dataType": "STRING",
    "fieldPosition": 1,
    "isRequired": true,
    "formatPattern": null
  },
  {
    "id": 102,
    "fieldLabel": "Start Date",
    "dataType": "DATE",
    "fieldPosition": 2,
    "isRequired": true,
    "formatPattern": "yyyy-MM-dd"
  }
]
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | `templateId` mismatch, or `fieldId` belongs to a different template |
| `404` | Template or field not found |

---

### `DELETE /api/templates/{templateId}`

Delete a template and all its `TemplateField` rows. Blocked if any document (contract or appendix) still references the template.

**Request**

| Path Param | Required | Example |
|------------|----------|---------|
| `templateId` | YES | `7` |

```
DELETE /api/templates/7
```

**Response** — `204 No Content` (no body)

**Errors**

| Status | Reason |
|--------|--------|
| `404` | Template not found |
| `409` | Template is referenced by one or more documents |

---

### `GET /api/templates/download/{templateId}/{format}`

Download a template as DOCX or PDF.

**Request**

| Path Param | Required | Values |
|------------|----------|--------|
| `templateId` | YES | e.g. `7` |
| `format` | YES | `docx` or `pdf` |

```
GET /api/templates/download/7/pdf
```

**Response** — `200 OK` (binary attachment)

```
Content-Disposition: attachment; filename=template-7.pdf
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Unsupported format value |
| `404` | Template not found |
| `422` | Conversion failure |

---

## Contract Endpoints

### `POST /api/contracts/generate`

Generate a contract from a fully mapped template. Validates required fields, persists entities, and produces a PDF output stored as BYTEA.

**Request** — `application/json`

| Field | Required | Notes |
|-------|----------|-------|
| `templateId` | YES | Must reference a fully mapped template |
| `userId` | YES | Staff member generating the contract |
| `userMail` | YES | Valid email format enforced |
| `clientId` | YES | External system client identifier |
| `startDate` | YES | ISO date — contract validity start |
| `endDate` | YES | ISO date — contract validity end |
| `mappings` | YES | `Map<String, String>` — key = `fieldLabel`, value = content |
| `value` | NO | Monetary contract value (`BigDecimal` — use decimal notation, e.g. `15000.00`) |
| `notes` | NO | Optional context |

```json
{
  "templateId": 7,
  "userId": 1,
  "userMail": "staff@company.com",
  "clientId": 42,
  "startDate": "2026-04-01",
  "endDate": "2027-03-31",
  "mappings": {
    "Client Name": "Acme Corp",
    "Start Date": "2026-04-01"
  },
  "value": 15000.00,
  "notes": "Renewal of previous agreement"
}
```

**Response** — `201 Created`

> A `Location` header is returned pointing to the new resource: `Location: /api/contracts/88`

```json
{
  "id": 88,
  "templateId": 7,
  "clientId": 42,
  "contractStatus": "PENDING_SIGNATURE",
  "generatedBy": 1,
  "generatedByMail": "staff@company.com",
  "contractValue": 15000.00,
  "contractStartDate": "2026-04-01",
  "contractEndDate": "2027-03-31",
  "notes": "Renewal of previous agreement",
  "createdAt": "2026-04-01T10:00:00",
  "fieldValues": [
    {
      "id": 201,
      "templateFieldId": 101,
      "fieldLabel": "Client Name",
      "fieldValue": "Acme Corp"
    }
  ]
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Missing required field value in mappings |
| `404` | Template not found |
| `422` | Contract generation / DOCX rendering failed |
| `500` | Unexpected error |

---

### `POST /api/contracts/{contractId}/upload-signed`

Attach a signed document to a contract. Converts DOCX uploads to PDF internally, then transitions status to `ACTIVE`. The contract must be in `PENDING_SIGNATURE` state.

**Request** — `multipart/form-data`

| Param | Required | Notes |
|-------|----------|-------|
| `contractId` (path) | YES | e.g. `88` |
| `file` | YES | Signed DOCX or PDF |

```
POST /api/contracts/88/upload-signed
```

**Response** — `200 OK`

```json
{
  "id": 88,
  "contractStatus": "ACTIVE",
  "templateId": 7,
  "clientId": 42,
  "generatedBy": 1,
  "generatedByMail": "staff@company.com",
  "contractValue": 15000.00,
  "contractStartDate": "2026-04-01",
  "contractEndDate": "2027-03-31",
  "notes": "Renewal of previous agreement",
  "createdAt": "2026-04-01T10:00:00",
  "fieldValues": [
    {
      "id": 201,
      "templateFieldId": 101,
      "fieldLabel": "Client Name",
      "fieldValue": "Acme Corp"
    }
  ]
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Empty file |
| `404` | Contract not found |
| `409` | Contract is not in `PENDING_SIGNATURE` state |
| `422` | PDF conversion failed |

---

### `PUT /api/contracts/terminate/{contractId}`

Terminate an active contract. Only contracts with status `ACTIVE` can be terminated.

**Request** — `application/json`

| Path Param | Required | Example |
|------------|----------|---------|
| `contractId` | YES | `88` |

| Body Field | Required | Notes |
|------------|----------|-------|
| `terminationDate` | YES | ISO date (`yyyy-MM-dd`) |
| `reasons` | NO | Free-text explanation |

```json
{
  "terminationDate": "2026-04-01",
  "reasons": "Client requested early exit"
}
```

**Response** — `204 No Content` (no body)

**Errors**

| Status | Reason |
|--------|--------|
| `404` | Contract not found |
| `409` | Contract is not `ACTIVE` |

---

### `GET /api/contracts`

List all contracts, ordered by `createdAt DESC`.

**Request**

| Query Param | Required | Default | Notes |
|-------------|----------|---------|-------|
| `page` | NO | `0` | Zero-based page index |
| `size` | NO | `20` | Items per page |

```
GET /api/contracts?page=0&size=20
```

**Response** — `200 OK`

```json
[
  {
    "id": 88,
    "templateId": 7,
    "clientId": 42,
    "contractStatus": "ACTIVE",
    "generatedBy": 1,
    "generatedByMail": "staff@company.com",
    "contractValue": 15000.00,
    "contractStartDate": "2026-04-01",
    "contractEndDate": "2027-03-31",
    "notes": "Renewal of previous agreement",
    "createdAt": "2026-04-01T10:00:00",
    "fieldValues": [
      {
        "id": 201,
        "templateFieldId": 101,
        "fieldLabel": "Client Name",
        "fieldValue": "Acme Corp"
      }
    ]
  }
]
```

> Returns `204 No Content` when the page is empty.

---

### `POST /api/contracts/search`

Filter and search contracts. Uses `POST` instead of `GET` because several HTTP clients and intermediaries (CDNs, proxies) do not support GET requests with a body.

**Request** — `application/json` body, all fields optional

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `notes` | `String` | `null` | Free-text trigram search |
| `contractStatus` | `Enum` | `null` | `PENDING_SIGNATURE`, `ACTIVE`, `TERMINATED`, `ARCHIVED` |
| `clientId` | `Integer` | `null` | Filter by client |
| `generatedBy` | `Integer` | `null` | Filter by creator |
| `labelValues` | `Array<String>` | `[]` | AND filter using trigram search across field values |
| `templateName` | `String` | `null` | Filter by source template name |
| `templateDescription` | `String` | `null` | Filter by source template description |
| `createdAfter` | ISO date | `null` | Lower bound on creation date |
| `createdBefore` | ISO date | `null` | Upper bound on creation date |
| `page` | `Integer` | `0` | Zero-based page index |
| `size` | `Integer` | `20` | Items per page |

```json
{
  "contractStatus": "ACTIVE",
  "clientId": 42,
  "labelValues": ["Acme", "USD"],
  "createdAfter": "2026-01-01",
  "page": 0,
  "size": 20
}
```

**Response** — `200 OK` — same shape as `GET /api/contracts`

> Returns `204 No Content` when no contracts match. Ordered by `createdAt DESC`.

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Invalid filter values |
| `500` | Database error |

---

### `GET /api/contracts/download/{contractId}/{type}/{format}`


Download a contract as DOCX or PDF, either the unsigned original or the signed version.

**Request**

| Path Param | Required | Values |
|------------|----------|--------|
| `contractId` | YES | e.g. `88` |
| `type` | YES | `unsigned` or `signed` |
| `format` | YES | `docx` or `pdf` |

```
GET /api/contracts/download/88/signed/pdf
```

**Response** — `200 OK` (binary attachment)

```
Content-Disposition: attachment; filename=contract-88.pdf
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Invalid `type` or `format` value |
| `404` | Contract not found, or signed document not yet uploaded |
| `422` | Format conversion failed |

---

## Report Endpoints

Used exclusively by the **notification microservice** to build email digests. Both endpoints return plain `ContractResponseDTO` objects — the caller uses the `id` field to fetch document bytes via the internal download endpoint (`GET /api/contracts/download/{contractId}/...`) and attaches them to outbound emails. No external URLs are embedded in the response.

---

### `GET /api/contracts/report/expiring`

Returns all `ACTIVE` contracts whose `contractEndDate` falls within the next `days` days (inclusive of today).

**Request**

| Query Param | Required | Constraints | Notes |
|-------------|----------|-------------|-------|
| `days` | YES | `≥ 1` | Look-ahead window in calendar days |

```
GET /api/contracts/report/expiring?days=30
```

**Response** — `200 OK` — same shape as `GET /api/contracts`

```json
[
  {
    "id": 88,
    "templateId": 7,
    "clientId": 42,
    "contractStatus": "ACTIVE",
    "generatedBy": 1,
    "generatedByMail": "staff@company.com",
    "contractValue": 15000.00,
    "contractStartDate": "2026-04-01",
    "contractEndDate": "2026-05-15",
    "notes": "Renewal of previous agreement",
    "createdAt": "2026-04-01T10:00:00"
  }
]
```

> Returns `204 No Content` when no contracts match. Results ordered by `contractEndDate ASC`.

**Errors**

| Status | Reason |
|--------|--------|
| `400` | `days` is less than `1` |

---

### `GET /api/contracts/report/inactive-clients`

Returns `ACTIVE` contracts where **every other contract the client holds within the look-back window carries the same `contractValue`** — meaning no monetary renegotiation has occurred in that period. Clients whose rate changed at any point within the window are excluded.

**Request**

| Query Param | Required | Constraints | Notes |
|-------------|----------|-------------|-------|
| `months` | YES | `≥ 1` | Minimum age of the contract in calendar months |

```
GET /api/contracts/report/inactive-clients?months=6
```

**Response** — `200 OK` — same shape as `GET /api/contracts`

```json
[
  {
    "id": 55,
    "templateId": 3,
    "clientId": 17,
    "contractStatus": "ACTIVE",
    "generatedBy": 2,
    "generatedByMail": "ops@company.com",
    "contractValue": 8000.00,
    "contractStartDate": "2025-09-01",
    "contractEndDate": "2026-08-31",
    "createdAt": "2025-09-01T08:30:00"
  }
]
```

> Returns `204 No Content` when no contracts match. Results ordered by `clientId ASC`, then `contractStartDate ASC`.

**Errors**

| Status | Reason |
|--------|--------|
| `400` | `months` is less than `1` |

---

## Appendix Endpoints

Appendices are auxiliary documents attached to a contract. They can be **fillable** (generated from a template, signed via the same DRAFT → SIGNED flow) or **non-fillable** (uploaded directly in any format).

> Fillability is implicit: an appendix with a `templateId` is fillable; one without is a direct upload.

---

### `POST /api/appendices/generate`

Generate a fillable appendix from a template and attach it to a contract.

**Request** — `application/json`

| Field | Required | Notes |
|-------|----------|-------|
| `contractId` | YES | Parent contract |
| `templateId` | YES | Must reference a fully mapped template |
| `title` | YES | Free-form label for the appendix |
| `userId` | NO | Staff member generating the appendix |
| `userMail` | NO | Valid email format |
| `notes` | NO | Optional context |
| `mappings` | YES | `Map<String, String>` — key = `fieldLabel`, value = content |

```json
{
  "contractId": 88,
  "templateId": 9,
  "title": "Exhibit A – Scope of Work",
  "userId": 1,
  "userMail": "staff@company.com",
  "notes": "Defines deliverables",
  "mappings": {
    "Project Name": "Platform Rewrite",
    "Budget": "50000"
  }
}
```

**Response** — `201 Created`

> A `Location` header is returned pointing to the new resource: `Location: /api/appendices/12`

```json
{
  "id": 12,
  "contractId": 88,
  "templateId": 9,
  "title": "Exhibit A – Scope of Work",
  "appendixStatus": "DRAFT",
  "documentFormat": "PDF",
  "generatedBy": 1,
  "generatedByMail": "staff@company.com",
  "notes": "Defines deliverables",
  "createdAt": "2026-04-01T10:05:00",
  "fieldValues": [
    {
      "id": 301,
      "templateFieldId": 201,
      "fieldLabel": "Project Name",
      "fieldValue": "Platform Rewrite"
    }
  ]
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Missing required field value in mappings |
| `404` | Contract or template not found |
| `422` | Document generation / DOCX rendering failed |

---

### `POST /api/appendices/upload`

Upload a non-fillable appendix directly and attach it to a contract. Format is auto-detected from file magic bytes. Direct uploads are immediately set to `SIGNED` status — no separate upload-signed step is required.

**Request** — `multipart/form-data`

| Field | Required | Notes |
|-------|----------|-------|
| `contractId` | YES | Parent contract |
| `title` | YES | Free-form label |
| `file` | YES | DOCX or PDF |
| `userId` | NO | Staff member uploading |
| `userMail` | NO | Uploader's email |
| `notes` | NO | Optional context |

```
POST /api/appendices/upload
Content-Type: multipart/form-data

contractId=88&title=ID+Copy&file=<binary>
```

**Response** — `201 Created`

> A `Location` header is returned pointing to the new resource: `Location: /api/appendices/13`

```json
{
  "id": 13,
  "contractId": 88,
  "title": "ID Copy",
  "appendixStatus": "SIGNED",
  "documentFormat": "PDF",
  "createdAt": "2026-04-01T10:10:00"
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Empty file or missing required fields |
| `404` | Contract not found |
| `415` | File is not DOCX or PDF |
| `422` | Storage failure |

---

### `POST /api/appendices/{appendixId}/upload-signed`

Attach a signed version of an appendix. Converts DOCX to PDF internally, then transitions status to `SIGNED`. The appendix must be in `DRAFT` state — already-`SIGNED` appendices are rejected.

**Request** — `multipart/form-data`

| Param | Required | Notes |
|-------|----------|-------|
| `appendixId` (path) | YES | e.g. `12` |
| `file` | YES | Signed DOCX or PDF |

```
POST /api/appendices/12/upload-signed
```

**Response** — `200 OK`

```json
{
  "id": 12,
  "contractId": 88,
  "templateId": 9,
  "title": "Exhibit A – Scope of Work",
  "appendixStatus": "SIGNED",
  "documentFormat": "PDF",
  "createdAt": "2026-04-01T10:05:00"
}
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Empty file |
| `404` | Appendix not found |
| `409` | Appendix is already `SIGNED` |
| `422` | PDF conversion failed |

---

### `GET /api/appendices/contract/{contractId}`

List all appendices for a contract.

**Request**

| Path Param | Required | Example |
|------------|----------|---------|
| `contractId` | YES | `88` |

```
GET /api/appendices/contract/88
```

**Response** — `200 OK`

```json
[
  {
    "id": 12,
    "contractId": 88,
    "templateId": 9,
    "title": "Exhibit A – Scope of Work",
    "appendixStatus": "SIGNED",
    "documentFormat": "PDF",
    "createdAt": "2026-04-01T10:05:00"
  },
  {
    "id": 13,
    "contractId": 88,
    "title": "ID Copy",
    "appendixStatus": "DRAFT",
    "documentFormat": "PDF",
    "createdAt": "2026-04-01T10:10:00"
  }
]
```

> Returns `204 No Content` when no appendices exist for the contract.

**Errors**

| Status | Reason |
|--------|--------|
| `404` | Contract not found |

---

### `DELETE /api/appendices/{appendixId}`

Delete an appendix and its audit trail field values.

**Request**

| Path Param | Required | Example |
|------------|----------|---------|
| `appendixId` | YES | `12` |

```
DELETE /api/appendices/12
```

**Response** — `204 No Content` (no body)

**Errors**

| Status | Reason |
|--------|--------|
| `404` | Appendix not found |

---

### `GET /api/appendices/download/{appendixId}/{type}/{format}`

Download an appendix as DOCX or PDF.

**Request**

| Path Param | Required | Values |
|------------|----------|--------|
| `appendixId` | YES | e.g. `12` |
| `type` | YES | `unsigned` or `signed` |
| `format` | YES | `docx` or `pdf` |

```
GET /api/appendices/download/12/signed/pdf
```

**Response** — `200 OK` (binary attachment)

```
Content-Disposition: attachment; filename=appendix-12.pdf
```

**Errors**

| Status | Reason |
|--------|--------|
| `400` | Invalid `type` or `format` value |
| `404` | Appendix not found, or signed document not yet uploaded |
| `422` | Format conversion failed |
