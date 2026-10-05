

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

# API Route Documentation

## Admin: Topics

Base path: `/api/admin/topics`

### Create a topic

`POST /api/admin/topics`

Creates a new topic entity.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `sectionId` | UUID | Yes | Section the topic belongs to |

**Request body**

```jsonc
{
  "topic": {
    "name": "string",
    "description": "string",
    "topicType": "TEST", // TEST | QUIZ | REVIEW | RANDOM_QUESTIONS
    "dueDate": "2026-12-03T18:30:00",
    "isActive": true,
    "questionPoolSize": 1, // optional, defaults to 1
    "sectionId": "uuid"
  },
  "questions": [
    {
      "id": "uuid", // optional, see note below
      "points": 1.0,
      "question": "string",
      "answer": "string",
      "imageUrl": "string", // optional
      "answers": [
        {
          "answer": "string",
          "isCorrect": true
        }
      ]
    }
  ]
}
```

---

### Update a topic

`PATCH /api/admin/topics`

Updates an existing topic.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `topicId` | UUID | Yes | Topic to update |

**Request body**

```jsonc
{
  "topic": {
    "name": "string",
    "description": "string",
    "topicType": "TEST", // TEST | QUIZ | REVIEW | RANDOM_QUESTIONS
    "dueDate": "2026-12-03T18:30:00",
    "isActive": true,
    "questionPoolSize": 1, // optional, defaults to 1
    "sectionId": "uuid"
  },
  "questions": [
    {
      "id": "uuid", // optional, see note below
      "points": 1.0,
      "question": "string",
      "answer": "string",
      "imageUrl": "string", // optional
      "answers": [
        {
          "id": "uuid", // optional, see note below
          "answer": "string",
          "isCorrect": true
        }
      ]
    }
  ]
}
```

---

### Field reference

**`topic`**

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | String | Yes | |
| `description` | String | Yes | |
| `topicType` | Enum | Yes | `TEST`, `QUIZ`, `REVIEW`, `RANDOM_QUESTIONS` |
| `dueDate` | ISO 8601 datetime | Yes | e.g. `2026-12-03T18:30:00` |
| `isActive` | Boolean | Yes | |
| `questionPoolSize` | Integer | No | Defaults to `1` |
| `sectionId` | UUID | Yes | |

**`questions[]`**

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID | No | If omitted, the question is deleted and reinserted |
| `points` | Decimal | Yes | |
| `question` | String | Yes | |
| `answer` | String | Yes | |
| `imageUrl` | String | No | |
| `answers` | Array | Yes | See below |

**`questions[].answers[]`**

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | String | No (PATCH only) | If omitted, the answer is deleted and reinserted |
| `answer` | String | Yes | |
| `isCorrect` | Boolean | Yes | |