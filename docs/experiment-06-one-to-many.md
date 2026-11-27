# Experiment 6: One-to-Many Relationship Between Student and Course

## Aim

Model the rule that one student can be enrolled in many courses.

```
One Student ----> Many Courses

Rahul  ->  Enterprise Java
           Database Management Systems
           Python
```

## Step 1: The Course entity

`Course.java` in `com.example.ucs067.studentmanagement`:

```java
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String courseName;

    private int credits;

}
```

Field to column mapping:

| Java field | Database column |
| --- | --- |
| `id` | `id` |
| `courseName` | `course_name` |
| `credits` | `credits` |

`@GeneratedValue(strategy = GenerationType.IDENTITY)` lets MySQL create the id with `AUTO_INCREMENT`, so clients do not send one.

## Step 2: The One-to-Many side on Student

```java
@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Course> courses = new ArrayList<>();
```

| Attribute | Effect |
| --- | --- |
| `mappedBy = "student"` | The relationship is owned by the `student` field in `Course`; `Student` does not store the foreign key |
| `cascade = CascadeType.ALL` | Saving or deleting a student is applied to its courses as well |
| `orphanRemoval = true` | A course removed from the list is deleted from the database |

## Step 3: The Many-to-One side on Course

```java
@ManyToOne
private Student student;
```

with a getter and setter for `student`. The foreign key lives in the `course` table:

```
course
+----+-----------------------------+---------+------------+
| id | course_name                 | credits | student_id |
+----+-----------------------------+---------+------------+
|  1 | Enterprise Java             |       4 |          1 |
|  2 | Database Management Systems |       3 |          1 |
+----+-----------------------------+---------+------------+
```

The key point of this experiment: `@OneToMany` describes the relationship from the Student side, while `@ManyToOne` is the owning side that maintains the foreign key.

## Step 4: CourseRepository

```java
public interface CourseRepository extends JpaRepository<Course, Integer> {

}
```

## Step 5: Controller changes

`CourseRepository` is injected next to `StudentRepository`:

```java
public HelloController(
        StudentRepository studentRepository,
        CourseRepository courseRepository) {
    this.studentRepository = studentRepository;
    this.courseRepository = courseRepository;
}
```

First endpoint, used only to confirm that Hibernate created the new table when the application restarts:

```java
@GetMapping("/courses")
public List<Course> getCourses() {
    return courseRepository.findAll();
}
```

## Step 6: Avoiding infinite JSON recursion

With both sides pointing at each other, converting a student to JSON loops forever:

```
Student -> courses -> Course -> student -> Student -> courses -> ...
```

Jackson's pair of annotations breaks the cycle:

In `Student.java`:

```java
@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference
private List<Course> courses = new ArrayList<>();
```

In `Course.java`:

```java
@ManyToOne
@JsonBackReference
private Student student;
```

`@JsonManagedReference` serializes the list of courses. `@JsonBackReference` leaves the `student` field out of the course JSON. Spring Boot 4 uses Jackson 3, but the annotation package `com.fasterxml.jackson.annotation` is unchanged, so the imports stay the same.

`Student` also gets `getCourses()` and `setCourses()`, which are standard JavaBean accessors that Jackson uses.

## Step 7: Endpoint that creates a course for a student

```java
@PostMapping("/students/{studentId}/courses")
public ResponseEntity<Course> addCourseToStudent(
        @PathVariable int studentId,
        @RequestBody Course course) {
    return studentRepository.findById(studentId)
            .map(student -> {
                course.setStudent(student);
                Course savedCourse = courseRepository.save(course);
                return ResponseEntity.ok(savedCourse);
            })
            .orElse(ResponseEntity.notFound().build());
}
```

Flow:

```
studentId -> findById -> student found?
          -> yes: course.setStudent(student)
          -> courseRepository.save(course)
          -> row stored with student_id
```

The line `course.setStudent(student)` is the important one. `Course` owns the relationship, so setting the student there is what writes the `student_id` value.

## Step 8: Test it

Add a course to student 1:

```
curl -X POST http://localhost:8081/students/1/courses -H "Content-Type: application/json" -d "{\"courseName\":\"Enterprise Java\",\"credits\":4}"
```

Expected response:

```json
{
  "id": 1,
  "courseName": "Enterprise Java",
  "credits": 4
}
```

Add a second course to the same student:

```
curl -X POST http://localhost:8081/students/1/courses -H "Content-Type: application/json" -d "{\"courseName\":\"Database Management Systems\",\"credits\":3}"
```

Check the student in the browser:

```
http://localhost:8081/students/1
```

The response shows Rahul with both courses in the `courses` array. Restart the application and open the same URL: the courses are still there, because they are stored in MySQL.

Using the final application, add `-u admin:admin123` to the commands. The script `scripts/exp06-course-relationship.sh` runs the whole flow, and `CourseRelationshipApiTests` checks assignment, multiple courses, an unknown student, the course list and the cascade delete.

## Concepts learned

- One student to many courses is expressed with `@OneToMany` on one side and `@ManyToOne` on the other.
- The side with `@ManyToOne` owns the foreign key (`student_id`).
- `mappedBy` points the inverse side to the owning field.
- Cascade and orphan removal keep child rows in step with the parent.
- Bidirectional relationships need `@JsonManagedReference` and `@JsonBackReference` to avoid endless JSON.
