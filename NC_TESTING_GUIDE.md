# Non-Conformity Management - API Testing Guide

## Overview
This guide provides step-by-step instructions for testing the Non-Conformity Management APIs using the provided Postman collection.

## Prerequisites

### 1. Environment Setup
- Ensure the Spring Boot application is running on `http://localhost:8080`
- Have a valid JWT token for authentication
- Import the `NC_Management_Postman_Collection.json` into Postman

### 2. Test Data Requirements
Before testing, ensure you have:
- At least one audit in the system
- At least two users (one auditor, one auditee)
- Valid user IDs for testing

## Testing Sequence

### Step 1: Authentication
1. **Login** to get a JWT token
2. **Set the token** in the Postman collection variable `{{jwt_token}}`

### Step 2: Create Test Data

#### Create a Non-Conformity
**Request:** `POST /api/nc`

**Body:**
```json
{
  "auditId": 1,
  "title": "Training Records Non-Conformity",
  "description": "Employee training records not maintained as per procedure",
  "severity": "HIGH",
  "assignedToId": 2,
  "createdById": 1
}
```

**Expected Response:** 201 Created with NC details

### Step 3: Test NC Retrieval

#### Get NC by ID
**Request:** `GET /api/nc/1`

**Expected Response:** 200 OK with NC details

#### Get All NCs
**Request:** `GET /api/nc`

**Expected Response:** 200 OK with list of NCs

#### Get NCs with Filtering
**Request:** `GET /api/nc?status=PENDING&severity=HIGH`

**Expected Response:** 200 OK with filtered NCs

### Step 4: Test Corrective Actions

#### Add Corrective Action
**Request:** `POST /api/nc/1/actions`

**Body:**
```json
{
  "description": "Implement training record management system",
  "responsibleId": 2,
  "dueDate": "2024-04-30"
}
```

**Expected Response:** 200 OK with updated NC (status should change to IN_PROGRESS)

#### Update Action Status
**Request:** `PATCH /api/nc/actions/1/status`

**Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Expected Response:** 200 OK with updated NC

### Step 5: Test Root Cause Analysis

#### Submit RCA (Required for HIGH severity)
**Request:** `POST /api/nc/1/rca`

**Body:**
```json
{
  "steps": [
    {
      "stepNumber": 1,
      "whyText": "Why were training records not maintained?"
    },
    {
      "stepNumber": 2,
      "whyText": "Why was the training procedure not followed?"
    },
    {
      "stepNumber": 3,
      "whyText": "Why was there no oversight of training compliance?"
    }
  ]
}
```

**Expected Response:** 200 OK with RCA steps added

### Step 6: Test NC Closure

#### Complete All Actions First
**Request:** `PATCH /api/nc/actions/1/status`

**Body:**
```json
{
  "status": "COMPLETED"
}
```

#### Close NC
**Request:** `POST /api/nc/1/close`

**Body:**
```json
{
  "finalEvidenceIds": ["evidence1", "evidence2"],
  "reviewerComment": "All corrective actions completed satisfactorily"
}
```

**Expected Response:** 200 OK with COMPLETED status

## Test Scenarios

### Scenario 1: Complete NC Lifecycle
1. Create NC with HIGH severity
2. Add corrective action
3. Submit RCA
4. Complete action
5. Close NC

### Scenario 2: Validation Testing
1. Try to create NC without required fields
2. Try to submit RCA with less than 3 steps
3. Try to close NC without completed actions
4. Try to close HIGH severity NC without RCA

### Scenario 3: Authorization Testing
1. Test with different user roles
2. Verify access restrictions
3. Test unauthorized operations

### Scenario 4: Filtering and Search
1. Test all filter combinations
2. Test overdue NC detection
3. Test assigned NC retrieval

## Expected Error Responses

### Validation Errors
```json
{
  "error": "Title is required"
}
```

### Business Rule Violations
```json
{
  "error": "RCA with at least 3 steps is required for HIGH severity NCs before closing."
}
```

### Authorization Errors
```json
{
  "error": "Insufficient permissions"
}
```

## Performance Testing

### Load Testing
1. Create multiple NCs
2. Test filtering with large datasets
3. Monitor response times

### Concurrent Testing
1. Test simultaneous NC updates
2. Test concurrent action status updates
3. Verify data consistency

## Security Testing

### Authentication
1. Test without JWT token
2. Test with invalid token
3. Test with expired token

### Authorization
1. Test role-based access
2. Test user isolation
3. Test privilege escalation attempts

## Database Testing

### Data Integrity
1. Verify foreign key constraints
2. Test cascade operations
3. Verify audit trail

### Transaction Testing
1. Test rollback scenarios
2. Test concurrent modifications
3. Verify ACID properties

## API Response Validation

### Response Structure
Verify all responses include:
- Correct HTTP status codes
- Proper JSON structure
- Required fields
- Timestamps

### Data Consistency
Verify:
- IDs are properly assigned
- Relationships are maintained
- Status transitions are correct
- Audit trail is complete

## Troubleshooting

### Common Issues

#### 1. 401 Unauthorized
- Check JWT token validity
- Verify token expiration
- Ensure proper Authorization header

#### 2. 400 Bad Request
- Check request body format
- Verify required fields
- Check data types

#### 3. 403 Forbidden
- Verify user role permissions
- Check endpoint authorization
- Ensure proper user context

#### 4. 404 Not Found
- Verify resource IDs exist
- Check URL path
- Ensure proper relationships

#### 5. 500 Internal Server Error
- Check application logs
- Verify database connectivity
- Check for constraint violations

### Debug Steps
1. Check application logs
2. Verify database state
3. Test with minimal data
4. Use Postman console for debugging
5. Verify environment variables

## Best Practices

### Testing Approach
1. Test happy path scenarios first
2. Test edge cases and error conditions
3. Test security and authorization
4. Test performance under load
5. Document test results

### Data Management
1. Use test data that's easy to identify
2. Clean up test data after testing
3. Use consistent naming conventions
4. Document test data requirements

### Automation
1. Consider automated API testing
2. Use CI/CD integration
3. Implement regression testing
4. Monitor API performance

## Conclusion

This testing guide provides comprehensive coverage of the Non-Conformity Management APIs. Follow the testing sequence and scenarios to ensure all functionality works correctly before deployment.

Remember to:
- Test all endpoints thoroughly
- Verify business rules
- Test security measures
- Document any issues found
- Validate performance requirements
