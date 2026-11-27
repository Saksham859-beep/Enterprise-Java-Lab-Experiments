# Experiment 7: Input Validation and Centralized Exception Handling

## Aim

1. Reject invalid student data such as a blank name or a malformed email.
2. Return a short, readable error response instead of a long error page or a stack trace.

The existing CRUD APIs and the course relationship stay as they are.

## Step 1: Add the validation starter

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This brings in Jakarta Bean Validation with Hibernate Validator as the implementation.

## Step 2: Annotate the Student fields

```java
@NotBlank(message = "Student name is required")
private String name;

@NotBlank(message = "Student email is required")
@Email(message = "Please provide a valid email address")
private String email;
```

At this point in the lab the id also carried `@Positive(message = "Student ID must be a positive number")`. That rule was dropped in Experiment 8 when ids became auto-generated, because a client no longer sends an id at all.

| Annotation | Rule |
| --- | --- |
| `@NotBlank` | Rejects null, empty and whitespace-only text |
| `@Email` | Requires a valid email format |
| `@Positive` | Requires a number greater than zero |

The `@OneToMany` relationship, the constructors and the accessors are untouched.

## Step 3: Switch validation on with `@Valid`

Annotations on a field do nothing until something asks for validation. In the controller, `@Valid` on the request body does that for the POST and PUT endpoints:

```java
@PostMapping("/students")
public Student addStudent(@Valid @RequestBody Student student) {
    return studentRepository.save(student);
}

@PutMapping("/students/{id}")
public ResponseEntity<Student> updateStudent(
        @PathVariable int id,
        @Valid @RequestBody Student updatedStudent) {
    ...
}
```

When validation fails, Spring throws `MethodArgumentNotValidException` before the method body runs, so nothing invalid reaches the database.

## Step 4: Centralized exception handler

New package `com.example.ucs067.studentmanagement.exception`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

}
```

| Piece | Job |
| --- | --- |
| `@RestControllerAdvice` | Makes the class apply to every REST controller |
| `@ExceptionHandler` | Names the exception this method deals with |
| `getFieldErrors()` | Gives one error per failed field |
| `HttpStatus.BAD_REQUEST` | Sends HTTP 400 |

Keeping this in one class means the controller stays focused on the business flow, and every endpoint reports validation errors in the same format.

## Step 5: Test it

Invalid POST:

```
curl -i -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"id\":10,\"name\":\"\",\"email\":\"not-an-email\"}"
```

Expected: HTTP 400 and

```json
{
  "name": "Student name is required",
  "email": "Please provide a valid email address"
}
```

Valid POST:

```
curl -i -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"id\":10,\"name\":\"Neha Sharma\",\"email\":\"neha@example.com\"}"
```

Expected: HTTP 200 with the saved student.

Invalid PUT:

```
curl -i -X PUT http://localhost:8081/students/10 -H "Content-Type: application/json" -d "{\"id\":10,\"name\":\"\",\"email\":\"invalid-email\"}"
```

Expected: HTTP 400 with the same two messages.

Confirm that the failed update changed nothing:

```
curl http://localhost:8081/students/10
```

Neha Sharma's original details with `neha@example.com` are still returned.

In the final application every request also needs `-u admin:admin123`, ids are generated automatically and the `id` field in the body is not needed. `scripts/exp07-validation.sh` runs this flow and `ValidationApiTests` automates it.

## Concepts learned

- Bean Validation annotations describe the rules next to the data they protect.
- `@Valid` is what triggers those rules for a request body.
- `@RestControllerAdvice` with `@ExceptionHandler` handles errors in one place for the whole API.
- 400 Bad Request is the correct response to invalid client input.
- A rejected request leaves existing data unchanged.
