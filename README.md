# Quality Management System

A secure Audit Management Tool with admin-only user management, JWT authentication, and comprehensive audit tracking capabilities.

## 🚀 Features

- **Admin-only User Management**: Only administrators can create and manage user accounts
- **Role-based Access Control**: Five distinct roles (Admin, Auditor, Auditee, Reviewer, Compliance_Officer)
- **Secure Authentication**: JWT-based authentication with bcrypt password hashing
- **Force Password Reset**: New users must change their temporary password on first login
- **Comprehensive Audit Management**: Full audit lifecycle management with ISO standards support
- **Document Management**: File upload and management for audit evidence
- **Database Migrations**: Automated schema management with Flyway

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.4, Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT, BCrypt
- **Database Migration**: Flyway
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## 🗄️ Database Setup

1. **Create PostgreSQL Database**:
   ```sql
   CREATE DATABASE compliance_portal;
   ```

2. **Update Database Configuration**:
   Edit `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/compliance_portal
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## 🚀 Running the Application

1. **Clone and Navigate**:
   ```bash
   cd Quality_Management_System
   ```

2. **Build the Application**:
   ```bash
   mvn clean install
   ```

3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## 🔐 Default Admin Credentials

- **Username**: admin@qms.com
- **Password**: Admin@123

## 📚 API Documentation

### Authentication Endpoints

#### POST /api/auth/login
User login with JWT token generation.

**Request Body**:
```json
{
  "username": "admin@qms.com",
  "password": "Admin@123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Login successful",
  "forcePasswordReset": false,
  "role": "ADMIN",
  "username": "admin@qms.com",
  "fullName": "System Administrator"
}
```

### Admin User Management Endpoints

#### POST /api/admin/users
Create a new user (Admin only).

**Request Body**:
```json
{
  "username": "jane@example.com",
  "fullName": "Jane Doe",
  "role": "AUDITOR"
}
```

**Response**:
```json
{
  "message": "User created successfully and credentials sent via email"
}
```

#### GET /api/admin/users
List all users with optional filters (Admin only).

**Query Parameters**:
- `role`: Filter by role (ADMIN, AUDITOR, AUDITEE, REVIEWER, COMPLIANCE_OFFICER)
- `isActive`: Filter by active status (true/false)

#### PATCH /api/admin/users/{id}/status
Activate/deactivate user (Admin only).

**Request Body**:
```json
{
  "isActive": false
}
```

#### POST /api/auth/reset-password
Reset user password (Admin only).

**Request Body**:
```json
{
  "userId": 102
}
```

#### GET /api/roles
Get all available user roles (Admin only).

### User Profile Endpoints

#### PUT /api/users/{id}/profile
Update user profile (self only).

**Request Body**:
```json
{
  "fullName": "Jane D.",
  "oldPassword": "Temp@123",
  "newPassword": "JaneSecure@456"
}
```

#### GET /api/users/me
Get current user information.

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/compliance_portal
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your_jwt_secret_key
jwt.expiration=36000000

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your_secret_key
```

## 🗂️ Database Schema

The system includes the following main tables:

- **user**: User accounts with roles and authentication
- **audit**: Audit records with status tracking
- **audit_auditor**: Many-to-many relationship between audits and auditors
- **clause_library**: ISO standard clauses
- **checklist**: Audit checklists
- **checklist_item**: Individual checklist items
- **correction_plan**: Corrective action plans
- **audit_clause_instance**: Audit findings and non-conformities
- **document**: File management for audit evidence
- **document_audit_mapping**: Document-audit relationships
- **document_clause_mapping**: Document-clause relationships

## 🔒 Security Features

- **JWT Authentication**: Stateless authentication with configurable expiration
- **Role-based Authorization**: Fine-grained access control using @PreAuthorize
- **Password Security**: BCrypt hashing with secure temporary password generation
- **Force Password Reset**: New users must change their password on first login
- **CORS Configuration**: Proper CORS setup for frontend integration
- **Input Validation**: Comprehensive request validation and error handling

## 📧 Email Integration

The system includes stubs for email notifications:

- **User Creation**: Sends temporary password and login instructions
- **Password Reset**: Sends new temporary password

To implement email functionality, update the email methods in `UserService.java` with your email service provider.

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

## 📝 Development

### Project Structure

```
src/main/java/com/example/QualityManagementSystem/
├── config/          # Security and application configuration
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects
├── model/          # JPA entities
├── repository/     # Data access layer
├── service/        # Business logic
└── util/           # Utility classes (JWT, etc.)
```

### Adding New Features

1. **Create Entity**: Add JPA entity in `model/` package
2. **Create Repository**: Add repository interface in `repository/` package
3. **Create Service**: Add business logic in `service/` package
4. **Create Controller**: Add REST endpoints in `controller/` package
5. **Add Migration**: Create Flyway migration script if schema changes needed

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection**: Ensure PostgreSQL is running and credentials are correct
2. **Migration Errors**: Check Flyway migration scripts for syntax errors
3. **JWT Issues**: Verify JWT secret and expiration configuration
4. **CORS Errors**: Update CORS configuration for your frontend domain

### Logs

Enable debug logging by adding to `application.properties`:

```properties
logging.level.com.example.QualityManagementSystem=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📞 Support

For support and questions, please contact the development team. 