# ✅ AppointmentController TEST SCENARIO MATRIX

| Scenario Type                  | Test Case Description                                              | Endpoint/Method                            | Notes                                                           |
| ------------------------------ | ------------------------------------------------------------------ | ------------------------------------------ | --------------------------------------------------------------- |
| ✅ **Happy Path**               | Book a valid appointment                                           | `POST /appointments/book`                  | Valid doctor, patient, and available slot                       |
| ✅ **Already Booked Slot**      | Try booking when slot is already `BOOKED`                          | `POST /appointments/book`                  | Should return `409 CONFLICT`                                    |
| ✅ **Slot Not Found**           | Try booking with non-existent or `null` slot                       | `POST /appointments/book`                  | Should return `409` or `400`                                    |
| ✅ **Patient Double Booking**   | Try booking another appointment for same patient and time          | `POST /appointments/book`                  | Conflict expected                                               |
| ✅ **Doctor Double Booking**    | Try booking another patient with same doctor and slot              | `POST /appointments/book`                  | Conflict expected                                               |
| ✅ **Cancel Already Cancelled** | Cancel an appointment already marked `CANCELLED`                   | `DELETE /appointments/{id}`                | Should return `400`                                             |
| ✅ **Cancel Non-Existent**      | Cancel a non-existent appointment ID                               | `DELETE /appointments/{id}`                | Should return `404`                                             |
| ✅ **Reschedule Valid**         | Reschedule a valid appointment                                     | `PUT /appointments/{id}/reschedule`        | Status should change to `CANCELLED`, new one should be `BOOKED` |
| ✅ **Reschedule Completed**     | Try rescheduling a `COMPLETED` appointment                         | `PUT /appointments/{id}/reschedule`        | Should return `400 BAD REQUEST`                                 |
| ✅ **Mark Arrived Valid**       | Mark a valid `BOOKED` appointment as `ARRIVED`                     | `PUT /appointments/{id}/arrive`            | Status should change                                            |
| ✅ **Mark Already Arrived**     | Try marking an already `ARRIVED` appointment again                 | `PUT /appointments/{id}/arrive`            | Should return `400`                                             |
| ✅ **Patient Fetch**            | Get paginated appointments for a patient                           | `GET /appointments/patient/{patientId}`    | Should return correct paginated list                            |
| ✅ **Doctor Fetch**             | Get appointments for a doctor on a given date                      | `GET /appointments/{doctorId}?date=...`    | Should return filtered list                                     |
| ✅ **Get Doctor Schedule**      | Get doctor schedule view for a date, filter by name/specialization | `GET /appointments/doctor-schedules/paged` | Should group and return slots per doctor                        |
| ✅ **Filters - TODAY**          | Get appointment filters by range `"TODAY"`                         | `GET /appointments?range=TODAY`            | Checks if only today's date entries returned                    |
| ✅ **Filters - WEEK**           | Filter by `"WEEK"` and ensure range calculation                    | `GET /appointments?range=WEEK`             | Should include past 7 days                                      |
| ✅ **Filters - doctorName**     | Filter using lowercase or partial name                             | `GET /appointments?doctorName=sm`          | Check `like` logic correctness                                  |
| ✅ **Concurrent Booking**       | Simulate 2 threads trying to book same slot simultaneously         | `POST /appointments/book` (parallel)       | Only one should succeed, one should get `409`                   |

# 🔁 CONCURRENCY TEST CASES

| # | Scenario                                                                       | Expected Behavior                                            | Area                                       | Notes                                          |
| - | ------------------------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------ | ---------------------------------------------- |
| 1 | Two users try to **book the same slot** at the same time                       | Only one succeeds, other gets `409 CONFLICT`                 | `/appointments/book`                       | Simulate with multithreaded test               |
| 2 | One thread **books** and another **cancels** the same appointment concurrently | One wins; status reflects final state                        | `/appointments/book`, `/appointments/{id}` | Use synchronized lock or optimistic locking    |
| 3 | One reschedules an appointment while another tries to **arrive** the same      | One fails with `400 BAD_REQUEST`                             | `/appointments/{id}/reschedule`, `/arrive` | State transition validation required           |
| 4 | Admin tries to **block a slot** while a patient is booking it                  | Blocking should fail or vice versa                           | SlotService & `/book`                      | Add atomic operation checks in `SlotService`   |
| 5 | Same patient books 2 different slots at same time                              | Allow only one if same date-time                             | `/book`                                    | Conflict for patient overlapping time          |
| 6 | Reschedule happens **twice in parallel**                                       | Only one should succeed, other should be blocked or rejected | `/reschedule`                              | Use DB transaction isolation or version checks |
| 7 | Arrival and cancel run simultaneously                                          | One should succeed; use transaction to rollback the other    | `/arrive`, `/cancel`                       | Add locking/transactional isolation            |
| 8 | Same slot marked **completed** and **cancelled** in parallel                   | Ensure state consistency                                     | `/complete`, `/cancel`                     | Final state should not be ambiguous            |
