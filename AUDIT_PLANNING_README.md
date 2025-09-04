# Audit Planning Module - Implementation Guide

## Overview
The Audit Planning module provides comprehensive functionality for scheduling, viewing, and managing audits in the Quality Management System. This module allows auditors and admins to create audit plans, view audit calendars, and track audit progress with live status updates.

## Key Features Implemented

### 1. Create Audit Plans
- **Enhanced Audit Creation**: Auditors can schedule audits with comprehensive details
- **Validation**: Robust input validation for all audit fields
- **Auditor Assignment**: Multiple auditors can be assigned to a single audit
- **Audit Types**: Support for different audit types (INTERNAL, EXTERNAL, SUPPLIER)

### 2. View Audit Calendar
- **Calendar Interface**: Calendar view with color-coded audit events
- **Status Visualization**: Different colors for different audit statuses
- **Event Details**: Hover information showing audit details
- **Navigation**: Direct links to audit details from calendar

### 3. Audit Status Tracking
- **Live Status Updates**: Real-time status tracking with timestamps
- **Progress Monitoring**: Checklist completion tracking
- **Phase Management**: Support for audit phases (PLANNING, EXECUTION, REPORTING, FOLLOW_UP)
- **Overdue Detection**: Automatic detection of overdue audits

## Technical Implementation

### Database Schema
The audit table has been enhanced with new fields:

```sql
-- New fields added to audit table
audit_type VARCHAR(50)           -- Type of audit
department VARCHAR(100)          -- Department being audited
location VARCHAR(100)            -- Location of audit
notes TEXT                       -- Additional notes
current_phase VARCHAR(50)        -- Current audit phase
last_activity TIMESTAMP          -- Last activity timestamp
```

### API Endpoints

#### Core Audit Management
1. **POST** `/api/audits` - Create new audit plan
2. **GET** `/api/audits` - Get all audits with filtering
3. **GET** `/api/audits/{id}` - Get specific audit
4. **PUT** `/api/audits/{id}` - Update audit
5. **DELETE** `/api/audits/{id}` - Delete audit (Admin only)

#### Status and Phase Management
6. **PATCH** `/api/audits/{id}/status` - Update audit status
7. **PATCH** `/api/audits/{id}/phase` - Update audit phase

#### Calendar and Progress
8. **GET** `/api/audits/calendar` - Get calendar view
9. **GET** `/api/audits/progress` - Get progress tracking

#### Filtering and Search
10. **GET** `/api/audits/status/{status}` - Get audits by status
11. **GET** `/api/audits/overdue` - Get overdue audits
12. **GET** `/api/audits/upcoming` - Get upcoming audits

### Data Transfer Objects (DTOs)

#### AuditRequest
Enhanced request DTO with validation:
```java
public class AuditRequest {
    @NotBlank @Size(min = 3, max = 200)
    public String title;
    
    @NotBlank @Size(min = 10, max = 1000)
    public String scope;
    
    @Size(max = 1000)
    public String objectives;
    
    @NotNull @FutureOrPresent
    public LocalDate startDate;
    
    @NotNull
    public LocalDate endDate;
    
    @Size(min = 1)
    public List<Long> auditorIds;
    
    public String auditType;
    public String department;
    public String location;
    public String notes;
}
```

#### AuditResponse
Comprehensive response DTO:
```java
public class AuditResponse {
    public Long id;
    public String title;
    public String scope;
    public String objectives;
    public LocalDate startDate;
    public LocalDate endDate;
    public Status status;
    public String auditType;
    public String department;
    public String location;
    public String notes;
    public String createdBy;
    public String createdByName;
    public List<String> auditorNames;
    public List<Long> auditorIds;
    public Integer totalChecklists;
    public Integer completedChecklists;
    public Double progressPercentage;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
```

#### AuditCalendarResponse
Calendar-specific DTO:
```java
public class AuditCalendarResponse {
    public Long id;
    public String title;
    public LocalDate startDate;
    public LocalDate endDate;
    public Status status;
    public String backgroundColor;
    public String borderColor;
    public Boolean allDay;
    public String url;
    // ... other fields
}
```

#### AuditProgress
Progress tracking DTO:
```java
public class AuditProgress {
    public Long auditId;
    public String title;
    public Status status;
    public Integer totalChecklists;
    public Integer completedChecklists;
    public Double progressPercentage;
    public String currentPhase;
    public Boolean isOverdue;
    public Boolean isOnTrack;
    // ... other fields
}
```

## Security and Authorization

### Role-Based Access Control
- **ADMIN**: Full access to all audit operations
- **AUDITOR**: Can create, update, and manage audits
- **USER**: Read-only access to audit information

### Endpoint Security
```java
@PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")  // Create audits
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')") // View audits
@PreAuthorize("hasRole('AUDITOR')")            // Update status
@PreAuthorize("hasRole('ADMIN')")              // Delete audits
```

## Validation Rules

### Input Validation
- **Title**: 3-200 characters, required
- **Scope**: 10-1000 characters, required
- **Objectives**: Max 1000 characters, optional
- **Start Date**: Must be today or future
- **End Date**: Must be after start date
- **Auditor IDs**: At least one auditor required

### Business Rules
- Status transitions: PLANNED → IN_PROGRESS → COMPLETED
- Only AUDITORs can update audit status
- Overdue detection based on end date and completion status
- Progress calculation based on checklist completion

## Performance Optimizations

### Database Indexes
```sql
CREATE INDEX idx_audit_status ON audit(status);
CREATE INDEX idx_audit_audit_type ON audit(audit_type);
CREATE INDEX idx_audit_department ON audit(department);
CREATE INDEX idx_audit_start_date ON audit(start_date);
CREATE INDEX idx_audit_end_date ON audit(end_date);
CREATE INDEX idx_audit_created_by ON audit(created_by);
```

### Query Optimization
- Efficient filtering with database indexes
- Lazy loading of related entities
- Defensive copying to prevent concurrent modification issues
- Stream-based processing for large datasets

## Error Handling

### Comprehensive Error Responses
```java
// Validation errors
{
  "error": "End date cannot be before start date"
}

// Authorization errors
{
  "error": "Insufficient permissions"
}

// Not found errors
{
  "error": "Audit not found"
}
```

### Exception Handling
- Input validation exceptions
- Business rule violations
- Database constraint violations
- Authorization failures

## Testing

### API Testing
Use the provided Postman collection (`Audit_Planning_Postman_Collection.json`) for comprehensive API testing.

### Test Scenarios
1. **Create Audit Plan**: Test audit creation with various inputs
2. **Status Management**: Test status transitions and validations
3. **Calendar View**: Test calendar data retrieval
4. **Progress Tracking**: Test progress calculation
5. **Filtering**: Test various filter combinations
6. **Authorization**: Test role-based access control

### Sample Test Data
```json
{
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
}
```

## Deployment

### Database Migration
Run the migration script to add new fields:
```sql
-- Execute V2__Add_Audit_Planning_Fields.sql
```

### Configuration
Ensure the following properties are configured:
```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/qms
spring.datasource.username=qms_user
spring.datasource.password=qms_password

# JPA configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Security configuration
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
```

## Monitoring and Logging

### Audit Trail
- All audit changes are logged with timestamps
- User information is tracked for all modifications
- Last activity timestamps are maintained

### Performance Monitoring
- Database query performance monitoring
- API response time tracking
- Error rate monitoring

## Future Enhancements

### Planned Features
1. **Email Notifications**: Automated notifications for audit milestones
2. **Audit Templates**: Predefined audit templates for common scenarios
3. **Resource Management**: Auditor workload balancing
4. **Reporting**: Advanced audit reporting and analytics
5. **Integration**: Integration with external calendar systems

### Scalability Considerations
- Database partitioning for large audit datasets
- Caching for frequently accessed audit data
- Asynchronous processing for audit calculations
- Microservices architecture for audit management

## Support and Documentation

### API Documentation
- Complete API documentation in `AUDIT_PLANNING_API_GUIDE.md`
- Postman collection for testing
- Sample requests and responses

### Troubleshooting
- Common error scenarios and solutions
- Performance optimization tips
- Security best practices

## Conclusion

The Audit Planning module provides a robust, scalable, and secure solution for managing audits in the Quality Management System. With comprehensive validation, role-based access control, and performance optimizations, it meets the requirements for production deployment.

The implementation follows Spring Boot best practices and includes proper error handling, validation, and security measures. The modular design allows for easy extension and maintenance.
