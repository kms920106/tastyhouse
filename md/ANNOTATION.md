# Spring Boot + JPA 애노테이션 순서 Best Practice

> **기본 원칙**: Spring 핵심 → Spring 부가 → Lombok → 외부 라이브러리(Swagger 등)  
> 완벽한 순서보다 **팀 내 일관성**이 가장 중요합니다.

---

## 목차

1. [Controller](#1-controller)
2. [Service](#2-service)
3. [Repository](#3-repository)
4. [Entity](#4-entity)
5. [DTO / Request / Response](#5-dto--request--response)
6. [Configuration](#6-configuration)
7. [메서드 레벨 애노테이션](#7-메서드-레벨-애노테이션)
8. [순서 요약표](#8-순서-요약표)

---

## 1. Controller

```java
@RestController                          // 1. 클래스 성격 정의 (핵심)
@RequestMapping("/api/auth")             // 2. URL 매핑 (핵심)
@RequiredArgsConstructor                 // 3. Lombok DI
@Slf4j                                   // 4. Lombok 로깅
@Tag(name = "Auth", description = "인증 관련 API")  // 5. Swagger (외부)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")               // 1. HTTP 메서드
    @Operation(summary = "로그인")       // 2. Swagger
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 조회")
    @PreAuthorize("hasRole('ADMIN')")    // Security는 Operation 뒤
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUser(id));
    }
}
```

---

## 2. Service

```java
@Service                                 // 1. 클래스 성격 정의 (핵심)
@RequiredArgsConstructor                 // 2. Lombok DI
@Transactional(readOnly = true)          // 3. 트랜잭션 기본값 (읽기 전용 권장)
@Slf4j                                   // 4. Lombok 로깅
public class AuthService {

    private final UserRepository userRepository;

    // 조회 - 클래스 레벨 readOnly 상속
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    // 쓰기 - 메서드 레벨에서 readOnly 오버라이드
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // ...
    }
}
```

> ✅ `@Transactional(readOnly = true)`를 클래스 레벨에 선언하고,  
> 쓰기 메서드에만 `@Transactional`을 별도 선언하는 것이 Best Practice입니다.

---

## 3. Repository

```java
// 기본 JPA Repository (애노테이션 불필요)
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findAllByStatus(@Param("status") UserStatus status);
}
```

```java
// QueryDSL 사용 시 구현체
@Repository                              // 1. 클래스 성격 정의
@RequiredArgsConstructor                 // 2. Lombok DI
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    // ...
}
```

---

## 4. Entity

```java
@Entity                                  // 1. JPA 핵심
@Table(name = "users")                   // 2. JPA 부가 설정
@Getter                                  // 3. Lombok (Setter는 지양)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 4. Lombok 생성자
@ToString(exclude = {"password", "orders"})          // 5. Lombok (연관관계 제외)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // 생성자는 정적 팩토리 메서드 권장
    public static User create(String email, String username, String password) {
        User user = new User();
        user.email = email;
        user.username = username;
        user.password = password;
        user.status = UserStatus.ACTIVE;
        return user;
    }
}
```

```java
// BaseEntity (공통 필드 분리)
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

> ⚠️ Entity에 `@Setter`는 사용하지 않습니다. 도메인 메서드로 상태를 변경하세요.

---

## 5. DTO / Request / Response

```java
// Request DTO
@Getter                                  // 1. Lombok
@NoArgsConstructor                       // 2. Lombok (역직렬화용)
@AllArgsConstructor                      // 3. Lombok
@Builder                                 // 4. Lombok
@Schema(description = "로그인 요청")     // 5. Swagger
public class LoginRequest {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;
}
```

```java
// Response DTO (record 사용 권장 - Java 16+)
@Builder
public record UserResponse(
    Long id,
    String email,
    String username,
    UserStatus status
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .status(user.getStatus())
            .build();
    }
}
```

---

## 6. Configuration

```java
@Configuration                           // 1. Spring 핵심
@EnableWebSecurity                       // 2. Spring 기능 활성화
@EnableMethodSecurity                    // 3. Spring 기능 활성화
@RequiredArgsConstructor                 // 4. Lombok DI
public class SecurityConfig {
    // ...
}
```

```java
@Configuration                           // 1. Spring 핵심
@EnableJpaAuditing                       // 2. JPA Auditing 활성화
public class JpaConfig {
    // ...
}
```

---

## 7. 메서드 레벨 애노테이션

### Controller 메서드

```java
@PostMapping("/signup")                  // 1. HTTP 메서드 매핑
@ResponseStatus(HttpStatus.CREATED)      // 2. HTTP 상태 코드
@Operation(summary = "회원가입")         // 3. Swagger 요약
@ApiResponses(value = {                  // 4. Swagger 응답 정의
    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
})
public UserResponse signup(@RequestBody @Valid SignupRequest request) {
    return authService.signup(request);
}
```

### Service 메서드

```java
@Transactional                           // 1. 트랜잭션
@CacheEvict(value = "users", key = "#id")  // 2. 캐시
public void updateUser(Long id, UpdateUserRequest request) {
    // ...
}
```

---

## 8. 순서 요약표

| 레이어 | 애노테이션 순서 |
|--------|----------------|
| **Controller** | `@RestController` → `@RequestMapping` → `@RequiredArgsConstructor` → `@Slf4j` → `@Tag` |
| **Service** | `@Service` → `@RequiredArgsConstructor` → `@Transactional(readOnly = true)` → `@Slf4j` |
| **Repository** | `@Repository` → `@RequiredArgsConstructor` _(인터페이스는 불필요)_ |
| **Entity** | `@Entity` → `@Table` → `@Getter` → `@NoArgsConstructor` → `@ToString` |
| **DTO** | `@Getter` → `@NoArgsConstructor` → `@AllArgsConstructor` → `@Builder` → `@Schema` |
| **Config** | `@Configuration` → `@Enable*` → `@RequiredArgsConstructor` |

---

## 핵심 원칙 정리

| 원칙 | 내용 |
|------|------|
| 🎯 **우선순위** | Spring 핵심 → Spring 부가 → Lombok → 외부 라이브러리 |
| 👁️ **가독성** | 코드 리뷰 시 Spring 핵심 애노테이션이 먼저 눈에 띄어야 함 |
| 🚫 **Entity Setter 금지** | 도메인 메서드 또는 정적 팩토리 메서드로 상태 변경 |
| 📖 **readOnly 기본** | Service 클래스 레벨에 `@Transactional(readOnly = true)` 선언 |
| 📝 **record 활용** | Response DTO는 Java 16+ `record` 사용 권장 |
| 🤝 **일관성 최우선** | 완벽한 순서보다 팀 컨벤션 통일이 중요 |
