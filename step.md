# 코드런 프로젝트 구현 가이드 (조성민 담당)

## 프로젝트 개요

교육용 실시간 Web IDE — 멘토가 학습자 코드를 실시간으로 보면서 하이라이팅/코멘트로 피드백을 주는 서비스

- **패키지**: `com.ide.project`
- **Java**: 21
- **Spring Boot**: 3.3.0
- **DB**: PostgreSQL
- **빌드**: Gradle (Groovy DSL)

---

## 데이터베이스 스키마

```sql
CREATE TABLE users (
    id                BIGSERIAL     PRIMARY KEY,
    email             VARCHAR(100)  UNIQUE,
    password          VARCHAR(255),
    name              VARCHAR(50)   NOT NULL,
    nickname          VARCHAR(50)   NOT NULL,
    role              VARCHAR(20)   NOT NULL CHECK (role IN ('MENTOR', 'STUDENT')),
    profile_image_url VARCHAR(500),
    login_type        VARCHAR(20)   NOT NULL CHECK (login_type IN ('LOCAL', 'SOCIAL')),
    email_verified    BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE spaces (
    id          BIGSERIAL    PRIMARY KEY,
    mentor_id   BIGINT       NOT NULL REFERENCES users(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_public   BOOLEAN      NOT NULL DEFAULT FALSE,
    invite_code VARCHAR(20)  UNIQUE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE space_member (
    id        BIGSERIAL PRIMARY KEY,
    space_id  BIGINT    NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    user_id   BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (space_id, user_id)
);

CREATE TABLE problems (
    id              BIGSERIAL    PRIMARY KEY,
    space_id        BIGINT       NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    created_by      BIGINT       NOT NULL REFERENCES users(id),
    problem_bank_id BIGINT       REFERENCES problem_bank(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL,
    language        VARCHAR(20)  NOT NULL,
    starter_code    TEXT,
    is_published    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE timers (
    id               BIGSERIAL   PRIMARY KEY,
    space_id         BIGINT      NOT NULL REFERENCES spaces(id),
    created_by       BIGINT      NOT NULL REFERENCES users(id),
    duration_seconds INT         NOT NULL,
    started_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at       TIMESTAMP   NOT NULL,
    status           VARCHAR(10) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'STOPPED', 'EXPIRED')),
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- submissions 테이블 (정겨운 담당 - 구조 확정 후 FK 연결 예정)
-- submissions.id 를 feedbacks.submission_id 에서 참조함

CREATE TABLE feedbacks (
    id            BIGSERIAL   PRIMARY KEY,
    submission_id BIGINT      NOT NULL,  -- REFERENCES submissions(id) 추후 추가
    mentor_id     BIGINT      NOT NULL REFERENCES users(id),
    type          VARCHAR(20) NOT NULL CHECK (type IN ('COMMENT', 'HIGHLIGHT')),
    content       TEXT,
    start_line    INT,
    end_line      INT,
    color         VARCHAR(10) CHECK (color IN ('YELLOW', 'RED', 'GREEN', 'BLUE')),
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);
```

---

## 공통 응답 구조

```json
{
  "status": 200,
  "code": "API_STATUS_CODE",
  "message": "설명 메시지",
  "data": {}
}
```

## 공통 요청 헤더

```
Content-Type: application/json
Authorization: Bearer {accessToken}  // 인증 필요한 경우
```

### ApiResponse 공통 래퍼 클래스 (이미 구현됨)

```java
ApiResponse.ok(data)
ApiResponse.ok(message, data)
ApiResponse.ok(message)
ApiResponse.fail(message)
```

---

## 구현 순서 및 API 명세서

---

## STEP 1 — 타이머 API

> `timers` 테이블만 있으면 되고 외부 의존 없음. 가장 먼저 구현.

**구현할 파일**

```
domain/timer/
├── controller/TimerController.java
├── service/TimerService.java
├── repository/TimerRepository.java
├── entity/Timer.java
└── dto/
    ├── request/TimerStartRequest.java
    └── response/TimerResponse.java
```

### 1-1. 타이머 시작

```
POST /api/v1/timer/start
인증 필요 (MENTOR 전용)
```

**요청 본문**

| 필드             | 타입    | 필수 | 설명                  |
| ---------------- | ------- | ---- | --------------------- |
| room_id          | Long    | ✅   | 워크스페이스 룸 ID    |
| duration_seconds | Integer | ✅   | 타이머 시간 (초 단위) |

```json
{
  "room_id": 1,
  "duration_seconds": 1800
}
```

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "TIMER_STARTED",
  "message": "타이머가 시작되었습니다.",
  "data": {
    "timer_id": 5,
    "room_id": 1,
    "duration_seconds": 1800,
    "started_at": "2025-05-11T13:00:00Z",
    "expires_at": "2025-05-11T13:30:00Z",
    "status": "RUNNING"
  }
}
```

**응답 - 실패**

| HTTP | 에러 코드             | 메시지                            | 원인                  |
| ---- | --------------------- | --------------------------------- | --------------------- |
| 400  | TIMER_ALREADY_RUNNING | 이미 실행 중인 타이머가 있습니다. | 중복 시작 시도        |
| 403  | FORBIDDEN             | 권한이 없습니다.                  | MENTOR가 아닌 경우    |
| 404  | ROOM_NOT_FOUND        | 워크스페이스를 찾을 수 없습니다.  | 존재하지 않는 room_id |

---

### 1-2. 타이머 종료

```
POST /api/v1/timer/{roomId}/stop
인증 필요 (MENTOR 전용)
```

**Path Variable**

| 파라미터 | 타입 | 필수 | 설명         |
| -------- | ---- | ---- | ------------ |
| roomId   | Long | ✅   | 종료할 룸 ID |

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "TIMER_STOPPED",
  "message": "타이머가 종료되었습니다.",
  "data": {
    "timer_id": 5,
    "status": "STOPPED",
    "stopped_at": "2025-05-11T13:15:00Z"
  }
}
```

**응답 - 실패**

| HTTP | 에러 코드       | 메시지                       | 원인               |
| ---- | --------------- | ---------------------------- | ------------------ |
| 403  | FORBIDDEN       | 권한이 없습니다.             | MENTOR가 아닌 경우 |
| 404  | TIMER_NOT_FOUND | 실행 중인 타이머가 없습니다. | 타이머 없음        |

---

### 1-3. 타이머 조회

```
GET /api/v1/timer?room_id={roomId}
인증 필요
```

**Query Parameter**

| 파라미터 | 타입 | 필수 | 설명         |
| -------- | ---- | ---- | ------------ |
| room_id  | Long | ✅   | 조회할 룸 ID |

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "TIMER_FOUND",
  "message": "타이머 정보를 조회했습니다.",
  "data": {
    "timer_id": 5,
    "duration_seconds": 1800,
    "remaining_seconds": 900,
    "started_at": "2025-05-11T13:00:00Z",
    "expires_at": "2025-05-11T13:30:00Z",
    "status": "RUNNING"
  }
}
```

**status 값**

| 값      | 설명             |
| ------- | ---------------- |
| RUNNING | 진행 중          |
| STOPPED | 멘토가 강제 종료 |
| EXPIRED | 시간 만료        |

---

## STEP 2 — 문제 제출 학생 리스트 API

> `submissions` 테이블 JOIN 조회만 하면 되는 단순 조회 API  
> ⚠️ 정겨운 씨한테 `submissions` 테이블 구조 확인 후 진행

**구현할 파일**

```
domain/submission/
└── controller/SubmissionListController.java
```

### 2-1. 제출 학생 목록 조회

```
GET /api/v1/questions/{questionId}/submissions
인증 필요 (MENTOR 전용)
```

**Path Variable**

| 파라미터   | 타입 | 필수 | 설명           |
| ---------- | ---- | ---- | -------------- |
| questionId | Long | ✅   | 조회할 문제 ID |

**Query Parameter**

| 파라미터 | 타입   | 필수 | 설명                                       |
| -------- | ------ | ---- | ------------------------------------------ |
| status   | String | 선택 | SUBMITTED / PASSED / FAILED (생략 시 전체) |

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "SUBMISSION_LIST_SUCCESS",
  "message": "제출 학생 목록을 조회했습니다.",
  "data": {
    "question_id": 3,
    "total_count": 2,
    "submissions": [
      {
        "submission_id": 1,
        "student_id": 42,
        "nickname": "겨운",
        "status": "PASSED",
        "submitted_at": "2025-05-11T13:10:00Z",
        "has_feedback": false
      },
      {
        "submission_id": 2,
        "student_id": 43,
        "nickname": "성민",
        "status": "SUBMITTED",
        "submitted_at": "2025-05-11T13:20:00Z",
        "has_feedback": false
      }
    ]
  }
}
```

**응답 - 실패**

| HTTP | 에러 코드          | 메시지                   | 원인                     |
| ---- | ------------------ | ------------------------ | ------------------------ |
| 403  | FORBIDDEN          | 권한이 없습니다.         | MENTOR가 아닌 경우       |
| 404  | QUESTION_NOT_FOUND | 문제를 찾을 수 없습니다. | 존재하지 않는 questionId |

---

## STEP 3 — 피드백 API

> COMMENT / HIGHLIGHT 두 타입 처리, CRUD 전체 구현  
> ⚠️ `submissions` 테이블 확정 후 FK 연결

**구현할 파일**

```
domain/feedback/
├── controller/FeedbackController.java
├── service/FeedbackService.java
├── repository/FeedbackRepository.java
├── entity/Feedback.java
└── dto/
    ├── request/CommentCreateRequest.java
    ├── request/HighlightCreateRequest.java
    ├── request/FeedbackUpdateRequest.java
    └── response/FeedbackResponse.java
```

### 3-1. 전체 코멘트 작성

```
POST /api/v1/feedback/comments
인증 필요 (MENTOR 전용)
```

**요청 본문**

| 필드          | 타입   | 필수 | 설명                      |
| ------------- | ------ | ---- | ------------------------- |
| submission_id | Long   | ✅   | 제출 ID                   |
| content       | String | ✅   | 코멘트 내용 (최대 1000자) |

```json
{
  "submission_id": 1,
  "content": "전반적으로 잘 작성했지만 변수명 개선이 필요합니다."
}
```

**응답 - 성공 (201)**

```json
{
  "status": 201,
  "code": "FEEDBACK_CREATED",
  "message": "피드백이 등록되었습니다.",
  "data": {
    "feedback_id": 10,
    "submission_id": 1,
    "type": "COMMENT",
    "content": "전반적으로 잘 작성했지만 변수명 개선이 필요합니다.",
    "created_by": "세현",
    "created_at": "2025-05-11T13:00:00Z"
  }
}
```

**응답 - 실패**

| HTTP | 에러 코드            | 메시지                        | 원인                        |
| ---- | -------------------- | ----------------------------- | --------------------------- |
| 400  | INVALID_INPUT        | 입력값이 올바르지 않습니다.   | 필수값 누락                 |
| 403  | FORBIDDEN            | 권한이 없습니다.              | MENTOR가 아닌 경우          |
| 404  | SUBMISSION_NOT_FOUND | 제출 정보를 찾을 수 없습니다. | 존재하지 않는 submission_id |

---

### 3-2. 라인 하이라이팅 + 코멘트 작성

```
POST /api/v1/feedback/highlights
인증 필요 (MENTOR 전용)
```

**요청 본문**

| 필드          | 타입    | 필수 | 설명                        |
| ------------- | ------- | ---- | --------------------------- |
| submission_id | Long    | ✅   | 제출 ID                     |
| start_line    | Integer | ✅   | 하이라이팅 시작 라인        |
| end_line      | Integer | ✅   | 하이라이팅 종료 라인        |
| color         | String  | ✅   | YELLOW / RED / GREEN / BLUE |
| content       | String  | 선택 | 라인 코멘트 내용            |

```json
{
  "submission_id": 1,
  "start_line": 5,
  "end_line": 8,
  "color": "YELLOW",
  "content": "이 부분 로직을 개선해보세요."
}
```

**응답 - 성공 (201)**

```json
{
  "status": 201,
  "code": "FEEDBACK_CREATED",
  "message": "피드백이 등록되었습니다.",
  "data": {
    "feedback_id": 11,
    "submission_id": 1,
    "type": "HIGHLIGHT",
    "start_line": 5,
    "end_line": 8,
    "color": "YELLOW",
    "content": "이 부분 로직을 개선해보세요.",
    "created_by": "세현",
    "created_at": "2025-05-11T13:00:00Z"
  }
}
```

**응답 - 실패**

| HTTP | 에러 코드            | 메시지                         | 원인                        |
| ---- | -------------------- | ------------------------------ | --------------------------- |
| 400  | INVALID_LINE_RANGE   | 라인 범위가 올바르지 않습니다. | start_line > end_line       |
| 400  | INVALID_COLOR        | 유효하지 않은 색상입니다.      | 허용 외 color 값            |
| 403  | FORBIDDEN            | 권한이 없습니다.               | MENTOR가 아닌 경우          |
| 404  | SUBMISSION_NOT_FOUND | 제출 정보를 찾을 수 없습니다.  | 존재하지 않는 submission_id |

---

### 3-3. 피드백 목록 조회

```
GET /api/v1/feedback?submission_id={submissionId}
인증 필요
```

**Query Parameter**

| 파라미터      | 타입 | 필수 | 설명           |
| ------------- | ---- | ---- | -------------- |
| submission_id | Long | ✅   | 조회할 제출 ID |

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "FEEDBACK_LIST_SUCCESS",
  "message": "피드백 목록을 조회했습니다.",
  "data": [
    {
      "feedback_id": 10,
      "type": "COMMENT",
      "content": "전반적으로 잘 작성했습니다.",
      "created_by": "세현",
      "created_at": "2025-05-11T13:00:00Z"
    },
    {
      "feedback_id": 11,
      "type": "HIGHLIGHT",
      "start_line": 5,
      "end_line": 8,
      "color": "YELLOW",
      "content": "이 부분 개선 필요합니다.",
      "created_by": "세현",
      "created_at": "2025-05-11T13:05:00Z"
    }
  ]
}
```

---

### 3-4. 피드백 수정

```
PUT /api/v1/feedback/{feedbackId}
인증 필요
```

**Path Variable**

| 파라미터   | 타입 | 필수 | 설명             |
| ---------- | ---- | ---- | ---------------- |
| feedbackId | Long | ✅   | 수정할 피드백 ID |

**요청 본문**

```json
{
  "content": "수정된 피드백 내용입니다."
}
```

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "FEEDBACK_UPDATED",
  "message": "피드백이 수정되었습니다.",
  "data": {}
}
```

**응답 - 실패**

| HTTP | 에러 코드          | 메시지                     | 원인                     |
| ---- | ------------------ | -------------------------- | ------------------------ |
| 403  | FORBIDDEN          | 권한이 없습니다.           | 작성자 본인이 아닌 경우  |
| 404  | FEEDBACK_NOT_FOUND | 피드백을 찾을 수 없습니다. | 존재하지 않는 feedbackId |

---

### 3-5. 피드백 삭제

```
DELETE /api/v1/feedback/{feedbackId}
인증 필요
```

**Path Variable**

| 파라미터   | 타입 | 필수 | 설명             |
| ---------- | ---- | ---- | ---------------- |
| feedbackId | Long | ✅   | 삭제할 피드백 ID |

**응답 - 성공 (200)**

```json
{
  "status": 200,
  "code": "FEEDBACK_DELETED",
  "message": "피드백이 삭제되었습니다.",
  "data": null
}
```

---

## STEP 4 — 자 (마지막, 독립적)

> 위 3개 기능과 완전히 독립적 → 나중에 별도로 구현  
> `space_sessions` 테이블 추가 필요  
> `build.gradle`에 의존성 추가 필요: `implementation 'org.springframework.boot:spring-boot-starter-websocket'`

---

## Claude에게 구현 요청 시 프롬프트 예시

### STEP 1 요청 예시

```
이 md 파일을 참고해서 STEP 1 타이머 API를 구현해줘.
- 패키지: com.ide.project
- DB: PostgreSQL
- 공통 응답은 ApiResponse 클래스 사용
- Lombok 사용
- 인증된 사용자 정보는 @AuthenticationPrincipal로 받아
```

### STEP 2 요청 예시

```
이 md 파일을 참고해서 STEP 2 문제 제출 학생 리스트 API를 구현해줘.
submissions 테이블 구조는 아래와 같아:
(여기에 정겨운 씨 테이블 구조 붙여넣기)
```

### STEP 3 요청 예시

```
이 md 파일을 참고해서 STEP 3 피드백 API를 구현해줘.
COMMENT 타입과 HIGHLIGHT 타입 둘 다 처리해야 해.
submission_id FK는 아직 없이 구현해줘.
```
