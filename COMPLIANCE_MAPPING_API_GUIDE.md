# Compliance Mapping & Clause Management API Guide

## Overview
This module provides comprehensive APIs for managing clause libraries and cross-standard compliance mappings between ISO 9001 and ISO 27001 standards. It enables compliance officers to map similar clauses across different standards, avoiding duplicate audits and ensuring comprehensive coverage.

## Base URLs
- **Clause Library API**: `/api/clauses`
- **Compliance Mapping API**: `/api/compliance-mapping`

## Authentication
All APIs require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 1. Clause Library Management APIs

### 1.1 Create New Clause
**POST** `/api/clauses`
- **Access**: Admin only
- **Description**: Creates a new clause in the library
- **Request Body**:
```json
{
  "clauseNumber": "4.1",
  "clauseName": "Understanding the organization and its context",
  "description": "The organization shall determine external and internal issues...",
  "standard": "ISO_9001",
  "version": "2015",
  "effectiveDate": "2015-09-15T00:00:00",
  "category": "Context",
  "riskLevel": "MEDIUM"
}
```
- **Response**: `201 Created` with clause details

### 1.2 Update Existing Clause
**PUT** `/api/clauses/{clauseId}`
- **Access**: Admin only
- **Description**: Updates an existing clause
- **Request Body**: Same as create
- **Response**: `200 OK` with updated clause details

### 1.3 Get Clause by ID
**GET** `/api/clauses/{clauseId}`
- **Access**: All authenticated users
- **Description**: Retrieves a specific clause by ID
- **Response**: `200 OK` with clause details

### 1.4 Get All Clauses
**GET** `/api/clauses`
- **Access**: All authenticated users
- **Description**: Retrieves all active clauses
- **Response**: `200 OK` with list of clauses

### 1.5 Get Clauses by Standard
**GET** `/api/clauses/standard/{standard}`
- **Access**: All authenticated users
- **Description**: Retrieves clauses for a specific standard (ISO_9001 or ISO_27001)
- **Response**: `200 OK` with list of clauses

### 1.6 Get Clauses by Standard and Category
**GET** `/api/clauses/standard/{standard}/category/{category}`
- **Access**: All authenticated users
- **Description**: Retrieves clauses filtered by standard and category
- **Response**: `200 OK` with list of clauses

### 1.7 Get Clauses by Standard and Risk Level
**GET** `/api/clauses/standard/{standard}/risk/{riskLevel}`
- **Access**: All authenticated users
- **Description**: Retrieves clauses filtered by standard and risk level
- **Response**: `200 OK` with list of clauses

### 1.8 Search Clauses by Keyword
**GET** `/api/clauses/search?keyword={keyword}`
- **Access**: All authenticated users
- **Description**: Searches clauses by keyword in name or description
- **Response**: `200 OK` with list of matching clauses

### 1.9 Search Clauses by Standard and Keyword
**GET** `/api/clauses/search/{standard}?keyword={keyword}`
- **Access**: All authenticated users
- **Description**: Searches clauses by standard and keyword
- **Response**: `200 OK` with list of matching clauses

### 1.10 Get Active Clauses by Standard and Date
**GET** `/api/clauses/standard/{standard}/active?date={date}`
- **Access**: All authenticated users
- **Description**: Retrieves active clauses for a standard as of a specific date
- **Response**: `200 OK` with list of active clauses

### 1.11 Deactivate Clause
**PATCH** `/api/clauses/{clauseId}/deactivate`
- **Access**: Admin only
- **Description**: Deactivates a clause (soft delete)
- **Response**: `204 No Content`

### 1.12 Activate Clause
**PATCH** `/api/clauses/{clauseId}/activate`
- **Access**: Admin only
- **Description**: Reactivates a deactivated clause
- **Response**: `204 No Content`

## 2. Compliance Mapping APIs

### 2.1 Create New Compliance Mapping
**POST** `/api/compliance-mapping`
- **Access**: Compliance Officer, Admin
- **Description**: Creates a mapping between clauses from different standards
- **Request Body**:
```json
{
  "sourceClauseId": 1,
  "targetClauseId": 12,
  "mappingType": "EXACT_MATCH",
  "similarityScore": 0.95,
  "mappingNotes": "Both clauses deal with understanding organizational context"
}
```
- **Response**: `201 Created` with mapping details

### 2.2 Update Existing Mapping
**PUT** `/api/compliance-mapping/{mappingId}`
- **Access**: Compliance Officer, Admin
- **Description**: Updates an existing compliance mapping
- **Request Body**: Same as create
- **Response**: `200 OK` with updated mapping details

### 2.3 Get Mapping by ID
**GET** `/api/compliance-mapping/{mappingId}`
- **Access**: All authenticated users
- **Description**: Retrieves a specific mapping by ID
- **Response**: `200 OK` with mapping details

### 2.4 Get All Mappings
**GET** `/api/compliance-mapping`
- **Access**: All authenticated users
- **Description**: Retrieves all compliance mappings
- **Response**: `200 OK` with list of mappings

### 2.5 Get Mappings Between Standards
**GET** `/api/compliance-mapping/standards?sourceStandard=ISO_9001&targetStandard=ISO_27001`
- **Access**: All authenticated users
- **Description**: Retrieves mappings between specific standards
- **Response**: `200 OK` with list of mappings

### 2.6 Get Mappings by Clause ID
**GET** `/api/compliance-mapping/clause/{clauseId}`
- **Access**: All authenticated users
- **Description**: Retrieves all mappings involving a specific clause
- **Response**: `200 OK` with list of mappings

### 2.7 Get Mappings by Type
**GET** `/api/compliance-mapping/type/{mappingType}`
- **Access**: All authenticated users
- **Description**: Retrieves mappings by mapping type (EXACT_MATCH, HIGH_SIMILARITY, etc.)
- **Response**: `200 OK` with list of mappings

### 2.8 Get Verified Mappings
**GET** `/api/compliance-mapping/verified`
- **Access**: All authenticated users
- **Description**: Retrieves all verified mappings
- **Response**: `200 OK` with list of verified mappings

### 2.9 Get Pending Verification Mappings
**GET** `/api/compliance-mapping/pending-verification`
- **Access**: Compliance Officer, Admin
- **Description**: Retrieves mappings pending verification
- **Response**: `200 OK` with list of pending mappings

### 2.10 Verify Mapping
**PATCH** `/api/compliance-mapping/{mappingId}/verify`
- **Access**: Compliance Officer, Admin
- **Description**: Marks a mapping as verified
- **Response**: `200 OK` with verified mapping details

### 2.11 Delete Mapping
**DELETE** `/api/compliance-mapping/{mappingId}`
- **Access**: Admin only
- **Description**: Deletes a compliance mapping
- **Response**: `204 No Content`

### 2.12 Get Compliance Matrix
**GET** `/api/compliance-mapping/matrix`
- **Access**: All authenticated users
- **Description**: Retrieves the complete compliance matrix showing all mappings
- **Response**: `200 OK` with matrix data

### 2.13 Get Mappings by Similarity Score
**GET** `/api/compliance-mapping/similarity?minScore=0.8`
- **Access**: All authenticated users
- **Description**: Retrieves mappings with similarity score above threshold
- **Response**: `200 OK` with list of mappings

### 2.14 Get Mappings by Source Standard
**GET** `/api/compliance-mapping/source-standard/{standard}`
- **Access**: All authenticated users
- **Description**: Retrieves mappings where the specified standard is the source
- **Response**: `200 OK` with list of mappings

### 2.15 Get Mappings by Target Standard
**GET** `/api/compliance-mapping/target-standard/{standard}`
- **Access**: All authenticated users
- **Description**: Retrieves mappings where the specified standard is the target
- **Response**: `200 OK` with list of mappings

## 3. Data Models

### 3.1 Clause Library
```json
{
  "clauseId": 1,
  "clauseNumber": "4.1",
  "clauseName": "Understanding the organization and its context",
  "description": "The organization shall determine external and internal issues...",
  "standard": "ISO_9001",
  "version": "2015",
  "effectiveDate": "2015-09-15T00:00:00",
  "isActive": true,
  "category": "Context",
  "riskLevel": "MEDIUM",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### 3.2 Compliance Mapping
```json
{
  "mappingId": 1,
  "sourceClause": { /* clause object */ },
  "targetClause": { /* clause object */ },
  "mappingType": "EXACT_MATCH",
  "similarityScore": 0.95,
  "mappingNotes": "Both clauses deal with understanding organizational context",
  "isVerified": true,
  "verifiedBy": "admin@company.com",
  "verifiedAt": "2024-01-01T00:00:00",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### 3.3 Compliance Matrix
```json
{
  "standardsClauses": {
    "ISO_9001": [ /* list of ISO 9001 clauses */ ],
    "ISO_27001": [ /* list of ISO 27001 clauses */ ]
  },
  "mappings": [ /* list of all mappings */ ],
  "matrix": {
    "4.1": {
      "4.1": "EXACT_MATCH",
      "4.2": "NO_MAPPING",
      /* ... */
    }
    /* ... */
  },
  "totalMappings": 11,
  "verifiedMappings": 11,
  "pendingVerification": 0
}
```

## 4. Enums

### 4.1 ISO Standards
- `ISO_9001` - Quality Management System
- `ISO_27001` - Information Security Management System

### 4.2 Risk Levels
- `LOW` - Low risk clauses
- `MEDIUM` - Medium risk clauses
- `HIGH` - High risk clauses
- `CRITICAL` - Critical risk clauses

### 4.3 Mapping Types
- `EXACT_MATCH` - Identical requirements
- `HIGH_SIMILARITY` - Very similar requirements
- `MEDIUM_SIMILARITY` - Somewhat similar requirements
- `LOW_SIMILARITY` - Minimal similarity
- `RELATED` - Related but not directly similar
- `NO_MAPPING` - No meaningful relationship

## 5. Error Handling

### 5.1 Common Error Responses
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

### 5.2 Error Response Format
```json
{
  "timestamp": "2024-01-01T00:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Clause with number 4.1 already exists for standard ISO_9001",
  "path": "/api/clauses"
}
```

## 6. Testing Examples

### 6.1 Test Data Setup
The system comes pre-loaded with:
- 11 ISO 9001 clauses (version 2015)
- 11 ISO 27001 clauses (version 2013)
- 11 pre-configured mappings between standards

### 6.2 Sample Test Scenarios

#### Scenario 1: Create New Clause
```bash
curl -X POST http://localhost:8080/api/clauses \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clauseNumber": "4.5",
    "clauseName": "New Test Clause",
    "description": "This is a test clause for testing purposes",
    "standard": "ISO_9001",
    "category": "Testing",
    "riskLevel": "LOW"
  }'
```

#### Scenario 2: Create Compliance Mapping
```bash
curl -X POST http://localhost:8080/api/compliance-mapping \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceClauseId": 1,
    "targetClauseId": 12,
    "mappingType": "EXACT_MATCH",
    "similarityScore": 0.95,
    "mappingNotes": "Test mapping between ISO 9001 and ISO 27001"
  }'
```

#### Scenario 3: Get Compliance Matrix
```bash
curl -X GET http://localhost:8080/api/compliance-mapping/matrix \
  -H "Authorization: Bearer <jwt-token>"
```

## 7. Performance Considerations

### 7.1 Database Indexes
The system includes optimized indexes for:
- Clause searches by standard, category, and risk level
- Full-text search on clause names and descriptions
- Compliance mapping queries by various criteria

### 7.2 Caching Recommendations
- Cache frequently accessed clauses
- Cache compliance matrix for read operations
- Implement Redis for session management

## 8. Security Features

### 8.1 Role-Based Access Control
- **Admin**: Full access to all operations
- **Compliance Officer**: Can create, update, and verify mappings
- **Auditor/Reviewer**: Read-only access to clauses and mappings
- **All Users**: Basic read access to public information

### 8.2 Data Validation
- Input validation for all request parameters
- SQL injection prevention through JPA
- XSS protection through proper encoding

## 9. Integration Points

### 9.1 Audit System Integration
- Clauses can be linked to audit checklists
- Mappings help identify overlapping audit requirements
- Risk levels influence audit planning

### 9.2 Document Management Integration
- Clauses can reference related documents
- Mappings help organize document libraries
- Version control for clause updates

## 10. Monitoring and Logging

### 10.1 Audit Logs
- All CRUD operations are logged
- User actions are tracked for compliance
- Mapping verification history is maintained

### 10.2 Performance Metrics
- API response times
- Database query performance
- User activity patterns

## 11. Future Enhancements

### 11.1 Planned Features
- Support for additional ISO standards
- Automated similarity scoring using AI
- Bulk import/export functionality
- Advanced reporting and analytics

### 11.2 API Versioning
- Version 1.0: Current implementation
- Future versions will maintain backward compatibility
- Deprecation notices for removed features
