# HR Management Portal

A backend **Human Resources management REST API** built with **Spring Boot 3.5 / Java 21**, backed by **PostgreSQL**. It handles the full Leave lifecycle — departments, employee records, and leave (time-off) management — under a strict **JWT-based, role-based access control (RBAC)** model with three actors: **Admin**, **HR**, and **Employee**.

The project also includes a **Model Context Protocol (MCP) server** (Python) that exposes every API endpoint as an AI-callable tool, so the entire portal can be driven by an LLM assistant.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Auth | JWT, BCrypt password hashing |
| Database | PostgreSQL |
| Boilerplate | Lombok |
| Build | Maven |
| Testing | JUnit 5, Mockito |
| AI Integration | Python MCP server |

---

## Role Responsibilities in Detail

###  Admin

The system-level super-user. There is exactly one Admin seeded at startup, and **no one can register as an Admin** (self-registration for the `ADMIN` role is explicitly blocked).

**Responsibilities & functions:**
- **Department lifecycle** — the *only* role that can create (`POST /api/departments`) and delete/deactivate (`DELETE /api/departments/{id}`) departments, including setting each department's leave allowances (sick / casual / paid).
- **Organization-wide employee management** — create, view, search, update, and soft-delete employees in **any** department. Admin queries are never department-filtered.
- **Delete HR staff** — only the Admin may soft-delete an employee who holds the HR role.
- Has visibility across the entire organization; none of the department/self scoping applies to Admin.

###  HR (Human Resources)

A department-scoped manager. HR operates strictly within the boundaries of **their own department** (the `departmentId` is included in the claims of their JWT).

**Responsibilities & functions:**
- **Employee management (own department only):**
  - Create employees — but only inside their own department; attempting to create in another department is rejected.
  - View / search / update / soft-delete employees — always filtered to and validated against their department.
  - **Cannot** delete another HR (that requires Admin).
- **Leave approval workflow:**
  - View pending leave requests for **their department** (`GET /api/leaves/pending` returns only their department's employee pending leaves).
  - **Approve** (`PATCH /api/leaves/{id}/approve`) or **reject** (`PATCH /api/leaves/{id}/reject`) requests. Approval automatically deducts the requested days from the employee's leave balance.
- **View** departments (read-only) and any employee's leave history (within reach).
- HR gets their login by **self-registering** with role `HR` — but only if an official employee record has been added by the admin with their username.

###  Employee

The end user whose records and leave the system tracks.

**Responsibilities & functions:**
- **Apply for leave** (`POST /api/leaves/employee/{username}`) — the only role allowed to create a leave request. Includes constraints like employees can only apply for past leaves within a month and future leaves within next year's April 1st (a chosen date where all leaves are reset).
- **View their own profile** (`GET /api/employees/{username}`) — an Employee may only fetch their *own* record; requesting anyone else's is denied.
- **View their own leave history** and **total leaves taken**.
- Employees get their login by **self-registering** with role `EMPLOYEE`, again only if the employee's username exists.
- Employees **cannot** touch departments, other employees, or the approval workflow.

---

## Domain Model (Entities)

```
Department 1───* Employee 1───1 LeaveSummary
                    │
                    1
                    │
                    *
                 TimeOff

User (login/auth) 1───1 Employee   (linked by shared `username`)
```

| Entity | Purpose | Key fields |
|--------|---------|-----------|
| **User** | Authentication record (login). PK = `username`. | `username`, `password` (BCrypt), `role` (ADMIN/HR/EMPLOYEE) |
| **Employee** | Official record of an employee. | `firstName`, `lastName`, `username` (unique, exactly 7 letters), `designation`, `salary`, `status` (ACTIVE/ON_LEAVE/TERMINATED), `department` |
| **Department** | Org unit + its leave policy. | `name` (unique), `description`, `active`, `sickLeaves`, `casualLeaves`, `paidLeaves` |
| **LeaveSummary** | Per-employee remaining leave balances (shares PK with Employee via `@MapsId`). | `sickLeaveBalance`, `casualLeaveBalance`, `paidLeaveBalance` |
| **TimeOff** | A single leave request. | `employee`, `leaveType` (SICK/CASUAL/PAID), `startDate`, `endDate`, `reason`, `status` (PENDING/APPROVED/REJECTED) |
| **BaseEntity** | Mapped superclass giving `id`, `createdAt`, `updatedAt` to Employee, Department, TimeOff. | auto timestamps |

**Note the deliberate `User` ↔ `Employee` split:** the `Employee` is the HR data record (created by Admin/HR), while `User` is the login credential (created by self-registration). They are linked by a shared `username`, which decouples authentication from HR data.

---

## Core Workflows

### 1. Onboarding a new employee
1. **Admin/HR** creates the `Employee` record (`POST /api/employees`). A `LeaveSummary` is auto-created, seeded from the department's leave allowances.
2. The new hire **self-registers** (`POST /api/auth/register`) with role `EMPLOYEE` and their assigned username. Registration succeeds only because a matching, non-terminated `Employee` record exists.
3. The employee **logs in** (`POST /api/auth/login`) → receives a JWT stamped with their `employeeId` and `departmentId`.

### 2. Leave request → approval
1. **Employee** applies for leave (`POST /api/leaves/employee/{username}`). Validations run (balance, dates, no existing pending request, active status).
2. Request is saved as `PENDING`.
3. **HR** sees it in `GET /api/leaves/pending` (scoped to their department).
4. HR **approves** → status becomes `APPROVED` and the days are deducted from the correct balance bucket; or **rejects** → status `REJECTED`.
5. Nightly scheduler flips the employee's status to `ON_LEAVE` on the start date and back to `ACTIVE` after the end date.

### 3. Soft deletion
- Employees and departments are **never hard-deleted**. Delete = set `status = TERMINATED` (employee) or `active = false` (department).
- A department cannot be deactivated while it still has non-terminated employees.

---

## API Reference

Base path: `/api`. All endpoints (except `/api/auth/**`) require an `Authorization: Bearer <token>` header.

### Auth — `/api/auth` (public)
| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| POST | `/login` | Authenticate, receive JWT | Returns token in `data` |
| POST | `/register` | Self-register a login | Role must be HR/EMPLOYEE; requires an existing non-terminated Employee record |

### Departments — `/api/departments`
| Method | Endpoint | Description | Allowed roles |
|--------|----------|-------------|---------------|
| GET | `/` | List all departments | ADMIN, HR |
| GET | `/{id}` | Get department by id | ADMIN, HR |
| POST | `/` | Create department | **ADMIN** |
| DELETE | `/{id}` | Soft-delete (deactivate) | **ADMIN** |

### Employees — `/api/employees`
| Method | Endpoint | Description | Allowed roles |
|--------|----------|-------------|---------------|
| POST | `/` | Create employee | ADMIN, HR (own dept) |
| GET | `/` | List employees | ADMIN (all), HR (own dept) |
| GET | `/{username}` | Get one employee | ADMIN, HR (own dept), EMPLOYEE (self) |
| GET | `/search?name=` | Search by first/last name | ADMIN, HR (own dept) |
| GET | `/department/{deptId}` | Employees in a department | ADMIN, HR (own dept) |
| PATCH | `/{username}` | Partial update | ADMIN, HR (own dept) |
| DELETE | `/{username}` | Soft-delete (terminate) | ADMIN, HR (own dept, not other HR) |

### Leaves / Time-off — `/api/leaves`
| Method | Endpoint | Description | Allowed roles |
|--------|----------|-------------|---------------|
| POST | `/employee/{username}` | Apply for leave | **EMPLOYEE** |
| GET | `/pending` | Pending requests | **HR** (own dept) |
| GET | `/employee/{username}` | An employee's leave history | HR, EMPLOYEE |
| PATCH | `/{id}/approve` | Approve + deduct balance | **HR** |
| PATCH | `/{id}/reject` | Reject | **HR** |
| GET | `/employee/{username}/total-leaves` | Total approved leave days | HR, EMPLOYEE |

---

## Scheduled Jobs

`LeaveResetScheduler` (enabled via `@EnableScheduling`) runs three cron jobs:

| Job | Cron | What it does |
|-----|------|--------------|
| `resetLeavesOnNewYear` | `0 0 0 1 4 *` (Apr 1, midnight) | Clears all TimeOff records and resets every active employee's leave balances to their department's allowances |
| `updateEmployeeLeaveStatus` | `0 0 0 * * *` (daily midnight) | Sets employees to `ON_LEAVE` when their approved leave starts, back to `ACTIVE` after it ends |
| `removeTerminatedEmployeeLeaveSummaries` | `0 0 0 * * *` (daily midnight) | Deletes leave summaries for terminated employees |

---

## Security Model

- **Stateless JWT** — no server sessions (`SessionCreationPolicy.STATELESS`); CSRF disabled.
- **`JwtAuthFilter`** runs before the standard auth filter, parses the `Bearer` token, and populates the `SecurityContext` with the role authority plus custom claims (`employeeId`, `departmentId`).
- **`JwtUtil`** signs tokens (HMAC-SHA) with the configured secret and 24h expiry (`jwt.expiration=86400000`), embedding `role`, `employeeId`, `departmentId`.
- **`SecurityContextHelper`** exposes convenience accessors (`isAdmin()`, `isHR()`, `isEmployee()`, `getEmployeeId()`, `getDepartmentId()`) used throughout the services for row-level checks.
- **`SecurityConfig`** declares the role access per HTTP method + path.
- Passwords are hashed with **BCrypt**.
- Auth/permission failures are funneled through `CustomAccessDeniedHandler` → `GlobalExceptionHandler` for clean JSON errors.


---

## Default Seeded Admin

On first startup (`DatabaseSeeder`), if no users exist, the app creates:

- Department: **IT Administration**
- Employee: **System Administrator** 

---

## MCP Server (AI Integration)

`leave-portal-mcp-server/main.py` is a **Model Context Protocol** server (built on `FastMCP`) that wraps every REST endpoint as an AI-callable tool. This lets an LLM assistant (e.g. Claude) operate the HR portal in natural language — "create a department", "approve leave for Ved", "search for employees named Ved", etc.

- Each tool forwards to `http://localhost:8080/api` and requires a `userToken` argument (the caller's JWT), so **the same RBAC rules apply** to AI-driven calls.
- Tools mirror the API: `get_departments`, `create_department`, `delete_department`, `create_employee`, `get_all_employees`, `get_employee_by_username`, `search_employees`, `get_employees_by_department`, `patch_employee`, `delete_employee`, `apply_for_leave`, `get_pending_leaves`, `get_employee_leaves`, `approve_leave`, `reject_leave`, `get_total_leaves_taken`.

---

## Testing

Unit tests (JUnit 5 + Mockito) cover the service layer's business rules:

- **DepartmentServiceTest** — creation, duplicate-name rejection, lookup, and soft-delete guards (blocked when active employees exist).
- **EmployeeServiceTest** — creation, duplicate username, inactive/missing department handling, lookups, search, patch, and soft-delete.
- **TimeOffServiceTest** — the full leave-validation matrix (date ordering, inactive employee, existing pending request, date bounds, insufficient balance per leave type) plus approve/reject state transitions and totals.
