# JetBrains Junie
## Spring 6 Rest MVC

### DTO-Based API and MapStruct
This application now separates the Web layer from the Persistence layer using Data Transfer Objects (DTOs):

- Web controllers exchange `BeerDto` objects with clients and never expose JPA entities directly.
- Mappings between the JPA `Beer` entity and `BeerDto` are implemented via MapStruct (`BeerMapper`) with `componentModel=spring` so the mapper is injected as a Spring bean.
- Server-managed fields (`id`, `version`, `createdDate`, `updateDate`) are ignored on create/update mappings and populated by the database/Hibernate.
- Validation is applied on the DTO (`@NotBlank`, `@PositiveOrZero`, `@DecimalMin`) and enforced by annotating controller request bodies with `@Valid`.
- Global exception handling returns RFC 9457 `ProblemDetail` responses for validation errors (400) and not found (404).

Key files:
- `src/main/java/guru/springframework/juniemvc/models/BeerDto.java`
- `src/main/java/guru/springframework/juniemvc/mappers/BeerMapper.java`
- `src/main/java/guru/springframework/juniemvc/services/BeerService.java` (DTO-based signatures)
- `src/main/java/guru/springframework/juniemvc/services/BeerServiceImpl.java`
- `src/main/java/guru/springframework/juniemvc/controllers/BeerController.java`
- `src/main/java/guru/springframework/juniemvc/handlers/GlobalExceptionHandler.java`

Build notes:
- MapStruct and Lombok annotation processors are configured in `pom.xml`.
- OSIV is disabled via `spring.jpa.open-in-view=false` to avoid lazy loading during serialization.

The application is a simple Spring Boot 3 / Spring Framework 6 web application. It is used to help students learn how
to use the Spring Framework. Step by step instructions and detailed explanations can be found within the course.

As you work through the course, please feel free to fork this repository to your out GitHub repo. Most links contain links
to source code changes. If you encounter a problem you can compare your code to the lesson code. [See this link for help with compares](https://github.com/springframeworkguru/spring5webapp/wiki#getting-an-error-but-cannot-find-what-is-different-from-lesson-source-code)

## Spring Framework 6: Beginner to Guru Course Wiki
Got a question about your Spring Framework 6 course? [Checkout these FAQs!](https://github.com/springframeworkguru/spring5webapp/wiki)

## Getting Your Development Environment Setup
### Recommended Versions
| Recommended             | Reference                                                                                                                                                     | Notes                                                                                                                                                                                                                  |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oracle Java 21 JDK      | [Download](https://www.oracle.com/java/technologies/downloads/#java21) | Java 17 or higher is required for Spring Framework 6. Java 21 is recommended for the course.                                                                                                                           |
| IntelliJ 2024 or Higher | [Download](https://www.jetbrains.com/idea/download/)                                                                                                          | Ultimate Edition recommended. Students can get a free 120 trial license [here](https://github.com/springframeworkguru/spring5webapp/wiki/Which-IDE-to-Use%3F#how-do-i-get-the-free-120-day-trial-to-intellij-ultimate) |
| Maven 3.9.6 or higher   | [Download](https://maven.apache.org/download.cgi)                                                                                                             | [Installation Instructions](https://maven.apache.org/install.html)                                                                                                                                                     |
| Gradle 8.7 or higher    | [Download](https://gradle.org/install/)                                                                                                                       |                                                                                                                                                                     |
| Git 2.39 or higher      | [Download](https://git-scm.com/downloads)                                                                                                                     |                                                                                                                                                                                                                        | 
| Git GUI Clients         | [Downloads](https://git-scm.com/downloads/guis)                                                                                                               | Not required. But can be helpful if new to Git. SourceTree is a good option for Mac and Windows users.                                                                                                                 |

## All Spring Framework Guru Courses
### Spring Framework 6
* [Spring Framework 6 - Beginner to Guru](https://www.udemy.com/course/spring-framework-6-beginner-to-guru/?referralCode=2BD0B7B7B6B511D699A9)
* [Spring AI: Beginner to Guru](https://www.udemy.com/course/spring-ai-beginner-to-guru/?referralCode=EF8DB31C723FFC8E2751)
* [Hibernate and Spring Data JPA: Beginner to Guru](https://www.udemy.com/course/hibernate-and-spring-data-jpa-beginner-to-guru/?referralCode=251C4C865302C7B1BB8F)
* [API First Engineering with Spring Boot](https://www.udemy.com/course/api-first-engineering-with-spring-boot/?referralCode=C6DAEE7338215A2CF276)
* [Introduction to Kafka with Spring Boot](https://www.udemy.com/course/introduction-to-kafka-with-spring-boot/?referralCode=15118530CA63AD1AF16D)
* [Spring Security: Beginner to Guru](https://www.udemy.com/course/spring-security-core-beginner-to-guru/?referralCode=306F288EB78688C0F3BC)

### Spring Framework 5
* [Spring Framework 5: Beginner to Guru](https://www.udemy.com/testing-spring-boot-beginner-to-guru/?couponCode=GITHUB_REPO) - Get the most modern and comprehensive course available for the Spring Framework! Join over 17,200 over Guru's in an Slack community exclusive to this course! More than 5,700 students have given this 53 hour course a 5 star review!
* [Spring Boot Microservices with Spring Cloud Beginner to Guru](https://www.udemy.com/course/spring-boot-microservices-with-spring-cloud-beginner-to-guru/?referralCode=6142D427AE53031FEF38) - Master Microservice Architectures Using Spring Boot 2 and Cloud Based Deployments with Spring Cloud and Docker
* [Reactive Programming with Spring Framework 5](https://www.udemy.com/reactive-programming-with-spring-framework-5/?couponCode=GITHUB_REPO_SF5B2G) - Keep your skills razor sharp and take a deep dive into Reactive Programming!
* [Testing Spring Boot: Beginner to Guru](https://www.udemy.com/testing-spring-boot-beginner-to-guru/?couponCode=GITHUB_REPO_SF5B2G) - ** Best Selling Course** Become an expert in testing Java and Spring Applications with JUnit 5, Mockito and much more!

### SQL
* [SQL Beginner to Guru: MySQL Edition](https://www.udemy.com/sql-beginner-to-guru-mysql-edition/?couponCode=GITHUB_REPO_SF5B2G) - SQL is a fundamental must have skill, which employers are looking for. Learn to master SQL on MySQL, the worlds most popular database!

### DevOps
* [Apache Maven: Beginner to Guru](https://www.udemy.com/apache-maven-beginner-to-guru/?couponCode=GITHUB_REPO_SF5B2G) - **Best Selling Course** Take the mystery out of Apache Maven. Learn how to use Maven to build your Java and Spring Boot projects!
* [OpenAPI: Beginner to Guru](https://www.udemy.com/course/openapi-beginner-to-guru/?referralCode=0E7F511C749013CA6AAD) - Master OpenAPI (formerly Swagger) to Create Specifications for Your APIs
* [OpenAPI: Specification With Redocly](https://www.udemy.com/course/openapi-specification-redocly-api-documentation/?referralCode=863C443928D61D9A3831)
* [Docker for Java Developers](https://www.udemy.com/docker-for-java-developers/?couponCode=GITHUB_REPO_SF5B2G) - Best Selling Course on Udemy! Learn how you can supercharge your development by leveraging Docker. Collaborate with other students in a Slack community exclusive to the course!
* [Spring Framework DevOps on AWS](https://www.udemy.com/spring-core-devops-on-aws/?couponCode=GITHUB_REPO_SF5B2G) - Learn how to build and deploy Spring applications on Amazon AWS!
* [Ready for Production with Spring Boot Actuator](https://www.udemy.com/ready-for-production-with-spring-boot-actuator/?couponCode=GITHUB_REPO_SF5B2G) - Learn how to leverage Spring Boot Actuator to monitor your applications running in production.

### Web Development with Spring Framework
* [Mastering Thymeleaf with Spring Boot](https://www.udemy.com/mastering-thymeleaf-with-spring/?couponCode=GITHUB_REPO_SF5B2G) - Once you learn Thymeleaf, you'll never want to go back to using JSPs for web development!


## Connect with Spring Framework Guru
* Spring Framework Guru [Blog](https://springframework.guru/)
* Subscribe to Spring Framework Guru on [YouTube](https://www.youtube.com/channel/UCrXb8NaMPQCQkT8yMP_hSkw)
* Like Spring Framework Guru on [Facebook](https://www.facebook.com/springframeworkguru/)
* Follow Spring Framework Guru on [Twitter](https://twitter.com/spring_guru)
* Connect with John Thompson on [LinkedIn](http://www.linkedin.com/in/springguru)


## Beer Order API
The project implements a Beer Order domain and DTO-based REST API following the included Spring Boot Guidelines.

Base URL
- `/api/v1/orders`

Endpoints
- POST `/api/v1/orders` — Create an order
  - Request: `BeerOrderDto` with `customerRef` and at least one `orderLines` item
  - Response: `201 Created` with `Location` header `/api/v1/orders/{id}` and created body
- GET `/api/v1/orders/{id}` — Fetch a single order
  - Response: `200 OK` with `BeerOrderDto` or `404 Not Found` if missing
- GET `/api/v1/orders` — List orders (paginated)
  - Query params: `page`, `size`, `sort` (default sort by `id`)
  - Response: `200 OK` with a `Page<BeerOrderDto>` JSON
- PUT `/api/v1/orders/{id}` — Full update/replace of an order
  - Response: `200 OK` with updated `BeerOrderDto` or `404 Not Found`
- DELETE `/api/v1/orders/{id}` — Idempotent delete
  - Response: `204 No Content`

DTOs
- `BeerOrderDto` — `id`, `version`, `customerRef`, `orderLines`, `createdDate`, `updateDate`
  - Validation: `@NotBlank customerRef`, `@NotNull @Size(min=1) orderLines`
- `BeerOrderLineDto` — `id`, `version`, `beerId`, `orderQuantity`, `price`, `createdDate`, `updateDate`
  - Validation: `@NotNull beerId`, `@NotNull @Positive orderQuantity`

Mappers
- MapStruct mappers convert between entities and DTOs while ignoring server-managed fields on input.

Transactions
- Service layer methods are annotated with `@Transactional` and `@Transactional(readOnly = true)` where appropriate.

Error Handling
- A global `@RestControllerAdvice` returns RFC 9457 `ProblemDetail` for:
  - Validation errors (400)
  - Not found errors (404)

Notes
- OSIV is disabled: `spring.jpa.open-in-view=false`
- Logging via SLF4J; no sensitive data is logged.
