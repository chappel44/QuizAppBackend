

## Springboot quiz app backend

### Technologies used in development
-   Java
-   Spring boot
-   Lombok
-   JPA Hibernate
-   Spring security
-   Supabase
-   Postgres
-   JWT

----------

### Motivation
This project is a backend to get me started with learning springboot. I am building this as a project for a friend who needs to give his students practice assessments. Assessments are divided into 3 categories (quizzes, tests, and a random sample of questions. 

----------

### Important Tips

- Routes under the prefix /api/admin are admin only actions such as creating, deleting, and updating quizzes.
- Routes under the prefix /api/student are meant to give students a way to grade their test submissions and grade individual questions.
- There are 2 different levels of authorization one for admin and one for students who are able to review topics, submit tests and receive a grading on how they did on the assessment

