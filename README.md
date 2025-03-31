# SWIFT Code API – Home Task

A backend API that parses SWIFT (BIC) codes from an Excel file, stores them in a relational database, and exposes endpoints for querying and managing the data.

---
## Features

- Parses SWIFT (BIC) code data from an Excel file on application startup
- Differentiates between headquarters and branch codes
- Validates consistency between SWIFT code, ISO2 country code, and bank name
- Stores all data in an SQL database (PostgreSQL), with indexing for fast querying
- Exposes a RESTful API for retrieving and managing SWIFT code records
- Handles creation, retrieval, and deletion of SWIFT records
- Structured error handling via global exception handling
- Includes unit and integration tests using H2 in-memory database
- Dockerized setup for easy deployment and reproducibility
---

## Tech Stack

- Java 17 + Spring Boot 3
- PostgreSQL
- Apache POI (Excel reader)
- Hibernate (JPA)
- H2 (for integration tests)
- Maven
- Docker + Docker Compose

---

## How to Run

### Required

- Java 17
- Docker & Docker Compose

---

### Run with Docker
Make sure app is built and packaged. In project main directory run:
```bash
./mvnw clean install -DskipTests
```

Start the app with Docker Compose
```bash
docker-compose up --build
```
The app will be running on: http://localhost:8080

---

### Run tests
In project main directory run:
```bash
./mvnw clean test
```
That will run both unit and integration tests.

---

## API Endpoints

### Get Swift Code

**GET** `/v1/swift-codes/{swift-code}`  
Returns details of a branch or headquarter by SWIFT code.
Case-insensitive

---

### Get All Swift Codes for Country

**GET** `/v1/swift-codes/country/{iso2}`  
Returns all SWIFT codes for a given ISO-2 country code. Case-insensitive

---

### Create Swift Code

**POST** `/v1/swift-codes`  
Creates a new SWIFT code record (headquarter or branch).

**Request Body:**

```json
{
  "swiftCode": "AAAAUSNYXXX",
  "bankName": "Example Bank",
  "address": "123 Main Street",
  "countryISO2": "US",
  "countryName": "UNITED STATES",
  "isHeadquarter": true
}
```
The address value is optional. Validation will check if countryISO2 match swiftCode 5 and 6 chars and will compare isHeadquarter value with length and 3 last chars of swiftCode. 
Moreover it will check is same country name is saved to provided iso2 code in database and is bank name the same in database for 4 first chars in swiftCode. 

---

### Delete Swift Code

**DELETE** `/v1/swift-codes/{swift-code}`  
Deletes a SWIFT code. If provided swift code is BIC11 it will delete this specific record. If provided swift code is BIC8 it will delete all records associated with it.

---

## Design Decisions

### Database table

To ensure fast and simple querying, I decided to use a single flat table (`swift_code`) instead of multiple normalized tables.

While this introduces some redundancy (e.g., repeated country names or bank names), it allows:

- Simpler structure with no JOINs required
- Better read performance for endpoints
- Easier Excel import and validation

This design works well here, as the dataset is append-only — there is **no update endpoint**, and the only mutation allowed is deletion.  

### Indexing

Two indexes were created for optimal lookup performance:

- `swift_code` — classic B-tree index, to support `LIKE 'XXXX%'` queries on SWIFT codes (used for branch lookups)
- `countryISO2` — B-tree index to support fast filtering by country

For small to medium datasets, B-tree performs well and avoids hash calculation overhead.

If the dataset grows significantly, replacing the index on `countryISO2` with a **hash index** could improve equality-based lookups.  
However, B-tree remains optimal for SWIFT code queries involving `LIKE`, so both index types could coexist in a large-scale version.

### Excel File Assumptions

The data is parsed from a provided Excel spreadsheet on application startup.

The following assumptions were made for simplicity and performance:

- The structure and order of columns are **fixed and known** in advance  
  (e.g. `SWIFT CODE` is always column 2, `BANK NAME` is always column 4, etc.)
- Only the **first worksheet** is considered
- Loading only from one file with provided name

Dynamic column matching or sheet iteration was intentionally skipped to reduce overhead,  
based on the assumption that the spreadsheet is controlled and stable.

### Validation & Error Handling

The application performs extensive validation for all incoming data.

During startup, invalid rows from Excel are skipped with warning logs.  
At runtime, invalid API requests return structured error responses (JSON), using global exception handling.

Validation includes:
- Required fields (excluding address)
- Swift code length (must be 8 or 11 characters)
- Headquarter format (ending with XXX or length 8)
- ISO2 match with SWIFT code (5th and 6th characters)
- Duplicate prevention
- Bank/country consistency check based on DB history

