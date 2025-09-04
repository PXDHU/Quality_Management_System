# Non-Conformity (NC) & Corrective Action Management

## Overview
The Non-Conformity Management module is a comprehensive solution for identifying, tracking, and resolving non-conformities found during audits in the Quality Management System. This production-ready backend application provides complete lifecycle management from NC identification to closure.

## Key Features

### 🎯 NC Identification
- **Auditor Flagging**: Auditors can flag any clause as non-compliant during audits
- **Severity Levels**: Support for Low, Medium, and High severity classifications
- **Detailed Documentation**: Comprehensive description and evidence tracking
- **Audit Integration**: Direct linkage to audit instances and clauses

### 📊 NC Dashboard
- **Auditee View**: Dedicated dashboard for assigned non-conformities
- **Status Tracking**: Real-time status updates (Pending, In Progress, Completed)
- **Advanced Filtering**: Filter by severity, status, audit, and assignment
- **Overdue Detection**: Automatic identification of overdue corrective actions

### 🔧 Corrective Action Management
- **Action Planning**: Create detailed corrective action plans
- **Responsibility Assignment**: Assign actions to specific personnel
- **Due Date Tracking**: Monitor action completion timelines
- **Status Updates**: Track action progress (Pending, In Progress, Completed)

### 🔍 Root Cause Analysis (RCA)
- **Structured Analysis**: 3-5 step Why-Why analysis methodology
- **Major NC Requirement**: Mandatory for HIGH severity non-conformities
- **Sequential Validation**: Ensures proper step progression
- **Comprehensive Documentation**: Detailed root cause identification

### ✅ NC Closure
- **Reviewer Validation**: Expert review of corrective actions
- **Evidence Management**: Upload and track closure evidence
- **Business Rule Enforcement**: Automated validation of closure criteria
- **Audit Trail**: Complete history of NC resolution

## Technical Architecture

### Database Schema
The system uses a robust relational database design with the following key entities:

```sql
-- Non-Conformity table
CREATE TABLE non_conformity (
    non_conformity_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    audit_id BIGINT NOT NULL,
    instance_id BIGINT,
    clause_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') NOT NULL,
    created_by BIGINT,
    assigned_to BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Corrective Action table
CREATE TABLE corrective_action (
    action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    non_conformity_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    responsible_id BIGINT,
    due_date DATE,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- RCA Steps table
CREATE TABLE rca_step (
    rca_step_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    non_conformity_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    why_text TEXT NOT NULL
);
```

### API Endpoints

#### Core NC Management
| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/nc` | Create new non-conformity | AUDITOR, ADMIN |
| GET | `/api/nc/{id}` | Get NC by ID | AUDITOR, ADMIN, REVIEWER |
| GET | `/api/nc` | Get all NCs with filtering | AUDITOR, ADMIN, REVIEWER |
| GET | `/api/nc/audit/{auditId}` | Get NCs by audit | AUDITOR, ADMIN, REVIEWER |
| GET | `/api/nc/severity/{severity}` | Get NCs by severity | AUDITOR, ADMIN, REVIEWER |
| GET | `/api/nc/overdue` | Get overdue NCs | AUDITOR, ADMIN, REVIEWER |
| PATCH | `/api/nc/{id}/status` | Update NC status | AUDITOR, ADMIN |

#### NC Dashboard
| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| GET | `/api/nc/assignee/{userId}` | Get assigned NCs | Authenticated users |

#### Corrective Actions
| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/nc/{ncId}/actions` | Add corrective action | AUDITOR, ADMIN |
| PATCH | `/api/nc/actions/{actionId}/status` | Update action status | AUDITOR, ADMIN |

#### Root Cause Analysis
| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/nc/{ncId}/rca` | Submit RCA | AUDITOR, ADMIN |

#### NC Closure
| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/nc/{ncId}/close` | Close NC | REVIEWER, ADMIN |

## Business Rules

### NC Creation
- **Required Fields**: Audit ID, title, description, severity, assignee
- **Initial Status**: All NCs start with PENDING status
- **Validation**: Comprehensive input validation for all fields

### Corrective Actions
- **Required Fields**: Description and responsible person
- **Status Impact**: Adding actions changes NC status to IN_PROGRESS
- **Due Dates**: Optional but recommended for tracking

### Root Cause Analysis
- **Step Count**: 3-5 steps required
- **Sequential Ordering**: Steps must be numbered 1, 2, 3, etc.
- **Content Validation**: Each step must have meaningful why text
- **High Severity Requirement**: Mandatory for HIGH severity NCs

### NC Closure
- **Action Completion**: All corrective actions must be completed
- **RCA Requirement**: HIGH severity NCs require RCA before closure
- **Evidence Upload**: Final evidence can be attached during closure
- **Status Protection**: Completed NCs cannot be modified

## Security Implementation

### Role-Based Access Control
- **ADMIN**: Full access to all NC operations
- **AUDITOR**: Can create, update, and manage NCs
- **REVIEWER**: Can view NCs and close them
- **USER**: Can only view assigned NCs

### Authentication & Authorization
- JWT-based authentication
- Role-based endpoint protection
- User context validation
- Secure data access patterns

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- Audit trail maintenance

## Performance Optimizations

### Database Indexes
```sql
-- Performance indexes for optimal query performance
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

### Caching Strategy
- Application-level caching for frequently accessed data
- Database query result caching
- Static data caching (severity levels, statuses)

## Error Handling

### Comprehensive Error Responses
```json
{
  "error": "Detailed error message",
  "timestamp": "2024-03-15T10:30:00",
  "path": "/api/nc",
  "status": 400
}
```

### Exception Categories
- **Validation Errors**: Input validation failures
- **Business Rule Violations**: Rule enforcement failures
- **Authorization Errors**: Permission-related issues
- **Not Found Errors**: Resource not found
- **System Errors**: Internal server errors

### Error Recovery
- Graceful error handling
- Meaningful error messages
- Proper HTTP status codes
- Error logging and monitoring

## Testing Strategy

### API Testing
- Comprehensive Postman collection provided
- Unit tests for all business logic
- Integration tests for API endpoints
- Performance and load testing

### Test Coverage
- Happy path scenarios
- Edge cases and error conditions
- Security and authorization testing
- Business rule validation

### Test Data Management
- Consistent test data sets
- Automated test data cleanup
- Isolated test environments
- Performance test scenarios

## Deployment

### Environment Configuration
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

# Application configuration
server.port=8080
logging.level.com.example.QualityManagementSystem=INFO
```

### Database Migration
```sql
-- Run database migration scripts
-- Ensure all tables and indexes are created
-- Verify foreign key constraints
```

### Health Checks
- Database connectivity verification
- Application health endpoints
- Performance monitoring
- Error rate tracking

## Monitoring and Logging

### Application Monitoring
- Request/response logging
- Performance metrics tracking
- Error rate monitoring
- User activity tracking

### Database Monitoring
- Query performance monitoring
- Connection pool monitoring
- Deadlock detection
- Index usage analysis

### Business Metrics
- NC creation rates
- Resolution time tracking
- Severity distribution
- Overdue action monitoring

## Future Enhancements

### Planned Features
1. **Email Notifications**: Automated notifications for NC milestones
2. **NC Templates**: Predefined templates for common scenarios
3. **Advanced Reporting**: Analytics and trend analysis
4. **Mobile Support**: Mobile-optimized interfaces
5. **Integration**: External system integration

### Scalability Improvements
- Database partitioning for large datasets
- Microservices architecture
- Event-driven processing
- Advanced caching strategies

## Documentation

### API Documentation
- Complete API guide in `NC_MANAGEMENT_API_GUIDE.md`
- Postman collection for testing
- Sample requests and responses
- Error code documentation

### Testing Documentation
- Testing guide in `NC_TESTING_GUIDE.md`
- Test scenarios and procedures
- Performance testing guidelines
- Security testing checklist

### Deployment Documentation
- Environment setup guide
- Database migration procedures
- Monitoring configuration
- Troubleshooting guide

## Support and Maintenance

### Maintenance Procedures
- Regular database maintenance
- Performance optimization
- Security updates
- Backup and recovery

### Support Resources
- API documentation
- Troubleshooting guides
- Performance monitoring
- Error tracking

## Conclusion

The Non-Conformity Management module provides a robust, scalable, and secure solution for managing non-conformities in the Quality Management System. With comprehensive validation, role-based access control, and performance optimizations, it meets all requirements for production deployment.

The implementation follows Spring Boot best practices and includes proper error handling, validation, and security measures. The modular design allows for easy extension and maintenance.

### Key Benefits
- **Complete Lifecycle Management**: From identification to closure
- **Robust Security**: Role-based access control and data protection
- **High Performance**: Optimized queries and caching strategies
- **Comprehensive Testing**: Full test coverage and validation
- **Production Ready**: Enterprise-grade error handling and monitoring
- **Scalable Architecture**: Designed for growth and expansion

### Getting Started
1. Review the API documentation
2. Import the Postman collection
3. Follow the testing guide
4. Deploy to your environment
5. Monitor and optimize performance

For additional support or questions, refer to the comprehensive documentation provided with this implementation.
