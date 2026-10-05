

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

### Delete a topic

`DELETE /api/admin/topics`

Deletes a topic and everything that belongs to it.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `topicId` | UUID | Yes | Topic to delete |

**Cascade behavior**

Deleting a topic also permanently deletes its related data:

```
Topic
├── questions
│   └── answers
└── attempts
    └── attempt_questions
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

----------

## Admin: Sections

Base path: `/api/admin/section`

Sections are the groups that topics belong to.

### Create a section

`POST /api/admin/section`

Creates a new section that topics can be grouped into.

**Request body**

```jsonc
{
  "name": "string",        // required, max 50 characters
  "description": "string"  // optional, max 255 characters
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | String | Yes | Max 50 characters |
| `description` | String | No | Max 255 characters |

---

### Update a section

`PATCH /api/admin/section`

Updates the name, description, and active status of an existing section.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `sectionId` | UUID | Yes | Section to update |

**Request body**

```jsonc
{
  "name": "string",
  "description": "string",
  "isActive": true // optional, defaults to true
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | String | Yes | Max 50 characters |
| `description` | String | No | Max 255 characters |
| `isActive` | Boolean | No | Defaults to `true` if omitted |

**Responses**

| Status | Message |
|---|---|
| `200` | Section updated successfully |
| `404` | Section not found |

---

### Delete a section

`DELETE /api/admin/section`

Deletes a section.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `sectionId` | UUID | Yes | Section to delete |

## Authenticated: Sections

### Get section overview

`GET /api/authenticated/section-overview`

Returns the sections and the topics that belong to each one. Used by the frontend for display purposes.

**Response**

```jsonc
{
  "id": "uuid",
  "name": "string",
  "description": "string",
  "topics": [
    {
      "id": "uuid",
      "name": "string",
      "description": "string",
      "topic": "TEST" // TEST | QUIZ | REVIEW | RANDOM_QUESTIONS
    }
  ]
}
```

---

## Student: Attempts

All routes in this section require an authenticated user. Attempts are always tied to the user making the request.

### Response format

Unless noted otherwise, these routes return the standard wrapper:

```jsonc
{
  "status": 200,
  "message": "string",
  "data": {} // route-specific, null on errors
}
```

| Status | Meaning |
|---|---|
| `200` | Success |
| `403` | The attempt does not belong to the authenticated user |
| `404` | The topic, attempt, or attempt question was not found |
| `409` | The request conflicts with the current state (for example, an inactive topic or an already-finalized attempt) |

---

### Create an attempt

`POST /api/student/attempt`

Creates a new attempt on a topic for the authenticated user.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `topicId` | UUID | Yes | Topic to attempt |

**Rules**

1. `topicId` must belong to an existing topic.
2. The topic must be active.

**Response `data`**

```jsonc
{
  "id": "uuid" // the new attempt's id, used for the frontend redirect
}
```

**Status codes:** `200`, `404`

---

### List attempts for a topic

`GET /api/student/attempts`

Returns all of the authenticated user's attempts on a topic, along with the topic itself.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `topicId` | UUID | Yes | Topic to list attempts for |

**Rules**

1. `topicId` must belong to an existing topic.
2. The topic must be active.

**Response `data`**

```jsonc
{
  "attempts": [
    {
      "id": "uuid",
      "totalPoints": 10.0,
      "pointsEarned": 8.5,
      "percentage": 85.0,
      "attemptQuestions": [],
      "finalized": true
    }
  ],
  "topic": {
    "id": "uuid",
    "createdAt": "2026-10-05T12:00:00",
    "name": "string",
    "description": "string",
    "topicType": "TEST",
    "dueDate": "2026-12-03T18:30:00",
    "isActive": true,
    "questions": []
  }
}
```

**Status codes:** `200`, `404`, `409`

---

### Get an attempt

`GET /api/student/attempt`

Returns a single attempt with its questions, the answer choices, the student's submitted answers, and the attempt's topic.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `attemptId` | UUID | Yes | Attempt to fetch |

**Rules**

1. `attemptId` must belong to an existing attempt.
2. The attempt must belong to the authenticated user.

**Response `data`**

```jsonc
{
  "attempt": {
    "id": "uuid",
    "totalPoints": 10.0,
    "pointsEarned": 8.5,
    "percentage": 85.0,
    "attemptQuestions": [
      {
        "id": "uuid",
        "isCorrect": true,
        "submittedAnswerId": "uuid",
        "question": {
          "id": "uuid",
          "points": 1.0,
          "question": "string",
          "imageUrl": "string",
          "answers": [
            {
              "id": "uuid",
              "createdAt": "2026-10-05T12:00:00",
              "answer": "string"
            }
          ]
        }
      }
    ]
  },
  "topic": {
    "id": "uuid",
    "createdAt": "2026-10-05T12:00:00",
    "name": "string",
    "description": "string",
    "topicType": "TEST",
    "dueDate": "2026-12-03T18:30:00",
    "isActive": true,
    "questions": []
  }
}
```

**Status codes:** `200`, `403`, `404`

---

### Record an answer to a question

`PATCH /api/student/attempt/record/question`

Records the answer a student selected for a question in an attempt. For non-test topics, it also returns whether the answer was correct.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `answerId` | UUID | Yes | Topic the attempt belongs to |
| `attemptQuestionId` | UUID | Yes | Topic the attempt belongs to |

**Rules**

1. The attempt must belong to the authenticated user.
2. The attempt must not be finalized. Answers can't be recorded on a finalized attempt.
3. `answerId` must be in the answer set of the attempt question.
4. `attemptQuestionId` must belong to an existing attempt question.
5. Feedback is only returned when the topic type is **not** `TEST`.

**Response `data`**

```jsonc
{
  "answerCorrect": true // boolean for QUIZ, REVIEW, RANDOM_QUESTIONS; null for TEST
}
```

**Status codes:** `200`, `403`, `404`, `409`

---

### Grade a test

`PATCH /api/student/attempt/test/grade`

Submits a test attempt and grades every question at once. Records the points earned and the percentage, and finalizes the attempt.

**Query params**

| Param | Type | Required | Description |
|---|---|---|---|
| `attemptId` | UUID | Yes | Attempt to grade |

**Rules**

1. A test can only be graded once. Regrading is not allowed.
2. The attempt must belong to the authenticated user.
3. The attempt's topic must be of type `TEST`.
4. `attemptId` must belong to an existing attempt.

**Response `data`:** `null`

**Status codes:** `200`, `403`, `404`, `409`