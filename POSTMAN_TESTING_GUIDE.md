# Postman Testing Guide for Document Management API

## Prerequisites
1. Make sure your Spring Boot application is running on `http://localhost:8080`
2. Ensure PostgreSQL database is running and connected
3. Have a valid JWT token for authentication

## Step-by-Step Testing Process

### Step 1: Verify Application is Running
**GET** `http://localhost:8080/actuator/health`
- This should return application health status
- If this fails, your application is not running

### Step 2: Test Basic Authentication
**GET** `http://localhost:8080/api/auth/login`
- This should be accessible without authentication
- If this returns 404, there's a routing issue

### Step 3: Test Document Controller Health Check
**GET** `http://localhost:8080/api/documents/health`
- This should return: `"Document Controller is working!"`
- If this returns 404, the controller is not being registered

### Step 4: Test Authentication with Existing Endpoint
**GET** `http://localhost:8080/api/audits`
- Headers:
  - `Authorization: Bearer <your_jwt_token>`
  - `X-User-Id: 1`
- This should work if authentication is working

### Step 5: Test Document Upload (Fixed Version)

**POST** `http://localhost:8080/api/documents`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
X-User-Id: 1
Content-Type: multipart/form-data
```

**Body (form-data):**
- `file`: [Select your PDF file]
- `description`: "Audit evidence document"
- `auditIds`: "1" (as string, not array)

**Expected Response:**
```json
{
  "documentId": 1,
  "fileName": "your_file.pdf",
  "fileType": "application/pdf",
  "description": "Audit evidence document",
  "uploadedBy": "User Name",
  "uploadedAt": "2024-01-15T10:30:00",
  "auditIds": [1],
  "auditTitles": ["Audit Title"]
}
```

## Troubleshooting 404 Errors

### 1. Check Application Startup
Look for these logs when starting the application:
```
Started QualityManagementSystemApplication in X.XXX seconds
Mapped "{[/api/documents],methods=[POST],consumes=[multipart/form-data]}" onto public ResponseEntity<DocumentResponse>
```

### 2. Check for Compilation Errors
Run: `mvn clean compile`
Look for any compilation errors in the console.

### 3. Check Database Connection
Ensure PostgreSQL is running and the connection details in `application.properties` are correct.

### 4. Check JWT Token
- Make sure your JWT token is valid and not expired
- Verify the token contains the correct role information

### 5. Test Without Authentication First
Temporarily remove `@PreAuthorize("isAuthenticated()")` from the controller to test if it's an authentication issue.

## Common Issues and Solutions

### Issue 1: 404 Not Found
**Possible Causes:**
- Application not running
- Controller not being registered
- Wrong URL path
- Missing dependencies

**Solutions:**
1. Check if application is running on port 8080
2. Verify controller has `@RestController` and `@RequestMapping` annotations
3. Check for compilation errors
4. Ensure all required dependencies are in `pom.xml`

### Issue 2: 401 Unauthorized
**Possible Causes:**
- Invalid JWT token
- Missing Authorization header
- Token expired

**Solutions:**
1. Generate a new JWT token
2. Check token format: `Bearer <token>`
3. Verify token contains valid user information

### Issue 3: 403 Forbidden
**Possible Causes:**
- User doesn't have required role
- JWT token doesn't contain role information

**Solutions:**
1. Check user role in database
2. Verify JWT token contains role claim
3. Check `@PreAuthorize` annotations

### Issue 4: 500 Internal Server Error
**Possible Causes:**
- Database connection issues
- Missing required data
- Service layer exceptions

**Solutions:**
1. Check database connection
2. Verify required audit IDs exist in database
3. Check application logs for detailed error

## Testing Checklist

- [ ] Application starts without errors
- [ ] Database connection is established
- [ ] JWT token is valid and not expired
- [ ] User exists in database with correct role
- [ ] Audit with ID 1 exists in database
- [ ] File upload size is under 10MB
- [ ] Content-Type is set to multipart/form-data
- [ ] All required headers are present

## Debug Steps

1. **Check Application Logs:**
   Look for startup logs and any error messages

2. **Test Health Endpoint:**
   `GET http://localhost:8080/api/documents/health`

3. **Test Authentication:**
   Use an existing working endpoint like `/api/audits`

4. **Check Database:**
   Verify tables exist and contain data

5. **Test File Upload:**
   Start with a simple text file, then try PDF

## Expected Behavior

When everything is working correctly:
1. Health check should return success message
2. Authentication should work with valid JWT
3. File upload should return 201 Created with document details
4. Document should be stored in database
5. Document should be linked to specified audit

## Next Steps After Successful Upload

1. **Test Document Retrieval:**
   `GET http://localhost:8080/api/documents`

2. **Test Document Download:**
   `GET http://localhost:8080/api/documents/1/download`

3. **Test Audit-Specific Documents:**
   `GET http://localhost:8080/api/documents/audit/1`

4. **Test User-Specific Documents:**
   `GET http://localhost:8080/api/documents/user/1`
