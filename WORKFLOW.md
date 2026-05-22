# 개발 워크플로우

> 이 프로젝트에서 기능을 구현할 때 반드시 따라야 하는 순서와 규칙입니다.
> 팀원이 구현한 `auth`, `user` 도메인의 패턴을 기준으로 작성했습니다.

---

## 목차

1. [Git 워크플로우](#1-git-워크플로우)
2. [구현 순서 (레이어 순서)](#2-구현-순서-레이어-순서)
3. [레이어별 코드 패턴](#3-레이어별-코드-패턴)
4. [전역 설정 수정 위치](#4-전역-설정-수정-위치)
5. [테스트 작성 규칙](#5-테스트-작성-규칙)
6. [PR 체크리스트](#6-pr-체크리스트)

---

## 1. Git 워크플로우

### 순서

```
① 브랜치 생성 → ② 개발 → ③ PR → ④ 머지
```

> GitHub Issues는 사용하지 않음. 태스크 관리는 외부 툴(COD-번호 형식) 기준으로 진행.

### ① 브랜치명 규칙

| 작업 종류 | 브랜치명 형식 | 예시 |
|---|---|---|
| 기능 추가 | `feat/<scope>` | `feat/workspace` |
| 버그 수정 | `fix/<scope>` | `fix/token-reissue` |
| 리팩토링 | `refactor/<scope>` | `refactor/auth-service` |

### ④ 커밋 메시지

```
[COD-이슈번호] 변경 내용 한 줄 요약
```

---

## 2. 구현 순서 (레이어 순서)

새 도메인을 추가할 때는 아래 순서를 반드시 지킵니다.

```
1. entity/          JPA 엔티티
2. repository/      JpaRepository 인터페이스
3. dto/request/     요청 DTO (record + @Valid)
4. dto/response/    응답 DTO (record + 정적 팩토리)
5. service/         비즈니스 로직
6. controller/      HTTP 엔드포인트
7. 전역 설정 수정   ErrorCode 추가 / SecurityConfig 퍼블릭 경로 추가
8. 테스트 작성      Service 레이어 단위 테스트
```

### 폴더 생성 위치

```
src/main/java/com/ide/project/
├── domain/
│   └── {도메인명}/           ← 새 도메인은 여기에 생성
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│           ├── request/
│           └── response/
└── global/                  ← 공통 설정만, 도메인 코드 추가 금지
```

---

## 3. 레이어별 코드 패턴

### Entity

```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // 외부 직접 생성 차단
@EntityListeners(AuditingEntityListener.class)        // createdAt/updatedAt 자동화
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String field;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder                                           // 생성자 대신 빌더 사용
    public MyEntity(String field) {
        this.field = field;
    }

    // 상태 변경은 반드시 메서드로 캡슐화 (setter 사용 금지)
    public void updateField(String newValue) {
        this.field = newValue;
    }
}
```

**규칙**
- `setter` 사용 금지 — 상태 변경은 의미 있는 메서드명으로
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- 소프트 삭제 패턴: 실제 삭제 대신 `isActive = false` 처리 (예: `user.deactivate()`)

---

### Repository

```java
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    boolean existsByField(String field);
    Optional<MyEntity> findByField(String field);
}
```

---

### Request DTO

```java
public record MyRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 50)
    String name,

    @NotNull
    SomeEnum role

) {}
```

**규칙**
- `record` 타입 사용
- 모든 필드에 Bean Validation 어노테이션 (`@NotBlank`, `@Email`, `@Size`, `@Pattern` 등)
- Controller에서 `@Valid`와 함께 사용

---

### Response DTO

```java
public record MyResponse(
        Long id,
        String name
) {
    // 정적 팩토리 메서드로 엔티티 → DTO 변환
    public static MyResponse from(MyEntity entity) {
        return new MyResponse(entity.getId(), entity.getName());
    }
}
```

---

### Service

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)          // 클래스 기본값: 읽기 전용
public class MyService {

    private final MyRepository myRepository;

    @Transactional                        // 쓰기 메서드만 별도로 선언
    public MyResponse create(MyRequest request) {

        if (myRepository.existsByField(request.field())) {
            throw new BusinessException(ErrorCode.DUPLICATE_FIELD);   // 비즈니스 예외는 ErrorCode로
        }

        MyEntity entity = MyEntity.builder()
                .field(request.field())
                .build();

        MyEntity saved = myRepository.save(entity);
        return MyResponse.from(saved);
    }

    public MyResponse find(Long id) {
        MyEntity entity = myRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOMETHING_NOT_FOUND));
        return MyResponse.from(entity);
    }
}
```

**규칙**
- `@Transactional(readOnly = true)` 클래스 레벨에 선언
- 조회 메서드는 별도 `@Transactional` 불필요
- 쓰기 메서드(`create`, `update`, `delete`)에만 `@Transactional` 추가
- 예외는 반드시 `BusinessException(ErrorCode.XXX)` 형태로

---

### Controller

```java
@RestController
@RequestMapping("/api/v1/{도메인}")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    // 인증 불필요 엔드포인트 예시
    @PostMapping
    public ResponseEntity<ApiResponse<MyResponse>> create(
            @Valid @RequestBody MyRequest request
    ) {
        MyResponse data = myService.create(request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "생성되었습니다.", data));
    }

    // 인증 필요 엔드포인트 예시 (JWT 필터가 SecurityContext에 userId를 저장)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyResponse>> getMyInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        MyResponse data = myService.find(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "조회되었습니다.", data));
    }

    // 데이터 없는 성공 응답
    @DeleteMapping("/me")
    public ResponseEntity<Void> delete() {
        // ...
        return ResponseEntity.noContent().build();  // 204
    }
}
```

**응답 형식 정리**

| 케이스 | 코드 |
|---|---|
| 데이터 있는 성공 | `ApiResponse.success(200, "SUCCESS", "메시지", data)` |
| 데이터 없는 성공 | `ApiResponse.success(200, "SUCCESS", "메시지")` |
| 204 No Content | `ResponseEntity.noContent().build()` |
| 에러 (자동 처리) | `GlobalExceptionHandler`가 `BusinessException`을 `ApiResponse.error()`로 변환 |

---

## 4. 전역 설정 수정 위치

### 새 ErrorCode 추가

`global/exception/ErrorCode.java`

```java
// 도메인별로 주석 구분하여 추가
// 워크스페이스
SPACE_NOT_FOUND(404, "존재하지 않는 스페이스입니다."),
MY_NEW_ERROR(400, "에러 메시지"),
```

### 인증 없이 접근 가능한 경로 추가

`global/config/SecurityConfig.java` — `permitAll()` 블록에 추가

```java
.requestMatchers(
    "/api/v1/auth/email/send",
    "/api/v1/auth/email/verify",
    "/api/v1/auth/signup",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/oauth/signup",
    "/api/v1/my-new-public-endpoint"   // ← 여기에 추가
).permitAll()
```

> 추가하지 않으면 인증 없이 요청 시 401 반환됨

### 새 Redis 키 추가

`global/util/RedisKeys.java`

```java
public class RedisKeys {
    public static final String REFRESH_TOKEN = "RT:";
    public static final String EMAIL_CODE = "EMAIL_CODE:";
    public static final String EMAIL_VERIFIED = "EMAIL_VERIFIED:";
    public static final String OAUTH_TEMP = "OAUTH_TEMP:";
    public static final String MY_NEW_KEY = "MY_KEY:";   // ← 여기에 추가
}
```

---

## 5. 테스트 작성 규칙

### 기본 구조

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private MyRepository myRepository;

    @InjectMocks
    private MyService myService;

    private static final Long TEST_ID = 1L;

    @Test
    @DisplayName("실패 케이스를 한국어로 명확하게 설명한다")
    void method_failCase() {
        // Given
        given(myRepository.findById(TEST_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> myService.find(TEST_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SOMETHING_NOT_FOUND);
    }

    @Test
    @DisplayName("정상 케이스: 성공 시 반환값과 호출 여부를 검증한다")
    void method_success() {
        // Given
        MyEntity entity = mock(MyEntity.class);
        given(myRepository.findById(TEST_ID)).willReturn(Optional.of(entity));
        given(entity.getId()).willReturn(TEST_ID);

        // When
        MyResponse result = myService.find(TEST_ID);

        // Then
        assertThat(result.id()).isEqualTo(TEST_ID);
        verify(myRepository).findById(TEST_ID);
    }
}
```

### 테스트 작성 규칙

| 항목 | 규칙 |
|---|---|
| `@DisplayName` | 한국어로, 조건과 결과를 명확히 |
| 구조 | `// Given / // When / // Then` 주석 필수 |
| 실패 케이스 | `assertThatThrownBy` + `.extracting("errorCode").isEqualTo(ErrorCode.XXX)` |
| 호출 검증 | `verify(mock).method(...)` / `verify(mock, never()).method(...)` |
| Mock 객체 | `@Mock` 필드 주입, 인라인은 `mock(Class.class)` |
| `@Value` 필드 | `ReflectionTestUtils.setField(service, "fieldName", value)` |
| 테스트 위치 | `src/test/java/.../{도메인}/service/{서비스명}Test.java` |

### 테스트 케이스 필수 항목

서비스 메서드 하나당:
- 정상 케이스 (성공) 1개 이상
- 실패 케이스 (각 예외 분기마다) 1개 이상

---

## 6. PR 체크리스트

`.github/pull_request_template.md` 기준

```markdown
## 요약
- 무엇을 왜 변경했는지

## 관련 이슈
- Closes #이슈번호

## 변경 유형
- [x] 기능 추가

## 테스트
- [ ] 유닛/통합 테스트 추가 또는 갱신
- [ ] 로컬에서 수동 테스트 완료
- [ ] 엣지 케이스 확인 (빈 값, 중복, 권한 없는 접근 등)
```

---

## 빠른 참조

### 새 도메인 추가 시 건드리는 파일 목록

```
✅ domain/{name}/entity/{Name}.java
✅ domain/{name}/repository/{Name}Repository.java
✅ domain/{name}/dto/request/{Action}Request.java
✅ domain/{name}/dto/response/{Action}Response.java
✅ domain/{name}/service/{Name}Service.java
✅ domain/{name}/controller/{Name}Controller.java
✅ global/exception/ErrorCode.java          (새 에러코드 추가)
✅ global/config/SecurityConfig.java        (퍼블릭 경로 있을 경우)
✅ global/util/RedisKeys.java               (Redis 사용할 경우)
✅ test/.../service/{Name}ServiceTest.java
```

### 인증 흐름 요약

```
로컬 로그인:  이메일 인증코드 발송 → 코드 확인 → 회원가입 → 로그인 → AT(body) + RT(cookie)
소셜 로그인:  /oauth2/authorization/{provider} → OAuth2SuccessHandler
              └─ 기존 유저: AT(쿼리파라미터) + RT(cookie) → 메인 리다이렉트
              └─ 신규 유저: tempKey(Redis) → 추가정보 입력 페이지 리다이렉트 → /oauth/signup

인증된 요청:  Authorization: Bearer {accessToken} 헤더
              └─ JwtAuthenticationFilter → SecurityContext에 userId 저장
              └─ Controller: (Long) auth.getPrincipal() 로 userId 추출
```
