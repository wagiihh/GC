# GC Application Setup

This Spring Boot application is configured to work with Supabase and includes web development capabilities.

## Environment Variables

Create a `.env` file in the root directory with the following variables:

```bash
# Supabase Configuration for "GC Project"
SUPABASE_PROJECT_ID=your-project-reference-id
SUPABASE_DB_PASSWORD=your-database-password
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key

# Admin Configuration
ADMIN_PASSWORD=your-admin-password
```

## Supabase Setup

1. **Project Name**: Your Supabase project is named "GC Project"
2. Go to Settings > API to get your project URL and API keys
3. Go to Settings > Database to get your database password
4. The `SUPABASE_PROJECT_ID` should be the project reference ID (not the display name)
5. Update the environment variables with your Supabase credentials

## Running the Application

1. Make sure you have Java 21 installed
2. Set the environment variables
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

- `GET /health` - Health check endpoint
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signin` - User login
- `POST /api/auth/signout` - User logout
- `GET /api/auth/session` - Get current session

## Features

- Spring Boot Web with REST API
- Supabase integration for authentication and database
- CORS configuration for web frontend integration
- Spring Security for API protection
- JPA/Hibernate for database operations
- Thymeleaf for server-side rendering (optional)

## Dependencies Added

- `spring-boot-starter-web` - Web development
- `spring-boot-starter-data-jpa` - Database operations
- `spring-boot-starter-security` - Security
- `spring-boot-starter-thymeleaf` - Server-side rendering
- `spring-boot-starter-validation` - Input validation
- `postgresql` - PostgreSQL driver for Supabase
- `supabase-java` - Supabase Java client
- `jackson-databind` - JSON processing
