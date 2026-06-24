import httpx
import json
from mcp.server.fastmcp import FastMCP

# Create a FastMCP instance
mcp = FastMCP("hr-management-mcp")

# Your Spring Boot API Base URL
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
            
            # Handle empty responses (like 204 No Content for DELETE) cleanly
            if not response.content:
                 return json.dumps({"success": True, "message": "Operation successful"}, indent=2)
            
            # Return the JSON response back to the AI
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        return f"API Error: {str(e)}"


# --- DEPARTMENT TOOLS ---

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

# Run the server using Standard I/O (required for Claude Desktop)
if __name__ == "__main__":
    mcp.run()