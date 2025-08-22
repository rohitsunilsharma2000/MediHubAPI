# 🏥 MediHubAPI – Hospital  System

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
