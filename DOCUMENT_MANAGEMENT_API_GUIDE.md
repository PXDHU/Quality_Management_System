# Document Management Module - API Guide

## Overview
The Document Management module provides comprehensive functionality for uploading, storing, linking, and searching audit-related documents and evidence files. This module supports file uploads, document linking to audits and non-conformities, advanced search capabilities, and access tracking.

## Key Features Implemented

### 1. Document Upload with Enhanced Metadata
- **File Upload**: Support for various file types (PDFs, images, Excel sheets, etc.)
- **Metadata Management**: Tags, clause references, department classification
- **Evidence Linking**: Direct linking to non-conformities as evidence
- **Audit Association**: Link documents to specific audits

### 2. Document Search and Discovery
- **Advanced Search**: Multi-criteria search with pagination
- **File Name Search**: Search by file name patterns
- **Description Search**: Full-text search in document descriptions
- **Tag-based Search**: Search by document tags
- **Clause-based Search**: Find documents by ISO clause reference
- **Department-based Search**: Filter by department

### 3. Document Linking and Management
- **Audit Linking**: Link documents to multiple audits
- **NC Evidence Linking**: Link documents as evidence to non-conformities
- **Metadata Updates**: Update document metadata without re-uploading
- **Access Tracking**: Track document access patterns

### 4. Performance and Analytics
- **Access Analytics**: Most accessed and recently accessed documents
- **Pagination Support**: Efficient handling of large document collections
- **Database Indexing**: Optimized search performance

## Technical Implementation

### Database Schema
The document table has been enhanced with new fields:

```sql
-- Enhanced document table
document_id BIGINT PRIMARY KEY
file_name VARCHAR(255) NOT NULL
file_type VARCHAR(100) NOT NULL
file_size BIGINT NOT NULL
data LONGBLOB
description TEXT
uploaded_by BIGINT NOT NULL
uploaded_at TIMESTAMP NOT NULL
tags VARCHAR(500)
clause_reference VARCHAR(100)
department VARCHAR(100)
is_evidence BOOLEAN NOT NULL DEFAULT FALSE
last_accessed TIMESTAMP
access_count INTEGER NOT NULL DEFAULT 0

-- Document-NC relationship table
document_nc (
    document_id BIGINT NOT NULL,
    non_conformity_id BIGINT NOT NULL,
    PRIMARY KEY (document_id, non_conformity_id)
)
```

### API Endpoints

#### Core Document Management
1. **POST** `/api/documents` - Upload new document
2. **GET** `/api/documents` - Get all documents
3. **GET** `/api/documents/{id}` - Get specific document
4. **PUT** `/api/documents/{id}/metadata` - Update document metadata
5. **DELETE** `/api/documents/{id}` - Delete document (Admin only)

#### Document Download
6. **GET** `/api/documents/{id}/download` - Download document file

#### Document Linking
7. **POST** `/api/documents/{id}/link-audits` - Link document to audits
8. **POST** `/api/documents/{id}/link-ncs` - Link document to NCs

#### Document Search
9. **POST** `/api/documents/search` - Advanced search with pagination
10. **GET** `/api/documents/search/filename` - Search by file name
11. **GET** `/api/documents/search/description` - Search by description
12. **GET** `/api/documents/search/tags` - Search by tags

#### Document Filtering
13. **GET** `/api/documents/audit/{auditId}` - Get documents by audit
14. **GET** `/api/documents/audit/{auditId}/page` - Get documents by audit with pagination
15. **GET** `/api/documents/nc/{ncId}` - Get documents by NC
16. **GET** `/api/documents/nc/{ncId}/page` - Get documents by NC with pagination
17. **GET** `/api/documents/evidence` - Get evidence documents
18. **GET** `/api/documents/clause/{clauseReference}` - Get documents by clause
19. **GET** `/api/documents/department/{department}` - Get documents by department
20. **GET** `/api/documents/user/{userId}` - Get documents by user

#### Analytics and Insights
21. **GET** `/api/documents/most-accessed` - Get most accessed documents
22. **GET** `/api/documents/recently-accessed` - Get recently accessed documents

### Data Transfer Objects (DTOs)

#### DocumentRequest
Enhanced request DTO for document upload and metadata updates:
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

#### DocumentResponse
Comprehensive response DTO:
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

#### DocumentSearchRequest
Advanced search DTO with pagination:
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

## Security and Authorization

### Role-Based Access Control
- **ADMIN**: Full access to all document operations including deletion
- **AUDITOR**: Can upload, link, and manage documents
- **USER**: Can view and download documents

### Endpoint Security
```java
@PreAuthorize("isAuthenticated()")     // View and download documents
@PreAuthorize("isAuthenticated()")     // Upload and link documents
@PreAuthorize("hasRole('ADMIN')")      // Delete documents
```

## File Upload Guidelines

### Supported File Types
- **Documents**: PDF, DOC, DOCX, TXT, RTF
- **Images**: JPG, JPEG, PNG, GIF, BMP
- **Spreadsheets**: XLS, XLSX, CSV
- **Presentations**: PPT, PPTX
- **Other**: ZIP, RAR (for compressed files)

### File Size Limits
- **Maximum file size**: 10MB per file
- **Recommended size**: Under 5MB for optimal performance

### Upload Parameters
```http
POST /api/documents
Content-Type: multipart/form-data

file: [binary file data]
description: "Document description"
auditIds: "1,2,3"
ncIds: "4,5"
tags: "audit,evidence,iso9001"
clauseReference: "7.5.1"
department: "Quality Assurance"
isEvidence: true
```

## Search and Filtering

### Basic Search Examples

#### Search by File Name
```http
GET /api/documents/search/filename?fileName=audit
```

#### Search by Description
```http
GET /api/documents/search/description?description=quality
```

#### Search by Tags
```http
GET /api/documents/search/tags?tag=evidence
```

### Advanced Search
```http
POST /api/documents/search
Content-Type: application/json

{
  "fileName": "audit",
  "fileType": "application/pdf",
  "department": "Quality Assurance",
  "clauseReference": "7.5.1",
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

### Filtering Examples

#### Get Documents by Audit
```http
GET /api/documents/audit/1
```

#### Get Documents by NC
```http
GET /api/documents/nc/5
```

#### Get Evidence Documents
```http
GET /api/documents/evidence
```

#### Get Documents by Clause
```http
GET /api/documents/clause/7.5.1
```

## Document Linking

### Link Document to Audits
```http
POST /api/documents/1/link-audits
Content-Type: application/json

[2, 3, 4]
```

### Link Document to NCs
```http
POST /api/documents/1/link-ncs
Content-Type: application/json

[5, 6, 7]
```

### Update Document Metadata
```http
PUT /api/documents/1/metadata
Content-Type: application/json

{
  "description": "Updated description",
  "tags": "updated,audit,evidence",
  "clauseReference": "8.2.1",
  "department": "Operations",
  "isEvidence": true
}
```

## Error Handling

### Common Error Responses
```json
// File too large
{
  "error": "File size exceeds maximum limit of 10MB"
}

// Invalid file type
{
  "error": "Unsupported file type"
}

// Document not found
{
  "error": "Document not found"
}

// Validation error
{
  "error": "Invalid audit ID format"
}
```

### HTTP Status Codes
- **200 OK**: Successful operation
- **201 Created**: Document uploaded successfully
- **400 Bad Request**: Invalid input or file
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Document not found
- **500 Internal Server Error**: Server error

## Performance Considerations

### Database Indexes
```sql
-- Performance indexes for search operations
CREATE INDEX idx_document_file_name ON document(file_name);
CREATE INDEX idx_document_file_type ON document(file_type);
CREATE INDEX idx_document_department ON document(department);
CREATE INDEX idx_document_clause_reference ON document(clause_reference);
CREATE INDEX idx_document_is_evidence ON document(is_evidence);
CREATE INDEX idx_document_uploaded_at ON document(uploaded_at);
CREATE INDEX idx_document_last_accessed ON document(last_accessed);
CREATE INDEX idx_document_access_count ON document(access_count);
CREATE INDEX idx_document_tags ON document(tags);
```

### Pagination
All list endpoints support pagination with default page size of 20:
```http
GET /api/documents/audit/1/page?page=0&size=10
```

### Caching Strategy
- Document metadata can be cached for frequently accessed documents
- Search results can be cached for common search patterns
- File downloads should not be cached for security reasons

## Testing

### Sample Test Data

#### Upload Document
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

#### Search Documents
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

#### Link Document to NCs
```bash
curl -X POST http://localhost:8080/api/documents/1/link-ncs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[5, 6, 7]'
```

## Monitoring and Analytics

### Access Tracking
- Document access count is automatically incremented on each download/view
- Last accessed timestamp is updated on each access
- Analytics endpoints provide insights into document usage patterns

### Performance Metrics
- Upload success rate
- Search response times
- Download frequency
- Most accessed documents
- Recently accessed documents

## Best Practices

### File Management
1. **Use descriptive file names**: Include date, type, and purpose
2. **Add comprehensive descriptions**: Include context and relevance
3. **Use appropriate tags**: Tag documents for easy discovery
4. **Link to relevant entities**: Connect documents to audits and NCs
5. **Regular cleanup**: Archive or delete outdated documents

### Search Optimization
1. **Use specific search criteria**: Combine multiple filters for precise results
2. **Leverage pagination**: Use pagination for large result sets
3. **Utilize tags**: Tag documents consistently for better search
4. **Update metadata**: Keep document metadata current and accurate

### Security Considerations
1. **File validation**: Validate file types and sizes
2. **Access control**: Ensure proper authorization for document access
3. **Audit trail**: Track document access and modifications
4. **Data protection**: Secure storage of sensitive documents

## Future Enhancements

### Planned Features
1. **Document Versioning**: Support for document version control
2. **Bulk Operations**: Upload and manage multiple documents
3. **Document Templates**: Predefined document templates
4. **OCR Integration**: Text extraction from images and PDFs
5. **Document Workflow**: Approval workflows for document publication
6. **Integration**: Integration with external document management systems

### Scalability Considerations
- File storage optimization for large files
- Database partitioning for large document collections
- CDN integration for global document access
- Asynchronous processing for document operations

## Conclusion

The Document Management module provides a robust, scalable, and secure solution for managing audit-related documents in the Quality Management System. With comprehensive search capabilities, flexible linking options, and performance optimizations, it meets the requirements for production deployment.

The implementation follows Spring Boot best practices and includes proper error handling, validation, and security measures. The modular design allows for easy extension and maintenance.
