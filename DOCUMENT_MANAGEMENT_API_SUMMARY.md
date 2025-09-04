# Document Management Module - API Summary

## Overview
This document provides a complete summary of all Document Management APIs with request/response formats and examples.

## Base URL
```
http://localhost:8080/api/documents
```

## Authentication
All endpoints require JWT authentication:
```
Authorization: Bearer <jwt_token>
```

## API Endpoints Summary

### 1. Health Check
**GET** `/api/documents/health`
- **Description**: Check if the Document Management module is accessible
- **Authentication**: Not required
- **Response**: `Document Controller is working!`

### 2. Upload Document
**POST** `/api/documents`
- **Description**: Upload a new document with metadata
- **Authentication**: Required
- **Content-Type**: `multipart/form-data`

**Request Parameters:**
```
file: [binary file data] (required)
description: string (optional)
auditIds: "1,2,3" (optional)
ncIds: "4,5" (optional)
tags: "audit,evidence,iso9001" (optional)
clauseReference: "9.2" (optional)
department: "Quality Assurance" (optional)
isEvidence: true/false (optional, default: false)
```

**Response:**
```json
{
  "documentId": 1,
  "fileName": "audit_report.pdf",
  "fileType": "application/pdf",
  "fileSize": 1024000,
  "description": "Q1 2024 Internal Audit Report",
  "uploadedBy": "John Doe",
  "uploadedAt": "2024-01-15T10:30:00",
  "auditIds": [1, 2],
  "auditTitles": ["Q1 Internal Audit", "Q2 Internal Audit"],
  "ncIds": [3],
  "ncTitles": ["Document Control Issue"],
  "tags": "audit,evidence,iso9001",
  "clauseReference": "9.2",
  "department": "Quality Assurance",
  "isEvidence": true,
  "lastAccessed": null,
  "accessCount": 0
}
```

### 3. Get All Documents
**GET** `/api/documents`
- **Description**: Retrieve all documents
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 4. Get Document by ID
**GET** `/api/documents/{id}`
- **Description**: Retrieve a specific document by ID
- **Authentication**: Required
- **Response**: Single DocumentResponse object

### 5. Download Document
**GET** `/api/documents/{id}/download`
- **Description**: Download the actual file
- **Authentication**: Required
- **Response**: File download with appropriate headers

### 6. Update Document Metadata
**PUT** `/api/documents/{id}/metadata`
- **Description**: Update document metadata without re-uploading
- **Authentication**: Required
- **Content-Type**: `application/json`

**Request Body:**
```json
{
  "description": "Updated description",
  "tags": "updated,audit,evidence",
  "clauseReference": "8.2.1",
  "department": "Operations",
  "isEvidence": true
}
```

**Response:** Updated DocumentResponse object

### 7. Link Document to Audits
**POST** `/api/documents/{id}/link-audits`
- **Description**: Link document to additional audits
- **Authentication**: Required
- **Content-Type**: `application/json`

**Request Body:**
```json
[2, 3, 4]
```

**Response:** Updated DocumentResponse object

### 8. Link Document to NCs
**POST** `/api/documents/{id}/link-ncs`
- **Description**: Link document to non-conformities as evidence
- **Authentication**: Required
- **Content-Type**: `application/json`

**Request Body:**
```json
[5, 6, 7]
```

**Response:** Updated DocumentResponse object

### 9. Get Documents by Audit
**GET** `/api/documents/audit/{auditId}`
- **Description**: Get all documents linked to a specific audit
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 10. Get Documents by Audit with Pagination
**GET** `/api/documents/audit/{auditId}/page`
- **Description**: Get documents by audit with pagination
- **Authentication**: Required
- **Query Parameters:**
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 20)
- **Response:** Page of DocumentResponse objects

### 11. Get Documents by NC
**GET** `/api/documents/nc/{ncId}`
- **Description**: Get all documents linked to a specific non-conformity
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 12. Get Documents by NC with Pagination
**GET** `/api/documents/nc/{ncId}/page`
- **Description**: Get documents by NC with pagination
- **Authentication**: Required
- **Query Parameters:**
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 20)
- **Response:** Page of DocumentResponse objects

### 13. Get Evidence Documents
**GET** `/api/documents/evidence`
- **Description**: Get all documents marked as evidence
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 14. Get Documents by Clause
**GET** `/api/documents/clause/{clauseReference}`
- **Description**: Get documents by ISO clause reference
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 15. Get Documents by Department
**GET** `/api/documents/department/{department}`
- **Description**: Get documents by department
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 16. Get Documents by User
**GET** `/api/documents/user/{userId}`
- **Description**: Get documents uploaded by a specific user
- **Authentication**: Required
- **Response**: Array of DocumentResponse objects

### 17. Search Documents by File Name
**GET** `/api/documents/search/filename`
- **Description**: Search documents by file name pattern
- **Authentication**: Required
- **Query Parameters:**
  - `fileName`: File name pattern to search for
- **Response**: Array of DocumentResponse objects

### 18. Search Documents by Description
**GET** `/api/documents/search/description`
- **Description**: Search documents by description content
- **Authentication**: Required
- **Query Parameters:**
  - `description`: Text to search in descriptions
- **Response**: Array of DocumentResponse objects

### 19. Search Documents by Tags
**GET** `/api/documents/search/tags`
- **Description**: Search documents by tags
- **Authentication**: Required
- **Query Parameters:**
  - `tag`: Tag to search for
- **Response**: Array of DocumentResponse objects

### 20. Advanced Search Documents
**POST** `/api/documents/search`
- **Description**: Advanced search with multiple criteria and pagination
- **Authentication**: Required
- **Content-Type**: `application/json`

**Request Body:**
```json
{
  "fileName": "audit",
  "fileType": "application/pdf",
  "department": "Quality Assurance",
  "clauseReference": "9.2",
  "isEvidence": true,
  "uploadedBy": "John Doe",
  "description": "quality",
  "tags": "evidence",
  "page": 0,
  "size": 20,
  "sortBy": "uploadedAt",
  "sortDirection": "DESC"
}
```

**Response:** Page of DocumentResponse objects

### 21. Get Most Accessed Documents
**GET** `/api/documents/most-accessed`
- **Description**: Get documents sorted by access count
- **Authentication**: Required
- **Query Parameters:**
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 20)
- **Response:** Page of DocumentResponse objects

### 22. Get Recently Accessed Documents
**GET** `/api/documents/recently-accessed`
- **Description**: Get documents sorted by last access time
- **Authentication**: Required
- **Query Parameters:**
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 20)
- **Response:** Page of DocumentResponse objects

### 23. Delete Document
**DELETE** `/api/documents/{id}`
- **Description**: Delete a document (Admin only)
- **Authentication**: Required (ADMIN role)
- **Response**: 204 No Content

## Data Transfer Objects (DTOs)

### DocumentRequest
```java
public class DocumentRequest {
    public MultipartFile file;
    public String description;
    public List<Long> auditIds;
    public List<Long> ncIds;
    public String tags;
    public String clauseReference;
    public String department;
    public Boolean isEvidence = false;
}
```

### DocumentResponse
```java
public class DocumentResponse {
    public Long documentId;
    public String fileName;
    public String fileType;
    public Long fileSize;
    public String description;
    public String uploadedBy;
    public LocalDateTime uploadedAt;
    public List<Long> auditIds;
    public List<String> auditTitles;
    public List<Long> ncIds;
    public List<String> ncTitles;
    public String tags;
    public String clauseReference;
    public String department;
    public Boolean isEvidence;
    public LocalDateTime lastAccessed;
    public Integer accessCount;
}
```

### DocumentSearchRequest
```java
public class DocumentSearchRequest {
    public String fileName;
    public String fileType;
    public String tags;
    public String clauseReference;
    public String department;
    public String uploadedBy;
    public LocalDate uploadedFrom;
    public LocalDate uploadedTo;
    public Long auditId;
    public Long ncId;
    public Boolean isEvidence;
    public String description;
    public Integer page = 0;
    public Integer size = 20;
    public String sortBy = "uploadedAt";
    public String sortDirection = "DESC";
}
```

## HTTP Status Codes

- **200 OK**: Successful operation
- **201 Created**: Document uploaded successfully
- **204 No Content**: Document deleted successfully
- **400 Bad Request**: Invalid input or file
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Document not found
- **500 Internal Server Error**: Server error

## Error Response Format
```json
{
  "error": "Error message description",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/documents/upload"
}
```

## File Upload Guidelines

### Supported File Types
- **Documents**: PDF, DOC, DOCX, TXT, RTF
- **Images**: JPG, JPEG, PNG, GIF, BMP
- **Spreadsheets**: XLS, XLSX, CSV
- **Presentations**: PPT, PPTX
- **Other**: ZIP, RAR

### File Size Limits
- **Maximum**: 10MB per file
- **Recommended**: Under 5MB for optimal performance

## Security and Authorization

### Role-Based Access
- **ADMIN**: Full access including deletion
- **AUDITOR**: Upload, link, and manage documents
- **USER**: View and download documents

### Authentication
All endpoints (except health check) require valid JWT token in Authorization header.

## Performance Considerations

### Pagination
- Default page size: 20
- Maximum page size: 100
- Sorting options: uploadedAt, fileName, fileSize, accessCount

### Database Indexes
Optimized indexes for:
- File name searches
- Department filtering
- Clause reference filtering
- Access tracking
- Upload date sorting

## Testing Examples

### Upload Document
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@audit_report.pdf" \
  -F "description=Q1 2024 Internal Audit Report" \
  -F "auditIds=1,2" \
  -F "ncIds=3" \
  -F "tags=audit,report,evidence" \
  -F "clauseReference=9.2" \
  -F "department=Quality Assurance" \
  -F "isEvidence=true"
```

### Advanced Search
```bash
curl -X POST http://localhost:8080/api/documents/search \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "audit",
    "department": "Quality Assurance",
    "isEvidence": true,
    "page": 0,
    "size": 10
  }'
```

### Link to NCs
```bash
curl -X POST http://localhost:8080/api/documents/1/link-ncs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[5, 6, 7]'
```

## Monitoring and Analytics

### Access Tracking
- Automatic access count increment on download/view
- Last accessed timestamp update
- Analytics endpoints for usage patterns

### Performance Metrics
- Upload success rate
- Search response times
- Download frequency
- Most/recently accessed documents

## Best Practices

### File Management
1. Use descriptive file names
2. Add comprehensive descriptions
3. Use appropriate tags
4. Link to relevant entities
5. Regular cleanup of outdated documents

### Search Optimization
1. Use specific search criteria
2. Leverage pagination for large result sets
3. Utilize tags consistently
4. Keep metadata current

### Security
1. Validate file types and sizes
2. Ensure proper access control
3. Track document access
4. Secure storage of sensitive documents
