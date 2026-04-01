# Contract Template Management API Documentation
This document mirrors the current Spring Boot controllers (`TemplateController`, `ContractController`) and the DTOs under `clm.demo.dto`. All endpoints emit JSON bodies directly (no wrapper object); errors are normalized through `GlobalExceptionHandler`.
## Response Format
### Success Response (2xx)
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
### Error Response (4xx/5xx)
Every error uses `ErrorResponseDTO`.
```json
{
  "status": 400,
  "message": "Template upload failed",
  "details": "File cannot be empty",
  "timestamp": "2026-03-30T10:30:00"
}
```
## Error Handling
`GlobalExceptionHandler` centralizes API failures. Common mappings:
| Exception | HTTP Status | Message | Typical Cause |
|-----------|-------------|---------|----------------|
| `IllegalArgumentException`, `EmptyFileNameException` | 400 | Invalid request / Template upload failed | Missing multipart parts, negative page, empty files |
| `TemplateFieldOwnershipException`, `MissingMandatoryFieldException` | 400 | Field does not belong to template / Missing required field mappings | Inconsistent template/field IDs or absent values |
| `ResourceNotFoundException` | 404 | Resource not found | Template/contract/field absent |
| `UnsupportedFileException` | 415 | Unsupported file format | Upload other than DOCX/PDF |
| `FileConversionException`, `TemplateUploadException` | 422 | Document conversion failed / Template upload failed | Apache POI/PDFBox failures, corrupt binaries |
| `InvalidContractStateException`, `SignedDocumentNotAvailableException` | 409 | Invalid contract state / Signed document not available | Uploading signed doc to inactive contract, downloading before signature |
| `ContractGenerationFailException`, generic `Exception` | 500 | Contract generation failed / Internal server error | Unexpected runtime issues |
## DTO Reference
### Request DTOs
#### UploadTemplateRequest (`multipart/form-data`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `file` | File | Yes | DOCX or PDF; PDFs are converted to DOCX internally |
| `templateName` | String | Yes | Must be unique; duplicates raise `DuplicateTemplateNameException` |
| `description` | String | No | Optional admin note |
#### FieldMappingRequest (`application/json`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `templateId` | Long | Yes | Must match template being updated |
| `mappings` | Array<FieldMappingDefinition> | Yes | At least one entry |
`FieldMappingDefinition`
| Field | Type | Required | Defaults |
|-------|------|----------|----------|
| `fieldId` | Long | Yes | Existing `TemplateField.id` |
| `fieldLabel` | String | Yes | Unique per template |
| `dataType` | Enum | No | Defaults to `STRING`; valid: `STRING`, `DATE`, `NUMBER`, `BOOLEAN`, `CURRENCY` |
| `isRequired` | Boolean | No | Defaults to `true` |
| `formatPattern` | String | No | Date patterns follow `DateTimeFormatter`, numbers use `DecimalFormat` |
#### GenContractRequest (`application/json`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `templateId` | Long | Yes | Must reference a fully mapped template |
| `userId` | Long | Yes | Staff member generating the contract |
| `userMail` | String (email) | Yes | Valid email format enforced |
| `clientId` | Long | Yes | External system client identifier |
| `startDate` | ISO date | Yes | Contract validity start |
| `endDate` | ISO date | Yes | Contract validity end |
| `mappings` | Map<String,String> | Yes | Key = `fieldLabel`, value = field content |
| `value` | Number | No | Monetary value |
| `notes` | String | No | Optional context |
#### ContractTerminationRequest (`application/json`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `terminationDate` | ISO timestamp | Yes | Converted to `LocalDate` internally |
| `reasons` | String | No | Optional explanation |
#### SearchRequest (body of `GET /api/contracts/search`)
| Field | Type | Required |
|-------|------|----------|
| `notes` | String | No |
| `contractStatus` | Enum (`PENDING_SIGNATURE`, `ACTIVE`, `TERMINATED`, `ARCHIVED`) | No |
| `clientId` | Integer | No |
| `generatedBy` | Integer | No |
| `labelValues` | Array<String> | No (AND filter using trigram search) |
| `templateName` | String | No |
| `templateDescription` | String | No |
| `createdAfter` | ISO date | No |
| `createdBefore` | ISO date | No |
| `page` | Integer | No (default 0) |
| `size` | Integer | No (default 20) |
### Response DTOs
#### TemplateUploadResponseDTO
| Field | Description |
|-------|-------------|
| `templateId` | Persisted template ID |
| `templateName` | Value echoed from request |
| `documentText` | Normalized DOCX text with placeholders replaced by `{{fieldId}}` tokens |
#### TemplateResponseDTO
| Field | Description |
|-------|-------------|
| `templateId`, `templateName`, `description` | Template metadata |
| `fieldCount` | Count of placeholders parsed |
| `fullyMapped` | `true` when all required fields have labels |
| `createdAt`, `updatedAt` | ISO timestamps |
| `fields[]` | Array of `TemplateFieldDTO` objects |
`TemplateFieldDTO`
| Field | Notes |
|-------|------|
| `id` | TemplateField ID |
| `fieldLabel` | Display label; can be null until mapped |
| `dataType` | String form of enum |
| `fieldPosition` | Positional order from top of document |
| `isRequired` | Boolean |
| `formatPattern` | Optional formatter |
#### TemplateFieldResponseDTO
Returned by the labels endpoint; mirrors `TemplateFieldDTO` but always populated (derived from entity constructor).
#### ContractResponseDTO
| Field | Description |
|-------|-------------|
| `id` | Generated contract ID |
| `templateId`, `clientId` | Foreign keys |
| `contractStatus` | Current status |
| `generatedBy`, `generatedByMail` | Audit of creator |
| `contractValue` | Monetary value (nullable) |
| `contractStartDate`, `contractEndDate` | Validity window |
| `notes` | Optional notes |
| `terminationDate`, `reasonsForTermination` | Populated when terminated |
| `createdAt`, `updatedAt` | Audit timestamps |
| `fieldValues[]` | Array of `ContractFieldValueResponseDTO` |
`ContractFieldValueResponseDTO` fields: `id`, `templateFieldId`, `fieldLabel`, `fieldValue`.
---
## Template Endpoints (`/api/templates`)
### Upload Template — `POST /api/templates/upload`
- **Consumes**: `multipart/form-data`
- **Produces**: `application/json`
- **Body**: `UploadTemplateRequest`
- **Behavior**: Detects format (DOCX/PDF), normalizes placeholders to four dots, compresses DOCX bytes, persists `Template` and placeholder `TemplateField` rows, and returns `TemplateUploadResponseDTO`.
- **Response**: `201 Created`
- **Sample**:
```json
{
  "templateId": 7,
  "templateName": "Service Agreement",
  "documentText": "This Agreement between {{101}} and {{102}}..."
}
```
- **Errors**: 400 (empty file/duplicate name), 415 (unsupported format), 422 (parse failure), 500 (unexpected).
### List Templates — `GET /api/templates`
- **Query Params**: `page` (default 0), `size` (default 20).
- **Response**: `200 OK` with `List<TemplateResponseDTO>`; `204 No Content` when page empty.
- **Notes**: `TemplateService` executes a paged query ordered by `createdAt DESC`.
### Get Template — `GET /api/templates/{templateId}`
- **Response**: `200 OK` `TemplateResponseDTO`.
- **Errors**: 404 when template is absent.
### Update Field Labels — `PUT /api/templates/{templateId}/labels`
- **Consumes**: `application/json`
- **Body**: `FieldMappingRequest` (the `templateId` in the body must equal the path parameter).
- **Response**: `200 OK` `List<TemplateFieldResponseDTO>`.
- **Validation**: rejects updates when a provided `fieldId` does not belong to the template (`TemplateFieldOwnershipException`).
### Delete Template — `DELETE /api/templates/{templateId}`
- **Response**: `204 No Content`.
- **Behavior**: Cascade deletes associated `TemplateField` rows and generated contracts.
### Download Template — `GET /api/templates/download/{templateId}/{format}`
- **Path Parameters**: `format` ∈ {`docx`, `pdf`}.
- **Response**: `200 OK` binary with `Content-Disposition: attachment; filename=template-{templateId}.{format}`.
- **Details**: Delegates to `DocumentDownloadService` with `DocumentType.TEMPLATE`. Rejects unsupported formats via `IllegalArgumentException`.
---
## Contract Endpoints (`/api/contracts`)
### Generate Contract — `POST /api/contracts/generate`
- **Consumes**: `application/json`
- **Body**: `GenContractRequest`
- **Behavior**: Validates template is fully mapped, enforces required fields, persists `Contract` + `ContractFieldValue` entities, generates DOCX/PDF via `FileContentReplacementService`, and compresses binary content before saving.
- **Response**: `200 OK` `ContractResponseDTO` (including generated field values when present).
- **Errors**: 404 (template missing), 400 (`MissingMandatoryFieldException`), 422 (`ContractGenerationFailException`), 500 fallback.
### Upload Signed Contract — `POST /api/contracts/{contractId}/upload-signed`
- **Consumes**: `multipart/form-data`
- **Body**: `file` (DOCX/PDF)
- **Behavior**: Converts uploads to PDF when necessary, stores compressed signed binary, updates status to `ACTIVE`.
- **Response**: `200 OK` `ContractResponseDTO`.
- **Errors**: 400 (empty file), 404 (contract missing), 422 (conversion failure).
### Terminate Contract — `PUT /api/contracts/terminate/{contractId}`
- **Consumes**: `application/json`
- **Body**: `ContractTerminationRequest`
- **Behavior**: Only `ACTIVE` contracts can transition to `TERMINATED`; sets termination metadata.
- **Response**: `204 No Content`.
- **Errors**: 404 (missing contract), 409 (`InvalidContractStateException`).
### List Contracts — `GET /api/contracts`
- **Query Params**: `page`, `size` with same defaults as template listing.
- **Response**: `200 OK` `List<ContractResponseDTO>` or `204 No Content`.
### Search Contracts — `GET /api/contracts/search`
- **Consumes**: `application/json` body despite GET (kept for backward compatibility).
- **Body**: `SearchRequest`.
- **Behavior**: Delegates to `ContractSpecification` + custom SQL functions/indexes (see DB documentation) for server-side filtering, ordering by `created_at DESC`.
- **Response**: `200 OK` `List<ContractResponseDTO>` or `204 No Content`.
- **Errors**: 400 for invalid filters, 500 for DB issues.
### Download Contract — `GET /api/contracts/download/{contractId}/{type}/{format}`
- **Path Parameters**: `type` ∈ {`unsigned`, `signed`} (mapped to `DocumentType.UNSIGNED_CONTRACT` / `SIGNED_CONTRACT`); `format` ∈ {`docx`, `pdf`}.
- **Response**: `200 OK` binary attachment named `contract-{contractId}.{format}`.
- **Errors**: 400 (invalid type/format), 404 (contract missing or signed artifact unavailable), 422 (conversion failure).
---
## Usage Tips
- Always map every required field via `/api/templates/{templateId}/labels` before generating a contract; otherwise `MissingMandatoryFieldException` is raised.
- For consistent pagination, reuse the `page`/`size` defaults from `clm.demo.utils.Constants` (currently 0 / 20).
- `FieldMappingRequest.dataType` must align with downstream formatting rules; e.g., use `DATE` plus `formatPattern: "yyyy-MM-dd"` to ensure deterministic rendering.
- GET `/api/contracts/search` accepts a body because consumers relied on GET semantics while experimenting with filters. Future versions may introduce a POST alias.
