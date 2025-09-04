# Checklist and Audit Execution API Guide

This guide provides comprehensive documentation for testing all the APIs in the Checklist and Audit Execution module.

## Base URL
```
http://localhost:8080/api/checklists
```

## Authentication
All APIs require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 1. Checklist Template Management

### 1.1 Create Checklist Template
**POST** `/api/checklists/templates`

**Request Body:**
```json
{
  "templateName": "ISO 9001:2015 Internal Audit Template",
  "description": "Comprehensive template for ISO 9001:2015 internal audits",
  "isoStandard": "ISO_9001",
  "clauses": [
    {
      "clauseId": 1,
      "customText": "Check if quality policy is documented and communicated",
      "customDescription": "Verify that the quality policy is properly documented and communicated to all employees"
    },
    {
      "clauseId": 2,
      "customText": "Review quality objectives and their measurement",
      "customDescription": "Ensure quality objectives are measurable and aligned with quality policy"
    }
  ]
}
```

**Response:**
```json
{
  "templateId": 1,
  "templateName": "ISO 9001:2015 Internal Audit Template",
  "description": "Comprehensive template for ISO 9001:2015 internal audits",
  "isoStandard": "ISO_9001",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "clauses": [
    {
      "clauseId": 1,
      "clauseNumber": "5.2",
      "clauseName": "Quality Policy",
      "customText": "Check if quality policy is documented and communicated",
      "customDescription": "Verify that the quality policy is properly documented and communicated to all employees"
    },
    {
      "clauseId": 2,
      "clauseNumber": "6.2",
      "clauseName": "Quality Objectives",
      "customText": "Review quality objectives and their measurement",
      "customDescription": "Ensure quality objectives are measurable and aligned with quality policy"
    }
  ]
}
```

### 1.2 Get All Templates
**GET** `/api/checklists/templates`

**Response:**
```json
[
  {
    "templateId": 1,
    "templateName": "ISO 9001:2015 Internal Audit Template",
    "description": "Comprehensive template for ISO 9001:2015 internal audits",
    "isoStandard": "ISO_9001",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "clauses": [...]
  }
]
```

### 1.3 Get Template by ID
**GET** `/api/checklists/templates/{id}`

**Response:** Same as 1.1 response

### 1.4 Get Templates by ISO Standard
**GET** `/api/checklists/templates/iso/{isoStandard}`

**Examples:**
- `/api/checklists/templates/iso/ISO_9001`
- `/api/checklists/templates/iso/ISO_27001`

### 1.5 Update Template
**PUT** `/api/checklists/templates/{id}`

**Request Body:** Same as 1.1

### 1.6 Delete Template
**DELETE** `/api/checklists/templates/{id}`

## 2. Checklist Creation from Template

### 2.1 Create Checklist from Template
**POST** `/api/checklists/audits/{auditId}/from-template?templateId={templateId}&selectedClauseIds={clauseIds}`

**Examples:**
- Create from all clauses: `POST /api/checklists/audits/1/from-template?templateId=1`
- Create from specific clauses: `POST /api/checklists/audits/1/from-template?templateId=1&selectedClauseIds=1,2,3`

**Response:**
```json
{
  "checklistId": 1,
  "isoStandard": "ISO_9001",
  "auditId": 1,
  "status": "PLANNED",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "checklistItems": [
    {
      "itemId": 1,
      "clauseId": 1,
      "clauseNumber": "5.2",
      "clauseName": "Quality Policy",
      "customText": "Check if quality policy is documented and communicated",
      "conformityStatus": "CONFORMITY",
      "comments": null,
      "evidenceNotes": null,
      "evaluatedBy": null,
      "evaluatedAt": null,
      "evidenceDocuments": [],
      "isEvaluated": false
    }
  ],
  "auditProgress": {
    "totalClauses": 1,
    "evaluatedClauses": 0,
    "completionPercentage": 0.0
  }
}
```

## 3. Checklist Management

### 3.1 Create Checklist
**POST** `/api/checklists`

**Request Body:**
```json
{
  "isoStandard": "ISO_9001",
  "auditId": 1,
  "status": "PLANNED"
}
```

### 3.2 Get All Checklists
**GET** `/api/checklists`

### 3.3 Get Checklist by ID
**GET** `/api/checklists/{id}`

### 3.4 Get Checklist by Audit ID
**GET** `/api/checklists/audits/{auditId}`

### 3.5 Update Checklist
**PUT** `/api/checklists/{id}`

**Request Body:** Same as 3.1

### 3.6 Delete Checklist
**DELETE** `/api/checklists/{id}`

### 3.7 Get Checklists by ISO Standard
**GET** `/api/checklists/iso/{isoStandard}`

## 4. Checklist Item Evaluation

### 4.1 Evaluate Single Checklist Item
**POST** `/api/checklists/items/evaluate`

**Request Body:**
```json
{
  "itemId": 1,
  "conformityStatus": "NON_CONFORMITY",
  "comments": "Quality policy is not properly communicated to all employees",
  "evidenceNotes": "Interviewed 5 employees, only 2 were aware of the quality policy",
  "evidenceDocumentIds": [1, 2, 3]
}
```

**Response:** `200 OK`

### 4.2 Evaluate Multiple Checklist Items
**POST** `/api/checklists/items/evaluate-batch`

**Request Body:**
```json
[
  {
    "itemId": 1,
    "conformityStatus": "CONFORMITY",
    "comments": "Quality policy is well documented and communicated",
    "evidenceNotes": "Reviewed policy document and interviewed employees",
    "evidenceDocumentIds": [1]
  },
  {
    "itemId": 2,
    "conformityStatus": "NON_CONFORMITY",
    "comments": "Quality objectives are not measurable",
    "evidenceNotes": "Objectives lack specific metrics and timelines",
    "evidenceDocumentIds": [2, 3]
  }
]
```

**Response:** `200 OK`

## 5. Audit Progress Tracking

### 5.1 Get Audit Progress
**GET** `/api/checklists/audits/{auditId}/progress`

**Response:**
```json
{
  "totalClauses": 10,
  "evaluatedClauses": 7,
  "completionPercentage": 70.0
}
```

### 5.2 Get Unevaluated Items
**GET** `/api/checklists/audits/{auditId}/items/unevaluated`

**Response:**
```json
[
  {
    "item_id": 8,
    "checklist": {...},
    "clause": {...},
    "custom_text": "Check if quality policy is documented and communicated",
    "conformity_status": null,
    "comments": null,
    "evidence_notes": null,
    "evaluated_by": null,
    "evaluated_at": null,
    "evidence_documents": [],
    "created_at": "2024-01-15T10:30:00",
    "updated_at": "2024-01-15T10:30:00"
  }
]
```

### 5.3 Get Items by Status
**GET** `/api/checklists/audits/{auditId}/items/status/{status}`

**Examples:**
- `/api/checklists/audits/1/items/status/CONFORMITY`
- `/api/checklists/audits/1/items/status/NON_CONFORMITY`
- `/api/checklists/audits/1/items/status/OBSERVATION`

## 6. Complete Audit Execution Workflow

### Step 1: Create Audit Template
```bash
curl -X POST http://localhost:8080/api/checklists/templates \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "ISO 9001:2015 Internal Audit Template",
    "description": "Comprehensive template for ISO 9001:2015 internal audits",
    "isoStandard": "ISO_9001",
    "clauses": [
      {
        "clauseId": 1,
        "customText": "Check if quality policy is documented and communicated",
        "customDescription": "Verify that the quality policy is properly documented and communicated to all employees"
      }
    ]
  }'
```

### Step 2: Create Checklist from Template
```bash
curl -X POST "http://localhost:8080/api/checklists/audits/1/from-template?templateId=1" \
  -H "Authorization: Bearer <token>"
```

### Step 3: Get Checklist for Audit
```bash
curl -X GET http://localhost:8080/api/checklists/audits/1 \
  -H "Authorization: Bearer <token>"
```

### Step 4: Evaluate Checklist Items
```bash
curl -X POST http://localhost:8080/api/checklists/items/evaluate \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 1,
    "conformityStatus": "CONFORMITY",
    "comments": "Quality policy is well documented and communicated",
    "evidenceNotes": "Reviewed policy document and interviewed employees",
    "evidenceDocumentIds": [1]
  }'
```

### Step 5: Track Progress
```bash
curl -X GET http://localhost:8080/api/checklists/audits/1/progress \
  -H "Authorization: Bearer <token>"
```

### Step 6: Get Items by Status
```bash
curl -X GET http://localhost:8080/api/checklists/audits/1/items/status/NON_CONFORMITY \
  -H "Authorization: Bearer <token>"
```

## 7. Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": "Template not found"
}
```

**401 Unauthorized:**
```json
{
  "error": "Access denied"
}
```

**404 Not Found:**
```json
{
  "error": "Checklist not found"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error occurred"
}
```

## 8. Testing Checklist

### Template Management
- [ ] Create template with valid data
- [ ] Create template with invalid clause IDs
- [ ] Get all templates
- [ ] Get template by ID (existing and non-existing)
- [ ] Get templates by ISO standard
- [ ] Update template
- [ ] Delete template

### Checklist Creation
- [ ] Create checklist from template (all clauses)
- [ ] Create checklist from template (selected clauses)
- [ ] Create checklist from non-existing template
- [ ] Create checklist for non-existing audit

### Checklist Management
- [ ] Create checklist manually
- [ ] Get all checklists
- [ ] Get checklist by ID
- [ ] Get checklist by audit ID
- [ ] Update checklist
- [ ] Delete checklist
- [ ] Get checklists by ISO standard

### Item Evaluation
- [ ] Evaluate single item (CONFORMITY)
- [ ] Evaluate single item (NON_CONFORMITY)
- [ ] Evaluate single item (OBSERVATION)
- [ ] Evaluate item with evidence documents
- [ ] Evaluate multiple items
- [ ] Evaluate non-existing item

### Progress Tracking
- [ ] Get audit progress (0% completion)
- [ ] Get audit progress (partial completion)
- [ ] Get audit progress (100% completion)
- [ ] Get unevaluated items
- [ ] Get items by status (CONFORMITY)
- [ ] Get items by status (NON_CONFORMITY)
- [ ] Get items by status (OBSERVATION)

### Authorization
- [ ] Test with ADMIN role
- [ ] Test with AUDITOR role
- [ ] Test with USER role (should fail)
- [ ] Test without authentication (should fail)

## 9. Performance Testing

### Load Testing
- Create 100 templates
- Create 50 checklists from templates
- Evaluate 1000 checklist items
- Get progress for 100 audits simultaneously

### Concurrent Testing
- Multiple users evaluating different checklist items
- Multiple users creating templates simultaneously
- Multiple users accessing audit progress

## 10. Security Testing

### Input Validation
- SQL injection attempts
- XSS attempts
- Large payload testing
- Malformed JSON testing

### Authorization Testing
- Access control for different roles
- Token validation
- Session management

This comprehensive API guide covers all aspects of the Checklist and Audit Execution module, providing production-ready endpoints for managing audit checklists, templates, and execution workflows.
