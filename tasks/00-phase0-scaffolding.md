# Phase 0 — 공통 스캐폴딩 (모든 도메인 작업의 선행 필수)

> 먼저 `tasks/README.md`를 읽을 것.
>
> **[개정]** 초기 계획의 "api 모듈 querydsl apt 추가"(구 §1 일부)는 query DAO의 infrastructure-module 이관 방침 확정으로 **롤백 대상**이 되었다 — 롤백은 `10-notice.md` 재작업에서 수행한다. 나머지 항목(의존 승격·이벤트 포트·락 예외 번역·DomainServiceConfig·ArchUnit)은 유효하며 이미 완료된 상태다.

## 작업 내용

### 1. 빌드 그래프 승격
- `web-api/build.gradle`, `admin-api/build.gradle`, `ceo-api/build.gradle`, `batch-module/build.gradle`:
  - `runtimeOnly project(':infrastructure-module')` → `implementation project(':infrastructure-module')` — api 모듈이 infra의 `<ctx>/query/` DAO·Result DTO를 주입·import하기 위함. (완료)
  - ~~querydsl apt 설정 추가~~ — **폐지·롤백 대상**. Result DTO(@QueryProjection)가 api 모듈이 아니라 infrastructure-module `<ctx>/query/`에 놓이는 것으로 확정되어, api 모듈에는 apt가 불필요하다. Phase 0에서 이미 추가된 apt·sourceSets 블록은 `10-notice.md` 재작업 단계에서 4개 모듈 모두 제거한다.

### 2. DomainEventPublisher 포트
- 신설: `core-module/src/main/java/com/tastyhouse/core/shared/event/DomainEventPublisher.java`
  ```java
  public interface DomainEventPublisher {
      void publish(Object event);
  }
  ```
- 어댑터 신설: `infrastructure-module/.../shared/event/SpringDomainEventPublisher.java` — `ApplicationEventPublisher` 위임, `@Component`.
- 목적: 도메인 서비스가 Spring 의존 없이 이벤트 발행. 이후 각 도메인 작업에서 core application의 `ApplicationEventPublisher` 사용처를 이 포트로 대체한다. (완료)

### 3. 낙관적 락 예외 번역
- 신설: `core-module/.../shared/exception/OptimisticLockConflictException.java` (RuntimeException 상속, 프레임워크-프리).
- `infrastructure-module/.../reservation/persistence/ReservationSlotRepositoryImpl.java`의 save 경로에서 `ObjectOptimisticLockingFailureException`을 catch하여 위 예외로 번역해 rethrow.
- core의 `ReservationCommandService`(또는 `ReservationCreator`)의 재시도 판별을 spring-orm 예외 대신 위 예외로 교체.
- 목적: 99-finalize에서 core의 `spring-orm` 의존 제거 가능하게 함. (완료)

### 4. DomainServiceConfig
- 신설: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/DomainServiceConfig.java` — `@Configuration`. 이후 각 도메인 작업이 하강시킨 도메인 서비스 POJO를 여기 `@Bean`으로 등록한다. 초기에는 빈 클래스로 생성. (완료)

### 5. ArchUnit 경계 테스트
- web/admin/ceo 각 모듈 test에 `architecture/LayerRulesTest.java` 추가:
  - `..application..` 패키지는 `org.springframework.web..`/`jakarta.servlet..`/`..request..`/`..response..`에 의존 금지.
  - `*ApiController`는 `*Repository`에 직접 의존 금지.
- ArchUnit 의존이 없으면 `testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'` 추가.
- **[개정 — 추가 규칙 2개]** query DAO의 infra 이관 방침에 따라 아래 규칙을 web/admin/ceo `LayerRulesTest`에 추가하고, **batch-module에도 `LayerRulesTest`를 신설**(archunit testImplementation 추가)한다 — `10-notice.md` 재작업에서 수행:
  - `noClasses().should().dependOnClassesThat().resideInAPackage("com.querydsl..")` — api 모듈의 QueryDSL 의존 금지.
  - `noClasses().should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..")` — api 모듈은 infra 중 `..query..`(직접)·`..listener..`(간접)만 허용, persistence 어댑터 직접 의존 금지.

## 완료 기준
- 전 모듈 LSP 컴파일 오류 0 (동작 변경 없음 — 순수 추가·승격).
- 추천 커밋 메시지 제시 (예: `chore(build): domain-module 전환 스캐폴딩 — infra 의존 승격·이벤트 포트·락 예외 번역·DomainServiceConfig`).
