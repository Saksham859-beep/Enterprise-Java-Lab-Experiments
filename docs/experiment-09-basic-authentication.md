# Experiment 9: Basic Authentication with Spring Security

## Aim

Protect the API so that only users who provide a valid username and password can use it.

## Step 1: Add Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Reload Maven after saving `pom.xml`.

## Step 2: Restart and observe

With only the dependency added and no configuration, Spring Boot secures every endpoint by default. The console prints a line like:

```
Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Opening `http://localhost:8081/students` in Chrome no longer shows the students. The user is asked to sign in first. This is the first security concept:

> Authentication verifies who the user is before access to protected resources is allowed.

Flow before and after:

```
Before:  Browser -> Student API -> Student records
After:   Browser -> Authentication -> Student API -> Student records
```

## Step 3: Log in with the default credentials

| Field | Value |
| --- | --- |
| Username | `user` |
| Password | The generated password from the console |

After signing in, the student records are shown again. That password changes on every start, which is awkward for repeated testing.

## Step 4: Fixed username and password

Two lines at the bottom of `application.properties`:

```
spring.security.user.name=admin
spring.security.user.password=admin123
```

Restart the application. Use a new Incognito window, or visit `http://localhost:8081/logout` first, so the earlier session is not reused. Then sign in with `admin` and `admin123`.

These credentials are for local classroom use only. A deployed application must never use them, and real passwords should come from environment variables or a secrets store.

## Step 5: SecurityConfig

New package `com.example.ucs067.studentmanagement.config` with this class:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.GET, "/hello"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        return http.build();
    }

}
```

| Line | What it does |
| --- | --- |
| `csrf(csrf -> csrf.disable())` | Turns off CSRF protection. That is acceptable for a stateless API that uses HTTP Basic and no cookie session, but must not be copied into a browser application that relies on session cookies |
| `requestMatchers(HttpMethod.GET, "/hello").permitAll()` | Leaves `/hello` public so protected and unprotected endpoints can be compared |
| `anyRequest().authenticated()` | Everything else needs a logged-in user |
| `httpBasic(withDefaults())` | Accepts credentials in the HTTP `Authorization: Basic ...` header, which is what cURL and API clients send |

No `UserDetailsService` bean is defined. Spring Boot builds an in-memory user from the `spring.security.user.*` properties, which is why `admin` and `admin123` from Step 4 keep working.

Spring Security was added without changing a single controller, entity or repository. Security sits in front of them as a filter chain.

Once `httpBasic` is the only login mechanism, a browser shows the browser's own username and password prompt for a protected URL instead of Spring's HTML login form. The form seen in Steps 2 to 4 is part of Spring Security's default setup, which this configuration replaces.

## Step 6: Test with cURL

Public endpoint:

```
curl -i http://localhost:8081/hello
```

Expected: `200 OK` with `Hello, Spring Boot!`.

Protected endpoint without credentials:

```
curl -i http://localhost:8081/students
```

Expected: `401 Unauthorized`.

Protected endpoint with correct credentials:

```
curl -i -u admin:admin123 http://localhost:8081/students
```

Expected: `200 OK` followed by the student records.

Protected endpoint with a wrong password:

```
curl -i -u admin:wrong123 http://localhost:8081/students
```

Expected: `401 Unauthorized`.

`scripts/exp09-security.sh` runs these checks, and `SecurityApiTests` verifies the same outcomes automatically, plus the courses endpoint, an unknown user and write requests without credentials.

## Resulting behavior

| Endpoint | Access |
| --- | --- |
| `GET /hello` | Public |
| `/students` and every sub-path | Requires authentication |
| `/courses` | Requires authentication |
| `admin` / `admin123` | Access granted |
| Any other credentials | 401 Unauthorized |

## Concepts learned

- Adding `spring-boot-starter-security` secures the whole application immediately.
- Authentication means confirming identity, here with a username and password.
- Basic authentication sends the credentials with every request, which is why it must only be used over HTTPS outside a classroom.
- A `SecurityFilterChain` bean declares which URLs are public and which are protected.
- Credentials can be configured through properties without changing application code.
- `401 Unauthorized` is the status for missing or invalid credentials.
