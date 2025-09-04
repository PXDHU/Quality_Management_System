# Checklist and Audit Execution - Quick Start Testing Guide

This guide provides a quick way to test all the Checklist and Audit Execution APIs using curl commands.

## Prerequisites

1. **Start the Application**
   ```bash
   cd Quality_Management_System
   mvn spring-boot:run
   ```

2. **Get Authentication Token**
   ```bash
   # Login to get JWT token
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin@example.com",
       "password": "admin123"
     }'
   ```

3. **Set Token Variable**
   ```bash
   # Replace <your-token> with the actual token from login response
   export TOKEN="<your-token>"
   ```

## Quick Test Sequence

### Step 1: Create Checklist Template
```bash
curl -X POST http://localhost:8080/api/checklists/templates \
  -H "Authorization: Bearer $TOKEN" \
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
      },
      {
        "clauseId": 2,
        "customText": "Review quality objectives and their measurement",
        "customDescription": "Ensure quality objectives are measurable and aligned with quality policy"
      }
    ]
  }'
```

### Step 2: Get All Templates
```bash
curl -X GET http://localhost:8080/api/checklists/templates \
  -H "Authorization: Bearer $TOKEN"
```

### Step 3: Create Checklist from Template
```bash
curl -X POST "http://localhost:8080/api/checklists/audits/1/from-template?templateId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### Step 4: Get Checklist for Audit
```bash
curl -X GET http://localhost:8080/api/checklists/audits/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Step 5: Evaluate Checklist Items
```bash
# Evaluate first item as CONFORMITY
curl -X POST http://localhost:8080/api/checklists/items/evaluate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 1,
    "conformityStatus": "CONFORMITY",
    "comments": "Quality policy is well documented and communicated",
    "evidenceNotes": "Reviewed policy document and interviewed employees",
    "evidenceDocumentIds": [1]
  }'

# Evaluate second item as NON_CONFORMITY
curl -X POST http://localhost:8080/api/checklists/items/evaluate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 2,
    "conformityStatus": "NON_CONFORMITY",
    "comments": "Quality objectives are not measurable",
    "evidenceNotes": "Objectives lack specific metrics and timelines",
    "evidenceDocumentIds": [2, 3]
  }'
```

### Step 6: Track Progress
```bash
curl -X GET http://localhost:8080/api/checklists/audits/1/progress \
  -H "Authorization: Bearer $TOKEN"
```

### Step 7: Get Items by Status
```bash
# Get conforming items
curl -X GET http://localhost:8080/api/checklists/audits/1/items/status/CONFORMITY \
  -H "Authorization: Bearer $TOKEN"

# Get non-conforming items
curl -X GET http://localhost:8080/api/checklists/audits/1/items/status/NON_CONFORMITY \
  -H "Authorization: Bearer $TOKEN"
```

## Complete Test Script

Create a file called `test_checklist_apis.sh`:

```bash
#!/bin/bash

# Set your token here
TOKEN="your-jwt-token-here"
BASE_URL="http://localhost:8080"

echo "=== Checklist and Audit Execution API Testing ==="

echo "1. Creating checklist template..."
TEMPLATE_RESPONSE=$(curl -s -X POST $BASE_URL/api/checklists/templates \
  -H "Authorization: Bearer $TOKEN" \
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
      },
      {
        "clauseId": 2,
        "customText": "Review quality objectives and their measurement",
        "customDescription": "Ensure quality objectives are measurable and aligned with quality policy"
      }
    ]
  }')

echo "Template created: $TEMPLATE_RESPONSE"

echo "2. Getting all templates..."
curl -s -X GET $BASE_URL/api/checklists/templates \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "3. Creating checklist from template..."
CHECKLIST_RESPONSE=$(curl -s -X POST "$BASE_URL/api/checklists/audits/1/from-template?templateId=1" \
  -H "Authorization: Bearer $TOKEN")

echo "Checklist created: $CHECKLIST_RESPONSE"

echo "4. Getting checklist for audit..."
curl -s -X GET $BASE_URL/api/checklists/audits/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "5. Evaluating checklist items..."
curl -s -X POST $BASE_URL/api/checklists/items/evaluate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 1,
    "conformityStatus": "CONFORMITY",
    "comments": "Quality policy is well documented and communicated",
    "evidenceNotes": "Reviewed policy document and interviewed employees",
    "evidenceDocumentIds": [1]
  }'

echo "6. Getting audit progress..."
curl -s -X GET $BASE_URL/api/checklists/audits/1/progress \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "7. Getting items by status..."
echo "Conforming items:"
curl -s -X GET $BASE_URL/api/checklists/audits/1/items/status/CONFORMITY \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "=== Testing Complete ==="
```

Make it executable and run:
```bash
chmod +x test_checklist_apis.sh
./test_checklist_apis.sh
```

## Expected Results

### Template Creation Response
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
    }
  ]
}
```

### Checklist Response
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
      "comments": "Quality policy is well documented and communicated",
      "evidenceNotes": "Reviewed policy document and interviewed employees",
      "evaluatedBy": "admin@example.com",
      "evaluatedAt": "2024-01-15T10:35:00",
      "evidenceDocuments": [
        {
          "documentId": 1,
          "fileName": "quality_policy.pdf",
          "fileType": "application/pdf",
          "uploadDate": "2024-01-15T10:30:00"
        }
      ],
      "isEvaluated": true
    }
  ],
  "auditProgress": {
    "totalClauses": 2,
    "evaluatedClauses": 1,
    "completionPercentage": 50.0
  }
}
```

### Progress Response
```json
{
  "totalClauses": 2,
  "evaluatedClauses": 1,
  "completionPercentage": 50.0
}
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check if token is valid
   - Ensure token is properly set in Authorization header

2. **404 Not Found**
   - Verify audit ID exists
   - Check if template ID is correct

3. **400 Bad Request**
   - Validate JSON format
   - Check required fields are present

4. **500 Internal Server Error**
   - Check application logs
   - Verify database connection

### Debug Commands

```bash
# Check application status
curl -X GET http://localhost:8080/actuator/health

# Check application logs
tail -f logs/application.log

# Test database connection
curl -X GET http://localhost:8080/api/checklists/templates \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

This quick start guide provides everything needed to test the Checklist and Audit Execution APIs efficiently.
