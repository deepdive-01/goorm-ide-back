# CodeRun Backend

실시간 코드 실행 및 피드백이 가능한 온라인 IDE 서비스의 백엔드입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Build | Gradle (Groovy) |
| DB | PostgreSQL |
| Cache / Pub-Sub | Redis |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT |
| WebSocket | STOMP over SockJS |
| 코드 실행 | Judge0 API |
| 파일 스토리지 | AWS S3 |
| 이메일 | SMTP (Gmail) |
| OAuth | Kakao, Google |
| API 문서 | Swagger (springdoc-openapi) |

---

## 폴더 구조

```
src/main/java/com/ide/project/
├── domain/
│   ├── auth/          # 인증 (회원가입, 로그인, OAuth)
│   ├── user/          # 사용자 (프로필, 회원 탈퇴)
│   ├── space/         # 워크스페이스
│   ├── files/         # 문제·테스트케이스·제출 관리
│   ├── code/          # 코드 실행·채점 (REST + WebSocket)
│   ├── feedback/      # 피드백 (코멘트, 하이라이팅)
│   ├── submission/    # 학생 제출 현황 조회
│   ├── timer/         # 풀이 타이머
│   └── alert/         # 알림
├── global/
│   ├── config/        # 설정 (Redis, WebSocket, Security, Swagger 등)
│   ├── exception/     # 공통 예외 처리
│   ├── redis/         # Redis Pub/Sub (Publisher, Subscriber)
│   ├── security/      # JWT 필터, OAuth2 핸들러
│   └── util/
└── integration/
    ├── mail/          # 이메일 발송
    └── s3/            # S3 업로드
```

---

## 환경 설정

### 프로파일

| 프로파일 | 용도 | 설정 방식 |
|----------|------|-----------|
| `local` | 로컬 개발 | `application-local.yml` 직접 작성 |
| `dev` | 개발 서버 (Docker) | 환경 변수로 주입 |

---

### 로컬 실행 (`local` 프로파일)

`src/main/resources/application-local.yml` 파일을 아래 내용으로 직접 생성합니다.
이 파일은 `.gitignore`에 포함되어 있습니다. **절대 커밋하지 마세요.**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dbname
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379
  cloud:
    aws:
      credentials:
        access-key: YOUR_AWS_ACCESS_KEY
        secret-key: YOUR_AWS_SECRET_KEY
      region:
        static: ap-northeast-2
      s3:
        bucket: your-s3-bucket-name
  mail:
    username: your_gmail@gmail.com
    password: your_gmail_app_password
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: YOUR_KAKAO_CLIENT_ID
            client-secret: YOUR_KAKAO_CLIENT_SECRET
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET

jwt:
  secret: your-secret-key-must-be-at-least-32-chars-long

app:
  oauth:
    redirect-url:
      success: http://localhost:5173/oauth/callback
      signup: http://localhost:5173/oauth/signup

judge0:
  base-url: http://your-judge0-host:2358
```

---

### 개발 서버 환경 변수 (`dev` 프로파일)

Docker 또는 서버에서 아래 환경 변수를 설정합니다.

| 환경 변수 | 설명 | 필수 |
|-----------|------|------|
| `DB_HOST` | PostgreSQL 호스트 | ✅ |
| `DB_NAME` | 데이터베이스 이름 | ✅ |
| `DB_USERNAME` | DB 계정 | ✅ |
| `DB_PASSWORD` | DB 비밀번호 | ✅ |
| `REDIS_HOST` | Redis 호스트 | ✅ |
| `REDIS_PORT` | Redis 포트 (기본: `6379`) | |
| `REDIS_PASSWORD` | Redis 비밀번호 (없으면 생략) | |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | ✅ |
| `MAIL_USERNAME` | Gmail 계정 | ✅ |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 | ✅ |
| `KAKAO_CLIENT_ID` | 카카오 앱 Key | ✅ |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret | ✅ |
| `GOOGLE_CLIENT_ID` | 구글 Client ID | ✅ |
| `GOOGLE_CLIENT_SECRET` | 구글 Client Secret | ✅ |
| `JUDGE0_URL` | Judge0 서버 주소 | ✅ |

> AWS S3 자격증명은 EC2 IAM 역할(권장) 또는 `application-local.yml`로 주입합니다.

---

## 실행 방법

```bash
# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# JAR 빌드 (테스트 제외)
./gradlew build -x test --no-daemon

# Docker 이미지 빌드 (JAR 빌드 후)
docker build -t coderun-be .
```

> **주의:** `docker build` 전에 반드시 Gradle 빌드로 JAR 파일을 먼저 생성해야 합니다.

---

## API 명세

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

### 인증 — `/api/v1/auth`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/email/send` | 이메일 인증 코드 발송 |
| POST | `/email/verify` | 이메일 인증 코드 확인 |
| POST | `/signup` | 일반 회원가입 |
| POST | `/login` | 로그인 (Access + Refresh 토큰 발급) |
| POST | `/logout` | 로그아웃 (Refresh 토큰 무효화) |
| POST | `/refresh` | Access 토큰 재발급 |
| POST | `/oauth/signup` | OAuth 신규 유저 추가 정보 등록 |

OAuth 로그인 진입점:
- `/oauth2/authorization/kakao`
- `/oauth2/authorization/google`

---

### 사용자 — `/api/v1/users`

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/me` | 내 정보 조회 |
| DELETE | `/me` | 회원 탈퇴 |
| POST | `/me/profile-image` | 프로필 이미지 업로드 (multipart/form-data) |
| DELETE | `/me/profile-image` | 프로필 이미지 삭제 |

---

### 워크스페이스 — `/api/v1/spaces`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 워크스페이스 생성 |
| GET | `/` | 내 워크스페이스 목록 조회 |
| GET | `/{spaceId}` | 워크스페이스 상세 조회 |
| PATCH | `/{spaceId}` | 워크스페이스 수정 |
| POST | `/join` | 초대 코드로 워크스페이스 참가 |
| GET | `/{spaceId}/members` | 멤버 목록 조회 |
| POST | `/{spaceId}/invite/email` | 이메일로 초대 발송 |

---

### 문제 관리 — `/api/v1/files`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/problems` | 문제 직접 생성 |
| PUT | `/problems/{problemId}` | 문제 수정 |
| DELETE | `/problems/{problemId}` | 문제 삭제 |
| POST | `/problems/assign` | 문제 은행에서 문제 가져오기 |
| GET | `/problems/{problemId}` | 문제 상세 조회 |
| GET | `/problems/space/{spaceId}` | 워크스페이스 문제 목록 조회 |
| POST | `/problems/{problemId}/testcases` | 테스트케이스 저장 (전체 교체) |
| GET | `/problems/{problemId}/testcases` | 테스트케이스 조회 |

**문제 생성 방식 두 가지:**
- `POST /problems` — `problem_bank_id` 없이 직접 생성
- `POST /problems/assign` — `problem_bank` 테이블에서 문제를 복사하여 생성

> `problem_bank` 테이블은 별도 API 없이 DB로 직접 관리됩니다.

---

### 제출 — `/api/v1/files/submissions`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/submissions` | 코드 제출 또는 임시저장 (`is_final_submit` 플래그로 구분) |
| GET | `/submissions/{problemId}/{userId}` | 제출 정보 조회 |
| DELETE | `/submissions/{problemId}/{userId}` | 제출 취소 |
| PUT | `/submissions/{problemId}/{userId}` | 저장된 코드 수정 |

---

### 코드 실행 및 채점 — `/api/v1/code`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/grade` | 테스트케이스 기반 채점 |

요청:
```json
{
  "problem_id": 1,
  "language": "PYTHON",
  "code": "print(input())"
}
```

응답:
```json
{
  "status": "PASS",
  "pass_count": 3,
  "total_count": 3,
  "results": [
    {
      "order_num": 1,
      "passed": true,
      "input": "hello",
      "expected_output": "hello",
      "actual_output": "hello"
    }
  ]
}
```

지원 언어: `PYTHON`, `JAVA`, `JAVASCRIPT`, `CPP`

---

### 학생 제출 현황 — `/api/v1/questions`

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/{questionId}/submissions` | 문제별 학생 제출 목록 조회 |

Query Parameter: `?status=PASS` (PASS / FAIL / PENDING / DRAFT 필터)

---

### 피드백 — `/api/v1/feedback`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/comments` | 전체 코멘트 피드백 생성 |
| POST | `/highlights` | 라인 하이라이팅 피드백 생성 |
| GET | `/` | 피드백 목록 조회 |
| PUT | `/{feedbackId}` | 피드백 수정 |
| DELETE | `/{feedbackId}` | 피드백 삭제 |

---

### 타이머 — `/api/v1/timer`

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/start` | 타이머 시작 |
| POST | `/{roomId}/stop` | 타이머 정지 |
| GET | `/` | 타이머 상태 조회 |

---

### 알림 — `/api/v1/notifications`

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 알림 목록 조회 (페이지네이션, `?is_read=true/false` 필터) |
| PATCH | `/read-all` | 전체 알림 읽음 처리 |
| PATCH | `/{id}/read` | 단건 알림 읽음 처리 |
| POST | `/` | 알림 생성 |

---

## WebSocket / STOMP

연결 엔드포인트: `ws://host:8080/ws` (SockJS 폴백 지원)

| 구분 | 값 |
|------|----|
| 연결 엔드포인트 | `/ws` |
| 클라이언트 → 서버 prefix | `/app` |
| 서버 → 클라이언트 prefix | `/topic`, `/queue` |

### 코드 실행

| 방향 | 경로 | 설명 |
|------|------|------|
| 클라이언트 → 서버 | `/app/code.{roomId}.execute` | 코드 실행 요청 |
| 서버 → 클라이언트 | `/topic/code.{roomId}.result` | 실행 결과 수신 |

실행 요청 payload:
```json
{
  "language": "PYTHON",
  "code": "print('hello')",
  "stdin": ""
}
```

실행 결과 payload:
```json
{ "status": "RUNNING" }
{ "status": "SUCCESS", "output": "hello\n", "stderr": "" }
{ "status": "ERROR",   "output": "",        "stderr": "ERROR: ..." }
```

---

## 데이터베이스

`dev` 프로파일은 `ddl-auto: validate`이므로 스키마가 사전에 존재해야 합니다.

| 테이블 | 설명 |
|--------|------|
| `users` | 사용자 |
| `oauth_accounts` | OAuth 연동 계정 |
| `spaces` | 워크스페이스 |
| `space_members` | 워크스페이스 멤버 |
| `problems` | 문제 |
| `problem_bank` | 문제 은행 (API 없음, DB 직접 관리) |
| `test_cases` | 테스트케이스 |
| `submissions` | 코드 제출 및 임시저장 |
| `feedbacks` | 피드백 |
| `timers` | 타이머 |
| `notifications` | 알림 |

---

## Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]
```
