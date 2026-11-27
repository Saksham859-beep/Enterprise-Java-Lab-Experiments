# Experiment 5: Derived Queries, Pagination and Sorting

## Aim

- Create derived query methods on a Spring Data JPA repository
- Search records without writing SQL
- Return data page by page
- Return data in sorted order
- Expose all of this through REST endpoints

## Part 1: Derived query methods

A derived query method is a repository method whose query Spring Data JPA works out from the method name.

```java
public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByName(String name);

    List<Student> findByNameContaining(String keyword);

    List<Student> findByEmail(String email);

}
```

| Method | What it means | Equivalent SQL |
| --- | --- | --- |
| `findByName(String name)` | Name matches exactly | `SELECT * FROM students WHERE name = 'Rahul';` |
| `findByNameContaining(String keyword)` | Name contains the keyword | `SELECT * FROM students WHERE name LIKE '%Rah%';` |
| `findByEmail(String email)` | Email matches exactly | `SELECT * FROM students WHERE email = '...';` |

Other keywords Spring Data understands in method names: `And`, `Or`, `StartingWith`, `EndingWith`, `GreaterThan`, `LessThan`, `Between`, `OrderBy`, `IgnoreCase`. The name of the method is the query definition, which is why no `@Query` annotation is needed.

### Exposing the queries

```java
@GetMapping("/students/search/name/{name}")
public List<Student> searchByName(@PathVariable String name) {
    return studentRepository.findByName(name);
}

@GetMapping("/students/search/name/contains/{keyword}")
public List<Student> searchByNameContaining(@PathVariable String keyword) {
    return studentRepository.findByNameContaining(keyword);
}

@GetMapping("/students/search/email/{email}")
public List<Student> searchByEmail(@PathVariable String email) {
    return studentRepository.findByEmail(email);
}
```

Request flow:

```
Client -> GET /students/search/name/Rahul
       -> HelloController.searchByName()
       -> studentRepository.findByName("Rahul")
       -> Spring Data JPA generates the query
       -> MySQL
       -> matching Student rows returned as JSON
```

Try them:

```
http://localhost:8081/students/search/name/Rahul
http://localhost:8081/students/search/name/contains/Rah
http://localhost:8081/students/search/email/rahul@example.com
```

Expected response for the first URL:

```json
[
  {
    "id": 1,
    "name": "Rahul",
    "email": "rahul@example.com"
  }
]
```

MySQL uses a case-insensitive collation by default, so `/students/search/name/rahul` also finds Rahul. A case-sensitive database would not, and `findByNameIgnoreCase` would be the fix there.

## Part 2: Pagination

Returning every row from `/students` gets slower as the table grows. Pagination returns a fixed number of rows per request:

| Request | Rows returned (size = 5) |
| --- | --- |
| page 0 | Students 1 to 5 |
| page 1 | Students 6 to 10 |
| page 2 | Students 11 to 15 |

`JpaRepository` already supports this through `findAll(Pageable)`. Spring MVC builds the `Pageable` object from the `page`, `size` and `sort` query parameters.

```java
@GetMapping("/students/page")
public Page<Student> getStudentsPage(Pageable pageable) {
    return studentRepository.findAll(pageable);
}
```

Add a few more students so there is something to page through:

```
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Priya\",\"email\":\"priya@example.com\"}"
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Amit\",\"email\":\"amit@example.com\"}"
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Neha\",\"email\":\"neha@example.com\"}"
curl -X POST http://localhost:8081/students -H "Content-Type: application/json" -d "{\"name\":\"Ravi\",\"email\":\"ravi@example.com\"}"
```

Because the final application has security enabled (Experiment 9) and generates ids automatically (Experiment 8), the commands here send no `id` and would need `-u admin:admin123`. At the time of this experiment the lab sent explicit ids from 2 to 5.

Request the pages:

```
http://localhost:8081/students/page?page=0&size=2
http://localhost:8081/students/page?page=1&size=2
```

Each response holds two students in its `content` array, together with the paging details (`totalElements`, `totalPages`, `number`, `size`). Page 0 and page 1 return different students.

## Part 3: Sorting

```java
@GetMapping("/students/sort/name")
public List<Student> getStudentsSortedByName() {
    return studentRepository.findAll(Sort.by("name"));
}
```

Open `http://localhost:8081/students/sort/name`. With the five sample students the names come back as Amit, Neha, Priya, Rahul, Ravi.

Sorting can also be combined with pagination in one request, which `Pageable` supports natively:

```
http://localhost:8081/students/page?page=0&size=2&sort=name,desc
```

## Automated checks

`StudentQueryApiTests` covers the exact search, the empty result, the partial search, the email search, the first and last pages and the sorted list.

The script `scripts/exp05-search-page-sort.sh` seeds five students and calls each of these endpoints.

## Concepts learned

- Derived query methods turn a method name into a database query.
- `findBy...` keywords such as `Containing` map to SQL conditions like `LIKE`.
- `Pageable` and `Page<T>` give pagination with almost no code.
- `Sort.by("name")` sorts by any entity field.
- A repository method can be exposed directly as a REST endpoint through the controller.
