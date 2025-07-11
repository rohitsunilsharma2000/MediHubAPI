# 🏥 MediHubAPI – Hospital RBAC System

Spring Boot backend with JWT authentication for managing hospital staff using strict role-based access control (RBAC).

---

## 🚀 Tech Stack

- Java 17  
- Spring Boot 3.x  
- Spring Security + JWT  
- Hibernate + JPA  
- MySQL  
- Lombok  

---

## 🔐 Authentication

### Login – `/api/auth/login` (POST)

**Request**
```json
{
  "username": "superadmin",
  "password": "superadmin123"
}

**Response**
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}


👥 User Creation – /api/auth/register (POST)
Requires valid token (SUPER_ADMIN or ADMIN depending on role).

**Payload Examples**
**Admin**
{
  "username": "admin1",
  "email": "admin1@hospital.com",
  "password": "admin123",
  "firstName": "Ravi",
  "lastName": "Kumar",
  "role": "ADMIN"
}


**Doctor**
{
  "username": "doctor1",
  "email": "doctor1@hospital.com",
  "password": "doctor123",
  "firstName": "Sneha",
  "lastName": "Mehta",
  "role": "DOCTOR"
}


**Nurse**
{
  "username": "nurse1",
  "email": "nurse1@hospital.com",
  "password": "nurse123",
  "firstName": "Kavita",
  "lastName": "Joshi",
  "role": "NURSE"
}

**Receptionist**
{
  "username": "reception1",
  "email": "reception1@hospital.com",
  "password": "reception123",
  "firstName": "Rahul",
  "lastName": "Patil",
  "role": "RECEPTIONIST"
}


**Billing Clerk**
{
  "username": "billing1",
  "email": "billing1@hospital.com",
  "password": "billing123",
  "firstName": "Anjali",
  "lastName": "Sharma",
  "role": "BILLING_CLERK"
}


**Pharmacist**
{
  "username": "pharma1",
  "email": "pharma1@hospital.com",
  "password": "pharma123",
  "firstName": "Manoj",
  "lastName": "Yadav",
  "role": "PHARMACIST"
}


**HR Manager**
{
  "username": "hrmanager1",
  "email": "hr1@hospital.com",
  "password": "hr123",
  "firstName": "Priya",
  "lastName": "Kapoor",
  "role": "HR_MANAGER"
}

| Role           | Can Manage                        |
| -------------- | --------------------------------- |
| SUPER\_ADMIN   | All roles + system configurations |
| ADMIN          | All roles except SUPER\_ADMIN     |
| HR\_MANAGER    | Nurse, Receptionist, Clerk        |
| DOCTOR         | Patient records only              |
| NURSE          | Patient vitals, medications       |
| RECEPTIONIST   | Appointments                      |
| BILLING\_CLERK | Payments, invoices                |
| PHARMACIST     | Medication inventory              |


**❌ Sample Error Response**
When unauthorized role creation is attempted (e.g., Admin creating SUPER_ADMIN)

{
  "status": 403,
  "error": "Forbidden",
  "message": "ADMIN cannot create SUPER_ADMIN users.",
  "path": "uri=/api/users",
  "timestamp": "2025-07-08T13:02:32Z",
  "validationErrors": null
}
