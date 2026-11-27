# Experiment 4: Database Connection, JPA Entity and Spring Data Repository

## Aim

Replace the in-memory list with a MySQL database. Three ideas are introduced:

1. The database connection
2. The JPA entity (`@Entity`)
3. The Spring Data JPA repository

## Step 1: Check MySQL

```
mysql --version
```

Expected: `mysql  Ver 8.4.x for Win64 on x86_64`. If the command is not recognized, install MySQL Server 8.4 and add its `bin` folder to the `PATH` environment variable.

## Step 2: Connect to MySQL

```
mysql -u root -p
```

Enter the root password chosen during installation. The `mysql>` prompt confirms the login worked.

## Step 3: Create the database

```sql
CREATE DATABASE studentdb;
SHOW DATABASES;
```

`studentdb` must appear in the list. The same statements are in `sql/01-create-database.sql`.

## Step 4: Add the dependencies

Two dependencies were added to `pom.xml`, right after `spring-boot-starter-webmvc`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

| Dependency | Purpose |
| --- | --- |
| `spring-boot-starter-data-jpa` | JPA annotations, Hibernate and the repository support |
| `mysql-connector-j` | JDBC driver that lets Java talk to MySQL |

## Step 5: Configure the connection

`application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/studentdb
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

In the repository these three datasource values are read from environment variables, with the lab values as defaults:

```
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/studentdb}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:YOUR_MYSQL_PASSWORD}
```

That way the real password never has to be committed to GitHub. Set it before starting the application, for example in CMD:

```
set DB_PASSWORD=MySQL@12345
```

| Property | Meaning |
| --- | --- |
| `ddl-auto=update` | Hibernate creates missing tables and columns at startup and keeps existing data |
| `show-sql=true` | Prints the SQL Hibernate runs, useful for learning |

## Step 6: Turn `Student` into an entity

```java
@Entity
@Table(name = "students")
public class Student {

    @Id
    private int id;

    private String name;

    private String email;
}
```

- `@Entity` marks the class as a table-backed object.
- `@Table(name = "students")` fixes the table name.
- `@Id` marks the primary key.

At this stage `@GeneratedValue` was left out deliberately, because the API still accepted the id from the client. Automatic id generation is added in Experiment 6 and finalised in Experiment 8.

## Step 7: Create the repository

Package `com.example.ucs067.studentmanagement.repository`:

```java
public interface StudentRepository extends JpaRepository<Student, Integer> {

}
```

`Student` is the entity the repository manages and `Integer` is the type of its primary key. There is no implementation class. Spring Data JPA generates one at startup, which provides `findAll()`, `findById()`, `save()`, `existsById()`, `deleteById()` and more.

## Step 8: First run, connection check

Before touching the controller, the application was started on its own. The console should show:

- The JDBC URL `jdbc:mysql://localhost:3306/studentdb`
- The MySQL Connector/J driver and the MySQL version
- A `create table students (...)` statement from Hibernate
- `Tomcat started on port 8081`

This confirms that Spring Boot, Hibernate and MySQL are connected before any API code depends on them.

## Step 9: Connect the controller to the repository

The controller no longer holds a list. It receives the repository through constructor injection and delegates to it:

| Operation | Repository call |
| --- | --- |
| GET all | `findAll()` |
| POST | `save(student)` |
| GET by id | `findById(id)` |
| PUT | `findById()` then `save()` |
| DELETE | `existsById()` then `deleteById()` |

```java
private final StudentRepository studentRepository;

public HelloController(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
}

@GetMapping("/students")
public List<Student> getStudents() {
    return studentRepository.findAll();
}

@PostMapping("/students")
public Student addStudent(@RequestBody Student student) {
    return studentRepository.save(student);
}

@DeleteMapping("/students/{id}")
public ResponseEntity<String> deleteStudent(@PathVariable int id) {
    if (!studentRepository.existsById(id)) {
        return ResponseEntity.notFound().build();
    }
    studentRepository.deleteById(id);
    return ResponseEntity.ok("Student deleted successfully");
}
```

`findById` returns an `Optional`, so `.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build())` turns a found record into 200 and a missing one into 404.

## Step 10: Test the persistence

Insert the first student:

```
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"id\":1,\"name\":\"Rahul\",\"email\":\"rahul@example.com\"}"
```

Expected response:

```json
{"id":1,"name":"Rahul","email":"rahul@example.com"}
```

Open `http://localhost:8081/students` in the browser to see the stored record.

Restart check:

1. Stop the application.
2. Start it again.
3. Refresh `http://localhost:8081/students`. Rahul is still there, which the in-memory version could not do.

Confirm in MySQL itself:

```sql
USE studentdb;
SELECT * FROM students;
```

The row for Rahul is listed in the `students` table. These statements are also in `sql/02-verify-tables.sql`.

## Concepts learned

- A JDBC URL, username and password are all Spring Boot needs to open a database connection.
- A JPA entity maps a Java class to a table and its fields to columns.
- `JpaRepository` provides ready-made CRUD methods, so basic database access needs no handwritten SQL.
- `ddl-auto=update` keeps the schema in step with the entities during development.
- Constructor injection is how the controller receives the repository.
- Data now survives an application restart.
