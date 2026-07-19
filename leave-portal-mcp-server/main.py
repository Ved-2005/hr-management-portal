import httpx
import json
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("hr-management-mcp")

API_BASE_URL = "http://localhost:8080/api"

def get_headers(user_token: str) -> dict:
    if not user_token:
        raise ValueError("Missing or invalid userToken")
    return {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {user_token}",
    }

async def make_request(method: str, endpoint: str, user_token: str, **kwargs) -> str:
    """Helper function to make HTTP requests and handle errors gracefully."""
    headers = get_headers(user_token)
    try:
        async with httpx.AsyncClient() as client:
            response = await client.request(method, f"{API_BASE_URL}{endpoint}", headers=headers, **kwargs)
            
            if not response.content:
                 return json.dumps({"success": True, "message": "Operation successful"}, indent=2)
            
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        return f"API Error: {str(e)}"


@mcp.tool()
async def get_departments(userToken: str) -> str:
    """Fetch a list of all departments."""
    return await make_request("GET", "/departments", userToken)

@mcp.tool()
async def get_department_by_id(id: int, userToken: str) -> str:
    """Fetch a single department's details by its ID."""
    return await make_request("GET", f"/departments/{id}", userToken)

@mcp.tool()
async def create_department(name: str, description: str, sickLeaves: int, paidLeaves: int, casualLeaves: int, userToken: str) -> str:
    """Create a new department including sick, paid, and casual leave limits."""
    payload = {
        "name": name,
        "description": description,
        "sickLeaves": sickLeaves,
        "paidLeaves": paidLeaves,
        "casualLeaves": casualLeaves
    }
    return await make_request("POST", "/departments", userToken, json=payload)

@mcp.tool()
async def delete_department(id: int, userToken: str) -> str:
    """Delete or deactivate a department by ID."""
    return await make_request("DELETE", f"/departments/{id}", userToken)


@mcp.tool()
async def create_employee(firstName: str, lastName: str, username: str, designation: str, salary: float, departmentId: int, userToken: str) -> str:
    """Create a new employee profile and assign them to a department."""
    payload = {
        "firstName": firstName,
        "lastName": lastName,
        "username": username,
        "designation": designation,
        "salary": salary,
        "departmentId": departmentId
    }
    return await make_request("POST", "/employees", userToken, json=payload)

@mcp.tool()
async def get_all_employees(userToken: str) -> str:
    """Fetch a list of all employees in the system (or department if HR)."""
    return await make_request("GET", "/employees", userToken)

@mcp.tool()
async def get_employee_by_username(username: str, userToken: str) -> str:
    """Fetch a single employee's full details using their username."""
    return await make_request("GET", f"/employees/{username}", userToken)

@mcp.tool()
async def search_employees(name: str, userToken: str) -> str:
    """Search for employees by matching their first or last name."""
    return await make_request("GET", f"/employees/search?name={name}", userToken)

@mcp.tool()
async def get_employees_by_department(deptId: int, userToken: str) -> str:
    """Fetch all employees that belong to a specific department ID."""
    return await make_request("GET", f"/employees/department/{deptId}", userToken)

@mcp.tool()
async def patch_employee(
    username: str, 
    userToken: str, 
    firstName: str = None, 
    lastName: str = None, 
    designation: str = None, 
    salary: float = None, 
    departmentId: int = None
) -> str:
    """Partially update an employee's details (PATCH). Only provide the fields that need to change."""
    raw_payload = {
        "firstName": firstName,
        "lastName": lastName,
        "designation": designation,
        "salary": salary,
        "departmentId": departmentId
    }
    
    payload = {k: v for k, v in raw_payload.items() if v is not None}
    
    return await make_request("PATCH", f"/employees/{username}", userToken, json=payload)

@mcp.tool()
async def delete_employee(username: str, userToken: str) -> str:
    """Terminate (soft delete) an employee by username."""
    return await make_request("DELETE", f"/employees/{username}", userToken)

@mcp.tool()
async def apply_for_leave(username: str, startDate: str, endDate: str, leaveType: str, reason: str, userToken: str) -> str:
    """
    Apply for a leave/timeoff request for a specific employee.
    - startDate / endDate format: YYYY-MM-DD
    - leaveType: 'SICK', 'CASUAL', or 'PAID'
    """
    payload = {
        "startDate": startDate,
        "endDate": endDate,
        "leaveType": leaveType,
        "reason": reason
    }
    return await make_request("POST", f"/leaves/employee/{username}", userToken, json=payload)

@mcp.tool()
async def get_pending_leaves(userToken: str) -> str:
    """Fetch a list of all pending leave requests (filtered by department if HR)."""
    return await make_request("GET", "/leaves/pending", userToken)

@mcp.tool()
async def get_employee_leaves(username: str, userToken: str) -> str:
    """Fetch the entire leave history (pending, approved, rejected) for a specific employee."""
    return await make_request("GET", f"/leaves/employee/{username}", userToken)

@mcp.tool()
async def approve_leave(id: int, userToken: str) -> str:
    """Approve a pending leave request by its ID and deduct from the employee's leave balance."""
    return await make_request("PATCH", f"/leaves/{id}/approve", userToken)

@mcp.tool()
async def reject_leave(id: int, userToken: str) -> str:
    """Reject a pending leave request by its ID."""
    return await make_request("PATCH", f"/leaves/{id}/reject", userToken)

@mcp.tool()
async def get_total_leaves_taken(username: str, userToken: str) -> str:
    """Retrieve the total number of approved leave days an employee has taken so far."""
    return await make_request("GET", f"/leaves/employee/{username}/total-leaves", userToken)

if __name__ == "__main__":
    mcp.run()