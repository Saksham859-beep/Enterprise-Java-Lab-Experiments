# Enterprise Java Lab: Exeriments 1-9(Rollno-12411032,Branch-CSE-A)

Welcome to my Enterprise Java Lab project! This repository contains a fully functional Spring Boot REST API for managing students and their enrolled courses. 

I built this incrementally across Experiments 1 through 9 for my lab coursework. It started as a basic `/hello` endpoint and evolved into a production-like API featuring a MySQL database, full CRUD operations, data validation, automated testing, and Spring Security.

Everything is housed in the `student-management/` directory, and you can find detailed write-ups for every single experiment in the `docs/` folder.

---

## 🚀 What This API Can Do

*   **Full CRUD Operations:** Create, read, update, and delete student records.
*   **Database Integration:** Persists all data in a MySQL database using Spring Data JPA and Hibernate.
*   **Advanced Searching:** Find students by exact name, partial keyword matches, or email addresses via JPA derived query methods.
*   **Pagination & Sorting:** Handles large datasets by returning students page-by-page and sorted by name.
*   **Relational Data:** Links students to their courses using a one-to-many relationship.
*   **Input Validation:** Validates incoming JSON and rejects bad data with clear, user-friendly HTTP 400 error messages.
*   **Security:** Locks down the endpoints using HTTP Basic Authentication.
*   **Automated Testing:** Includes a suite of tests that run against an in-memory database, keeping the real database untouched.

---

## 🛠️ The Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.10 |
| **Web** | Spring Web MVC |
| **Persistence** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8.4 (H2 in-memory used for testing) |
| **Validation** | Jakarta Bean Validation |
| **Security** | Spring Security (HTTP Basic) |
| **Build Tool** | Maven |
| **Testing** | JUnit, MockMvc |

---

## 📂 Project Structure

I've organized the repository so it's easy to navigate the code, docs, and helper scripts:

```text
enterprise-java-lab/
├── docs/                   # Detailed write-ups for each experiment
├── sql/                    # Database setup and sample data scripts
├── scripts/                # Ready-to-use cURL scripts for testing the API
└── student-management/     # The actual Spring Boot application source code
    ├── pom.xml
    └── src/
        ├── main/java/...   # Controllers, Entities, Repositories, Config
        └── test/java/...   # Automated tests
```

---

## 💻 How to Run It Locally

### 1. Prerequisites
Before you start, make sure you have these installed:
*   JDK 21
*   Maven 3.9+ (or just use IntelliJ's bundled Maven)
*   MySQL Server 8.4
*   cURL (built into Windows 10/11, macOS, and Linux)

### 2. Set Up the Database
Log into MySQL and create the database. You don't need to manually create the tables—Hibernate will do that for you when the app starts.
```bash
mysql -u root -p
```
```sql
CREATE DATABASE studentdb;
```
*(Note: You can also just run the `sql/01-create-database.sql` script).*

### 3. Configure Your Credentials
To keep secrets out of the codebase, the app looks for your MySQL password via an environment variable. 

**Windows (Command Prompt):**
```cmd
set DB_PASSWORD=your_mysql_root_password
```
**Linux / macOS:**
```bash
export DB_PASSWORD=your_mysql_root_password
```
*(By default, the app connects to `jdbc:mysql://localhost:3306/studentdb` with the username `root`. You can override `DB_URL` and `DB_USERNAME` via environment variables if your setup is different).*

### 4. Start the Server
Navigate to the project folder and run it with Maven:
```bash
cd student-management
mvn spring-boot:run
```
*If you are using IntelliJ IDEA, simply open the `student-management` folder as a Maven project, ensure your `DB_PASSWORD` environment variable is set in your Run Configuration, and start `StudentManagementApplication`.*

The API will spin up and listen on `http://localhost:8081`.

### 5. Test It Out
The `/hello` endpoint is public, but everything else requires authentication (Username: `admin`, Password: `admin123`).

Here is how you can interact with it:
```bash
# Check the public endpoint
curl http://localhost:8081/hello

# Get all students (requires auth)
curl -u admin:admin123 http://localhost:8081/students
```

**Adding a student (POST request):**
*If you're on Windows CMD:*
```cmd
curl -u admin:admin123 -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Rahul\",\"email\":\"rahul@example.com\"}"
```
*If you're on Linux or macOS (use single quotes):*
```bash
curl -u admin:admin123 -X POST http://localhost:8081/students -H "Content-Type: application/json" -d '{"name":"Rahul","email":"rahul@example.com"}'
```

*Tip: I've included automated Bash scripts in the `scripts/` folder for each experiment so you don't have to type these out manually!*

---

## 🧪 Running the Tests

You can run the full test suite without even having MySQL turned on. The `test` profile automatically spins up an in-memory H2 database, so your real data is totally safe.

```bash
cd student-management
mvn test
```

---

## 📡 API Reference

Here are all the available endpoints. Endpoints with a request body expect standard JSON. 

| Method | Endpoint | Auth Required? | Description |
| :--- | :--- | :--- | :--- |
| **GET** | `/hello` | No | Returns a simple `Hello, Spring Boot!` |
| **GET** | `/student` | Yes | Returns a hardcoded sample student (Exp 2) |
| **GET** | `/students` | Yes | Lists all students in the database |
| **POST** | `/students` | Yes | Creates a new student (Requires `name`, `email`) |
| **GET** | `/students/{id}` | Yes | Fetches a specific student by their ID (or returns 404) |
| **PUT** | `/students/{id}` | Yes | Updates a student's data (or returns 404) |
| **DELETE** | `/students/{id}` | Yes | Deletes a student from the database |
| **GET** | `/students/search/name/{name}` | Yes | Finds students by exact name match |
| **GET** | `/students/search/name/contains/{keyword}`| Yes | Finds students containing the keyword |
| **GET** | `/students/search/email/{email}` | Yes | Finds a student by exact email match |
| **GET** | `/students/page?page=0&size=2` | Yes | Gets a paginated list of students |
| **GET** | `/students/sort/name` | Yes | Gets all students sorted alphabetically |
| **GET** | `/courses` | Yes | Lists all available courses |
| **POST** | `/students/{id}/courses` | Yes | Enrolls a student in a new course |

**Sample JSON Payloads:**
*   **Student:** `{"name": "Neha Sharma", "email": "neha@example.com"}`
*   **Course:** `{"courseName": "Enterprise Java", "credits": 4}`

---

## 🏗️ Architecture & Flow

Here is a quick look at how a request travels through the application:

```text
Client (Browser / cURL)
        │
        ▼
Spring Security Filter Chain  --> (Blocks unauthorized users with 401)
        │
        ▼
Controllers                   --> (Handles routing. If @Valid fails -> GlobalExceptionHandler -> 400 Bad Request)
        │
        ▼
Repositories                  --> (Spring Data JPA translates Java to SQL)
        │
        ▼
Hibernate & MySQL             --> (Reads/Writes to `students` and `course` tables)
```

---

## 📚 The Lab Journey (Experiments 1-9)

This project was built strictly following the lab sheets. Here is how it evolved:

| Exp | Topic | What I built |
| :--- | :--- | :--- |
| **1 & 2** | Setup & Foundation | Installed tools, generated the Spring project, created `/hello` on port 8081. |
| **3** | CRUD REST API | Built the controller with in-memory data, handled GET, POST, PUT, DELETE and 404s. |
| **4** | JPA & MySQL | Hooked up MySQL, mapped the `Student` entity to a table, and implemented `StudentRepository`. |
| **5** | Repository Queries | Added custom search methods, pagination (`Pageable`), and sorting (`Sort`). |
| **6** | One-to-Many | Created the `Course` entity, mapped foreign keys, and handled JSON serialization to prevent infinite recursion. |
| **7** | Validation & Errors | Added `@NotBlank`/`@Email`, intercepted bad data with `@Valid`, and standardized errors via `@RestControllerAdvice`. |
| **8** | API Testing | Fixed database auto-incrementing issues, wrote MockMvc tests, and set up H2 for isolated testing. |
| **9** | Security | Secured the app with Spring Security, set up HTTP Basic auth, and configured public vs. private routes. |

---

## 🧠 Key Concepts & Learning Outcomes

Building this project taught me how real-world Java backends operate. Some major takeaways include:
*   **RESTful Design:** Properly mapping HTTP methods (GET, POST, PUT, DELETE) and utilizing accurate status codes via `ResponseEntity`.
*   **Object-Relational Mapping (ORM):** Using JPA/Hibernate annotations (`@Entity`, `@Table`, `@Id`) to let Java objects talk to relational tables without writing raw SQL.
*   **Spring Data Magic:** Leveraging `JpaRepository` to get automatic CRUD functionality, and writing derived query methods (like `findByNameContaining`).
*   **Managing Relationships:** Implementing `@OneToMany` and `@ManyToOne` relationships, and understanding who "owns" the foreign key.
*   **Data Integrity:** Keeping garbage data out of the database using Jakarta Bean Validation.
*   **Global Exception Handling:** Using `@RestControllerAdvice` so the client gets a clean, predictable JSON error instead of an ugly Java stack trace.
*   **Automated Testing:** Realizing the value of MockMvc to programmatically test endpoints and assert behaviors. 

---

## 🛠️ Troubleshooting Guide

Running into issues? Here are the most common culprits:

| The Problem | How to Fix It |
| :--- | :--- |
| `Access denied for user 'root'@'localhost'` | You forgot to set the `DB_PASSWORD` environment variable, or it's typed incorrectly. Set it and restart. |
| `Unknown database 'studentdb'` | MySQL is running, but the database doesn't exist. Run the SQL script from Step 2. |
| `Port 8081 was already in use` | Another process is hogging the port. Kill it, or change `server.port` in the properties file. |
| **I keep getting a 401 Unauthorized** | The API is secured! Pass the credentials with `-u admin:admin123` in cURL. |
| **POST works in cURL (Linux) but fails in CMD** | Windows CMD is picky about JSON quotes. Escape them like `\"name\":\"John\"`. |
| **IDs aren't auto-generating properly** | If you're using an older version of the table from before Exp 8, drop the tables and let Hibernate recreate them, or run `sql/03-exp8-auto-increment-fix.sql`. |
