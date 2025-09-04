# Document Management Module - Quick Start Testing Guide

## Overview
This guide provides step-by-step instructions for testing the Document Management module APIs. Follow these steps to verify all functionality is working correctly.

## Prerequisites
1. **Application Running**: Ensure the QMS application is running on `http://localhost:8080`
2. **Database Setup**: Ensure the database is running and migrations are applied
3. **Authentication**: Obtain a valid JWT token for testing
4. **Test Files**: Prepare some test files (PDF, images, etc.) for upload testing

## Step 1: Health Check
First, verify the Document Management module is accessible:

```bash
curl -X GET http://localhost:8080/api/documents/health
```

**Expected Response**: `Document Controller is working!`

## Step 2: Authentication Setup
Get a JWT token by logging in:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@qms.com",
    "password": "admin123"
  }'
```

**Save the JWT token** from the response for use in subsequent requests.

## Step 3: Upload Document Test
Test document upload with metadata:

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test_document.pdf" \
  -F "description=Test audit report for Q1 2024" \
  -F "auditIds=1,2" \
  -F "ncIds=3" \
  -F "tags=test,audit,evidence" \
  -F "clauseReference=9.2" \
  -F "department=Quality Assurance" \
  -F "isEvidence=true"
```

**Expected Response**: Document details with ID, file information, and linked entities.

## Step 4: Retrieve All Documents
Test getting all documents:

```bash
curl -X GET http://localhost:8080/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**: List of all documents with metadata.

## Step 5: Get Specific Document
Test retrieving a specific document (replace `{document_id}` with actual ID):

```bash
curl -X GET http://localhost:8080/api/documents/{document_id} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**: Detailed document information including linked audits and NCs.

## Step 6: Download Document
Test document download:

```bash
curl -X GET http://localhost:8080/api/documents/{document_id}/download \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output downloaded_file.pdf
```

**Expected Response**: File download with proper headers.

## Step 7: Search Documents
Test various search functionalities:

### Search by File Name
```bash
curl -X GET "http://localhost:8080/api/documents/search/filename?fileName=test" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Search by Description
```bash
curl -X GET "http://localhost:8080/api/documents/search/description?description=audit" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Search by Tags
```bash
curl -X GET "http://localhost:8080/api/documents/search/tags?tag=evidence" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Advanced Search
```bash
curl -X POST http://localhost:8080/api/documents/search \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test",
    "department": "Quality Assurance",
    "isEvidence": true,
    "page": 0,
    "size": 10
  }'
```

## Step 8: Filter Documents
Test filtering by different criteria:

### Get Documents by Audit
```bash
curl -X GET http://localhost:8080/api/documents/audit/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Documents by NC
```bash
curl -X GET http://localhost:8080/api/documents/nc/3 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Evidence Documents
```bash
curl -X GET http://localhost:8080/api/documents/evidence \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Documents by Clause
```bash
curl -X GET http://localhost:8080/api/documents/clause/9.2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Documents by Department
```bash
curl -X GET "http://localhost:8080/api/documents/department/Quality%20Assurance" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Step 9: Document Linking
Test linking documents to additional entities:

### Link to Additional Audits
```bash
curl -X POST http://localhost:8080/api/documents/{document_id}/link-audits \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[4, 5, 6]'
```

### Link to Additional NCs
```bash
curl -X POST http://localhost:8080/api/documents/{document_id}/link-ncs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[7, 8, 9]'
```

## Step 10: Update Document Metadata
Test updating document metadata:

```bash
curl -X PUT http://localhost:8080/api/documents/{document_id}/metadata \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated test audit report",
    "tags": "test,audit,evidence,updated",
    "clauseReference": "9.2.1",
    "department": "Quality Assurance",
    "isEvidence": true
  }'
```

## Step 11: Analytics and Insights
Test analytics endpoints:

### Most Accessed Documents
```bash
curl -X GET "http://localhost:8080/api/documents/most-accessed?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Recently Accessed Documents
```bash
curl -X GET "http://localhost:8080/api/documents/recently-accessed?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Step 12: Pagination Testing
Test pagination with different page sizes:

```bash
curl -X GET "http://localhost:8080/api/documents/audit/1/page?page=0&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Step 13: Error Handling Testing
Test various error scenarios:

### Invalid Document ID
```bash
curl -X GET http://localhost:8080/api/documents/99999 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**: 404 Not Found

### Unauthorized Access
```bash
curl -X GET http://localhost:8080/api/documents
```

**Expected Response**: 401 Unauthorized

### Invalid File Upload
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "description=Test without file"
```

**Expected Response**: 400 Bad Request

## Step 14: Admin Operations
Test admin-only operations (requires ADMIN role):

### Delete Document
```bash
curl -X DELETE http://localhost:8080/api/documents/{document_id} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**: 204 No Content (if successful)

## Testing Checklist

### Core Functionality
- [ ] Document upload with metadata
- [ ] Document retrieval by ID
- [ ] Document download
- [ ] Document listing
- [ ] Document metadata update

### Search and Filtering
- [ ] Search by file name
- [ ] Search by description
- [ ] Search by tags
- [ ] Advanced search with multiple criteria
- [ ] Filter by audit
- [ ] Filter by NC
- [ ] Filter by evidence status
- [ ] Filter by clause reference
- [ ] Filter by department

### Linking and Relationships
- [ ] Link document to audits
- [ ] Link document to NCs
- [ ] View linked entities in document details

### Analytics
- [ ] Most accessed documents
- [ ] Recently accessed documents
- [ ] Access count tracking

### Pagination
- [ ] Paginated results
- [ ] Different page sizes
- [ ] Sorting options

### Error Handling
- [ ] Invalid document ID
- [ ] Unauthorized access
- [ ] Invalid file upload
- [ ] Missing required fields

### Security
- [ ] Authentication required
- [ ] Role-based access control
- [ ] Admin-only operations

## Performance Testing

### Large File Upload
Test with files of different sizes:
- Small files (< 1MB)
- Medium files (1-5MB)
- Large files (5-10MB)

### Search Performance
Test search with:
- Small result sets (< 10 documents)
- Large result sets (> 100 documents)
- Complex search criteria

### Concurrent Access
Test multiple simultaneous:
- Document uploads
- Document downloads
- Search operations

## Troubleshooting

### Common Issues

1. **File Upload Fails**
   - Check file size limits
   - Verify file type is supported
   - Ensure proper multipart/form-data format

2. **Authentication Errors**
   - Verify JWT token is valid
   - Check token expiration
   - Ensure proper Authorization header format

3. **Database Errors**
   - Check database connection
   - Verify migrations are applied
   - Check database logs for errors

4. **Search Not Working**
   - Verify search parameters are correct
   - Check database indexes are created
   - Test with simple search criteria first

### Debug Information
Enable debug logging in application.properties:
```properties
logging.level.com.example.QualityManagementSystem=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Next Steps
After completing these tests:

1. **Review Results**: Ensure all tests pass
2. **Performance Analysis**: Monitor response times
3. **Security Review**: Verify access controls
4. **Documentation**: Update API documentation if needed
5. **Integration Testing**: Test with frontend application

## Support
If you encounter issues during testing:

1. Check the application logs
2. Verify database connectivity
3. Review the API documentation
4. Test with Postman collection
5. Contact the development team
