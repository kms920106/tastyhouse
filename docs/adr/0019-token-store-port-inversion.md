# ADR-0019: 토큰 저장소 6종을 security-core 포트 + infrastructure:redis 어댑터로 역전한다

- 상태: 채택
- 결정 커밋: `8cba65373` (2026-09-06)
- 대체한 결정: —

## 맥락

**토큰 저장소 6종**은 JWT 인증에 쓰는 Redis 저장소입니다. RefreshToken·Blacklist(로그아웃된 토큰 목록)와 소셜 로그인 임시토큰 4종(kakao·naver·apple·facebook)이 여기에 들어갑니다.

챕터 03에서 서블릿에 의존하지 않는 보안 타입을 `security-core` 모듈로 떼어냈고, 토큰 저장소 6종도 이때 함께 옮겼습니다. 다만 저장소는 `StringRedisTemplate`으로 키를 직접 조립하는 **구체 Redis 클래스**였기 때문에, `security-core`가 `implementation project(':infrastructure:redis')`를 의존해야 했습니다. 순수 계약을 담아야 할 core 모듈이 구체 인프라를 의존하는 **역방향 간선**이었습니다.

이 간선 때문에 `application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 4앱 전부의 runtimeClasspath(실행 시 클래스패스)에 실렸습니다. 그래서 Redis를 쓰지 않는 batch-module에서도 `RedisModuleAutoConfiguration`이 발화했고, springdoc·starter-web이 배포 산출물에 들어갔습니다. 또 web/admin/ceo 3앱은 앱별 키 접두사(`admin:rt:` 등)를 넘기려고 `config/jwt/RedisRepositoryConfig`에서 `StringRedisTemplate`을 직접 참조했습니다. 그 결과 각 앱 `build.gradle`에 `spring-boot-starter-data-redis`를 따로 명시해야 했습니다.

## 이전 규칙

### backend/infrastructure/redis/AGENTS.md — security-module과의 관계

토큰 저장소 6종(`RefreshToken`·`Blacklist`·소셜 임시토큰 4종)은 `security-module`에 남긴다(이후 챕터 03에서 `security-core`로 옮겨짐). 근거는 "그것들은 보안 관심사"라는 것이었고, Redis key prefix(`rt:`/`bl:`/`admin:rt:`/`admin:bl:`)도 바꾸지 않았다. 이 모듈이 넘겨받은 것은 연결 설정·템플릿 빈·rate limiting뿐이었다. 그래서 `security-module`은 `implementation project(':infrastructure:redis')`를 선언하고 data-redis 타입은 직접 선언하지 않았다. 이 모듈이 그 타입을 `api`로 노출했기 때문이다.

### backend/CLAUDE.md — 앱별 runtimeClasspath 감사표

`application → security-core → infrastructure:redis → api-common-module` 전이 사슬이 4앱 전부의 runtimeClasspath에 있다고 기록했다.

### backend/CLAUDE.md — 후속 작업자가 밟기 쉬운 함정 2가지

함정 2의 사례가 이 상황이었다. 챕터 02 시점에 3앱의 `config/jwt/RedisRepositoryConfig`는 앱별 키 접두사를 넘기려고 `StringRedisTemplate`을 직접 참조했다. 그런데 `infrastructure:redis`를 `runtimeOnly`로 내리자 전이가 끊겨 컴파일이 깨졌다. 그래서 3앱 `build.gradle`에 `implementation 'org.springframework.boot:spring-boot-starter-data-redis'`를 명시해 막았다.

### backend/CLAUDE.md — 모듈 지도 (챕터 03 — `security-core` 분리)

챕터 03 서술은 토큰 저장소 6종을 서블릿-프리 타입으로 `security-core`에 옮기면서, 그 `@Repository` 빈을 `security-module`이 스캔해 등록한다는 배선을 전제로 했다. 이 결정 이후 그 배선은 토큰 저장소에 해당하지 않는다. 어댑터는 당시 `RedisModuleAutoConfiguration`이 등록했고, 지금은 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔이 등록한다.

### backend/api-common-module/AGENTS.md — `@ConditionalOnWebApplication(SERVLET)` 두 건

batch-module은 이 모듈을 직접 의존하지 않았지만, 위 전이 사슬 때문에 클래스패스에는 갖고 있었다. batch의 `spring.main.web-application-type: none`이 이 조건을 Negative로 만드는 유일한 근거였다.

## 결정

토큰 저장소 6종을 **포트(계약)와 어댑터(구현)로 나눕니다.** 인터페이스(포트)는 `security-core`에 남기고, Redis 구현은 `infrastructure:redis`의 `token` 패키지로 내립니다. 의존 방향은 `infrastructure:redis → security-core`(어댑터 → 계약)입니다.

| 항목 | before | after |
|---|---|---|
| 토큰 저장소 6종의 형태 | `security-core`의 구체 Redis 클래스 | `security-core`의 포트 인터페이스(`RefreshTokenRepository`·`BlacklistRepository` 등) |
| Redis 구현 위치 | `security-core` | `infrastructure:redis`의 `token` 패키지(`RedisBlacklistRepository` 등) |
| 모듈 간선 | `security-core → infrastructure:redis` | `infrastructure:redis → security-core` |
| 앱별 키 접두사 주입 | 3앱의 `config/jwt/RedisRepositoryConfig`가 `StringRedisTemplate`을 직접 받아 조립 | `security.token-store.key-prefix` 프로퍼티(`RedisRepositoryConfig` 삭제) |
| 3앱의 Redis 선언 | `implementation 'spring-boot-starter-data-redis'` + `runtimeOnly project(':infrastructure:redis')` | `runtimeOnly project(':infrastructure:redis')` 하나 |
| application·batch runtimeClasspath | redis·api-common·springdoc 포함 | 없음 |
| Redis 키 공간(`rt:`/`admin:rt:`/`ceo:rt:`/`kakao_temp:` 등) | — | 바이트 단위로 동일(동작 변경 없음) |

## 근거

- **관심사의 귀속과 기술 구현의 귀속은 층위가 다릅니다.** "토큰 저장소는 보안 관심사"라는 옛 근거는 지금도 맞습니다. 다만 보안 관심사인 것은 *계약*(무엇을 저장하고 언제 무효화하는가)이고, `StringRedisTemplate`으로 키를 조립하는 것은 *구현*입니다. 계약을 `security-core`에, 구현을 `infrastructure:redis`에 두면 두 귀속을 모두 지키면서 역방향 간선을 없앨 수 있습니다.
- **전이 사슬이 끊깁니다.** `security-core → infrastructure:redis` 간선이 사라지면 application과 batch의 runtimeClasspath에서 redis·api-common·springdoc이 빠집니다.
- 커밋 메시지는 이 구조가 `RedisRateLimitCounter`가 외부 모듈의 `RateLimitCounterPort`를 구현하던 선례와 같은 모양이라고 밝힙니다.

## 결과·감수한 손실

- 지금 web·admin·ceo는 `runtimeOnly project(':infrastructure:redis')`로만 Redis를 받습니다. `application`과 `batch-module`의 runtimeClasspath에는 `infrastructure:redis`·`api-common-module`·springdoc이 없습니다.
- 앱 compileClasspath(컴파일 시 클래스패스)에서도 Redis 타입이 사라졌습니다. 그래서 "함정 2" 사례와 3앱의 `spring-boot-starter-data-redis` 명시 선언도 함께 없어졌습니다.
- `security-module`은 서블릿 결합 타입만 남깁니다. `JwtAuthenticationFilter`는 `BlacklistRepository` **포트**를 받으므로 Redis 타입을 모릅니다.
- 앱별 키 접두사 프로퍼티 `security.token-store.key-prefix`의 값은 web `""`, admin `"admin:"`, ceo `"ceo:"`입니다. 접두사가 필요한 새 저장소는 `RedisTokenStoreProperties`를 주입받는 어댑터를 `infrastructure:redis`에 추가합니다.
- 키 공간이 바뀌면 배포 시점에 기존 로그인 세션이 전부 무효화되는데, 이것은 예외가 아니라 조용한 무효화로 드러납니다. 그래서 `infrastructure:redis`의 고정값 단위 테스트가 키 접두사의 유일한 자동 방어선입니다.
- `security-core`의 compileClasspath에서 `spring-boot-starter-data-redis`가 사라졌습니다. 확인 명령은 `./gradlew :security-core:dependencies --configuration compileClasspath | grep -c redis`이고 0이어야 합니다.
- 계약 소유 모듈이 다시 `infrastructure:redis`를 의존하게 되면 이 간선이 되살아납니다. 이 회귀는 컴파일과 기동이 모두 성공하고 batch 배포 산출물만 조용히 커지는 형태로 드러나므로, 문서 규칙으로 막습니다.
- 이후 [ADR-0023](0023-rate-limit-port-in-security-core.md)에서 `RateLimitCounterPort`도 같은 구조(계약은 `security-core`, 구현은 `infrastructure:redis`)로 옮겨졌습니다.

## 영향받은 문서

- `backend/infrastructure/redis/AGENTS.md` · "security-core / security-module과의 관계" · 토큰 저장소의 계약은 `security-core`, 구현은 이 모듈의 `token` 패키지에 있으며 key prefix는 바뀌지 않았다
- `backend/infrastructure/redis/AGENTS.md` · "패키지 구조" · `token` 패키지가 security-core 포트 6종을 구현하고, 이 간선을 끊어 전이 사슬을 없앴다
- `backend/infrastructure/redis/AGENTS.md` · "주의" · 계약 소유 모듈이 이 모듈을 의존하게 만들지 않는다
- `backend/CLAUDE.md` · "앱별 runtimeClasspath 감사표 (§4 — 어떤 auto-config가 어느 앱에서 발화하는가)" · web·admin·ceo만 redis를 `runtimeOnly`로 선언하고, application·batch에는 redis·api-common·springdoc이 없다
- `backend/CLAUDE.md` · "후속 작업자가 밟기 쉬운 함정 2가지" · 함정 2의 Redis 사례가 사라졌고, 3앱의 유일한 Redis 선언은 `runtimeOnly project(':infrastructure:redis')`다
- `backend/CLAUDE.md` · "모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)" (도메인 포트 없는 공유 기술은 별도 공유 모듈이 소유한다) · 토큰 저장소의 계약과 구현이 갈렸다
- `backend/api-common-module/AGENTS.md` · "`@ConditionalOnWebApplication(SERVLET)` — 재유입 방어선이므로 제거하지 않는다" · batch runtimeClasspath에 이 jar가 없다
- `backend/security-core/AGENTS.md` · "Purpose" / "Working In This Directory" · 토큰 저장소 포트의 계약 소유 모듈(포트 수는 이후 [ADR-0050](0050-social-login-router.md)으로 3종이 됐다)
- `backend/{web,admin,ceo}-api/AGENTS.md` · "External — starter를 직접 선언하지 않는다" · starter-data-redis를 직접 선언하지 않는다
