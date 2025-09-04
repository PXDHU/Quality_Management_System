# 400 Bad Request Error - Troubleshooting Guide

## Current Status
✅ **Good News**: The 404 error is fixed! The endpoint is now being found.
❌ **Current Issue**: 400 Bad Request - There's an issue with the request parameters or data.

## Step-by-Step Debugging Process

### Step 1: Test the Health Check
**GET** `http://localhost:8080/api/documents/health`
- Should return: `"Document Controller is working!"`

### Step 2: Test Simple Upload (No Authentication)
**POST** `http://localhost:8080/api/documents/test-upload`

**Headers:**
```
Content-Type: multipart/form-data
```

**Body (form-data):**
- `file`: [Select any small file]
- `description`: "Test upload"

**Expected Response:**
```
"Test upload successful! File: filename.pdf"
```

### Step 3: Check Application Logs
Look for debug messages in your application console:
```
=== Test Upload Debug ===
File name: your_file.pdf
File size: 12345
Description: Test upload
```

### Step 4: Test Full Upload with Authentication
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
- `auditIds`: "1"

## Common 400 Bad Request Causes

### 1. File Issues
- **File is null or empty**
- **File size exceeds 10MB limit**
- **Invalid file format**

**Solution:**
- Use a small PDF file (< 1MB) for testing
- Check file is properly selected in Postman

### 2. Parameter Issues
- **Missing required parameters**
- **Invalid parameter format**
- **Wrong parameter names**

**Solution:**
- Ensure parameter names match exactly: `file`, `description`, `auditIds`
- Use string format for `auditIds`: "1" not [1]

### 3. Authentication Issues
- **Invalid JWT token**
- **Missing X-User-Id header**
- **User doesn't exist in database**

**Solution:**
- Generate a new JWT token
- Ensure user with ID 1 exists in database

### 4. Database Issues
- **Audit with ID 1 doesn't exist**
- **Database connection issues**

**Solution:**
- Check if audit with ID 1 exists in database
- Verify database connection

## Debugging Checklist

### Before Testing:
- [ ] Application is running without errors
- [ ] Database is connected
- [ ] User with ID 1 exists in database
- [ ] Audit with ID 1 exists in database
- [ ] JWT token is valid and not expired

### During Testing:
- [ ] Use a small test file (< 1MB)
- [ ] Check all parameter names are correct
- [ ] Verify Content-Type is multipart/form-data
- [ ] Ensure Authorization header is present
- [ ] Check X-User-Id header is present

### After Testing:
- [ ] Check application console for debug messages
- [ ] Look for specific error messages
- [ ] Verify database tables exist

## Quick Fixes to Try

### Fix 1: Test Without Authentication
Use the `/test-upload` endpoint first to verify file upload works.

### Fix 2: Test with Minimal Parameters
Try uploading with just the file parameter:
```
file: [your_file]
```

### Fix 3: Check Database
Verify these exist in your database:
```sql
SELECT * FROM auth_user WHERE user_id = 1;
SELECT * FROM audit WHERE audit_id = 1;
```

### Fix 4: Test with Different File
Try with a simple text file instead of PDF.

### Fix 5: Check JWT Token
Decode your JWT token to verify it contains:
- Valid user information
- Correct role
- Not expired

## Expected Debug Output

When successful, you should see:
```
=== Document Upload Debug ===
File name: test.pdf
File size: 12345
Description: Audit evidence document
AuditIds string: 1
UserId: 1
Parsed auditIds: [1]
SUCCESS: Document uploaded with ID: 1
```

## Next Steps

1. **Run the test upload endpoint** first
2. **Check application logs** for specific error messages
3. **Verify database data** exists
4. **Test with minimal parameters**
5. **Report the specific error message** from the logs

## If Still Getting 400 Error

Please provide:
1. The exact error message from application logs
2. The response body from Postman
3. The debug output from console
4. Database query results for user and audit tables
