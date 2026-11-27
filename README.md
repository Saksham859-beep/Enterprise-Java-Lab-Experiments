# Enterprise Java Lab: Exeriments 1-9(Rollno-12411032,Branch-CSE-A)

A Spring Boot REST API for managing students and their courses. It was built step by step across Enterprise Java Lab Experiments 1 to 9, starting from a single `/hello` endpoint and ending with a secured, validated, tested API backed by MySQL.

The finished application lives in [`student-management/`](student-management). Each experiment has its own write-up in [`docs/`](docs) explaining what was added, how it was implemented and how it was tested.

## What the application does

- Creates, reads, updates and deletes students (CRUD)
- Stores data in MySQL through Spring Data JPA
- Searches students by name, partial name and email using derived query methods
- Returns students page by page and sorted by name
- Links each student to many courses (one-to-many)
- Rejects invalid input with clear HTTP 400 messages
- Protects the API with HTTP Basic authentication
- Ships with automated tests that run without MySQL

## Tech stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.0.10 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8.4 (H2 in-memory for tests) |
| Validation | Jakarta Bean Validation |
| Security | Spring Security (HTTP Basic) |
| Build | Maven |
| Testing | JUnit, MockMvc |

## Repository layout

```
enterprise-java-lab/
├── README.md
├── .gitignore
├── docs/
│   ├── 00-setup-experiments-1-2.md
│   ├── experiment-03-crud-api.md
│   ├── experiment-04-jpa-mysql.md
│   ├── experiment-05-repository-queries.md
│   ├── experiment-06-one-to-many.md
│   ├── experiment-07-validation-exception-handling.md
│   ├── experiment-08-api-testing.md
│   └── experiment-09-basic-authentication.md
└── student-management/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/example/ucs067/studentmanagement/
        │   │   ├── StudentManagementApplication.java
        │   │   ├── Student.java
        │   │   ├── Course.java
        │   │   ├── config/SecurityConfig.java
        │   │   ├── controller/HelloController.java
        │   │   ├── exception/GlobalExceptionHandler.java
        │   │   └── repository/
        │   │       ├── StudentRepository.java
        │   │       └── CourseRepository.java
        │   └── resources/application.properties
        └── test/
            ├── java/com/example/ucs067/studentmanagement/
            │   ├── ApiTestSupport.java
            │   ├── StudentManagementApplicationTests.java
            │   ├── StudentCrudApiTests.java
            │   ├── StudentQueryApiTests.java
            │   ├── CourseRelationshipApiTests.java
            │   ├── ValidationApiTests.java
            │   └── SecurityApiTests.java
            └── resources/application-test.properties
```

## Prerequisites

- JDK 21
- Apache Maven 3.9 or newer (or the Maven bundled with IntelliJ IDEA)
- MySQL Server 8.4
- cURL (included with Windows 10 and 11)

Check them with:

```
java -version
mvn -version
mysql --version
curl --version
```

## Running the application

### 1. Create the database

```
mysql -u root -p
```

```sql
CREATE DATABASE studentdb;
SHOW DATABASES;
```

The same statements are in `sql/01-create-database.sql`. The tables are created by Hibernate on the first run, so nothing else needs to be set up.

### 2. Provide the database password

The password is read from an environment variable so it is never stored in the repository.

Windows Command Prompt:

```
set DB_PASSWORD=your_mysql_root_password
```

Linux or macOS:

```
export DB_PASSWORD=your_mysql_root_password
```

Optional overrides if MySQL is not on the default settings:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/studentdb` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `YOUR_MYSQL_PASSWORD` |

Alternatively, replace `YOUR_MYSQL_PASSWORD` directly in `student-management/src/main/resources/application.properties` for local use, and do not commit that change.

### 3. Start the server

```
cd student-management
mvn spring-boot:run
```

In IntelliJ IDEA, open the `student-management` folder as a Maven project and run `StudentManagementApplication`. If the environment variable was set in a terminal, start IntelliJ from that terminal or add `DB_PASSWORD` to the run configuration.

The API listens on `http://localhost:8081`.

### 4. Try it

Everything except `/hello` needs the credentials `admin` / `admin123`.

```
curl http://localhost:8081/hello
curl -u admin:admin123 http://localhost:8081/students
curl -u admin:admin123 -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Rahul\",\"email\":\"rahul@example.com\"}"
```

Those quoting rules are for Windows CMD. On Linux or macOS use single quotes around the JSON instead:

```
curl -u admin:admin123 -X POST http://localhost:8081/students -H "Content-Type: application/json" -d '{"name":"Rahul","email":"rahul@example.com"}'
```

Ready-made scripts for each experiment are in [`scripts/`](scripts). They use `BASE_URL`, `API_USER` and `API_PASSWORD` environment variables if the defaults need changing:

```
bash scripts/exp03-04-crud.sh
bash scripts/exp05-search-page-sort.sh
bash scripts/exp06-course-relationship.sh
bash scripts/exp07-validation.sh
bash scripts/exp09-security.sh
```

## Running the tests

```
cd student-management
mvn test
```

The tests use an in-memory H2 database through the `test` profile, so MySQL does not need to be running and the real data is never touched. Details are in [Experiment 8](docs/experiment-08-api-testing.md).

## API reference

| Method | URL | Auth | Description | Experiment |
| --- | --- | --- | --- | --- |
| GET | `/hello` | Public | Returns `Hello, Spring Boot!` | 2 |
| GET | `/student` | Required | Returns a sample student | 2 |
| GET | `/students` | Required | Lists all students | 3, 4 |
| POST | `/students` | Required | Creates a student (validated) | 3, 4, 7 |
| GET | `/students/{id}` | Required | Returns one student or 404 | 3, 4 |
| PUT | `/students/{id}` | Required | Updates name and email (validated) or 404 | 3, 4, 7 |
| DELETE | `/students/{id}` | Required | Deletes a student or 404 | 3, 4 |
| GET | `/students/search/name/{name}` | Required | Exact name match | 5 |
| GET | `/students/search/name/contains/{keyword}` | Required | Name contains keyword | 5 |
| GET | `/students/search/email/{email}` | Required | Exact email match | 5 |
| GET | `/students/page?page=0&size=2` | Required | Paged list; also accepts `sort=name,desc` | 5 |
| GET | `/students/sort/name` | Required | All students sorted by name | 5 |
| GET | `/courses` | Required | Lists all courses | 6 |
| POST | `/students/{studentId}/courses` | Required | Adds a course to a student or 404 | 6 |

Request body for students:

```json
{
  "name": "Neha Sharma",
  "email": "neha@example.com"
}
```

Request body for courses:

```json
{
  "courseName": "Enterprise Java",
  "credits": 4
}
```

Validation error (HTTP 400):

```json
{
  "name": "Student name is required",
  "email": "Please provide a valid email address"
}
```

## Implementation steps by experiment

| Experiment | Topic | What was implemented | Write-up |
| --- | --- | --- | --- |
| 1 and 2 | Setup and project foundation | Tools installed, Spring Boot project created, `/hello` and `/student` endpoints, port 8081 | [docs](docs/00-setup-experiments-1-2.md) |
| 3 | CRUD REST API | Default constructor and setters on `Student`, in-memory list, POST, GET by id, PUT and DELETE with 404 handling | [docs](docs/experiment-03-crud-api.md) |
| 4 | JPA and MySQL | `studentdb`, JPA and MySQL dependencies, `@Entity`, `StudentRepository`, controller moved from the list to the repository | [docs](docs/experiment-04-jpa-mysql.md) |
| 5 | Repository queries | `findByName`, `findByNameContaining`, `findByEmail`, pagination with `Pageable`, sorting with `Sort` | [docs](docs/experiment-05-repository-queries.md) |
| 6 | One-to-many | `Course` entity, `@OneToMany` and `@ManyToOne`, `CourseRepository`, Jackson back and managed references, course endpoints | [docs](docs/experiment-06-one-to-many.md) |
| 7 | Validation and error handling | `@NotBlank`, `@Email`, `@Valid`, `@RestControllerAdvice` returning 400 | [docs](docs/experiment-07-validation-exception-handling.md) |
| 8 | API testing | cURL test plan, `AUTO_INCREMENT` fix, `Integer` generated id, MockMvc tests | [docs](docs/experiment-08-api-testing.md) |
| 9 | Basic authentication | Spring Security, fixed credentials, `SecurityConfig` with HTTP Basic, public `/hello` | [docs](docs/experiment-09-basic-authentication.md) |

Experiments 10 and 11 cover deployment with Docker and are outside the scope of this repository.

## How the pieces fit together

```
Client (browser or cURL)
        |
        v
Spring Security filter chain   (Experiment 9: HTTP Basic, 401 if not authenticated)
        |
        v
HelloController                (Experiments 2 to 7: all endpoints)
        |   @Valid                         GlobalExceptionHandler -> 400 (Experiment 7)
        v
StudentRepository / CourseRepository   (Experiments 4 to 6: Spring Data JPA)
        |
        v
Hibernate -> MySQL studentdb   (tables: students, course)
```

Database tables:

```
students                          course
+----------------+                +----------------+
| id (PK, auto)  |<------+        | id (PK, auto)  |
| name           |       |        | course_name    |
| email          |       +--------| student_id (FK)|
+----------------+                | credits        |
                                  +----------------+
```

## Key concepts and learning outcomes

- **REST controllers.** `@RestController`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PathVariable` and `@RequestBody` map HTTP requests to Java methods and JSON to objects.
- **HTTP semantics.** Choosing the right verb and status code (200, 400, 401, 404) for each outcome with `ResponseEntity`.
- **JPA entities.** `@Entity`, `@Table`, `@Id` and `@GeneratedValue` map classes to tables and let MySQL generate keys.
- **Spring Data JPA.** `JpaRepository` provides CRUD without SQL, and derived query methods build queries from method names.
- **Pagination and sorting.** `Pageable`, `Page` and `Sort` return large result sets in manageable pieces.
- **Relationships.** One student has many courses; `@ManyToOne` owns the foreign key, `@OneToMany(mappedBy = ...)` is the inverse side, and Jackson annotations prevent infinite recursion.
- **Validation.** Constraint annotations plus `@Valid` keep bad data out of the database.
- **Centralized error handling.** One `@RestControllerAdvice` class gives every endpoint the same error format.
- **API testing.** cURL for manual checks, MockMvc and JUnit for repeatable automated checks.
- **Security basics.** Authentication versus authorization, HTTP Basic, public versus protected endpoints, and why CSRF can be disabled for a stateless API but not for a cookie-based web app.
- **Configuration hygiene.** Secrets come from environment variables, and tests run against their own database.

## Notes on the implementation

- The package name `com.example.ucs067.studentmanagement` follows the lab sheets.
- The code was written to match the lab sheets experiment by experiment. In the final version the controller uses the repository from Experiment 4, ids are generated by the database from Experiment 8, and security from Experiment 9 applies to every endpoint except `/hello`.
- The `@NotBlank` and `@Email` messages from Experiment 7 are kept in the final `Student` class, because the Experiment 8 test asserts them.
- `ddl-auto=update` and `show-sql=true` are convenient for learning. A production system would manage the schema with migrations and turn SQL logging off.
- The admin credentials are for local classroom use only.

## Troubleshooting

| Problem | Likely cause and fix |
| --- | --- |
| `Access denied for user 'root'@'localhost'` on startup | `DB_PASSWORD` is not set or is wrong. Set it and restart the application |
| `Unknown database 'studentdb'` | The database was not created. Run `sql/01-create-database.sql` |
| `Port 8081 was already in use` | Stop the other process or change `server.port` |
| 401 on every request | Add `-u admin:admin123` to the cURL command, or use `/hello` which is public |
| POST works in cURL but fails in CMD with a JSON parse error | Escape the double quotes as `\"` in Windows CMD |
| Id is not auto-generated on an old database | The table was created before `@GeneratedValue` existed. Run `sql/03-exp8-auto-increment-fix.sql`, or drop and recreate `studentdb` |
| Tests fail to start | Make sure Maven can download dependencies and that JDK 21 is the active Java version |

## Author

Name-Saksham Jain.ROLL-NO-12411032.Branch-CSE-A.Enterprise Java Lab coursework.Experiments 3 to 9.
