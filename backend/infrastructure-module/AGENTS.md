# infrastructure-module

`domain-module`의 순수 도메인 모델을 영속화하고, 표현 목적 조회를 캡슐화하는 **인프라 어댑터 모듈**. 헥사고날 아키텍처에서 `domain-module`이 선언한 포트(`<ctx>/repository/XxxRepository` write 포트, `shared/event/DomainEventPublisher`)를 JPA/QueryDSL/Spring으로 구현한다. `external-api`가 파일/OAuth/PG 어댑터를 담당하는 것과 같은 원리로 DB 어댑터를 domain 밖으로 분리해 "domain은 프레임워크를 모른다"를 모듈 경계로 강제한다.

**QueryDSL이 이 모듈 안에 갇혀 있다는 점이 이 모듈의 또 하나의 정체성이다.** Q타입 생성(annotationProcessor)이 전 프로젝트에서 이 모듈에서만 일어나고, `querydsl-jpa`는 `implementation`으로만 의존해 소비 모듈(web/admin/ceo/batch)로 전이되지 않는다. 조회는 이 모듈의 `<ctx>/query/` DAO가 캡슐화하며, api 모듈은 그 DAO와 Result DTO만 주입·import한다.

## 패키지 구조

```
com.tastyhouse.infrastructure/
├── InfrastructurePersistenceConfig.java  @EnableJpaRepositories/@EntityScan(basePackageClasses) +
│                                         @EnableJpaAuditing + @EnableTransactionManagement
├── config/QueryDslConfig.java            JPAQueryFactory 빈
├── shared/persistence/BaseEntity.java    @MappedSuperclass — @CreatedDate/@LastModifiedDate 감사 필드
├── shared/event/SpringDomainEventPublisher.java  domain DomainEventPublisher 포트 구현(ApplicationEventPublisher 위임)
└── <도메인>/
    ├── config/<Ctx>DomainConfig.java     @Configuration(proxyBeanMethods = false) —
    │                                     그 컨텍스트 domain <ctx>/service/ POJO들의 @Bean 등록
    ├── persistence/                      write 어댑터
    │   ├── XxxJpaEntity.java             @Entity — DB 매핑 전용(비즈니스 행위 없음), BaseEntity 상속
    │   ├── XxxMapper.java                도메인 ↔ 엔티티 변환 (package-private, toDomain/toEntity/applyChanges)
    │   ├── XxxJpaRepository.java         Spring Data JpaRepository<XxxJpaEntity, Long>
    │   ├── XxxRepositoryImpl.java        @Repository — domain XxxRepository(write 포트) 구현
    │   └── XxxIdConverter.java           AttributeConverter<XxxId, Long> (@Convert FK VO 매핑)
    ├── query/                            read 어댑터 (CQRS query 측)
    │   ├── XxxQueryDao.java              @Repository — JPAQueryFactory + QXxxJpaEntity로 Result 직접 투영
    │   ├── XxxListItemResult.java        Result DTO(@QueryProjection) — QXxxResult가 이 모듈에서 생성됨
    │   └── XxxSearchCondition.java       검색 조건 record
    └── listener/                         크로스커팅 도메인 이벤트 리스너(@TransactionalEventListener)
```

현재 `<ctx>/query/`를 가진 도메인: `banner`·`bug`·`ceo`·`coupon`·`event`·`faq`·`member`(+`follow`/`referral`)·`notice`·`order`·`partnership`·`payment`·`point`·`policy`·`product`·`rank`·`reservation`·`review`·`search`·`shop`. `<ctx>/listener/`를 가진 도메인: `coupon`·`file`·`mail`·`member`·`payment`·`point`·`product`·`sms`.

## 규칙

- **패키지 루트는 `com.tastyhouse.infrastructure`** — web/admin/ceo/batch의 `scanBasePackages`에 이 패키지가 등록되어 있어야 빈(RepositoryImpl·QueryDao·Listener·Config)이 인식된다. JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)뿐 아니라 **JPA Auditing(`@EnableJpaAuditing`)·트랜잭션 관리(`@EnableTransactionManagement`) 전역 설정도 이 모듈의 `InfrastructurePersistenceConfig`가 `basePackageClasses`(타입 세이프)로 스스로 선언**한다. domain-module은 이 모듈을 의존하지 않아 컴파일 타임에 이 패키지를 볼 수 없으므로, 엔티티·리포지토리를 소유한 모듈이 스스로 선언하는 것이 Spring Boot 공식 권장과 일치한다.
- **api 모듈은 이 모듈을 `implementation`으로 의존**한다. `{도메인}QueryService`가 `<ctx>/query/`의 DAO를 컴파일 타임에 직접 주입하기 때문이다(과거 `runtimeOnly` 은닉에서 변경). 대신 은닉은 의존 스코프가 아니라 **ArchUnit 규칙**이 담당한다 — api 모듈에서 `..infrastructure..persistence..`(write 어댑터) import와 `com.querydsl..` 의존은 금지이고, `..query..`만 허용된다(`<ctx>/listener/`는 빈으로만 동작해 import 자체가 없다).
- **QueryDSL은 이 모듈 안에 갇힌다**: `querydsl-jpa`는 `api`가 아니라 `implementation`으로 의존해 소비 모듈에 전이 노출되지 않는다. `domain-module`도 `querydsl-core`/`querydsl-apt` 의존을 완전히 제거했으므로, **전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 이 모듈 하나뿐**이다. api 4개 모듈 `src/main`의 `com.querydsl.*` import·`@QueryProjection` 선언은 0건이며 각 모듈 `architecture/LayerRulesTest`가 이를 강제한다.
- **Q타입 생성 위치**: `QXxxJpaEntity`(엔티티)와 `@QueryProjection` Result DTO의 `QXxxResult` **둘 다 이 모듈에서 생성**된다(`build/generated/sources/annotationProcessor/java/main`). domain-module에는 apt가 없어 Q타입이 생성되지 않는다.
- **JPA 엔티티(`XxxJpaEntity`)는 영속 전용**: 행위 메서드를 두지 않고, 신규 생성용 정적 팩토리 `create(...)`와 update 복사용 `applyChanges(...)`만 둔다(update 경로가 없는 애그리거트는 `applyChanges`도 두지 않는다). 감사 필드는 `shared/persistence/BaseEntity`(`@MappedSuperclass`)에서 상속한다 — 단 `mail`·`sms` 인증 도메인처럼 `updated_at` 컬럼이 없는 테이블은 `BaseEntity`를 상속하지 않는다.
- **`@Embedded` VO 컬럼 매핑은 이 모듈이 소유한다**: domain의 VO(`PhoneNumber`·`ProductDiscountInfo`·`VerificationCode`)는 어노테이션 없는 순수 `record`이므로, 컬럼 매핑을 각 JpaEntity에서 `@Embedded` + `@AttributeOverride`(복수 필드는 `@AttributeOverrides`)로 재선언한다. `@AttributeOverride(name = ...)`의 `name`은 record 컴포넌트명과 정확히 일치해야 한다(reference: `MemberJpaEntity`/`EventWinnerJpaEntity`/`SmsVerificationJpaEntity`의 `PhoneNumber` 매핑, `ProductJpaEntity`의 `ProductDiscountInfo`).
- **저장 시맨틱은 load-copy-save**: `save(domain)`에서 id null이면 insert, id 있으면 managed 엔티티를 PK로 조회 후 `Mapper.applyChanges` 복사(동일 트랜잭션 1차 캐시 히트 — 추가 쿼리 없음). detached `save()`(merge)는 `@CreatedDate(updatable = false)` 감사 필드 파손·전 필드 UPDATE 문제로 금지한다.
- **낙관적 락 예외 번역은 이 모듈 책임**: 스프링 `ObjectOptimisticLockingFailureException`을 catch해 프레임워크-프리 `OptimisticLockConflictException`(domain `shared/exception/`)으로 번역한다(reference: `reservation/persistence/ReservationSlotRepositoryImpl`). 경합을 커밋 전에 노출시켜야 하는 지점은 write 포트에 `saveAndFlush`를 둔다.
- **`getReferenceById`/`getOne` 사용 시 주의**: 이 프로젝트는 현재 두 메서드를 어디서도 쓰지 않는다. 쓰게 되면 lazy proxy 접근 시 `jakarta.persistence.EntityNotFoundException`(도메인의 `ResourceNotFoundException`과 무관한 JPA 예외)이 던져질 수 있는데, `GlobalExceptionHandler`는 도메인 `BusinessException` 계층만 처리하므로 이 예외는 `Exception` 핸들러에 잡혀 404가 아닌 500이 된다. 사용한다면 호출부에서 반드시 도메인 예외로 번역할 것.
- **엔티티 enum 매핑**: 항상 `@Enumerated(EnumType.STRING)` + `@Column(length = n, columnDefinition = "VARCHAR(n)")`. `columnDefinition`을 빼면 Hibernate 6 `MySQLDialect`가 네이티브 `ENUM`을 기대해 `ddl-auto=validate`가 실패한다. `EnumType.ORDINAL` 금지. DDL은 `VARCHAR(n)` + 허용값 주석. 상세는 루트 `CLAUDE.md` "enum ↔ DB 컬럼 매핑 규칙".
- **도메인 서비스 빈 등록은 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 담당**: domain의 `<ctx>/service/` 클래스들은 `@Service`/`@Component`가 없는 순수 POJO이므로 컴포넌트 스캔에 잡히지 않는다. 각 컨텍스트의 `@Configuration(proxyBeanMethods = false)`이 write 포트·출력 포트를 주입해 `@Bean`으로 조립한다. **domain에 새 도메인 서비스를 추가하면 해당 컨텍스트의 `<Ctx>DomainConfig`에 `@Bean` 메서드를 추가한다(그 config가 없으면 신설)** — 누락 시 부팅 시 주입 실패.
  - 과거에는 모듈 루트의 `DomainServiceConfig` 하나가 17개 컨텍스트의 `@Bean` 55개를 전부 조립했으나(959줄), 모든 도메인 작업이 이 한 파일을 수정해 리포지토리에서 가장 충돌이 잦은 파일이 되어 컨텍스트별로 분할했다. `InfrastructureModuleConfig`가 `com.tastyhouse.infrastructure` 전체를 `@ComponentScan`하므로 앱 쪽 변경 없이 자동 등록된다.
  - **빈 이름(= `@Bean` 메서드명)은 바꾸지 않는다** — `@Qualifier` 참조가 깨질 수 있다.
  - **컨텍스트 분류가 애매한 빈**(여러 컨텍스트 서비스를 파라미터로 받는 것)은 **반환 타입이 속한 컨텍스트**의 config에 둔다.
  - member의 하위 컨텍스트(`follow`·`referral`) 빈은 `member/config/MemberDomainConfig`에 함께 둔다(별도 파일로 쪼개지 않음).
  - **모듈 진입점인 `InfrastructureModuleConfig`·`InfrastructurePersistenceConfig`는 모듈 루트에 그대로 둔다**(apps가 `@Import`하므로 경로 변경 금지). `<ctx>/config/` 규칙은 신설 도메인 서비스 config에만 적용된다.
- **이벤트 리스너는 `<ctx>/listener/`에 둔다**: 특정 api 모듈에 두면 다른 모듈이 같은 이벤트를 트리거할 때 리스너가 없어 누락되므로, 크로스커팅 리스너는 모든 실행 모듈이 스캔하는 이 모듈에 둔다.

reference 구현: `notice` 도메인 — write 어댑터 `notice/persistence/`(`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl` — 단건 로드·저장만), read 어댑터 `notice/query/`(`NoticeQueryDao` + `NoticeManagementListItemResult`/`NoticeListItemResult`/`NoticeDetailResult`/`NoticeSearchCondition`).

## `<ctx>/query/` — read 어댑터 (CQRS query 측)

표현 목적 조회(목록·검색·페이징·상세)는 write 포트(`XxxRepository`)가 아니라 이 패키지의 `{도메인}QueryDao`(`@Repository`)가 담당한다. DAO는 같은 모듈의 `JPAQueryFactory`와 `QXxxJpaEntity`로 JPA 엔티티에서 Result DTO로 **직접 투영**하며(도메인 모델을 거치지 않음), Result DTO·SearchCondition도 이 패키지가 소유한다. 반환 페이징 타입은 domain의 `shared/page/PageResult`, 페이징 입력은 `shared/page/PageQuery`다.

- **도메인당 DAO 1개, 소비자별 메서드 분리**: admin용/web용/ceo용 메서드를 한 DAO에 둔다. 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다(`findAllNotices`=비노출 포함 전체 / `findVisibleNotices`=노출분만). 대형 도메인(`shop` 등, 대략 400줄 초과)만 용도별 DAO 분리를 허용한다.
- **Result 접미어는 `Result`로 통일하고 `Dto`는 쓰지 않는다**. admin 전용 Result가 비-admin 형제와 같은 패키지에 공존해 충돌하면 `Management` 한정어를 부여한다(`NoticeManagementListItemResult` vs `NoticeListItemResult`). 필드 셋이 다른 admin/web Result는 통합하지 않는다(과잉 노출 방지). 타입명에 역할 마커 `Admin`은 붙이지 않는다.
- **write 포트 잔류 판정**: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?" — 그렇다면 write 포트에 남기고(`findById`/`existsByX`/락 획득용 조회), 화면 조립용이면 이 DAO로 보낸다.
- **소비 모듈이 실제 쓰는 메서드·필드만 이관**한다(미사용은 삭제).
- **소비 모듈은 web/admin/ceo-api만이 아니다**: `batch-module`도 이 DAO를 직접 소비한다(reference: `product/query/ProductQueryDao#findFirstBbqSyncTarget` — BBQ 옵션 동기화 대상 조회). batch 역시 QueryDSL을 알지 않는다.

### QueryDSL 동적 where 조건 조립 규칙 (`BooleanBuilder` 대신 `BooleanExpression` varargs 헬퍼)

동적 검색(필터가 null이면 조건 무시)은 `BooleanBuilder` + `if`문이 아니라, **`private BooleanExpression xxxEq(arg)` 헬퍼(arg가 null이면 null 반환) + `.where(가변인자)`** 로 조립한다. QueryDSL이 `.where(...)`에 전달된 null 인자를 자동으로 무시하므로 이것으로 동적 쿼리가 된다. 정적 고정 조건(필터링 대상이 아닌 조건)은 헬퍼 없이 인라인으로 둔다. **이 규칙은 QueryDSL을 소유한 이 모듈의 규칙이다**(과거 core-module AGENTS.md에 있었으나 QueryDSL이 이 모듈에만 남아 이관됨).

```java
// 권장 — BooleanExpression 헬퍼 + varargs where
.where(
    noticeJpaEntity.deleted.isFalse(),  // 정적 고정 조건은 인라인
    titleContains(condition.title()),   // 동적 조건은 헬퍼로
    visibleEq(condition.visible())
)
...
private BooleanExpression titleContains(String title) {
    return StringUtils.hasText(title) ? noticeJpaEntity.title.containsIgnoreCase(title) : null;
}

private BooleanExpression visibleEq(Boolean visible) {
    return visible != null ? noticeJpaEntity.visible.eq(visible) : null;
}
```

```java
// 지양 — BooleanBuilder + if
BooleanBuilder where = new BooleanBuilder();
if (condition.title() != null) { where.and(noticeJpaEntity.title.containsIgnoreCase(condition.title())); }
```

- `BooleanBuilder`는 OR 조합·복잡한 그룹핑처럼 varargs `.where(...)`(AND만 지원)로 표현 불가능한 경우에만 예외적으로 쓰고, 그 이유를 주석으로 남긴다.
- 서브쿼리로 ID 집합을 먼저 계산해 교집합하는 등 **where 조립이 아닌 선행 데이터 계산**은 이 규칙 대상이 아니다(계산된 집합을 최종 where에 넣을 때만 `xxxIn(Set<Long>)` 헬퍼를 쓴다).
- **크로스 도메인 조인은 정식 Q타입으로 한다**: 전 도메인이 이 모듈로 이동해 모든 JPA 엔티티 Q타입이 같은 모듈에 있으므로, 다른 도메인 엔티티를 조인할 때 `QXxxJpaEntity`를 직접 import한다. 과거 전환 과도기에 쓰였던 `PathBuilder<Object>("XxxJpaEntity")` 문자열 우회는 전부 정식 Q타입 조인으로 복원되었으며, 신규 코드에서 이 우회를 다시 도입하지 않는다.

reference 구현: `notice/query/NoticeQueryDao`.

**대형 도메인 용도별 DAO 분리 reference: `shop`** — 소비 모듈 3개(web/admin/ceo)가 함께 쓰는 최대 도메인이라 DAO를 용도별로 3개로 나눴다.

| DAO | 담당 |
|---|---|
| `ShopQueryDao` | 가게별 설정·관리 조회(전화번호·편의정보·콘텐츠보드·위생뱃지·이미지 변경요청·편의시설/음식유형 카테고리·배정·배너·사진) |
| `ShopSearchQueryDao` | 목록·검색 대형 조인(지도 마커·베스트·최신·키워드 검색·즐겨찾기·관리 목록) |
| `ShopChoiceQueryDao` | 가게에 종속되지 않는 독립 조회(에디터 추천 목록·전역 태그·역 목록) |

- 목록 조회는 페이지 대상 가게를 먼저 뽑고 역·썸네일·음식유형·리뷰수·즐겨찾기수를 shopId 일괄 조회(in절)로 채운다 — 컬렉션 필드(음식유형 다건)가 있어 단일 조인 투영은 카티전 곱이 생기기 때문이다.
- **필드 셋이 달라 Result를 통합하지 않은 사례**: 사진 카테고리 이미지 조회는 회원용 `ShopPhotoCategoryImageResult`(노출분 표시용)와 관리용 `ShopPhotoCategoryImageManagementResult`(`visible` 포함 — 관리 화면은 미노출 이미지도 상태와 함께 보여줘야 함)로 나뉜다. 같은 패키지에 공존해 충돌하므로 `Management` 한정어를 부여했다.
- **write 포트 잔류 판정이 갈린 사례**: `findBusinessHoursByShopId`·`findBreakTimesByShopId`·`findClosedDaysByShopId`·`findByShopId`(임시중지·임시휴무)는 표현용으로도 쓰이지만 **휴게시간 범위 검증·정기휴무 개수 제한·영업 상태 판정**이라는 불변식에 필요하므로 write 포트(`ShopDetailRepository` 등)에 남겼다. 반면 Result DTO를 반환하던 카테고리·배정·배너·사진 목록은 전부 DAO로 보냈다.

## 설정 파일 (`src/main/resources/application-infrastructure.yml`)

이 모듈이 실제로 구현·소비하는 datasource/hibernate(`ddl-auto`)/mysql driver/`spring.sql.init` 등 JPA·DB 설정을 이 모듈의 `application-infrastructure.yml`이 소유한다(과거 `core-module`의 `application-core.yml`이었으나, 도메인 모듈이 JPA-free로 전환되며 이 모듈로 이동·리네이밍됨). 실행 모듈(`web-api`/`admin-api`/`ceo-api`/`batch-module`)의 `application.yml`이 `spring.config.import: classpath:application-infrastructure.yml`로 로딩하며, 이는 `application-external.yml`(external-api 소유)·`application-security.yml`(security-module 소유)·`application-logging.yml`(logging-module 소유)과 동일한 패턴이다.

## Dependencies

### Internal
- `domain-module` (api) — 도메인 모델·write 포트·출력 포트·`shared/page`·`shared/event`·`shared/exception`·`exception` 참조

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (**implementation** — 소비 모듈 전이 차단. OpenFeign 포크는 CVE-2024-49203 대응이며 패키지명 `com.querydsl.*` 유지, 6.x부터 jpa는 `:jakarta` classifier 없이 jakarta 기본·apt만 `:jakarta` 유지) + `querydsl-apt` annotationProcessor

<!-- MANUAL: -->
