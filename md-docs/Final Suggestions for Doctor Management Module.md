Here’s a **comprehensive and professionally structured document** that includes:

1. ✅ **Doctor Management APIs (15–20)**
2. 📬 **Corresponding `curl` commands and responses**
3. 🧐 **Detailed review of each response with suggestions**

---

## 🩺 Doctor Management API (Endpoints 15–20)

---

### ✅ **15. Get Doctor Profile by ID**

#### 📬 Request:

```bash
curl --location 'http://localhost:8080/doctors/5' \
--header 'Authorization: Bearer <JWT_TOKEN>' \
--data ''
```

#### 📥 Response:

```json
{
  "id": null,
  "name": null,
  "email": "sonal.rao@hospitalmarathi.com",
  "enabled": true,
  "specializations": null
}
```

#### 🧐 Review:

* **Issue:** `id`, `name`, and `specializations` are `null`, which is unexpected.
* **Possible Causes:**

    * Improper DTO → Entity → Response mapping (e.g., ModelMapper config issue).
    * Lazy loading issue for `specializations`.
* **Suggestion:**

    * Return `id` as `5`, `name` as full doctor name, and `specializations` as `[]` or valid list, not `null`.

---

### ✅ **16. Define Weekly Working Hours (Availability)**

#### 📬 Request:

```bash
curl --location 'http://localhost:8080/doctors/5/availability' \
--header 'Authorization: Bearer <JWT_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "slotDurationInMinutes": 30,
  "weeklyAvailability": {
    "MONDAY": [
      { "start": "09:00", "end": "11:00" },
      { "start": "14:00", "end": "16:00" }
    ],
    "WEDNESDAY": [
      { "start": "10:00", "end": "12:00" }
    ]
  }
}'
```

#### 📥 Response:

```text
Availability defined successfully.
```

#### 🧐 Review:

* **Good:** Message is clear and confirms the action.
* **Suggestion:**

    * Add metadata for better traceability:

      ```json
      {
        "message": "Availability defined successfully",
        "doctorId": 5,
        "slotDurationInMinutes": 30,
        "updatedDays": ["MONDAY", "WEDNESDAY"]
      }
      ```

---

### ✅ **17. Update Weekly Availability**

#### 📬 Request:

```bash
curl --location 'http://localhost:8080/doctors/5/availability' \
--header 'Authorization: Bearer <JWT_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "slotDurationInMinutes": 45,
  "weeklyAvailability": {
    "MONDAY": [
      { "start": "09:30", "end": "12:30" }
    ],
    "WEDNESDAY": [
      { "start": "15:00", "end": "17:00" }
    ]
  }
}'
```

#### 📥 Response:

```text
Availability defined successfully.
```

#### 🧐 Review:

* **Same feedback as #16.**
* **Ensure** the update overwrites old data correctly.
* Suggest **returning confirmation with timestamp and updated fields.**

---

### ✅ **18. Get Slots for a Specific Date**

#### 📬 Request:

```bash
curl --location 'http://localhost:8080/doctors/5/slots?date=2025-08-11' \
--header 'Authorization: Bearer <JWT_TOKEN>'
```

#### 📥 Response:

```json
[
  {
    "date": "2025-08-11",
    "startTime": "09:30:00",
    "endTime": "10:15:00",
    "status": "AVAILABLE"
  },
  {
    "date": "2025-08-11",
    "startTime": "10:15:00",
    "endTime": "11:00:00",
    "status": "AVAILABLE"
  },
  {
    "date": "2025-08-11",
    "startTime": "11:00:00",
    "endTime": "11:45:00",
    "status": "AVAILABLE"
  },
  {
    "date": "2025-08-11",
    "startTime": "11:45:00",
    "endTime": "12:30:00",
    "status": "AVAILABLE"
  }
]
```

#### 🧐 Review:

* **Good:** Slot segmentation by duration is accurate.
* **Suggestions:**

    * Include a `slotId` or `slotReference` for booking use.
    * Use **ISO 8601** timestamps (e.g., `"09:30:00+05:30"`).
    * Optional: Add `isBookable`, `doctorId`, or `location` fields for clarity.

---

### ✅ **19. Deactivate Doctor**

#### 📬 Request:

```bash
curl --location --request PUT 'http://localhost:8080/doctors/5/deactivate' \
--header 'Authorization: Bearer <JWT_TOKEN>'
```

#### 📥 Response:

```text
Doctor deactivated.
```

#### 🧐 Review:

* **Good:** Simple message confirms the operation.
* **Suggestions:**

    * Add timestamp and doctor ID for logs or UI:

      ```json
      {
        "message": "Doctor deactivated successfully",
        "doctorId": 5,
        "timestamp": "2025-07-20T06:20:00Z"
      }
      ```

---

### ❌ **20. Delete Doctor**

#### 📬 Request:

```bash
curl --location --request DELETE 'http://localhost:8080/doctors/5' \
--header 'Authorization: Bearer <JWT_TOKEN>'
```

#### 📥 Response:

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": null,
  "path": "uri=/doctors/5",
  "timestamp": "2025-07-20T06:25:58Z",
  "validationErrors": null
}
```

#### 🧐 Review:

* **Issue:** HTTP 500 without a meaningful error message.
* **Root Cause (likely):**

    * Foreign key constraint (e.g., appointments, slots still linked).
* **Fix Suggestions:**

    * Implement `@ControllerAdvice` with proper `@ExceptionHandler`.
    * Return message like:

      ```json
      {
        "status": 409,
        "error": "Conflict",
        "message": "Doctor cannot be deleted due to existing scheduled appointments.",
        "timestamp": "2025-07-20T06:25:58Z"
      }
      ```

---

## 📌 Final Suggestions for Doctor Management Module

| Aspect             | Recommendation                                                                      |
| ------------------ | ----------------------------------------------------------------------------------- |
| **DTO Mapping**    | Ensure proper mapping for null fields using ModelMapper or manual mapping           |
| **Error Handling** | Use `GlobalExceptionHandler` with structured JSON errors                            |
| **Logging**        | Add SLF4J logs for each operation with doctor ID and outcome                        |
| **Consistency**    | All endpoints should return structured JSON, not plain text                         |
| **Validation**     | Prevent delete if doctor has active slots/appointments, return meaningful 4xx error |

---

