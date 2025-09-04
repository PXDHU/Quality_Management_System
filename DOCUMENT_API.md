# Document Management API

## Overview
The Document Management module allows authenticated users to upload, view, and manage documents in the Quality Management System. Documents can be linked to multiple audits and are stored centrally in PostgreSQL.

## Authentication
All endpoints require authentication via JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
X-User-Id: <user_id>
```

## Endpoints

### 1. Upload Document
**POST** `/api/documents`
- **Content-Type**: `multipart/form-data`
- **Authorization**: Any authenticated user
- **Parameters**:
  - `file` (required): The file to upload (max 10MB)
  - `description` (optional): Document description
  - `auditIds` (optional): Set of audit IDs to link the document to

**Example Request**:
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer <jwt_token>" \
  -H "X-User-Id: 1" \
  -F "file=@document.pdf" \
  -F "description=Audit evidence document" \
  -F "auditIds=1,2,3"
```

### 2. Get All Documents
**GET** `/api/documents`
- **Authorization**: Any authenticated user
- **Response**: List of all documents with metadata

### 3. Get Document by ID
**GET** `/api/documents/{id}`
- **Authorization**: Any authenticated user
- **Response**: Document metadata

### 4. Download Document
**GET** `/api/documents/{id}/download`
- **Authorization**: Any authenticated user
- **Response**: File content with appropriate headers

### 5. Get Documents by Audit
**GET** `/api/documents/audit/{auditId}`
- **Authorization**: Any authenticated user
- **Response**: List of documents linked to the specified audit

### 6. Get Documents by User
**GET** `/api/documents/user/{userId}`
- **Authorization**: Any authenticated user
- **Response**: List of documents uploaded by the specified user

### 7. Link Document to Audits
**POST** `/api/documents/{id}/link-audits`
- **Authorization**: Any authenticated user
- **Body**: Set of audit IDs
- **Response**: Updated document metadata

### 8. Delete Document
**DELETE** `/api/documents/{id}`
- **Authorization**: ADMIN role only
- **Response**: 204 No Content

## Response Format

### DocumentResponse
```json
{
  "documentId": 1,
  "fileName": "document.pdf",
  "fileType": "application/pdf",
  "description": "Audit evidence document",
  "uploadedBy": "John Doe",
  "uploadedAt": "2024-01-15T10:30:00",
  "auditIds": [1, 2, 3],
  "auditTitles": ["QMS Audit 2024", "Compliance Review", "Process Audit"]
}
```

## Error Handling

### Common Error Responses
- **401 Unauthorized**: Invalid or missing JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Document not found
- **413 Payload Too Large**: File size exceeds 10MB limit
- **500 Internal Server Error**: Server error

### Error Response Format
```json
{
  "error": "Error message description"
}
```

## Features

### Security
- JWT-based authentication required for all endpoints
- Role-based access control (ADMIN required for deletion)
- File size limits (10MB max)

### Functionality
- Central document storage in PostgreSQL
- Many-to-many relationship with audits
- Support for multiple file types
- Transactional upload operations
- Document metadata tracking

### User Roles
- **AUDITEE**: Can upload and view documents
- **AUDITOR**: Can upload and view documents
- **ADMIN**: Can upload, view, and delete documents
- **REVIEWER**: Can upload and view documents
- **COMPLIANCE_OFFICER**: Can upload and view documents

## Database Schema

### Document Table
- `document_id` (Primary Key)
- `file_name` (Required)
- `file_type` (Required)
- `data` (BLOB - file content)
- `description` (Optional)
- `uploaded_by` (Foreign Key to AuthUser)
- `uploaded_at` (Timestamp)

### Audit-Document Relationship
- Many-to-many relationship via `audit_document` junction table
- Unique constraint on audit_id + document_id combination
