# Library Management System

A Spring Boot 4 application for running a small library: manage books and members, issue and return books, and track overdue loans and late fines. It has both a web UI (Thymeleaf) and a JSON REST API.

## Run it

Requires Java 21 or newer. Maven is not needed, because the wrapper downloads it.

```bash
./mvnw spring-boot:run        # macOS/Linux/Git Bash
mvnw.cmd spring-boot:run      # Windows cmd/PowerShell
```

Open http://localhost:8080. Sample books, members and loans are loaded on startup.

Other commands:

```bash
./mvnw test                                     # run tests
./mvnw package && java -jar target/library-management-1.0.0.jar
```

The H2 database console is at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:librarydb`, user `sa`, no password). The data is in memory and resets on restart.

## Build and deploy

GitHub Actions ([`.github/workflows/ci-deploy.yml`](.github/workflows/ci-deploy.yml)) runs on every push to `main`:

1. **Build & test:** compiles the app, runs the tests, and saves the jar as a workflow artifact.
2. **Docker image:** builds the [`Dockerfile`](Dockerfile) and publishes it to GitHub Packages as `ghcr.io/<owner>/<repo>:latest`.
3. **Release:** pushing a tag such as `v1.0.0` also creates a GitHub Release with the jar attached.

GitHub itself can't host a Java server (GitHub Pages is static only). To put the app online, run the published image on a container host. For example, on [Render](https://render.com), create a **Web Service**, choose "Deploy an existing image", and enter `ghcr.io/<owner>/<repo>:latest`. The app reads the port from `$PORT`.

Run the image locally:

```bash
docker run -p 8080:8080 ghcr.io/<owner>/<repo>:latest
```

## Features

- **Books:** add, edit, delete, and search by title, author, ISBN or genre. Tracks total and available copies.
- **Members:** register, edit, deactivate and delete members, and view each member's loan history.
- **Loans:** issue a book, return it, and filter by Active, Overdue or Returned.
- **Rules** (set in `application.properties`):
  - `library.loan-period-days` (14)
  - `library.max-active-loans-per-member` (5)
  - `library.fine-per-day` (0.50)
- Books and members can't be deleted while they have active loans. Inactive members can't borrow.

## REST API

| Method | Path | Description |
|---|---|---|
| GET | `/api/books?q=` | List and search books |
| GET/PUT/DELETE | `/api/books/{id}` | Get, update or delete a book |
| POST | `/api/books` | Create a book |
| GET | `/api/members` | List members |
| GET/PUT/DELETE | `/api/members/{id}` | Get, update or delete a member |
| GET | `/api/members/{id}/loans` | A member's loan history |
| POST | `/api/members` | Create a member |
| GET | `/api/loans?status=ACTIVE\|OVERDUE\|RETURNED` | List loans |
| POST | `/api/loans` | Borrow: `{"bookId":1,"memberId":1}` |
| POST | `/api/loans/{id}/return` | Return a book (fine is calculated) |

Errors come back as RFC 9457 problem JSON: 404 when a record is not found, 409 when a library rule is broken, and 400 when validation fails.

## Project layout

```
src/main/java/com/library
├── model/        Book, Member, Loan (JPA entities)
├── repository/   Spring Data JPA repositories
├── service/      Business logic (BookService, MemberService, LoanService, DashboardService)
├── api/          REST controllers + API exception handler
├── web/          Thymeleaf MVC controllers
├── config/       Library properties, sample-data seeder
└── exception/    NotFoundException, LibraryException
src/main/resources/templates   Thymeleaf pages
```

To use MySQL or PostgreSQL, add the JDBC driver dependency and change the `spring.datasource.*` properties.
