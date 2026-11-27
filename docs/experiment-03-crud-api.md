# Experiment 3: CRUD REST API with an In-Memory List

## Aim

Turn the read-only student endpoint into a full CRUD API. The data is kept in an `ArrayList` for now, so it lives only while the application is running. A database arrives in Experiment 4.

## What changed

| File | Change |
| --- | --- |
| `Student.java` | Default constructor and setters added so Jackson can build a `Student` from JSON |
| `HelloController.java` | In-memory `List<Student>` plus POST, GET by id, PUT and DELETE endpoints |

## Why the model needed changes

Spring converts the JSON body of a request into a Java object with the following steps: create the object with a no-argument constructor, then call a setter for each JSON field. Without those two things, `@RequestBody` cannot populate a `Student`.

```java
public class Student {

    private int id;
    private String name;
    private String email;

    public Student() {
    }

    public Student(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
```

## In-memory storage

```java
private List<Student> students = new ArrayList<>(
        List.of(
                new Student(1, "Rahul", "rahul@example.com"),
                new Student(2, "Priya", "priya@example.com"),
                new Student(3, "Amit", "amit@example.com")
        )
);
```

`List.of(...)` is immutable, so it is wrapped in a new `ArrayList`. That makes the list modifiable and lets the API add, update and remove students.

## Endpoints implemented

| Operation | Method and URL | Annotations used | Result |
| --- | --- | --- | --- |
| Read all | `GET /students` | `@GetMapping` | List of all students |
| Create | `POST /students` | `@PostMapping`, `@RequestBody` | The student that was added |
| Read one | `GET /students/{id}` | `@PathVariable`, `ResponseEntity` | 200 with the student, or 404 |
| Update | `PUT /students/{id}` | `@PutMapping`, `@PathVariable`, `@RequestBody` | 200 with the updated student, or 404 |
| Delete | `DELETE /students/{id}` | `@DeleteMapping`, `@PathVariable` | 200 with a confirmation text, or 404 |

The handler methods at this stage loop through the list and compare ids:

```java
@GetMapping("/students/{id}")
public ResponseEntity<Student> getStudentById(@PathVariable int id) {
    for (Student student : students) {
        if (student.getId() == id) {
            return ResponseEntity.ok(student);
        }
    }
    return ResponseEntity.notFound().build();
}

@PutMapping("/students/{id}")
public ResponseEntity<Student> updateStudent(
        @PathVariable int id,
        @RequestBody Student updatedStudent) {
    for (Student student : students) {
        if (student.getId() == id) {
            student.setName(updatedStudent.getName());
            student.setEmail(updatedStudent.getEmail());
            return ResponseEntity.ok(student);
        }
    }
    return ResponseEntity.notFound().build();
}

@DeleteMapping("/students/{id}")
public ResponseEntity<String> deleteStudent(@PathVariable int id) {
    for (Student student : students) {
        if (student.getId() == id) {
            students.remove(student);
            return ResponseEntity.ok("Student deleted successfully");
        }
    }
    return ResponseEntity.notFound().build();
}
```

Returning inside the loop right after `remove` avoids a `ConcurrentModificationException`, because the loop never continues after the list has been modified.

## How to test (Windows CMD)

Start the application, then run these commands. If the final version of the project is running, Experiment 9 has already added security, so append `-u admin:admin123` to each command. The steps below show the commands as they were written for this experiment.

Create a student:

```
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"id\":4,\"name\":\"Neha\",\"email\":\"neha@example.com\"}"
```

Check that the new student appears:

```
http://localhost:8081/students
```

Update student 2:

```
curl -X PUT http://localhost:8081/students/2 -H "Content-Type: application/json" -d "{\"id\":2,\"name\":\"Priya Sharma\",\"email\":\"priya.sharma@example.com\"}"
```

Expected output:

```json
{
  "id": 2,
  "name": "Priya Sharma",
  "email": "priya.sharma@example.com"
}
```

Delete student 3:

```
curl -X DELETE http://localhost:8081/students/3
```

Expected output:

```
Student deleted successfully
```

Request the deleted student again to confirm the 404 handling:

```
curl -i http://localhost:8081/students/3
```

Expected result: `404 Not Found`. The `-i` flag prints the status line and headers together with the body.

## State of this experiment in the final repository

The final code in `student-management/` no longer uses the `ArrayList`. Experiment 4 replaces it with a repository backed by MySQL, which is why the controller there calls `studentRepository` instead of looping over a list. The endpoints, URLs and responses described above are unchanged. The shell script `scripts/exp03-04-crud.sh` runs the same create, read, update and delete flow against the running application.

## Concepts learned

- `@RequestBody` turns JSON into an object, which needs a default constructor and setters.
- `@PathVariable` binds a part of the URL to a method parameter.
- `ResponseEntity` gives control over the HTTP status code, so a missing record can return 404 instead of an empty 200.
- HTTP verbs map to CRUD: POST to create, GET to read, PUT to update, DELETE to delete.
- Data kept in a Java collection is lost when the application stops, which is the reason a database is needed.
