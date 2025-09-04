# Quick Start Testing Guide - Audit Planning Module

## Prerequisites
1. Java 17 or higher
2. Maven 3.6+
3. MySQL 8.0+
4. Postman (for API testing)

## Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE qms;
USE qms;

-- Run the migration script
-- Execute: V2__Add_Audit_Planning_Fields.sql
```

### 2. Application Configuration
Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/qms
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
jwt.secret=your_secret_key_here
```

### 3. Build and Run
```bash
cd Quality_Management_System
mvn clean install
mvn spring-boot:run
```

## Quick Test Steps

### Step 1: Get Authentication Token
```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Step 2: Create Test Audit
```bash
# Replace <token> with your JWT token
curl -X POST http://localhost:8080/api/audits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test ISO 9001 Audit",
    "scope": "Testing the audit planning functionality",
    "objectives": "Verify API functionality",
    "startDate": "2024-03-15",
    "endDate": "2024-03-20",
    "auditorIds": [2],
    "auditType": "INTERNAL",
    "department": "Testing",
    "location": "Test Lab",
    "notes": "Quick test audit"
  }'
```

### Step 3: Test Calendar View
```bash
curl -X GET http://localhost:8080/api/audits/calendar \
  -H "Authorization: Bearer <token>"
```

### Step 4: Test Progress Tracking
```bash
curl -X GET http://localhost:8080/api/audits/progress \
  -H "Authorization: Bearer <token>"
```

## Postman Testing

### Import Collection
1. Open Postman
2. Import the file: `Audit_Planning_Postman_Collection.json`
3. Set the `authToken` variable with your JWT token
4. Update `baseUrl` if needed (default: http://localhost:8080/api)

### Test Sequence
1. **Login** - Get authentication token
2. **Create Audit** - Test audit creation
3. **Get All Audits** - Verify audit was created
4. **Update Status** - Test status management
5. **Calendar View** - Test calendar functionality
6. **Progress Tracking** - Test progress monitoring

## Common Test Scenarios

### Scenario 1: Complete Audit Lifecycle
```bash
# 1. Create audit
POST /api/audits

# 2. Update status to IN_PROGRESS
PATCH /api/audits/{id}/status
{"status": "IN_PROGRESS"}

# 3. Update phase to EXECUTION
PATCH /api/audits/{id}/phase
{"phase": "EXECUTION"}

# 4. Complete audit
PATCH /api/audits/{id}/status
{"status": "COMPLETED"}
```

### Scenario 2: Filtering and Search
```bash
# Filter by status
GET /api/audits?status=PLANNED

# Search by term
GET /api/audits?searchTerm=ISO

# Filter overdue audits
GET /api/audits?isOverdue=true
```

### Scenario 3: Calendar and Progress
```bash
# Get calendar data
GET /api/audits/calendar

# Get progress tracking
GET /api/audits/progress

# Get upcoming audits
GET /api/audits/upcoming
```

## Expected Responses

### Successful Audit Creation
```json
{
  "id": 1,
  "title": "Test ISO 9001 Audit",
  "status": "PLANNED",
  "auditType": "INTERNAL",
  "department": "Testing",
  "location": "Test Lab",
  "auditorNames": ["Test Auditor"],
  "progressPercentage": 0.0,
  "createdAt": "2024-03-10T10:30:00"
}
```

### Calendar Response
```json
[
  {
    "id": 1,
    "title": "Test ISO 9001 Audit",
    "startDate": "2024-03-15",
    "endDate": "2024-03-20",
    "status": "PLANNED",
    "backgroundColor": "#3498db",
    "allDay": true
  }
]
```

### Progress Response
```json
[
  {
    "auditId": 1,
    "title": "Test ISO 9001 Audit",
    "status": "PLANNED",
    "progressPercentage": 0.0,
    "isOverdue": false,
    "isOnTrack": true
  }
]
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check JWT token is valid
   - Ensure token is in Authorization header

2. **400 Bad Request**
   - Check request body format
   - Verify required fields are present
   - Check date format (YYYY-MM-DD)

3. **403 Forbidden**
   - Check user role permissions
   - Ensure user has required role

4. **404 Not Found**
   - Check audit ID exists
   - Verify endpoint URL

### Validation Errors
```json
{
  "error": "End date cannot be before start date"
}
```

### Database Issues
- Ensure MySQL is running
- Check database connection
- Verify migration script was executed

## Performance Testing

### Load Testing
```bash
# Test with multiple concurrent requests
ab -n 100 -c 10 -H "Authorization: Bearer <token>" \
   http://localhost:8080/api/audits
```

### Memory Usage
Monitor application memory usage during testing:
```bash
# Check JVM memory
jstat -gc <pid>
```

## Security Testing

### Authorization Tests
1. Test with different user roles
2. Verify endpoint access controls
3. Test token expiration
4. Check input validation

### Input Validation
1. Test with invalid data
2. Test SQL injection attempts
3. Test XSS attempts
4. Verify proper error responses

## Next Steps

After successful testing:
1. Review the complete API documentation
2. Test all endpoints systematically
3. Verify business logic
4. Check performance under load
5. Validate security measures

## Support

For issues or questions:
1. Check the comprehensive API guide
2. Review error logs
3. Verify configuration
4. Test with minimal data first
