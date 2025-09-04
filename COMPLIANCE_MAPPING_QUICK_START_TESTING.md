# Compliance Mapping & Clause Management - Quick Start Testing Guide

## Overview
This guide provides a quick way to test the Compliance Mapping & Clause Management module APIs. The system comes pre-loaded with sample data including ISO 9001 and ISO 27001 clauses with pre-configured mappings.

## Prerequisites
1. **Database**: PostgreSQL running on localhost:5432
2. **Application**: Spring Boot application running on localhost:8080
3. **Authentication**: Valid JWT token (you'll need to login first)

## Quick Test Setup

### 1. Start the Application
```bash
cd Quality_Management_System
mvn spring-boot:run
```

### 2. Get JWT Token
First, you need to authenticate to get a JWT token. Use the existing auth endpoints:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@company.com",
    "password": "admin123"
  }'
```

Copy the JWT token from the response and set it as an environment variable:
```bash
export JWT_TOKEN="your-jwt-token-here"
```

## Quick Test Scenarios

### Scenario 1: View Pre-loaded Data (No Authentication Required for Read)

#### 1.1 View All Clauses
```bash
curl -X GET http://localhost:8080/api/clauses \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return 22 clauses (11 ISO 9001 + 11 ISO 27001)

#### 1.2 View ISO 9001 Clauses
```bash
curl -X GET http://localhost:8080/api/clauses/standard/ISO_9001 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return 11 ISO 9001 clauses

#### 1.3 View ISO 27001 Clauses
```bash
curl -X GET http://localhost:8080/api/clauses/standard/ISO_27001 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return 11 ISO 27001 clauses

#### 1.4 View Compliance Matrix
```bash
curl -X GET http://localhost:8080/api/compliance-mapping/matrix \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return a matrix showing 11 pre-configured mappings between ISO 9001 and ISO 27001

### Scenario 2: Test Clause Management (Admin Required)

#### 2.1 Create New Clause
```bash
curl -X POST http://localhost:8080/api/clauses \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clauseNumber": "4.6",
    "clauseName": "Test Clause for Quick Start",
    "description": "This is a test clause created during quick start testing",
    "standard": "ISO_9001",
    "category": "Testing",
    "riskLevel": "LOW"
  }'
```

**Expected**: `201 Created` with the new clause details

#### 2.2 Search for the New Clause
```bash
curl -X GET "http://localhost:8080/api/clauses/search?keyword=Quick%20Start" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return the newly created clause

#### 2.3 Update the Clause
```bash
curl -X PUT http://localhost:8080/api/clauses/23 \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clauseNumber": "4.6",
    "clauseName": "Updated Test Clause for Quick Start",
    "description": "This clause has been updated during quick start testing",
    "standard": "ISO_9001",
    "category": "Testing",
    "riskLevel": "MEDIUM"
  }'
```

**Expected**: `200 OK` with updated clause details

### Scenario 3: Test Compliance Mapping (Compliance Officer/Admin Required)

#### 3.1 Create New Mapping
```bash
curl -X POST http://localhost:8080/api/compliance-mapping \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceClauseId": 23,
    "targetClauseId": 12,
    "mappingType": "HIGH_SIMILARITY",
    "similarityScore": 0.85,
    "mappingNotes": "Test mapping created during quick start testing"
  }'
```

**Expected**: `201 Created` with the new mapping details

#### 3.2 View All Mappings
```bash
curl -X GET http://localhost:8080/api/compliance-mapping \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return 12 mappings (11 pre-configured + 1 new)

#### 3.3 View Mappings by Type
```bash
curl -X GET http://localhost:8080/api/compliance-mapping/type/HIGH_SIMILARITY \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return mappings with HIGH_SIMILARITY type

#### 3.4 Verify the New Mapping
```bash
curl -X PATCH http://localhost:8080/api/compliance-mapping/12/verify \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: `200 OK` with verified mapping details

### Scenario 4: Test Search and Filter Operations

#### 4.1 Search by Keyword
```bash
curl -X GET "http://localhost:8080/api/clauses/search?keyword=management" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return clauses containing "management" in name or description

#### 4.2 Filter by Risk Level
```bash
curl -X GET http://localhost:8080/api/clauses/standard/ISO_9001/risk/HIGH \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return ISO 9001 clauses with HIGH risk level

#### 4.3 Filter by Category
```bash
curl -X GET http://localhost:8080/api/clauses/standard/ISO_9001/category/Context \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return ISO 9001 clauses in the Context category

#### 4.4 Get Mappings by Similarity Score
```bash
curl -X GET "http://localhost:8080/api/compliance-mapping/similarity?minScore=0.9" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected**: Should return mappings with similarity score >= 0.9

## Validation Checklist

### ✅ Pre-loaded Data
- [ ] 22 clauses loaded (11 ISO 9001 + 11 ISO 27001)
- [ ] 11 pre-configured mappings between standards
- [ ] Sample clauses cover major ISO sections (4-10)

### ✅ Clause Management
- [ ] Create new clause
- [ ] Update existing clause
- [ ] Search by keyword
- [ ] Filter by standard, category, risk level
- [ ] Activate/deactivate clauses

### ✅ Compliance Mapping
- [ ] Create new mapping
- [ ] View all mappings
- [ ] Filter by mapping type
- [ ] Verify mappings
- [ ] View compliance matrix

### ✅ Search and Filter
- [ ] Full-text search
- [ ] Filter by multiple criteria
- [ ] Similarity score filtering
- [ ] Standard-specific queries

## Common Issues and Solutions

### Issue 1: Authentication Required
**Problem**: Getting 401 Unauthorized errors
**Solution**: Ensure you have a valid JWT token and include it in the Authorization header

### Issue 2: Database Connection
**Problem**: Getting database connection errors
**Solution**: 
1. Check PostgreSQL is running on localhost:5432
2. Verify database credentials in application.properties
3. Ensure database 'compliance_portal' exists

### Issue 3: Flyway Migration
**Problem**: Database tables not created
**Solution**: 
1. Check Flyway is enabled in application.properties
2. Verify migration scripts are in src/main/resources/db/migration
3. Check application logs for migration errors

### Issue 4: Role-based Access
**Problem**: Getting 403 Forbidden errors
**Solution**: Ensure your user has the required role (ADMIN for clause management, COMPLIANCE_OFFICER for mapping management)

## Performance Testing

### Load Testing
```bash
# Test clause search performance
for i in {1..100}; do
  curl -X GET "http://localhost:8080/api/clauses/search?keyword=management" \
    -H "Authorization: Bearer $JWT_TOKEN" &
done
wait
```

### Database Query Performance
Check application logs for SQL query execution times. The system includes optimized indexes for:
- Clause searches by standard, category, and risk level
- Full-text search on clause names and descriptions
- Compliance mapping queries by various criteria

## Next Steps

After completing the quick start testing:

1. **Explore the API Documentation**: Review the complete API guide for all available endpoints
2. **Test Edge Cases**: Try invalid data, boundary conditions, and error scenarios
3. **Integration Testing**: Test how the module integrates with other QMS modules
4. **Performance Testing**: Load test with larger datasets
5. **Security Testing**: Verify role-based access control and input validation

## Support

If you encounter issues during testing:
1. Check the application logs for detailed error messages
2. Verify database connectivity and schema
3. Ensure all required dependencies are available
4. Review the API documentation for correct usage patterns
