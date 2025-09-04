# Audit Planning Module - API Testing Guide

## Overview
This document provides comprehensive testing information for the Audit Planning module APIs. All endpoints require authentication via JWT token in the Authorization header: `Authorization: Bearer <token>`

## Base URL
```
http://localhost:8080/api/audits
```

## Authentication
All requests must include the JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## API Endpoints

### 1. Create Audit Plan
**POST** `/api/audits`

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "title": "ISO 9001:2015 Internal Audit - Q1 2024",
  "scope": "Quality Management System audit covering all departments and processes",
  "objectives": "Verify compliance with ISO 9001:2015 requirements and identify improvement opportunities",
  "startDate": "2024-03-15",
  "endDate": "2024-03-20",
  "auditorIds": [2, 3],
  "auditType": "INTERNAL",
  "department": "Quality Assurance",
  "location": "Main Office",
  "notes": "Focus on document control and process improvements"
}
```

**Response:**
```json
{
  "id": 1,
  "title": "ISO 9001:2015 Internal Audit - Q1 2024",
  "scope": "Quality Management System audit covering all departments and processes",
  "objectives": "Verify compliance with ISO 9001:2015 requirements and identify improvement opportunities",
  "startDate": "2024-03-15",
  "endDate": "2024-03-20",
  "status": "PLANNED",
  "auditType": "INTERNAL",
  "department": "Quality Assurance",
  "location": "Main Office",
  "notes": "Focus on document control and process improvements",
  "createdBy": "admin",
  "createdByName": "System Administrator",
  "auditorNames": ["John Doe", "Jane Smith"],
  "auditorIds": [2, 3],
  "totalChecklists": 0,
  "completedChecklists": 0,
  "progressPercentage": 0.0,
  "createdAt": "2024-03-10T10:30:00",
  "updatedAt": "2024-03-10T10:30:00"
}
```

### 2. Get All Audits (with filters)
**GET** `/api/audits`

**Authorization:** ADMIN, AUDITOR

**Query Parameters:**
- `status` (optional): PLANNED, IN_PROGRESS, COMPLETED
- `auditType` (optional): INTERNAL, EXTERNAL, SUPPLIER
- `department` (optional): Department name
- `location` (optional): Location name
- `searchTerm` (optional): Search in title, scope, objectives
- `isOverdue` (optional): true/false

**Example Requests:**
```
GET /api/audits
GET /api/audits?status=PLANNED
GET /api/audits?auditType=INTERNAL&department=Quality Assurance
GET /api/audits?searchTerm=ISO 9001
GET /api/audits?isOverdue=true
```

### 3. Get Specific Audit
**GET** `/api/audits/{id}`

**Authorization:** ADMIN, AUDITOR

**Example:**
```
GET /api/audits/1
```

### 4. Update Audit
**PUT** `/api/audits/{id}`

**Authorization:** ADMIN, AUDITOR

**Request Body:**
```json
{
  "title": "Updated ISO 9001:2015 Internal Audit - Q1 2024",
  "scope": "Updated scope description",
  "objectives": "Updated objectives",
  "startDate": "2024-03-16",
  "endDate": "2024-03-21",
  "auditorIds": [2, 3, 4],
  "auditType": "INTERNAL",
  "department": "Quality Assurance",
  "location": "Main Office",
  "notes": "Updated notes"
}
```

### 5. Update Audit Status
**PATCH** `/api/audits/{id}/status`

**Authorization:** AUDITOR

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Available Statuses:** PLANNED, IN_PROGRESS, COMPLETED

### 6. Update Audit Phase
**PATCH** `/api/audits/{id}/phase`

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "phase": "EXECUTION"
}
```

**Available Phases:** PLANNING, EXECUTION, REPORTING, FOLLOW_UP

### 7. Get Audit Calendar View
**GET** `/api/audits/calendar`

**Authorization:** ADMIN, AUDITOR

**Response:**
```json
[
  {
    "id": 1,
    "title": "ISO 9001:2015 Internal Audit - Q1 2024",
    "scope": "Quality Management System audit",
    "startDate": "2024-03-15",
    "endDate": "2024-03-20",
    "status": "PLANNED",
    "auditType": "INTERNAL",
    "department": "Quality Assurance",
    "location": "Main Office",
    "auditorNames": ["John Doe", "Jane Smith"],
    "createdByName": "System Administrator",
    "backgroundColor": "#3498db",
    "borderColor": "#2980b9",
    "allDay": true,
    "url": "/audits/1"
  }
]
```

### 8. Get Audit Progress Tracking
**GET** `/api/audits/progress`

**Authorization:** ADMIN, AUDITOR

**Response:**
```json
[
  {
    "auditId": 1,
    "title": "ISO 9001:2015 Internal Audit - Q1 2024",
    "status": "IN_PROGRESS",
    "startDate": "2024-03-15",
    "endDate": "2024-03-20",
    "totalChecklists": 10,
    "completedChecklists": 5,
    "pendingChecklists": 5,
    "progressPercentage": 50.0,
    "currentPhase": "EXECUTION",
    "lastActivity": "2024-03-12T14:30:00",
    "lastActivityBy": "John Doe",
    "isOverdue": false,
    "isOnTrack": true,
    "estimatedCompletion": "2024-03-18"
  }
]
```

### 9. Get Audits by Status
**GET** `/api/audits/status/{status}`

**Authorization:** ADMIN, AUDITOR

**Example:**
```
GET /api/audits/status/PLANNED
GET /api/audits/status/IN_PROGRESS
GET /api/audits/status/COMPLETED
```

### 10. Get Overdue Audits
**GET** `/api/audits/overdue`

**Authorization:** ADMIN, AUDITOR

**Response:** List of audits that are past their end date but not completed

### 11. Get Upcoming Audits
**GET** `/api/audits/upcoming`

**Authorization:** ADMIN, AUDITOR

**Response:** List of audits starting within the next 7 days

### 12. Delete Audit
**DELETE** `/api/audits/{id}`

**Authorization:** ADMIN

**Response:**
```json
{
  "message": "Audit deleted successfully"
}
```

## Testing Scenarios

### Scenario 1: Create and Manage Audit Plan
1. **Create Audit Plan**
   ```bash
   curl -X POST http://localhost:8080/api/audits \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "ISO 9001:2015 Internal Audit - Q1 2024",
       "scope": "Quality Management System audit covering all departments",
       "objectives": "Verify compliance with ISO 9001:2015 requirements",
       "startDate": "2024-03-15",
       "endDate": "2024-03-20",
       "auditorIds": [2, 3],
       "auditType": "INTERNAL",
       "department": "Quality Assurance",
       "location": "Main Office",
       "notes": "Focus on document control"
     }'
   ```

2. **Update Audit Status**
   ```bash
   curl -X PATCH http://localhost:8080/api/audits/1/status \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"status": "IN_PROGRESS"}'
   ```

3. **Update Audit Phase**
   ```bash
   curl -X PATCH http://localhost:8080/api/audits/1/phase \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"phase": "EXECUTION"}'
   ```

### Scenario 2: Calendar View and Progress Tracking
1. **Get Calendar View**
   ```bash
   curl -X GET http://localhost:8080/api/audits/calendar \
     -H "Authorization: Bearer <token>"
   ```

2. **Get Progress Tracking**
   ```bash
   curl -X GET http://localhost:8080/api/audits/progress \
     -H "Authorization: Bearer <token>"
   ```

### Scenario 3: Filtering and Search
1. **Filter by Status**
   ```bash
   curl -X GET "http://localhost:8080/api/audits?status=PLANNED" \
     -H "Authorization: Bearer <token>"
   ```

2. **Search by Term**
   ```bash
   curl -X GET "http://localhost:8080/api/audits?searchTerm=ISO 9001" \
     -H "Authorization: Bearer <token>"
   ```

3. **Filter Overdue Audits**
   ```bash
   curl -X GET "http://localhost:8080/api/audits?isOverdue=true" \
     -H "Authorization: Bearer <token>"
   ```

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": "End date cannot be before start date"
}
```

**401 Unauthorized:**
```json
{
  "error": "Access denied"
}
```

**403 Forbidden:**
```json
{
  "error": "Insufficient permissions"
}
```

**404 Not Found:**
```json
{
  "error": "Audit not found"
}
```

## Validation Rules

### Audit Request Validation:
- **title**: Required, 3-200 characters
- **scope**: Required, 10-1000 characters
- **objectives**: Optional, max 1000 characters
- **startDate**: Required, must be today or future
- **endDate**: Required, must be after startDate
- **auditorIds**: Required, at least one auditor
- **auditType**: Optional
- **department**: Optional
- **location**: Optional
- **notes**: Optional

### Status Transitions:
- PLANNED → IN_PROGRESS → COMPLETED
- Cannot move backwards in status

## Notes for Testing

1. **Authentication**: Ensure you have a valid JWT token for all requests
2. **User Roles**: Different endpoints require different user roles
3. **Date Format**: Use ISO date format (YYYY-MM-DD)
4. **Auditor IDs**: Must be valid user IDs with AUDITOR role
5. **Status Updates**: Only AUDITORs can update audit status
6. **Calendar Colors**: Different statuses have different colors for visualization

## Production Considerations

1. **Security**: All endpoints are protected with role-based access control
2. **Validation**: Comprehensive input validation on all endpoints
3. **Error Handling**: Proper error responses with meaningful messages
4. **Performance**: Efficient database queries with proper indexing
5. **Audit Trail**: All changes are tracked with timestamps and user information
