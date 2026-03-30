# Contract Template Management API Documentation

## Response Format

### Success Response (2xx)
```json
{
  "data": {
    // Response payload varies by endpoint
  },
  "timestamp": "2026-03-30T10:30:00",
  "status": 200
}
```

### Error Response (4xx, 5xx)
```json
{
  "status": 400,
  "message": "Template upload failed",
  "details": "File name cannot be empty",
  "timestamp": "2026-03-30T10:30:00"
}
```

---

## Error Handling

The API uses standardized HTTP status codes and provides detailed error messages via the `ErrorResponseDTO`:

| Error Type | HTTP Status | Trigger Condition |
|-----------|-------------|-------------------|
| **Bad Request** | 400 | Invalid request parameters, validation failures, data constraint violations |
| **Not Found** | 404 | Template or resource does not exist |
| **Unsupported Media Type** | 415 | File format is not DOCX or PDF |
| **Unprocessable Content** | 422 | Document binary processing or parsing fails |
| **Internal Server Error** | 500 | Unexpected server errors, uncaught exceptions |

### Common Error Responses

#### 1. Empty File Name Error
**Payload Example:**
```json
{
  "status": 400,
  "message": "Template upload failed",
  "details": "File name cannot be empty",
  "timestamp": "2026-03-30T10:30:00"
}
```

#### 2. Unsupported File Type Error
**Payload Example:**
```json
{
  "status": 415,
  "message": "Unsupported file format",
  "details": "Unsupported file type. Only .docx and .pdf files are allowed.",
  "timestamp": "2026-03-30T10:30:00"
}
```

#### 3. Resource Not Found Error
**Payload Example:**
```json
{
  "status": 404,
  "message": "Resource not found",
  "details": "Template with ID 999 not found",
  "timestamp": "2026-03-30T10:30:00"
}
```

#### 4. Data Validation Error
**Payload Example:**
```json
{
  "status": 400,
  "message": "Data validation error",
  "details": "Data violates validation constraints.",
  "timestamp": "2026-03-30T10:30:00"
}
```

#### 5. Document Processing Error
**Payload Example:**
```json
{
  "status": 422,
  "message": "Document processing failed",
  "details": "Error reading DOCX file: ZIP entry not found",
  "timestamp": "2026-03-30T10:30:00"
}
```

---

## Endpoints

### 1. Upload Template
**POST** `/api/templates/upload`

Uploads a contract template file, parses it for placeholders, and persists the template with its extracted fields.

#### Request
- **Content-Type:** `multipart/form-data`
- **Parameters:**

  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | file | File | Yes | Contract template file (DOCX or PDF) |
  | templateName | String | Yes | Name for the template (non-blank) |
  | description | String | No | Optional description of the template |

#### Response
- **Status:** `201 Created`
- **Body:** `ParsedTemplateResponseDTO`

#### Response Schema
```json
{
  "templateId": 1,
  "templateName": "Service Agreement Template",
  "documentText": "Service Agreement\n\nThis agreement is between [COMPANY_NAME] and [CLIENT_NAME]...",
  "placeholderCount": 5,
  "placeholders": [
    {
      "position": 1,
      "placeholderText": ".....",
      "startIndex": 45,
      "endIndex": 57,
      "fieldId": 101
    },
    {
      "position": 2,
      "placeholderText": "...",
      "startIndex": 62,
      "endIndex": 73,
      "fieldId": 102
    }
  ]
}
```

#### Possible Errors
- `400 Bad Request` - Missing required fields or empty file name
- `415 Unsupported Media Type` - File is not DOCX or PDF
- `422 Unprocessable Content` - File is corrupted or cannot be parsed
- `500 Internal Server Error` - Unexpected server error

---
### 1.2 Update Field Labels
**PUT** `/api/templates/{templateId}/labels`

Updates multiple field mappings for a template. LABELS WILL BE USED FOR CONTRACT GENERATION

#### Request
- **Content-Type:** `application/json`
- **Body:** `FieldMappingRequest`

- By default all are STRINGS and no format pattern, but for numbers and dates (ie specific labels you can send specific data types and format patterns to ensure correct formatting during contract generation)
- By default all fields are required, but you can set isRequired to false for optional fields (ie Client Name may be optional for some templates, but Effective Date is always required)

#### Request Payload Example
```json
{
  "templateId": 1,
  "mappings": [
    {
      "fieldId": 101,
      "fieldLabel": "Company_Name",
      "dataType": "STRING",
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "fieldId": 102,
      "fieldLabel": "Client Name",
      "dataType": "STRING",
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "fieldId": 103,
      "fieldLabel": "Service Term",
      "dataType": "STRING",
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "fieldId": 104,
      "fieldLabel": "Effective Date",
      "dataType": "DATE",
      "isRequired": true,
      "formatPattern": "yyyy-MM-dd"
    },
    {
      "fieldId": 105,
      "fieldLabel": "Total Price",
      "dataType": "DECIMAL",
      "isRequired": false,
      "formatPattern": "###,##0.00"
    }
  ]
}
```

#### Response (you can ignore this or show it on the screen, it's UI decision)
- **Status:** `200 OK`
- **Body:** `List<TemplateFieldResponseDTO>`

#### Response Payload Example
```json
[
  {
    "id": 101,
    "fieldLabel": "Company Name",
    "dataType": "STRING",
    "placeholderText": "COMPANY_NAME",
    "fieldPosition": 1,
    "isRequired": true,
    "formatPattern": ""
  },
  {
    "id": 102,
    "fieldLabel": "Client Name",
    "dataType": "STRING",
    "placeholderText": "CLIENT_NAME",
    "fieldPosition": 2,
    "isRequired": true,
    "formatPattern": ""
  },
  {
    "id": 103,
    "fieldLabel": "Service Term",
    "dataType": "STRING",
    "placeholderText": "SERVICE_TERM",
    "fieldPosition": 3,
    "isRequired": true,
    "formatPattern": ""
  },
  {
    "id": 104,
    "fieldLabel": "Effective Date",
    "dataType": "DATE",
    "placeholderText": "EFFECTIVE_DATE",
    "fieldPosition": 4,
    "isRequired": true,
    "formatPattern": "yyyy-MM-dd"
  },
  {
    "id": 105,
    "fieldLabel": "Total Price",
    "dataType": "DECIMAL",
    "placeholderText": "TOTAL_PRICE",
    "fieldPosition": 5,
    "isRequired": true,
    "formatPattern": "###,##0.00"
  }
]
```

#### Validation Rules
- `templateId` - Must not be null
- `mappings` - Must not be empty, must contain at least one field mapping
- `fieldId` - Must not be null (within each mapping)
- `fieldLabel` - Must not be null (within each mapping)
- `dataType` - Default value: "STRING" (valid types: STRING, INTEGER, DECIMAL, DATE, BOOLEAN)
- `isRequired` - Default value: true
- `formatPattern` - Default value: "" (empty string)

#### Possible Errors
- `400 Bad Request` - Invalid request data, empty mappings list, or missing required fields
- `404 Not Found` - Template or field with specified ID does not exist
- `422 Unprocessable Content` - Data type mismatch or constraint violation
- `500 Internal Server Error` - Database access failure

---

### 2. Get All Templates
**GET** `/api/templates/all`

Retrieves all available templates with their metadata and field counts.

#### Request
-  NO BODY

#### Response
- **Status:** `200 OK`
- **Body:** `List<TemplateResponseDTO>`

#### INFO
- fullYMapped is true if all fields that ARE REQUIRED have been mapped to database columns, false if any field is still unmapped (therefore it cannot be used to generate contract if false)
#### Response Schema
```json
[
  {
    "templateId": 1,
    "templateName": "Service Agreement Template",
    "description": "Standard service agreement",
    "fieldCount": 5,
    "fullyMapped": true,
    "createdAt": "2026-03-25T14:30:00",
    "updatedAt": "2026-03-28T10:15:00",
    "fields": [
      {
        "id": 101,
        "fieldLabel": "Company_Name",
        "dataType": "STRING",
        "fieldPosition": 1,
        "isRequired": true,
        "formatPattern": ""
      },
      {
        "id": 102,
        "fieldLabel": "Client Name",
        "placeholderText": "CLIENT_NAME",
        "dataType": "STRING",
        "fieldPosition": 2,
        "isRequired": true,
        "formatPattern": ""
      },
      {
        "id": 104,
        "fieldLabel": "Effective Date",
        "placeholderText": "EFFECTIVE_DATE",
        "dataType": "DATE",
        "fieldPosition": 4,
        "isRequired": true,
        "formatPattern": "yyyy-MM-dd"
      }
    ]
  },
  {
    "templateId": 2,
    "templateName": "NDA Template",
    "description": "Non-Disclosure Agreement",
    "fieldCount": 3,
    "fullyMapped": false,
    "createdAt": "2026-03-20T09:00:00",
    "updatedAt": "2026-03-20T09:00:00",
    "fields": [...]
  }
]
```

#### Possible Errors
- `500 Internal Server Error` - Database access failure

---

### 3. Get Template by ID
**GET** `/api/templates/{templateId}`

Retrieves a specific template by ID with all its parsed fields and current mappings.

#### Request
- NO BODY

#### Response
- **Status:** `200 OK`
- **Body:** `TemplateResponseDTO`

#### Response Payload Example
```json
{
  "templateId": 1,
  "templateName": "Service Agreement Template",
  "description": "Standard service agreement for all clients",
  "fieldCount": 5,
  "fullyMapped": true,
  "createdAt": "2026-03-25T14:30:00",
  "updatedAt": "2026-03-28T10:15:00",
  "fields": [
    {
      "id": 101,
      "fieldLabel": "Company Name",
      "placeholderText": "....",
      "dataType": "STRING",
      "fieldPosition": 1,
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "id": 102,
      "fieldLabel": "Client Name",
      "placeholderText": "CLIENT_NAME",
      "dataType": "STRING",
      "fieldPosition": 2,
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "id": 103,
      "fieldLabel": "Service Term",
      "placeholderText": "SERVICE_TERM",
      "dataType": "STRING",
      "fieldPosition": 3,
      "isRequired": true,
      "formatPattern": ""
    },
    {
      "id": 104,
      "fieldLabel": "Effective Date",
      "placeholderText": "EFFECTIVE_DATE",
      "dataType": "DATE",
      "fieldPosition": 4,
      "isRequired": true,
      "formatPattern": "yyyy-MM-dd"
    },
    {
      "id": 105,
      "fieldLabel": "Total Price",
      "placeholderText": "TOTAL_PRICE",
      "dataType": "DECIMAL",
      "fieldPosition": 5,
      "isRequired": true,
      "formatPattern": "###,##0.00"
    }
  ]
}
```

#### Possible Errors
- `404 Not Found` - Template with specified ID does not exist
- `500 Internal Server Error` - Database access failure


### 5. Delete Template
**DELETE** `/api/templates/{templateId}`

Deletes a template and cascades to all its fields, mappings, and generated contracts.

#### Request
- NO BODY

#### Response
- **Status:** `204 No Content`
- **Body:** Empty

#### Possible Errors
- `404 Not Found` - Template with specified ID does not exist
- `500 Internal Server Error` - Database access failure during cascade delete

---

### 6. Download Template as DOCX
**GET** `/api/templates/download/docx/{templateId}`

Downloads a template in DOCX format. If the template is stored in another format, automatically converts it.

#### Request
- NO BODY

#### Response
- **Status:** `200 OK`
- **Content-Type:** `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- **Body:** Binary DOCX file
- **Headers:**
  - `Content-Disposition: attachment; filename=template-{templateName}.docx`

#### Possible Errors
- `404 Not Found` - Template with specified ID does not exist
- `422 Unprocessable Content` - File decompression or conversion fails
- `500 Internal Server Error` - Unexpected error during file processing

---

### 7. Download Template as PDF
**GET** `/api/templates/download/pdf/{templateId}`

Downloads a template in PDF format. If the template is stored in another format, automatically converts it.

#### Request
- **Path Parameters:**
  | Parameter | Type | Required | Description |
  |-----------|------|----------|-------------|
  | templateId | Long | Yes | The unique identifier of the template |

#### Response
- **Status:** `200 OK`
- **Content-Type:** `application/pdf`
- **Body:** Binary PDF file
- **Headers:**
  - `Content-Disposition: attachment; filename=template-{templateName}.pdf`

#### Possible Errors
- `404 Not Found` - Template with specified ID does not exist
- `422 Unprocessable Content` - File decompression or conversion fails
- `500 Internal Server Error` - Unexpected error during file processing

---