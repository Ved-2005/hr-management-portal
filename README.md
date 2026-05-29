# HR Management Portal

   A Spring Boot backend for managing HR operations — employees, departments, leave requests, and attendance tracking. Includes an MCP (Model Context Protocol) server that enables natural-language interaction through Claude CLI.

   ## Tech Stack

   - **Backend**: Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA
   - **Database**: PostgreSQL
   - **Auth**: JWT-based stateless authentication
   - **MCP Server**: TypeScript, @modelcontextprotocol/sdk
   - **AI Frontend**: Claude CLI

   ## Features

   - Employee CRUD with search and department filtering
   - Department management
   - Leave request workflow (apply → approve/reject)
   - Attendance tracking (check-in/check-out)
   - JWT authentication
   - MCP server with 10+ tools for AI-driven HR management

   ## Getting Started

   ### Prerequisites

   - JDK 21
   - PostgreSQL 16+
   - Node.js 20+

   ### Setup

   1. Create the database:
      ```sql
      CREATE DATABASE hr_portal;

   2. Run the backend:

      ./mvnw spring-boot:run

   3. API available at http://localhost:8080

   API Endpoints
   ┌─────────────────────┬──────────────────────────────────┬──────────────────────────┐
   │ Method              │ Endpoint                         │ Description              │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ POST                │ /api/auth/login                  │ Authenticate and get JWT │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ GET/POST            │ /api/departments                 │ List/Create departments  │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ GET/POST/PUT/DELETE │ /api/employees                   │ CRUD employees           │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ GET                 │ /api/employees/search?name=      │ Search employees         │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ POST                │ /api/leaves                      │ Apply for leave          │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ PATCH               │ /api/leaves/{id}/approve         │ Approve leave            │
   ├─────────────────────┼──────────────────────────────────┼──────────────────────────┤
   │ POST                │ /api/attendance/check-in/{empId} │ Record check-in          │
   └─────────────────────┴──────────────────────────────────┴──────────────────────────┘

   Commit History

   Phase 1: Project Setup (Commits 1–2)

   ┌─────┬─────────────────────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                             │ Description                                                                                   │
   ├─────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 1   │ init: scaffold Spring Boot project with Maven and base dependencies │ Generate project from start.spring.io with Web, JPA, PostgreSQL, Lombok, Validation, Security │
   ├─────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 2   │ chore: add application config and disable default security          │ Add database connection properties and temporarily disable Spring Security login              │
   └─────┴─────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────┘

   Phase 2: Foundation (Commit 3)

   ┌─────┬─────────────────────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                             │ Description                                                                                                                                           │
   ├─────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 3   │ chore: add base entity, exception handler, and API response wrapper │ Shared building blocks — BaseEntity (auto ID + timestamps), ApiResponse (consistent JSON format), GlobalExceptionHandler (centralized error handling) │
   └─────┴─────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

   Phase 3: Department Module (Commits 4–5)

   ┌─────┬─────────────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                     │ Description                                                                                   │
   ├─────┼─────────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 4   │ feat(department): add entity, repository, and service layer │ Department entity mapped to DB, JPA repository for queries, service layer with business logic │
   ├─────┼─────────────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 5   │ feat(department): add REST controller with CRUD endpoints   │ Expose department operations as REST API — create, list, get by ID, delete                    │
   └─────┴─────────────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────┘

   Phase 4: Employee Module (Commits 6–8)

   ┌─────┬──────────────────────────────────────────────────────────────┬─────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                      │ Description                                                                     │
   ├─────┼──────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
   │ 6   │ feat(employee): add entity with department relationship      │ Employee entity with @ManyToOne relationship to Department                      │
   ├─────┼──────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
   │ 7   │ feat(employee): add repository, DTO, and service with search │ Custom queries (search by name, filter by department), input validation via DTO │
   ├─────┼──────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
   │ 8   │ feat(employee): add REST controller with search and filter   │ Full CRUD + search + department filter endpoints                                │
   └─────┴──────────────────────────────────────────────────────────────┴─────────────────────────────────────────────────────────────────────────────────┘

   Phase 5: Leave Management (Commits 9–11)

   ┌─────┬───────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                               │ Description                                                                               │
   ├─────┼───────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
   │ 9   │ feat(leave): add leave request entity and repository  │ LeaveRequest entity with type (CASUAL/SICK/EARNED) and status (PENDING/APPROVED/REJECTED) │
   ├─────┼───────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
   │ 10  │ feat(leave): add service with approval workflow       │ State machine logic — apply sets PENDING, admin can approve/reject                        │
   ├─────┼───────────────────────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
   │ 11  │ feat(leave): add REST controller for leave management │ Endpoints for applying, viewing pending, and approving/rejecting leaves                   │
   └─────┴───────────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────┘

   Phase 6: Attendance Module (Commits 12–13)

   ┌─────┬───────────────────────────────────────────────────────┬─────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                               │ Description                                                         │
   ├─────┼───────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤
   │ 12  │ feat(attendance): add entity, repository, and service │ Attendance with check-in/out times, date-range queries              │
   ├─────┼───────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤
   │ 13  │ feat(attendance): add REST controller                 │ Endpoints for check-in, check-out, and querying by employee or date │
   └─────┴───────────────────────────────────────────────────────┴─────────────────────────────────────────────────────────────────────┘

   Phase 7: Security (Commits 14–15)

   ┌─────┬──────────────────────────────────────────────────────────┬─────────────────────────────────────────────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                  │ Description                                                                                             │
   ├─────┼──────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 14  │ feat(auth): add JWT authentication with Spring Security  │ JwtUtil class for token generation/validation, jjwt dependency                                          │
   ├─────┼──────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────────────┤
   │ 15  │ feat(auth): add login endpoint and security filter chain │ Login endpoint returns JWT, JwtAuthFilter validates token on every request, all endpoints now protected │
   └─────┴──────────────────────────────────────────────────────────┴─────────────────────────────────────────────────────────────────────────────────────────────────────────┘

   Phase 8: Testing (Commits 16–17)

   ┌─────┬───────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────┐
   │ #   │ Message                                                   │ Description                                              │
   ├─────┼───────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
   │ 16  │ test: add unit tests for employee and department services │ Mockito-based unit tests for service layer logic         │
   ├─────┼───────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
   │ 17  │ test: add integration test for department controller      │ MockMvc + H2 in-memory DB to test full request lifecycle │
   └─────┴───────────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────┘

   Phase 9: MCP Server (Commits 18–20)

   ┌─────┬─────────────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────────────────┐
   │ #   │ Message                                                                 │ Description                                                    │
   ├─────┼─────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────┤
   │ 18  │ feat(mcp): initialize MCP server project with TypeScript                │ Separate Node.js project with @modelcontextprotocol/sdk        │
   ├─────┼─────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────┤
   │ 19  │ feat(mcp): add MCP tools for employee, department, and leave operations │ 10+ tools that call Spring Boot API — usable via Claude CLI    │
   ├─────┼─────────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────┤
   │ 20  │ docs: add README with setup instructions and Claude CLI config          │ Final documentation, setup guide, and Claude CLI configuration │
   └─────┴─────────────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────────────────┘

   Project Structure

   ├── src/main/java/com/hrportal/hr_management_api/
   │   ├── common/          # Base entity, API response wrapper
   │   ├── config/          # Security configuration
   │   ├── exception/       # Global exception handling
   │   ├── department/      # Department module
   │   ├── employee/        # Employee module
   │   ├── leave/           # Leave management module
   │   ├── attendance/      # Attendance tracking module
   │   └── auth/            # JWT authentication
   ├── hr-mcp-server/       # MCP server (TypeScript)
   └── pom.xml

   MCP Server

   The MCP server wraps the REST API into tools that Claude CLI can invoke via natural language.

   cd hr-mcp-server
   npm install && npm run build

   Example Usage (Claude CLI)

   │ "List all employees in Engineering department"
   │ "Apply sick leave for employee #3 from June 1 to June 3"
   │ "Approve pending leave request #5"

   License

   MIT