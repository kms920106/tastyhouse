# Clean Architecture 전환 기록

이 문서는 tastyhouse-api의 아키텍처 전환 이력을 기록한다. 전통적 계층형(Service가 Repository와 엔티티를 직접 다루는 구조)에서 시작해 **Strangler Fig 방식으로 도메인 단위 점진 전환**을 진행했고, 마지막 단계로 `core-module` → `domain-module` 전환을 통해 **도메인 계층이 프레임워크를 전혀 모르는 구조**에 도달했다.

각 단계의 세부 컨벤션은 루트 `CLAUDE.md`와 각 모듈 `AGENTS.md`가 소유한다. 이 문서는 "왜 그렇게 됐는지"와 "무엇이 빌드 그래프 수준에서 강제되는지"를 기록한다.

---

## 1단계 — 도메인 모델 / JPA 엔티티 분리 (Strangler Fig 점진 전환)

**문제**: 도메인 모델이 곧 `@Entity`였다. 그 결과 (1) 도메인 객체가 `jakarta.persistence`에 결합되어 단위 테스트에 DB/컨텍스트가 필요했고, (2) 상태전이 메서드와 JPA 프록시 제약(필드에 `final` 금지, no-arg 생성자 필요)이 한 클래스에 섞였으며, (3) `core-module`이 JPA 구현을 알고 있어 "안쪽 계층은 바깥을 모른다"는 원칙이 코드로 강제되지 않았다.

**해결**: 도메인마다 순수 POJO 도메인 모델(`core-module`)과 JPA 엔티티(`infrastructure-module`)를 분리했다.

- 순수 도메인 모델: 신규 생성 `of(...)` + DB 재구성 전용 `reconstitute(...)` 두 팩토리만 공개. `reconstitute`는 인프라(매퍼)만 호출해 불변식 우회를 막는다. 재대입되지 않는 필드는 `final`.
- JPA 엔티티: `XxxJpaEntity`(DB 매핑 전용, 행위 없음) + `XxxMapper`(`toDomain`/`toEntity`/`applyChanges`) + `XxxJpaRepository` + `XxxRepositoryImpl`.
- 저장 시맨틱은 **load-copy-save**: id null이면 insert, 있으면 managed 엔티티를 PK로 조회해 `applyChanges` 복사. detached merge는 `@CreatedDate(updatable = false)` 감사 필드 파손·전 필드 UPDATE 문제로 금지.
- **명시적 save 규칙**: POJO는 더티 체킹으로 자동 flush되지 않으므로 변경 후 반드시 `repository.save(domain)`을 호출한다. 이 전환 과정에서 실제 회귀(무저장 업데이트)가 여러 도메인에서 발견되어 함께 고쳤다.

**전환 순서가 강제된 사례**: core에 남은 `RepositoryImpl`이 이미 이동한 도메인의 Q타입을 참조하면 core → infra 역참조가 되어 컴파일이 깨진다. 그래서 참조자를 먼저 옮겨야 하는 순서 제약이 생겼다(`order` → `payment`, `shop`/`review` → `product`, 참조자 14개를 가진 hub 도메인 `file`은 마지막). 과도기에는 `PathBuilder<Object>("XxxJpaEntity")` 문자열 우회를 썼고, 전 도메인 이동이 끝난 뒤 전부 정식 `QXxxJpaEntity` 조인으로 복원했다(현재 잔존 0건).

**결과**: 22개 도메인 전부 전환 완료. `core-module`이 100% JPA-free가 되어 `spring-boot-starter-data-jpa`·`mysql-connector-j`·`querydsl-jpa` 의존을 제거했고, `@Embeddable` VO도 어노테이션 없는 순수 POJO(record)가 되어 컬럼 매핑은 각 JpaEntity의 `@AttributeOverride`로 이전했다.

## 2단계 — 모듈 경계 정리 (관심사별 공유 모듈 분리)

`infrastructure-module`을 "core Repository 포트의 DB 어댑터 전용"으로 못 박고, core에 포트가 없는 공유 기술 관심사는 별도 모듈로 뺐다.

- `security-module`: Redis 기반 JWT 세션(RefreshToken/Blacklist/소셜 임시토큰)·Rate Limiting·공용 JWT 인증 메커니즘. `TokenService`류가 저장소 구체 클래스를 컴파일 타임에 주입하는 구조라 `implementation`으로 노출한다.
- `logging-module`: 요청/응답 로깅·마스킹. 사내 모듈 의존이 0.
- `batch-module`: 시간 기반 스케줄러를 web-api에서 분리한 독립 실행 모듈.
- `ceo-api`: 점주용 3번째 프레젠테이션 모듈.
- 설정값도 같은 패턴: 소유 모듈이 `application-{모듈}.yml`을 갖고 실행 모듈이 `spring.config.import`로 로딩한다.

---

## 3단계 — `core-module` → `domain-module` 전환 (application 계층 해체)

1·2단계로 도메인 모델은 POJO가 됐지만, `core-module`에는 여전히 도메인마다 `application/`(서비스 + Command/Result DTO) 계층이 있었다. 이 단계가 그 계층을 해체하고 모듈을 리네이밍한 최종 전환이다.

### 왜 필요했나 (전환 전 문제)

1. **`application/`이 3-hop 구조를 만들었다**: 컨트롤러 → api 모듈 `{도메인}Service`(파사드) → core `application` 서비스 → 도메인/Repository. 중간 두 계층이 대개 위임·변환만 하는데도 시그니처를 이중으로 유지해야 했다.
2. **application 서비스가 web-api·admin-api·ceo-api에 공유되어 결합을 만들었다**: 한 액터의 화면 요구로 command/query 시그니처를 바꾸면 다른 두 모듈이 함께 깨졌다. 액터별로 다른 워크플로가 하나의 서비스에 누적되기도 했다.
3. **조회 결과 DTO(`@QueryProjection`)가 core에 있어 QueryDSL 의존이 core에 남았다**: `querydsl-core`/`querydsl-apt`가 core에 필요했고, 그 결과 **QueryDSL이 소비 모듈(web/admin/ceo/batch)로 전이 노출**되어 api 모듈에서 `com.querydsl.*`를 쓸 수 있었다. "어댑터 기술이 프레젠테이션까지 새어나간다"는 레이어 위반의 온상이었다.
4. **`spring-tx`/`spring-orm`이 core에 남아 있었다**: `@Transactional`과 `ObjectOptimisticLockingFailureException`을 core가 알고 있었다.
5. **모듈·패키지 이름이 실체와 어긋났다**: `core-module`/`com.tastyhouse.core`는 "무엇의 core인가"를 말해주지 않고, `com.tastyhouse.core.domain.<ctx>.domain.model`처럼 `domain` 세그먼트가 두 번 나오는 중복 경로를 만들었다.
6. **ArchUnit 규칙이 공허하게 통과하고 있었다**: `applicationShouldNotDependOnWebLayer`가 `..application..` 패키지를 매칭했는데, 전환이 진행되며 그 패키지의 대상이 0건이 되어 **규칙이 아무것도 검사하지 않으면서 초록불**이었다. `allowEmptyShould(true)`가 이 상태를 숨기고 있었다.

### 무엇을 했나

**(1) 모듈 리네이밍**: `core-module` → `domain-module`. `settings.gradle`의 `include 'domain-module'`, 전 모듈 build.gradle이 `project(':domain-module')`을 참조한다.

**(2) 패키지 리네이밍**: `com.tastyhouse.core.domain.<ctx>...` → `com.tastyhouse.domain.<ctx>...`(중복 `domain` 세그먼트 1개 제거), `com.tastyhouse.core.shared.*` → `com.tastyhouse.domain.shared.*`, `com.tastyhouse.core.exception.*` → `com.tastyhouse.domain.exception.*`. 도메인 내부 계층(`...<ctx>.domain.model`/`.vo`/`.repository`/`.service`/`.port`)은 그대로 유지했다.

```
com.tastyhouse.core.domain.order.domain.model.Order  →  com.tastyhouse.domain.order.domain.model.Order
com.tastyhouse.core.shared.page.PageResult           →  com.tastyhouse.domain.shared.page.PageResult
```

**(3) application 계층 해체** — use case를 5가지로 분류해 각자 제 위치로 보냈다.

| 분류 | 판정 기준 | 행선지 |
|---|---|---|
| (A) 액터 특화 command | 한 애그리거트만 다루거나 특정 액터 워크플로 | 소비 모듈의 `{도메인}CommandService`(3-hop → 1-hop) |
| (B) read model | Result DTO·`PageResult` 반환, 조인 투영, 목록·검색·페이징 | **infrastructure-module `<ctx>/query/`** — `{도메인}QueryDao`(`@Repository`) + Result DTO(`@QueryProjection`) + `SearchCondition` |
| (C) 불변식 오케스트레이션 | 한 트랜잭션에서 2+ 애그리거트 타입을 load & save | domain `<ctx>/domain/service/` POJO로 **하강**(모듈로 복제 금지) |
| (D) 무상태 정책 | Calculator/Validator | domain `<ctx>/domain/service/`로 이동 |
| (E) 이벤트 리스너 | `@TransactionalEventListener` | infrastructure-module `<ctx>/listener/` |
| 출력 포트 | 과거 `application/port/**` | domain `<ctx>/domain/port/` |

- **write 포트 잔류 판정**: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?" — 그렇다면 write 포트에 남기고(`findById`/`existsByX`/락 획득용 조회), 화면 조립용이면 query DAO로 보냈다.
- (C)를 소비 모듈로 복제하지 않은 이유: 복제하면 한쪽만 고쳐질 때 다른 액터가 같은 유스케이스를 실행할 때 불변식을 우회한다. domain에 한 곳만 두어야 세 api 모듈이 동일한 규칙을 공유한다.
- (E)를 특정 api 모듈에 두지 않은 이유: 리스너가 없는 모듈이 그 이벤트를 발행하면 후속 처리가 조용히 누락된다. 모든 실행 모듈이 스캔하는 infrastructure-module에 두어야 안전하다.
- 결과적으로 소비 모듈은 도메인당 **CQRS 쌍**을 갖는다 — `{도메인}CommandService`(`@Transactional`, domain write 포트·도메인 서비스 주입, 식별자만 반환)와 `{도메인}QueryService`(`@Transactional(readOnly = true)`, infra QueryDao 주입 + Response 조립 private 매퍼). 이 서비스들은 `..application..` 패키지가 아니라 각 모듈의 도메인 패키지에 직접 놓인다(예: `com.tastyhouse.webapi.notice.NoticeQueryService`). command 결과 응답은 커밋 이후 컨트롤러가 QueryService로 재조회해 조립한다.
- 도메인 서비스가 POJO(`@Service`/`@Transactional` 없음)이므로 빈 등록은 infrastructure-module의 `DomainServiceConfig`가 `@Bean` 팩토리로 담당하고, 트랜잭션 경계는 api 모듈의 CommandService가 소유한다.

**(4) `domain-module` 의존 다이어트**: production 의존이 **Lombok 하나**만 남았다.

- `querydsl-core`·`querydsl-apt`·querydsl sourceSets/generated 블록 제거 — `@QueryProjection` Result DTO가 전부 `infrastructure-module <ctx>/query/`로 갔으므로 domain이 `com.querydsl.*`를 컴파일할 이유가 없다. **이 제거가 api 모듈로의 QueryDSL 전이를 원천 차단하는 지점이다.**
- `spring-tx`·`spring-orm` 제거 — 도메인 서비스가 전부 POJO이고, 낙관적 락 충돌은 프레임워크-프리 `OptimisticLockConflictException`(`shared/exception/`)으로 표현하며 스프링 `ObjectOptimisticLockingFailureException` → 이 예외 번역은 infrastructure-module의 `RepositoryImpl`이 담당하기 때문이다(reference: `reservation/persistence/ReservationSlotRepositoryImpl`).

**(5) api 모듈 QueryDSL 전면 금지**: web/admin/ceo/batch의 `src/main`에 `com.querydsl.*` import **0건**, `@QueryProjection` 선언 **0건**, `..infrastructure..persistence..` import **0건**. `infrastructure-module`의 `querydsl-jpa`는 `api` → `implementation`으로 강등해 소비 모듈 클래스패스로 전이되지 않게 했다. 조회는 infra `<ctx>/query/` DAO가 캡슐화하고, api 모듈은 그 DAO와 Result DTO만 주입·import한다.

> 다만 api 모듈의 `infrastructure-module` 의존 스코프는 `runtimeOnly` → `implementation`으로 **완화**됐다. QueryService가 QueryDao를 컴파일 타임에 주입해야 하기 때문이다. 은닉은 의존 스코프가 아니라 ArchUnit 규칙이 담당한다 — `..query..`만 허용, `..persistence..`와 `com.querydsl..`은 금지.

**(6) `scanBasePackages`에서 domain 스캔 엔트리 제거**: `domain-module`에 `@Component`/`@Service`/`@Configuration`이 하나도 없으므로(도메인 서비스는 POJO, 빈 등록은 infra `DomainServiceConfig`), 4개 앱의 `scanBasePackages`(및 admin/ceo의 `@ComponentScan basePackages`)에서 domain 패키지 엔트리를 삭제했다. 남은 엔트리는 각 앱 자신 + `com.tastyhouse.infrastructure`·`com.tastyhouse.external`·`com.tastyhouse.security`(web/admin/ceo)·`com.tastyhouse.logging`이다.

**(7) ArchUnit 규칙 개정 — 공허 통과 제거**:

- `applicationShouldNotDependOnWebLayer`(`..application..` 패키지 매칭, 대상 0건으로 공허 통과) → **`applicationServicesShouldNotDependOnWebLayer`**로 개정. 전환 후 서비스가 `..application..`이 아니라 도메인 패키지에 놓이므로, 클래스 **이름**(`*CommandService`/`*QueryService`)으로 대상을 잡는다. 차단 대상은 web **플럼빙**으로 한정: `org.springframework.web.bind..`, `org.springframework.web.servlet..`, `org.springframework.http..`, `jakarta.servlet..`.
  - `org.springframework.web.multipart.MultipartFile`은 **제외**했다. 업로드 자체를 받는 경계 타입이라 ceo-api의 이미지 변경요청·콘텐츠보드 서비스가 정당하게 파라미터로 쓰며, 이를 금지하려면 업로드 흐름 재설계가 필요해 이번 범위를 벗어난다.
- `shouldNotDependOnQuerydsl`·`shouldNotDependOnInfrastructurePersistence`·`controllersShouldNotDependOnRepositories`에서 **`allowEmptyShould(true)`를 제거**했다. 대상이 0건이면 초록불이 아니라 실패로 드러나야 한다. 예외는 `batch-module`의 web-layer 규칙 하나뿐이다 — 이 모듈만 클래스명이 `*SchedulerService`라 `*CommandService`/`*QueryService` 대상이 0개이므로 그 규칙에 한해 유지했다.

### 검증 방법과 결과

이 프로젝트는 `gradle build` 실행이 금지되어 있어(루트 `CLAUDE.md` 규칙), **javac + annotationProcessor를 직접 호출하는 방식**으로 전 모듈을 컴파일하고 JUnit을 실행해 검증했다.

| 검증 항목 | 결과 |
|---|---|
| 전 9개 모듈 javac 컴파일 | 오류 **0** (main 소스 1,430개 파일) |
| `domain-module` 단위 테스트 | **368개 전부 통과**(순수 JUnit, 스프링 컨텍스트·DB 불필요) |
| api 4개 모듈 ArchUnit `LayerRulesTest` | **15개 테스트 전부 통과**(web/admin/ceo 각 4개 + batch 3개) |
| `grep -r "com.tastyhouse.core" --include="*.java"` | **0건** |
| api 4모듈 `src/main`의 `com.querydsl` / `@QueryProjection` / `..persistence..` import | 각 **0건** |
| DB 스키마·DDL | **무변경**(순수 코드 재배치 — 테이블·컬럼 매핑 동일, `ddl-auto=validate` 그대로) |

### 이 전환으로 빌드 그래프가 강제하게 된 것

전환의 핵심 성과는 "문서상의 규칙"이 **컴파일 실패로 드러나는 구조적 제약**이 되었다는 점이다.

1. **domain은 프레임워크를 모른다.** `domain-module/build.gradle`에 Spring·JPA·QueryDSL 의존이 아예 없으므로, 도메인 코드에 `@Transactional`이나 `@Entity`, `com.querydsl.*`를 넣으려는 시도는 문서 위반이 아니라 **컴파일 에러**다. 이는 도메인 단위 테스트가 영구히 가벼움을 보장한다(368개 테스트가 컨텍스트 없이 돈다).
2. **api는 QueryDSL과 persistence 어댑터를 모른다.** `querydsl-jpa`가 `infrastructure-module`의 `implementation`이라 api 클래스패스에 없고, 그 위에 ArchUnit이 `com.querydsl..`·`..infrastructure..persistence..` 의존을 실패로 만든다. 조회를 하려면 반드시 `<ctx>/query/` DAO를 거쳐야 하므로, "컨트롤러가 편의상 QueryDSL 한 줄" 같은 우회가 원천적으로 불가능하다.
3. **Q타입 생성은 한 모듈에서만 일어난다.** annotationProcessor가 `infrastructure-module`에만 있어 Q타입 생성 위치가 모호해질 여지가 없다.
4. **레이어 규칙이 공허하게 통과하지 않는다.** `allowEmptyShould(true)` 제거로, 리팩터링이 규칙의 매칭 대상을 없애버리면 그 사실이 실패로 즉시 드러난다.

### 남아 있는 것 / 후속 과제

- **`MultipartFile` 경계**: ceo-api의 업로드 서비스가 `MultipartFile`을 파라미터로 받으므로 web-layer 규칙에서 제외 중이다. 업로드 흐름을 재설계(바이트/스트림 + 메타데이터로 경계 타입 축소)하면 이 예외를 없앨 수 있다.
- **`batch-module`의 web-layer 규칙 `allowEmptyShould(true)`**: 스케줄러 서비스 네이밍이 `*SchedulerService`라 대상이 0개다. 네이밍을 CQRS 쌍에 맞추거나 규칙 대상 조건을 조정하면 예외를 없앨 수 있다.
- **흐름 지향 서비스의 CQRS 미적용**: 인증·회원·파일·등급·추천처럼 애그리거트 CRUD가 아닌 서비스는 web-api에서 단일 `Service`로 남아 있다(`AuthService`·`MemberService`·`FileService`·`GradeService`·`ReferralService`). CQRS 쌍이 자연스럽지 않은 유스케이스라 의도된 상태다.
