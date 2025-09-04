# Non-Conformity (NC) & Corrective Action Management - API Guide

## Overview
The Non-Conformity Management module provides comprehensive functionality for identifying, tracking, and resolving non-conformities found during audits. This system supports the complete lifecycle of NC management from identification to closure.

## Key Features

### 1. NC Identification
- Auditors can flag any clause as non-compliant during audits
- Support for severity levels (Low, Medium, High)
- Detailed description and evidence tracking

### 2. NC Dashboard
- Auditees can view all assigned NCs
- Status-based categorization (Pending, In Progress, Completed)
- Filtering by severity, status, and audit

### 3. Corrective Action Management
- Create corrective action plans with descriptions
- Assign responsible persons and due dates
- Track action completion status

### 4. Root Cause Analysis (RCA)
- Structured 3-5 step Why-Why analysis for major NCs
- Required for HIGH severity NCs before closure
- Sequential step validation

### 5. NC Closure
- Reviewer validation of corrective actions
- Evidence upload for closure
- Business rule enforcement

## API Endpoints

### Core NC Management

#### 1. Create Non-Conformity
**POST** `/api/nc`

Creates a new non-conformity during an audit.

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "auditId": 1,
  "instanceId": 5,
  "clauseId": 3,
  "title": "Document Control Non-Conformity",
  "description": "Quality manual not updated with latest procedures",
  "severity": "MEDIUM",
  "assignedToId": 2,
  "createdById": 1
}
```

**Response:**
```json
{
  "nonConformityId": 1,
  "auditId": 1,
  "instanceId": 5,
  "clauseId": 3,
  "title": "Document Control Non-Conformity",
  "description": "Quality manual not updated with latest procedures",
  "severity": "MEDIUM",
  "status": "PENDING",
  "createdById": 1,
  "assignedToId": 2,
  "evidenceIds": [],
  "actions": [],
  "rcaSteps": [],
  "createdAt": "2024-03-15T10:30:00",
  "updatedAt": "2024-03-15T10:30:00"
}
```

#### 2. Get NC by ID
**GET** `/api/nc/{ncId}`

Retrieves a specific non-conformity by ID.

**Authorization:** AUDITOR, ADMIN, REVIEWER

**Response:** Same as create response

#### 3. Get All NCs with Filtering
**GET** `/api/nc?status=PENDING&severity=HIGH&auditId=1`

Retrieves all non-conformities with optional filtering.

**Authorization:** AUDITOR, ADMIN, REVIEWER

**Query Parameters:**
- `status` (optional): PENDING, IN_PROGRESS, COMPLETED
- `severity` (optional): LOW, MEDIUM, HIGH
- `auditId` (optional): Filter by specific audit

#### 4. Get NCs by Audit
**GET** `/api/nc/audit/{auditId}`

Retrieves all non-conformities for a specific audit.

**Authorization:** AUDITOR, ADMIN, REVIEWER

#### 5. Get NCs by Severity
**GET** `/api/nc/severity/{severity}`

Retrieves all non-conformities by severity level.

**Authorization:** AUDITOR, ADMIN, REVIEWER

#### 6. Get Overdue NCs
**GET** `/api/nc/overdue`

Retrieves all non-conformities with overdue corrective actions.

**Authorization:** AUDITOR, ADMIN, REVIEWER

### NC Dashboard for Auditees

#### 7. Get Assigned NCs
**GET** `/api/nc/assignee/{userId}?status=PENDING`

Retrieves all non-conformities assigned to a specific user.

**Authorization:** Authenticated users (can only access their own NCs)

**Query Parameters:**
- `status` (optional): Filter by status

### Corrective Action Management

#### 8. Add Corrective Action
**POST** `/api/nc/{ncId}/actions`

Adds a corrective action to a non-conformity.

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "description": "Update quality manual with latest procedures",
  "responsibleId": 2,
  "dueDate": "2024-04-15"
}
```

**Response:** Updated NC with new action

#### 9. Update Action Status
**PATCH** `/api/nc/actions/{actionId}/status`

Updates the status of a corrective action.

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Response:** Updated NC with modified action

### Root Cause Analysis (RCA)

#### 10. Submit RCA
**POST** `/api/nc/{ncId}/rca`

Submits a Root Cause Analysis with 3-5 steps.

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "steps": [
    {
      "stepNumber": 1,
      "whyText": "Why was the quality manual not updated?"
    },
    {
      "stepNumber": 2,
      "whyText": "Why was the document control process not followed?"
    },
    {
      "stepNumber": 3,
      "whyText": "Why was there no training on document control procedures?"
    }
  ]
}
```

**Response:** Updated NC with RCA steps

### NC Closure

#### 11. Close NC
**POST** `/api/nc/{ncId}/close`

Closes a non-conformity after validation.

**Authorization:** REVIEWER, ADMIN

**Request Body:**
```json
{
  "finalEvidenceIds": ["evidence1", "evidence2"],
  "reviewerComment": "All corrective actions completed satisfactorily"
}
```

**Response:** Updated NC with COMPLETED status

### Status Management

#### 12. Update NC Status
**PATCH** `/api/nc/{ncId}/status`

Updates the status of a non-conformity.

**Authorization:** AUDITOR, ADMIN

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

## Data Models

### NonConformity
```java
public class NonConformity {
    private Long nonConformityId;
    private Audit audit;
    private Instance instance;
    private Clause_library clause;
    private String title;
    private String description;
    private Severity severity;
    private Status status;
    private AuthUser createdBy;
    private AuthUser assignedTo;
    private List<String> evidenceIds;
    private List<CorrectiveAction> actions;
    private List<RCAStep> rcaSteps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CorrectiveAction
```java
public class CorrectiveAction {
    private Long actionId;
    private NonConformity nc;
    private String description;
    private AuthUser responsible;
    private LocalDate dueDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### RCAStep
```java
public class RCAStep {
    private Long rcaStepId;
    private NonConformity nc;
    private int stepNumber;
    private String whyText;
}
```

## Business Rules

### NC Creation
- Audit ID is required
- Title and description are required
- Severity must be specified (LOW, MEDIUM, HIGH)
- Assignee is required
- Initial status is PENDING

### Corrective Actions
- Description is required
- Responsible person is required
- Due date is optional
- Adding an action changes NC status to IN_PROGRESS

### Root Cause Analysis
- 3-5 steps are required
- Steps must be sequential (1, 2, 3, etc.)
- Each step must have why text
- Required for HIGH severity NCs before closure

### NC Closure
- All corrective actions must be completed
- RCA required for HIGH severity NCs (minimum 3 steps)
- Cannot close already completed NCs
- Final evidence can be uploaded

### Status Transitions
- PENDING → IN_PROGRESS (when action added)
- IN_PROGRESS → COMPLETED (when closed)
- COMPLETED status cannot be changed

## Validation Rules

### Input Validation
- **Title**: Required, non-empty
- **Description**: Required, non-empty
- **Severity**: Required, must be LOW/MEDIUM/HIGH
- **Audit ID**: Required, must exist
- **Assignee ID**: Required, must exist
- **RCA Steps**: 3-5 steps, sequential numbering, non-empty why text

### Business Validation
- NC cannot be closed without completed actions
- HIGH severity NCs require RCA before closure
- Status transitions must follow business rules
- Users can only access their assigned NCs

## Error Responses

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

### Not Found Errors
```json
{
  "error": "NC not found"
}
```

## Security

### Role-Based Access Control
- **ADMIN**: Full access to all NC operations
- **AUDITOR**: Can create, update, and manage NCs
- **REVIEWER**: Can view NCs and close them
- **USER**: Can only view assigned NCs

### Endpoint Security
```java
@PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")  // Create NCs
@PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')") // View NCs
@PreAuthorize("hasAnyRole('REVIEWER','ADMIN')") // Close NCs
@PreAuthorize("isAuthenticated()")              // View assigned NCs
```

## Testing

### Sample Test Data

#### Create NC
```json
{
  "auditId": 1,
  "title": "Training Records Non-Conformity",
  "description": "Employee training records not maintained as per procedure",
  "severity": "HIGH",
  "assignedToId": 3,
  "createdById": 1
}
```

#### Add Corrective Action
```json
{
  "description": "Implement training record management system",
  "responsibleId": 3,
  "dueDate": "2024-04-30"
}
```

#### Submit RCA
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

### Test Scenarios
1. **Create NC**: Test NC creation with various inputs
2. **Add Actions**: Test corrective action creation
3. **Submit RCA**: Test RCA submission with different step counts
4. **Close NC**: Test NC closure with various conditions
5. **Status Management**: Test status transitions
6. **Authorization**: Test role-based access control
7. **Validation**: Test input validation and business rules

## Performance Considerations

### Database Indexes
```sql
CREATE INDEX idx_nc_audit_id ON non_conformity(audit_id);
CREATE INDEX idx_nc_assigned_to ON non_conformity(assigned_to);
CREATE INDEX idx_nc_status ON non_conformity(status);
CREATE INDEX idx_nc_severity ON non_conformity(severity);
CREATE INDEX idx_nc_created_at ON non_conformity(created_at);
CREATE INDEX idx_action_due_date ON corrective_action(due_date);
CREATE INDEX idx_action_status ON corrective_action(status);
```

### Query Optimization
- Efficient filtering with database indexes
- Lazy loading of related entities
- Stream-based processing for large datasets
- Defensive copying to prevent concurrent modification

## Monitoring and Logging

### Audit Trail
- All NC changes are logged with timestamps
- User information is tracked for all modifications
- Status change history is maintained

### Performance Monitoring
- Database query performance monitoring
- API response time tracking
- Error rate monitoring

## Future Enhancements

### Planned Features
1. **Email Notifications**: Automated notifications for NC milestones
2. **NC Templates**: Predefined NC templates for common scenarios
3. **Advanced Reporting**: NC analytics and trend analysis
4. **Integration**: Integration with external systems
5. **Mobile Support**: Mobile-optimized NC management

### Scalability Considerations
- Database partitioning for large NC datasets
- Caching for frequently accessed NC data
- Asynchronous processing for NC calculations
- Microservices architecture for NC management

## Conclusion

The Non-Conformity Management module provides a robust, scalable, and secure solution for managing non-conformities in the Quality Management System. With comprehensive validation, role-based access control, and performance optimizations, it meets the requirements for production deployment.

The implementation follows Spring Boot best practices and includes proper error handling, validation, and security measures. The modular design allows for easy extension and maintenance.
