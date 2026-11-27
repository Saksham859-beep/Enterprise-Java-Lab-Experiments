# Experiments 1 and 2: Setup and Project Foundation

These two experiments do not add business features. They prepare the machine and create the Spring Boot project that Experiments 3 to 9 build on. They are written up here so the repository is complete from the first step.

## Tools installed

| Tool | Version used | Why it is needed |
| --- | --- | --- |
| JDK | 21 | Compiles and runs the application |
| Apache Maven | 3.9+ | Builds the project and downloads dependencies |
| IntelliJ IDEA | Community or Ultimate | IDE used in the lab |
| MySQL Server | 8.4 | Database used from Experiment 4 onward |
| Google Chrome | Latest | Quick GET requests and the browser login check in Experiment 9 |
| cURL | Bundled with Windows 10/11 | Sending POST, PUT and DELETE requests |

Check the installs from a terminal:

```
java -version
mvn -version
mysql --version
curl --version
```

## Project created

The project was generated with Spring Initializr using these settings:

| Setting | Value |
| --- | --- |
| Build tool | Maven |
| Language | Java 21 |
| Spring Boot | 4.0.x |
| Group | com.example.ucs067 |
| Artifact | student-management |
| Package | com.example.ucs067.studentmanagement |
| Packaging | Jar |
| Initial dependency | Spring Web (`spring-boot-starter-webmvc`) |

## Base configuration

`application.properties` starts with only two lines:

```
spring.application.name=student-management
server.port=8081
```

Port 8081 is used so the application does not clash with other services that already occupy 8080.

## First endpoints

The very first controller exposes two read-only endpoints:

| Method | URL | Response |
| --- | --- | --- |
| GET | `/hello` | `Hello, Spring Boot!` |
| GET | `/student` | A single hard-coded student as JSON |

Both endpoints are still present in the final application. `/hello` is also the endpoint that stays public once Spring Security is switched on in Experiment 9.

## What these experiments taught

- A Spring Boot application starts from a single class annotated with `@SpringBootApplication`.
- `@RestController` and `@GetMapping` are enough to publish an HTTP endpoint that returns text or JSON.
- Jackson converts Java objects to JSON automatically, so no manual serialization code is needed.
- The Maven `pom.xml` is the single place where dependencies and the Java version are declared.
