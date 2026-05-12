# IDE Project

> 프로젝트 한 줄 설명

---

## 🛠 기술 스택

| 분류      | 기술                  |
| --------- | --------------------- |
| Language  | Java 21               |
| Framework | Spring Boot 3.3.0     |
| Build     | Gradle - Groovy       |
| DB        | PostgreSQL            |
| ORM       | Spring Data JPA       |
| Security  | Spring Security + JWT |
| Editor    | VS Code               |

---

## 📁 폴더 구조

```
src/main/java/com/ide/
├── IdeApplication.java
├── domain/
│   ├── user/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │       ├── request/
│   │       └── response/
│   └── auth/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│           ├── request/
│           └── response/
├── global/
│   ├── config/
│   ├── exception/
│   │   ├── handler/
│   │   └── custom/
│   ├── response/
│   ├── security/
│   │   ├── jwt/
│   │   └── filter/
│   └── util/
└── integration/
    └── mail/
```

---

## ⚙️ 환경 설정

### 환경 변수

`.env` 파일을 프로젝트 루트에 생성하거나 IDE Run Configuration에 설정:

```env
JWT_SECRET=your-secret-key-must-be-at-least-32-characters
DB_HOST=localhost
DB_NAME=ide_db
DB_USERNAME=postgres
DB_PASSWORD=password
```

### PostgreSQL DB 생성

```sql
CREATE DATABASE ide_db;
```

### 프로파일

| 프로파일 | 용도      | DB                     |
| -------- | --------- | ---------------------- |
| `local`  | 로컬 개발 | PostgreSQL (localhost) |
| `dev`    | 개발 서버 | PostgreSQL (서버)      |

---

## 🚀 실행 방법

```bash
# 로컬 실행
./gradlew bootRun

# 빌드
./gradlew build

# 테스트
./gradlew test
```

### VS Code에서 실행 시 `.vscode/launch.json` 추가

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "IdeApplication (local)",
      "request": "launch",
      "mainClass": "com.ide.IdeApplication",
      "env": {
        "SPRING_PROFILES_ACTIVE": "local",
        "JWT_SECRET": "your-secret-key-must-be-at-least-32-characters",
        "DB_NAME": "ide_db",
        "DB_USERNAME": "postgres",
        "DB_PASSWORD": "password"
      }
    }
  ]
}
```

---

## 🔗 API 명세

> Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---
