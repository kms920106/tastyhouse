<!-- Parent: ../../AGENTS.md -->

# infrastructure:persistence

> **경로 이동 (챕터 05)**: 이 모듈은 `infrastructure-module/`에서 **`infrastructure/persistence/`로 이동**했고 Gradle 좌표는 `:infrastructure:persistence`다. 자바 패키지(`com.tastyhouse.infrastructure..`)와 `application-infrastructure.yml`은 **불변**이다. **클래스명은 챕터 02로 바뀌었다** — 모듈 진입점 `InfrastructureModuleConfig`는 **`PersistenceModuleAutoConfiguration`**으로 리네임 + `@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)`로 전환됐고(`InfrastructurePersistenceConfig`는 이름 불변), `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록해 앱은 더 이상 `@Import`하지 않는다. 그 밖의 본문 패키지 경로는 그대로 유효하다. 형제 모듈 `infrastructure:redis`가 Redis를(`../redis/AGENTS.md`), `infrastructure:external`이 외부 연동 코어를(`../external/AGENTS.md`) 소유하고, 실제 외부 어댑터는 `infrastructure:{firebase,aws,oauth,payment,messaging,crawling}`이 기술별로 나눠 갖는다 — 전부 driven 어댑터다.
>
> 재편 이유는 `infrastructure` 아래를 **기술별로** 나누기 위해서다 — 모듈 이름이 곧 "infrastructure = DB"라는 암묵 전제가 되지 않게 한다.

`domain`의 순수 도메인 모델을 영속화하고, 읽기 계약 패키지 `com.tastyhouse.application..port.out`이 선언한 읽기 포트를 구현하는 **인프라 어댑터 모듈**. 헥사고날 아키텍처에서 `domain`이 선언한 포트(`<ctx>/repository/XxxRepository` write 포트, `shared/event/DomainEventPublisher`)를 JPA/QueryDSL/Spring으로 구현하고, 그 읽기 포트(`{Ctx}QueryPort`)도 함께 구현한다. 외부 연동 모듈들이 파일/OAuth/PG 어댑터를 담당하는 것과 같은 원리로 DB 어댑터를 domain 밖으로 분리해 "domain은 프레임워크를 모른다"를 모듈 경계로 강제한다.

**QueryDSL이 이 모듈 안에 갇혀 있다는 점이 이 모듈의 또 하나의 정체성이다.** Q타입 생성(annotationProcessor)이 전 프로젝트에서 이 모듈에서만 일어나고, `querydsl-jpa`는 `implementation`으로만 의존해 소비 모듈(web/admin/ceo/batch)로 전이되지 않는다. 조회는 이 모듈의 `<ctx>/query/` DAO가 캡슐화하지만, **그 계약(포트 인터페이스와 Result·SearchCondition 입출력 타입)은 이 모듈이 아니라 `application` 모듈이 소유한다** — api 모듈은 그 포트 인터페이스만 주입·import하고, `com.tastyhouse.infrastructure..`는 전혀 알지 않는다(읽기 경로 포트화, 챕터 04).

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
    ├── query/                            read 어댑터 (CQRS query 측) — **DAO만 소유(개정)**
    │   └── XxxQueryDao.java              @Repository — com.tastyhouse.application..port.out의 읽기 포트를 implements.
    │                                     (챕터 04 이후 포트는 소비 앱별로 갈려 DAO 하나가 여러 개를 구현한다)
    │                                     JPAQueryFactory + QXxxJpaEntity로 `Projections.constructor(XxxResult.class, ...)` 투영
    └── listener/                         크로스커팅 도메인 이벤트 리스너(@TransactionalEventListener)
```

**Result record·SearchCondition은 이 패키지에 없다 (개정 — 읽기 경로 포트화, 챕터 04).** `{용도}Result`·`{도메인}SearchCondition`은 `com.tastyhouse.application.<ctx>.port.out`으로 이관됐고, 소유 모듈은 `application` 하나다(챕터 04로 공유 계약까지 돌아와 단독 소유가 됐다). `<ctx>/query/`에는 이제 읽기 포트를 구현하는 `XxxQueryDao`만 남는다.

현재 `<ctx>/query/`를 가진 도메인: `banner`·`bug`·`ceo`·`coupon`·`event`·`faq`·`member`(+`follow`/`referral`)·`notice`·`order`·`partnership`·`payment`·`point`·`policy`·`product`·`rank`·`reservation`·`review`·`search`·`shop`. `<ctx>/listener/`를 가진 도메인: `coupon`·`file`·`mail`·`member`·`payment`·`point`·`policy`·`product`·`sms`.

## 규칙

- **패키지 루트는 `com.tastyhouse.infrastructure`** — **챕터 02 이후 앱의 `scanBasePackages`가 아니라 이 모듈의 `PersistenceModuleAutoConfiguration`이 `@ComponentScan("com.tastyhouse.infrastructure")`으로 스스로 스캔**해 빈(RepositoryImpl·QueryDao·Listener·Config)을 등록한다(`redis` 하위 패키지는 `excludeFilters`로 제외 — 그쪽은 `RedisModuleAutoConfiguration`이 갖는다). 앱은 `runtimeOnly project(':infrastructure:persistence')` 한 줄만 갖는다. JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)뿐 아니라 **JPA Auditing(`@EnableJpaAuditing`)·트랜잭션 관리(`@EnableTransactionManagement`) 전역 설정도 이 모듈의 `InfrastructurePersistenceConfig`가 `basePackageClasses`(타입 세이프)로 스스로 선언**한다. domain은 이 모듈을 의존하지 않아 컴파일 타임에 이 패키지를 볼 수 없으므로, 엔티티·리포지토리를 소유한 모듈이 스스로 선언하는 것이 Spring Boot 공식 권장과 일치한다.
- **api 모듈은 소스 레벨에서 이 모듈을 알지 않는다 (개정 — 읽기 경로 포트화, 챕터 04)**: `{도메인}QueryService`는 이제 DAO 구현체가 아니라 `com.tastyhouse.application..port.out`의 `{Ctx}QueryPort` 인터페이스를 컴파일 타임에 주입한다. `com.tastyhouse.infrastructure..`(과거 허용되던 `..query..` 포함) import는 4개 api 모듈에서 **전면 0건**이며, 각 모듈 `LayerRulesTest`가 강제한다(챕터 04의 임시 장치 `shouldNotDependOnInfrastructureQuery`는 챕터 05에서 제거됐다). `..persistence..`(write 어댑터) import와 `com.querydsl..` 의존 금지는 그대로다. Gradle 의존 자체(`implementation project(':infrastructure:persistence')`)는 남아 있다 — 이 모듈이 실행 시점에 빈 스캔 대상이기 때문이며, 소스 import 여부와는 별개다.
- **반대 방향(이 모듈 → application)도 이 모듈의 `LayerRulesTest#shouldNotDependOnApiModules`가 막는다 (개정 — 챕터 03으로 예외 범위 확대)**: 과거(챕터 03까지)는 금지 대상이 `com.tastyhouse.{webapi,adminapi,ceoapi,batch}..` + 앱별 application 패키지 4개(`com.tastyhouse.{web|admin|ceo|batch}application..`)의 개별 열거였으나, 챕터 03의 패키지 평탄화로 그 앱별 패키지가 사라지고 유스케이스·읽기 계약이 `com.tastyhouse.application` 한 패키지에 공존하게 되면서 **금지 대상을 `com.tastyhouse.application..` 전체로 단순화**하고 그중 이 모듈이 구현해야 하는 아웃바운드 계약 패키지 `..port.out..`만 예외로 뺐다. 이 모듈은 `{Ctx}QueryPort`·Result·SearchCondition은 정당하게 import하지만, application의 서비스·UseCase(`<ctx>/service/`·`..port.in..`)는 절대 참조하지 않는다.
- **QueryDSL은 이 모듈 안에 갇힌다**: `querydsl-jpa`는 `api`가 아니라 `implementation`으로 의존해 소비 모듈에 전이 노출되지 않는다. 계약 소유 모듈 어느 쪽도 `querydsl-core`/`querydsl-apt` 의존을 갖지 않으므로, **전 프로젝트에서 QueryDSL을 컴파일하는 모듈은 이 모듈 하나뿐**이다. api 4개 모듈 `src/main`의 `com.querydsl.*` import·`@QueryProjection` 선언은 0건이며 각 모듈 `architecture/LayerRulesTest`가 이를 강제한다.
- **Q타입 생성 위치 (개정됨)**: `QXxxJpaEntity`(엔티티)는 이 모듈에서 생성된다(`build/generated/sources/annotationProcessor/java/main`). **`QXxxResult`(Result DTO의 Q타입)는 더 이상 생성되지 않는다** — Result record가 QueryDSL을 모르는 계약 모듈로 이관되며 `@QueryProjection`을 뗐고, DAO는 `Projections.constructor(XxxResult.class, ...)`로 조립한다(리포 전체 `@QueryProjection` 선언 0건). 계약 소유 모듈 어디에도 apt가 없어 Q타입이 생성되지 않는다.
- **JPA 엔티티(`XxxJpaEntity`)는 영속 전용**: 행위 메서드를 두지 않고, 신규 생성용 정적 팩토리 `create(...)`와 update 복사용 `applyChanges(...)`만 둔다(update 경로가 없는 애그리거트는 `applyChanges`도 두지 않는다). 감사 필드는 `shared/persistence/BaseEntity`(`@MappedSuperclass`)에서 상속한다 — 단 `mail`·`sms` 인증 도메인처럼 `updated_at` 컬럼이 없는 테이블은 `BaseEntity`를 상속하지 않는다.
- **`@Embedded` VO 컬럼 매핑은 이 모듈이 소유한다**: domain의 VO(`PhoneNumber`·`ProductDiscountInfo`·`VerificationCode`)는 어노테이션 없는 순수 `record`이므로, 컬럼 매핑을 각 JpaEntity에서 `@Embedded` + `@AttributeOverride`(복수 필드는 `@AttributeOverrides`)로 재선언한다. `@AttributeOverride(name = ...)`의 `name`은 record 컴포넌트명과 정확히 일치해야 한다(reference: `MemberJpaEntity`/`EventWinnerJpaEntity`/`SmsVerificationJpaEntity`의 `PhoneNumber` 매핑, `ProductJpaEntity`의 `ProductDiscountInfo`).
- **저장 시맨틱은 load-copy-save**: `save(domain)`에서 id null이면 insert, id 있으면 managed 엔티티를 PK로 조회 후 `Mapper.applyChanges` 복사(동일 트랜잭션 1차 캐시 히트 — 추가 쿼리 없음). detached `save()`(merge)는 `@CreatedDate(updatable = false)` 감사 필드 파손·전 필드 UPDATE 문제로 금지한다.
- **낙관적 락 예외 번역은 이 모듈 책임**: 스프링 `ObjectOptimisticLockingFailureException`을 catch해 프레임워크-프리 `OptimisticLockConflictException`(domain `shared/exception/`)으로 번역한다(reference: `reservation/persistence/ReservationSlotRepositoryImpl`). 경합을 커밋 전에 노출시켜야 하는 지점은 write 포트에 `saveAndFlush`를 둔다.
- **`getReferenceById`/`getOne` 사용 시 주의**: 이 프로젝트는 현재 두 메서드를 어디서도 쓰지 않는다. 쓰게 되면 lazy proxy 접근 시 `jakarta.persistence.EntityNotFoundException`(도메인의 `ResourceNotFoundException`과 무관한 JPA 예외)이 던져질 수 있는데, `GlobalExceptionHandler`는 도메인 `BusinessException` 계층만 처리하므로 이 예외는 `Exception` 핸들러에 잡혀 404가 아닌 500이 된다. 사용한다면 호출부에서 반드시 도메인 예외로 번역할 것.
- **엔티티 enum 매핑**: 항상 `@Enumerated(EnumType.STRING)` + `@Column(length = n, columnDefinition = "VARCHAR(n)")`. `columnDefinition`을 빼면 Hibernate 6 `MySQLDialect`가 네이티브 `ENUM`을 기대해 `ddl-auto=validate`가 실패한다. `EnumType.ORDINAL` 금지. DDL은 `VARCHAR(n)` + 허용값 주석. 상세는 루트 `CLAUDE.md` "enum ↔ DB 컬럼 매핑 규칙".
- **도메인 서비스 빈 등록은 컨텍스트별 `<ctx>/config/<Ctx>DomainConfig`가 담당**: domain의 `<ctx>/service/` 클래스들은 `@Service`/`@Component`가 없는 순수 POJO이므로 컴포넌트 스캔에 잡히지 않는다. 각 컨텍스트의 `@Configuration(proxyBeanMethods = false)`이 write 포트·출력 포트를 주입해 `@Bean`으로 조립한다. **domain에 새 도메인 서비스를 추가하면 해당 컨텍스트의 `<Ctx>DomainConfig`에 `@Bean` 메서드를 추가한다(그 config가 없으면 신설)** — 누락 시 부팅 시 주입 실패.
  - **단, 생성자가 요구하는 아웃바운드 포트의 구현이 일부 앱에만 있으면 그 포트를 구현하는 모듈이 등록한다**: `mail/config/MailDomainConfig`·`sms/config/SmsDomainConfig`는 이 예외로 `infrastructure:messaging`(`com.tastyhouse.external.messaging.config`)으로 **이관됐고 이 모듈에 없다**. 두 설정이 `MailSender`·`SmsSender` 빈을 무조건 요구해서 발송 기능이 없는 admin·ceo·batch까지 발송 어댑터를 강제로 들여와야 했기 때문이다. `file/config/FileDomainConfig`의 `FileStoragePort`는 4개 앱 전부가 구현을 가지므로 여기 잔류한다. 주입이 없는 `mail/listener/MailVerificationEventListener`·`sms/listener/SmsVerificationEventListener`도 잔류한다.

    판정 기준은 "외부 연동 포트인가"가 **아니라** "구현이 일부 앱에만 있는가"다. 이것을 헷갈리면 `FileDomainConfig`까지 옮기려 든다.

    **`@ConditionalOnBean(MailSender.class)`으로 persistence에 남기는 대안은 채택하지 않았다.** 사용자 `@Configuration` 사이의 등록 순서에 결과가 좌우돼 **조용히 빈이 빠진다** — 부팅은 성공하고 그 기능만 동작하지 않으므로 발견이 늦다. 이 판단을 모르고 "조건부로 남기면 되지 않나"로 되돌리지 않는다.
  - 과거에는 모듈 루트의 `DomainServiceConfig` 하나가 17개 컨텍스트의 `@Bean` 55개를 전부 조립했으나(959줄), 모든 도메인 작업이 이 한 파일을 수정해 리포지토리에서 가장 충돌이 잦은 파일이 되어 컨텍스트별로 분할했다. `PersistenceModuleAutoConfiguration`(챕터 02로 `InfrastructureModuleConfig`에서 리네임)이 `com.tastyhouse.infrastructure` 전체를 `@ComponentScan`하므로(`com.tastyhouse.infrastructure.redis.*`는 REGEX로 제외 — 챕터 02) 앱 쪽 변경 없이 자동 등록된다.
  - **빈 이름(= `@Bean` 메서드명)은 바꾸지 않는다** — `@Qualifier` 참조가 깨질 수 있다.
  - **컨텍스트 분류가 애매한 빈**(여러 컨텍스트 서비스를 파라미터로 받는 것)은 **반환 타입이 속한 컨텍스트**의 config에 둔다.
  - member의 하위 컨텍스트(`follow`·`referral`) 빈은 `member/config/MemberDomainConfig`에 함께 둔다(별도 파일로 쪼개지 않음).
  - **모듈 진입점인 `PersistenceModuleAutoConfiguration`(구 `InfrastructureModuleConfig`)·`InfrastructurePersistenceConfig`는 모듈 루트에 그대로 둔다**(`AutoConfiguration.imports`가 FQCN으로 참조하므로 경로 변경 금지 — 앱의 `@Import` 때문이 아니라 챕터 02로 그 필요 자체가 사라졌다). `<ctx>/config/` 규칙은 신설 도메인 서비스 config에만 적용된다.
- **이벤트 리스너는 `<ctx>/listener/`에 둔다**: 특정 api 모듈에 두면 다른 모듈이 같은 이벤트를 트리거할 때 리스너가 없어 누락되므로, 크로스커팅 리스너는 모든 실행 모듈이 스캔하는 이 모듈에 둔다. 유실 위험과 리스너/Recorder 선택 기준은 아래 [도메인 이벤트 리스너](#ctxlistener--도메인-이벤트-리스너) 절을 따른다.

reference 구현: `notice` 도메인 — write 어댑터 `notice/persistence/`(`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl` — 단건 로드·저장만), read 어댑터 `notice/query/`(`NoticeQueryDao` + `NoticeManagementListItemResult`/`NoticeListItemResult`/`NoticeDetailResult`/`NoticeSearchCondition`).

## `<ctx>/query/` — read 어댑터 (CQRS query 측, 개정됨 — 읽기 경로 포트화)

표현 목적 조회(목록·검색·페이징·상세)는 write 포트(`XxxRepository`)가 아니라 이 패키지의 `{도메인}QueryDao`(`@Repository`)가 담당한다. **Result·SearchCondition·`{Ctx}QueryPort` 인터페이스는 이제 이 패키지가 아니라 `com.tastyhouse.application.<ctx>.port.out`(소유 모듈은 `application`)이 소유**하고, `XxxQueryDao`는 그 포트를 `implements`한다. DAO는 같은 모듈의 `JPAQueryFactory`와 `QXxxJpaEntity`로 JPA 엔티티에서 Result record로 `Projections.constructor(XxxResult.class, ...)`로 **직접 투영**한다(도메인 모델을 거치지 않음, `@QueryProjection`은 더 이상 쓰지 않음). 반환 페이징 타입은 domain의 `shared/page/PageResult`, 페이징 입력은 `shared/page/PageQuery`다.

- **도메인당 DAO 1개, 소비자별 메서드 분리**: admin용/web용/ceo용 메서드를 한 DAO에 둔다. 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다(`findAllNotices`=비노출 포함 전체 / `findVisibleNotices`=노출분만). 대형 도메인(`shop` 등, 대략 400줄 초과)만 용도별 DAO 분리를 허용한다.
- **DAO 1개 : 포트 N개 (챕터 04)**: 계약 쪽은 DAO와 달리 **소비 앱별로 갈린다**. 한 DAO의 public 표면에 여러 앱의 조회가 섞여 있으면 [소비자별 분할 규칙](../../CLAUDE.md#조회-포트-소비자별-분할-규칙-포트명은-반환-result-계열을-승계--챕터-04)에 따라 포트를 쪼개고 **DAO가 그것을 전부 `implements`** 한다(예: `ShopQueryDao implements ShopQueryPort, ShopBasicInfoQueryPort, ShopManagementQueryPort, ShopOwnerQueryPort`). **DAO 본문은 이 분할로 바뀌지 않는다** — 늘어나는 것은 `implements` 목록뿐이고, `@Override` 개수는 분할 전후가 같아야 한다.
- **포트에 없는 public 메서드도 있을 수 있다**: application 소비자가 없고 infra 내부에서만 쓰는 조회는 포트에 선언하지 않는다(`ShopQueryDao#findShopName` — 같은 모듈의 `ReviewOwnerReplyEventListener`가 구체 타입으로 주입). `MemberReviewCountQueryPort`와 같은 취지이며, `LayerRulesTest#queryDaosShouldImplementQueryPorts`는 DAO가 포트를 하나라도 구현하면 통과하므로 이 형태를 막지 않는다.
- **Result 접미어는 `Result`로 통일하고 `Dto`는 쓰지 않는다**. admin 전용 Result가 비-admin 형제와 같은 패키지에 공존해 충돌하면 `Management` 한정어를 부여한다(`NoticeManagementListItemResult` vs `NoticeListItemResult`). 필드 셋이 다른 admin/web Result는 통합하지 않는다(과잉 노출 방지). 타입명에 역할 마커 `Admin`은 붙이지 않는다.
- **write 포트 잔류 판정**: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?" — 그렇다면 write 포트에 남기고(`findById`/`existsByX`/락 획득용 조회), 화면 조립용이면 이 DAO로 보낸다.
- **소비 모듈이 실제 쓰는 메서드·필드만 이관**한다(미사용은 삭제).
- **소비 모듈은 web/admin/ceo-api만이 아니다**: `batch-module`도 이 DAO를 포트 인터페이스로 직접 소비한다(reference: `product` 도메인의 `ProductQueryPort#findFirstBbqSyncTarget` — BBQ 옵션 동기화 대상 조회). batch 역시 QueryDSL도 `com.tastyhouse.infrastructure..`도 알지 않는다.
- **Result record는 반드시 `public`이고 select 절과 생성자가 일치해야 한다**: `Projections.constructor`는 리플렉션으로 런타임에 생성자를 찾으므로, record가 package-private이거나 select 절 인자 개수·타입·순서가 생성자와 어긋나면 컴파일은 통과하고 **호출 시점에만 500**이 난다. `ProjectionConstructorMatchingTest`(이 모듈)가 select 절 인자 개수와 대상 record의 public 생성자 파라미터 개수 일치를 소스 스캔으로 검증한다. 전환·신규 작성한 쿼리는 반드시 한 번 호출해 확인한다.

### 읽기 계약 가드 2종은 이 모듈이 소유한다 (챕터 09 — `application-common-module`에서 이관)

계약은 `application` 모듈이 소유하지만, **그 계약을 검증하는 가드는 이 모듈에 있다** — 이 모듈이 `application`을 `implementation`으로 의존해 계약이 테스트 런타임 클래스패스에 올라오고, 동시에 그 계약을 투영하는 DAO 소스를 갖고 있기 때문이다.

> **`ReadContractSingleOwnerTest`는 챕터 04에서 삭제됐다.** 같은 FQCN이 두 모듈에 정의되는 것을 막던 가드인데, 공유 계약 55개가 `domain`에서 `application`으로 돌아오며 split package 자체가 사라졌다. 이제 같은 모듈 안의 중복 정의는 컴파일 에러라 가드가 필요 없다.

| 가드 | 무엇을 막나 | 컴파일러가 못 잡는 이유 |
|---|---|---|
| `QueryResultRecordVisibilityTest` | Result record가 package-private인 것 | `Projections.constructor`가 `Class<?>`를 받아 리플렉션으로 찾는다 |
| `ProjectionConstructorMatchingTest` | select 절 인자 개수 ≠ 생성자 파라미터 개수 | 가변인자 `Expression<?>...`라 개수가 어긋나도 통과한다 |

**`public` record 강제의 근거는 실제 장애다.** `ShopRiderGuidePickupPresenceResult`가 "DAO 내부에서만 쓰는 중간 투영이니 노출을 좁힌다"는 의도로 package-private으로 선언되어, admin "라이더 안내 검수" 목록 조회(`GET /api/shops/v1/rider-guides`)가 **전부 500**으로 실패했다. 같은 패키지의 다른 Result record 30여 개는 모두 `public`이라 이 한 건만 어긋난 상태였고, 빌드·리뷰 어디에서도 걸리지 않아 브라우저 검증 단계에서야 발견됐다. 실패 형태는 아래와 같다.

```
com.querydsl.core.types.ExpressionException: No constructor found for class
com.tastyhouse.infrastructure.shop.query.ShopRiderGuidePickupPresenceResult
with parameters: [class java.lang.Long, class java.lang.String, ...]
```

**`@QueryProjection` → `Projections.constructor` 전환의 배경.** 챕터 03까지 Result record는 이 모듈이 소유해 `@QueryProjection`을 달 수 있었고, 생성된 `QXxxResult` 타입이 **컴파일 타임에** 생성자 시그니처를 강제해 주었다. 읽기 경로 포트화(챕터 04) 이후 Result가 QueryDSL을 모르는 계약 모듈로 옮겨가면서 그 어노테이션을 쓸 수 없게 됐다 — 계약 모듈에 querydsl-apt를 붙이는 것은 "QueryDSL은 infra 밖으로 새지 않는다"는 확정 결정의 역행이라 **금지**다. 잃어버린 컴파일 게이트를 위 가드 2종이 대신한다.

```java
// before (챕터 03까지, 이 모듈 소유 시절): @QueryProjection 생성자 — 컴파일 타임 검증
.select(new QNoticeManagementListItemResult(notice.id, notice.title, notice.content, notice.visible, notice.createdAt))

// after: Projections.constructor — 리플렉션, 런타임에만 실패
.select(Projections.constructor(NoticeManagementListItemResult.class,
    notice.id, notice.title, notice.content, notice.visible, notice.createdAt))
```

- **select 절 인자 개수·타입·순서가 record 생성자와 일치해야 한다.** 전환하는 select 절마다 record 컴포넌트 순서와 대조하고, **그 조회 경로를 실제로 한 번 호출해** 확인한다.
- **DAO 본문에 중첩된 `private` 헬퍼 record는 가드 대상이 아니다** — `new`로 직접 조립하는 내부 계산용이라 리플렉션 탐색을 거치지 않는다. 투영에 쓰려면 애초에 독립 파일로 분리해야 하고, 그 시점에 가드 대상이 된다.
- **`FileUrlResolver` 재조립(`withResolvedXxx` 패턴)은 이 전환과 무관하다** — fetch 직후 Result를 재조립하는 로직은 소유 모듈이 바뀌어도 그대로 동작한다.

#### 두 가드가 어디까지 잡고 어디부터 못 잡는가

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/architecture/ProjectionConstructorMatchingTest.java` · `.../shared/query/QueryResultRecordVisibilityTest.java`

- **개수 불일치**는 정적으로 확실히 판별되므로 `ProjectionConstructorMatchingTest`가 잡는다.
- **타입 불일치는 잡지 못한다.** 정적으로 판별하려면 QueryDSL 표현식의 제네릭 타입을 추론해야 하는데 소스 수준 파싱으로는 신뢰할 수 없다. 그 층은 짝 가드(가시성)와 **컨텍스트별 조회 테스트, 그리고 그 조회를 실제로 한 번 호출해 보는 것**이 맡는다.
- **스캔 대상은 소스 파일이다** — 바이트코드에는 가변인자가 배열로 뭉쳐 있어 호출 지점의 인자 개수를 복원할 수 없다. 그래서 이 테스트를 바이트코드 스캔으로 바꾸지 않는다.

##### 인자 순서 뒤바뀜 탐지 (`detectReordering`)

**개수가 같은 채 순서만 뒤바뀌는 것은 이 리포의 반복 사고 유형**이라 별도로 잡는다. 개수 검사만으로는 통과하고 **조용히 틀린 값을 돌려준다.**

판별은 QueryDSL 경로의 **마지막 프로퍼티 이름**(`memberJpaEntity.fullName` → `fullName`)을 같은 자리의 record 컴포넌트 이름과 맞춰 본다. 다만 **불일치 자체를 실패로 삼지 않는다** — 컬럼명과 컴포넌트명이 다른 것은 별칭·VO 언랩·표현용 개명으로 정상이기 때문이다. 실패로 보는 것은 **순열**인 경우뿐이다: 이름 집합이 양쪽에 같은데 순서만 다르면 "이름은 다 있는데 자리가 어긋났다"는 뜻이라 뒤바뀜이 거의 확실하다.

이 방식은 **오탐이 없는 대신 놓치는 경우가 있다**(이름이 애초에 다른 투영). 남는 층은 컨텍스트별 조회 테스트와 코드 리뷰가 맡는다. 두 가지를 완화하지 않는다.

- 이름을 뽑을 수 없는 자리(`stringValue()`·`coalesce()` 등)가 섞였다고 **검사 전체를 포기하지 않는다** — 그러면 실제 투영 대부분이 빠져나간다. 뽑을 수 있는 자리만 본다.
- **이름이 양쪽에서 정확히 한 번씩만 나올 때만 자리를 논한다.** 서로 다른 엔티티의 같은 컬럼(`shopJpaEntity.name`·`productJpaEntity.name`)이 각각 `shopName`·`name`으로 투영되는 형태가 흔해, 중복 이름으로 자리를 추론하면 오탐이 된다.

##### 공허한 통과를 막는 장치

이 모듈의 가드는 **스캔이 아무것도 못 찾으면 규칙이 공허하게 통과**하므로, 대상이 존재하는 것 자체를 먼저 검증한다. 같은 이유로 ArchUnit 규칙에 **`allowEmptyShould(true)`를 쓰지 않는다** — 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다. 이 판단을 "규칙이 실패하니 허용으로 바꾸자"로 되돌리지 않는다.

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

reference 구현: `notice/query/NoticeQueryDao`(`com.tastyhouse.application.notice.port.out.NoticeQueryPort` implements).

**대형 도메인 용도별 DAO 분리 reference: `shop`** — 소비 모듈 3개(web/admin/ceo)가 함께 쓰는 최대 도메인이라 DAO를 용도별로 3개로 나눴다.

| DAO | 담당 |
|---|---|
| `ShopQueryDao` | 가게별 설정·관리 조회(전화번호·편의정보·콘텐츠보드·위생뱃지·이미지 변경요청·편의시설/음식유형 카테고리·배정·배너·사진) |
| `ShopSearchQueryDao` | 목록·검색 대형 조인(지도 마커·베스트·최신·키워드 검색·즐겨찾기·관리 목록) |
| `ShopChoiceQueryDao` | 가게에 종속되지 않는 독립 조회(에디터 추천 목록·전역 태그·역 목록) |

- 목록 조회는 페이지 대상 가게를 먼저 뽑고 역·썸네일·음식유형·리뷰수·즐겨찾기수를 shopId 일괄 조회(in절)로 채운다 — 컬렉션 필드(음식유형 다건)가 있어 단일 조인 투영은 카티전 곱이 생기기 때문이다.
- **필드 셋이 달라 Result를 통합하지 않은 사례**: 사진 카테고리 이미지 조회는 회원용 `ShopPhotoCategoryImageResult`(노출분 표시용)와 관리용 `ShopPhotoCategoryImageManagementResult`(`visible` 포함 — 관리 화면은 미노출 이미지도 상태와 함께 보여줘야 함)로 나뉜다. 같은 패키지에 공존해 충돌하므로 `Management` 한정어를 부여했다.
- **write 포트 잔류 판정이 갈린 사례**: `findBusinessHoursByShopId`·`findBreakTimesByShopId`·`findClosedDaysByShopId`·`findByShopId`(임시중지·임시휴무)는 표현용으로도 쓰이지만 **휴게시간 범위 검증·정기휴무 개수 제한·영업 상태 판정**이라는 불변식에 필요하므로 write 포트(`ShopDetailRepository` 등)에 남겼다. 반면 Result DTO를 반환하던 카테고리·배정·배너·사진 목록은 전부 DAO로 보냈다.

## `<ctx>/listener/` — 도메인 이벤트 리스너

domain이 `shared/event/DomainEventPublisher` 포트로 발행한 도메인 이벤트를 구독하는 크로스커팅 리스너를 둔다. 전부 `@TransactionalEventListener(phase = AFTER_COMMIT)`이며, 어댑터 `shared/event/SpringDomainEventPublisher`가 `ApplicationEventPublisher`로 위임한다.

**미소비 이벤트를 남기지 않는다.** 모든 `*Event` record에는 대응 리스너가 있어야 한다. 리스너 없는 이벤트는 "누군가 처리하고 있겠지"라는 착각을 낳고, 발행 지점만 보고는 그 착각이 드러나지 않는다. 소비 수요가 없다고 판단되면 리스너를 만드는 대신 **이벤트 record와 발행 호출을 함께 삭제**한다 — 둘 중 하나를 고르되 "발행만 하고 두는" 상태는 허용하지 않는다.

### ⚠️ AFTER_COMMIT은 실패하면 조용히 유실된다 — 금전 처리를 리스너에 두지 말 것

**이 프로젝트에는 재시도도 outbox도 없다.** AFTER_COMMIT 리스너가 예외로 죽으면 원본 트랜잭션은 이미 커밋된 뒤이므로 롤백되지 않고, 후속 처리만 소리 없이 사라진다. 로그 한 줄이 남을 뿐 실패를 감지하는 장치가 없다.

payment·point·coupon 리스너는 **금전에 직접 영향을 준다**(포인트 적립·환급·회수). 지금 이 처리들이 리스너에 있는 것은 유실이 허용돼서가 아니라 기존 구조가 그렇기 때문이며, **새로 추가하는 후속 처리에는 이 배치를 선례로 삼지 않는다.**

**판단 기준 — 유실되면 곤란한가?**

| 유실 시 결과 | 두는 곳 |
|---|---|
| 관측성만 손해(로그·통계 누락) | `<ctx>/listener/`의 `@TransactionalEventListener(AFTER_COMMIT)` |
| **데이터가 어긋남**(금전 정산, 목록에서 사라짐, 상태 불일치) | **동기 Recorder 패턴** — 원본 상태 전이와 **같은 트랜잭션**에서 도메인 서비스가 직접 호출 |

동기 Recorder의 선례는 `ShopChangeHistoryRecorder`·`ShopRequestIndexRecorder`다. 특히 후자는 이 판단을 명시적으로 기록해 두었다 — 요청처리 현황은 기록 유실이 곧 "요청이 목록에서 사라짐"이라 이벤트를 쓰지 않고 동기 기록을 택했고, Recorder를 도메인 서비스의 **생성자 필수 의존**으로 받아 새 상태 전이를 추가할 때 배선 필요성이 컴파일 단계에서 드러나게 했다. 상세는 루트 `CLAUDE.md`의 "요청 인덱스 동기화 규칙".

**outbox 도입은 현재 범위 밖이다.** 도입을 검토해야 할 시점의 근거만 남긴다 — (1) 유실 시 데이터가 어긋나는 후속 처리인데 동기 트랜잭션에 넣을 수 없는 경우(외부 API 호출처럼 원본 트랜잭션을 길게 잡으면 안 되는 것), (2) 그런 처리가 여러 컨텍스트에 생겨 Recorder 패턴만으로 감당되지 않는 경우. 그 전까지는 위 표의 두 선택지로 충분하다.

### 리스너 작성 규칙

- **도메인별로 분리한다**: 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드리게 된다. 같은 도메인의 이벤트 여러 개를 한 리스너가 받는 것은 정상이다(`CouponEventListener`가 발급·사용을 함께 받는 형태).
- **규칙 본체를 리스너에 두지 않는다**: 리스너는 이벤트 수신과 트랜잭션 경계만 담당하고, 판단·계산은 도메인 서비스가 갖는다(`PaymentEventListener` → `PointLedgerService`·`PaymentConfirmationService`, `ProductReviewEventListener` → `ProductReviewStatsService`).
- **DB를 쓰면 `@Transactional(propagation = REQUIRES_NEW)`를 붙인다**: AFTER_COMMIT 시점에는 원본 트랜잭션이 이미 끝나 있다. 기록만 하는 핸들러는 붙이지 않는다.

### 리스너 단위 테스트

**리스너 파일마다 `<ctx>/listener/` 아래 대응 테스트를 둔다.** 스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 이벤트 객체로 호출하는 순수 단위 테스트이며, AFTER_COMMIT 발화·`@Async`·트랜잭션 전파 같은 배선 자체는 프레임워크 몫이라 검증하지 않는다.

- **협력자가 있는 리스너**(payment·product)는 mock으로 **무엇을 호출/미호출하는지**를 검증한다. 조건 분기(현장 결제만 적립, `usedPoint > 0`일 때만 환급, `productId == null`이면 통계 미갱신)가 이 리스너들의 실질이고, 잘못되면 이중 정산·환급 누락으로 이어진다.
- **기록만 하는 리스너**(coupon·file·mail·member×2·point·policy·sms)는 `shared/listener/ListenerLogCapture`로 Logback appender를 붙여 **무엇이 기록되는지**까지 확인한다. 로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트가 된다.
- **같은 타입 파라미터가 여러 개면 서로 다른 값을 넣는다**: `ReferralRegisteredEvent`의 추천인·피추천인은 둘 다 `MemberId`라 순서를 바꿔도 컴파일된다 — 값이 뒤바뀌면 "누가 누구를 추천했는지"가 반대로 기록되므로 각각이 제 자리에 들어가는지 확인한다.

reference 구현: `PaymentEventListenerTest`(협력자 mock + 조건 분기 3종 + 환불 접수의 "포인트 미개입" 계약), `ProductReviewEventListenerTest`(null 가드), `CouponEventListenerTest`(로그 캡처 기준 예시), 공용 유틸 `shared/listener/ListenerLogCapture`.

## 설정 파일 (`src/main/resources/application-infrastructure.yml`)

이 모듈이 실제로 구현·소비하는 datasource/hibernate(`ddl-auto`)/mysql driver/`spring.sql.init` 등 JPA·DB 설정을 이 모듈의 `application-infrastructure.yml`이 소유한다(과거 `core-module`의 `application-core.yml`이었으나, 도메인 모듈이 JPA-free로 전환되며 이 모듈로 이동·리네이밍됨). 실행 모듈(`web-api`/`admin-api`/`ceo-api`/`batch-module`)의 `application.yml`이 `spring.config.import: classpath:application-infrastructure.yml`로 로딩하며, 이는 외부 연동 모듈이 각자 소유하는 `application-file-storage.yml`(파일 저장 스타터 `infrastructure:file-storage` — 챕터 03에서 `application-external.yml`을 대체했고 `application-firebase.yml`을 중첩 import한다)·`application-payment.yml`(`infrastructure:payment`)·`application-messaging.yml`(`infrastructure:messaging`)·`application-crawling.yml`(`infrastructure:crawling`)·`application-aws.yml`(`infrastructure:aws`)과, `application-redis.yml`(`infrastructure:redis` 소유)·`application-logging.yml`(logging-module 소유)과 동일한 패턴이다.

## Dependencies

### Internal
- `domain` (api) — 도메인 모델·write 포트·출력 포트·`shared/page`·`shared/event`·`shared/exception`·`exception` 참조
- `application` (implementation) — 읽기 계약(`{Ctx}QueryPort`·Result·SearchCondition)을 구현·투영하기 위해 의존한다(QueryDao가 그 인터페이스를 `implements`). 챕터 04로 공유 계약 55개까지 이 모듈로 돌아와, 읽기 계약은 전부 이 한 의존으로 보인다

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (**implementation** — 소비 모듈 전이 차단. OpenFeign 포크는 CVE-2024-49203 대응이며 패키지명 `com.querydsl.*` 유지, 6.x부터 jpa는 `:jakarta` classifier 없이 jakarta 기본·apt만 `:jakarta` 유지) + `querydsl-apt` annotationProcessor

<!-- MANUAL: -->

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

원문 주석은 챕터 05에서 제거되므로, 이 문서가 그 금지 지시의 유일한 소재지다.

### `SEALED_PERSISTENCE_TO_QUERY` 3건 — read→write 단방향 위반 봉인

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/architecture/LayerRulesTest.java`
→ `SEALED_PERSISTENCE_TO_QUERY` · `persistenceShouldNotDependOnQuery()`

`..persistence..`(write 어댑터)는 `..query..`(read 모델)를 의존하지 않는다. **반대 방향(`..query..` → `..persistence..`)은 정상이다** — DAO가 같은 모듈의 `QXxxJpaEntity`를 static import해 조인하는 것이 조회 구현의 기본 형태다. 금지하는 것은 그 역방향으로, write 경로가 표현용 투영에 결합되면 api 모듈에서 막아 둔 CQRS 교차 주입 금지(`commandServicesShouldNotDependOnQueryDaos`)가 infra 안쪽에서 우회된다.

봉인 구성원 3개 — 전부 *도메인 출력 포트 어댑터*다.

- `com.tastyhouse.infrastructure.product.persistence.ProductReviewStatisticsAdapter`
- `com.tastyhouse.infrastructure.rank.persistence.MemberReviewCountAdapter`
- `com.tastyhouse.infrastructure.search.persistence.KeywordCountAdapter`

이들은 도메인이 선언한 포트를 구현하면서 그 데이터의 소유 도메인이 이미 갖고 있는 read model을 재사용한다(예: 랭킹 집계용 리뷰 수는 리뷰 도메인 소유라 `review/query/`에 있고, 랭킹 포트 어댑터가 그것을 도메인 값 타입으로 옮겨 담는다). write 경로가 아니라 *포트 구현*이므로 위 위험에 해당하지 않지만, 패키지 위치(`..persistence..`)가 규칙의 표현과 어긋나 잡힌다. **규칙 전체를 끄지 않고 클래스명(FQN)으로 명시 제외하며, 목록은 줄어들기만 해야 한다 — 새 항목 추가는 새 위반을 승인하는 것이다.** 해소 방향은 이 어댑터들을 `..persistence..`가 아닌 별도 위치로 옮기는 것이다.

**짝 테스트 2종**.

- `LayerRulesTest.sealedPersistenceToQueryShouldNotBeStale()` — 목록의 클래스가 더 이상 위반하지 않으면(이관·삭제됐으면) 실패시켜, 낡은 항목이 조용히 남아 다른 위반을 가리는 것을 막는다.
- `LayerRulesTest.sealedPersistenceToQueryListShouldNotBeEmpty()` — 목록이 비면 **봉인 장치 자체를 제거하고 순수 강제로 전환하라**고 알린다.

### `INFRA_OWNED_QUERY_PORTS` 1건 — infra 자체 소유 읽기 계약 봉인

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/architecture/LayerRulesTest.java`
→ `INFRA_OWNED_QUERY_PORTS` · `queryDaosShouldImplementQueryPorts()`

봉인 구성원 1개 — `com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryPort`.

읽기 계약은 원칙적으로 응용 계층이 소유하지만, *application 소비자가 하나도 없고* infra 어댑터·DAO만 소비하는 내부 투영 계약은 계약 모듈을 부풀릴 뿐이므로 infra가 자체 소유한다(`ShopNoticeRow` 선례).

**패키지 술어가 아니라 클래스명으로 봉인하는 이유**: 모든 QueryDao가 이미 `com.tastyhouse.infrastructure.<ctx>.query` 패키지에 살기 때문에, 예외를 `resideInAPackage("com.tastyhouse.infrastructure..query..")`로 표현하면 **DAO가 자기 패키지에 인터페이스를 하나 선언하기만 해도 통과한다** — 이 규칙이 원래 잡아야 할 위반("application이 소유해야 할 계약을 infra가 몰래 자기 패키지에 만드는 것")이 그대로 허용 범위가 되어 규칙이 무력해진다. 그래서 FQN으로 명시 제외하며, **목록은 줄어들기만 해야 한다.**

**짝 테스트 2종**: `infraOwnedQueryPortListShouldNotBeStale()`(계약이 사라졌거나 application으로 되돌아갔으면 실패) · `infraOwnedQueryPortListShouldNotBeEmpty()`(목록이 비면 봉인 장치를 제거하고 `queryDaosShouldImplementQueryPorts`를 순수 강제로 되돌리라고 알림).

### `MemberGradeReviewCountAdapter` — 봉인 목록을 늘리는 대신 패키지를 옮긴 선례

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/member/adapter/MemberGradeReviewCountAdapter.java`

**패키지가 `..persistence..`가 아니라 `..adapter..`인 이유**: 이 클래스는 write 어댑터가 아니라 *도메인 출력 포트 구현*이라 read model(`review/query/`)을 재사용하는 것이 정상이다. `..persistence..`에 두면 `LayerRulesTest#persistenceShouldNotDependOnQuery`에 걸리는데, **그 봉인 목록은 "줄어들기만 해야" 하므로 새 항목을 추가하지 않고** 그 규칙이 제시한 해소 방향(포트 어댑터를 `..persistence..` 밖으로)을 따랐다.

**클래스명에 `Grade`가 붙은 이유**: rank 쪽 어댑터와 단순 클래스명이 같으면 스프링이 유도하는 기본 빈 이름(`memberReviewCountAdapter`)이 충돌해 컴포넌트 스캔이 `ConflictingBeanDefinitionException`으로 거부하고 **앱이 부팅하지 못한다.**

### `ProductReviewStatisticsAdapter` — 위치·이름을 그대로 두어 봉인 목록을 늘리지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/product/persistence/ProductReviewStatisticsAdapter.java`

위임 대상이 `ReviewStatisticsQueryDao` → `MenuReviewStatisticsQueryDao`로 바뀌었다(`PRODUCT.rating`의 근거가 REVIEW에서 MENU_REVIEW로 이관됐기 때문). **클래스 위치·이름은 그대로 두므로 `LayerRulesTest`의 `persistenceShouldNotDependOnQuery` 봉인 목록에 항목이 늘지 않는다.**

### `EventQueryDao` — 삭제 필터링은 이관 이전 동작을 그대로 보존한다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/event/query/EventQueryDao.java`

**삭제 필터링은 이관 이전 동작을 그대로 보존한다** — admin 관리 목록/상세와 당첨자 목록은 soft delete 분을 제외하고, **web 노출 목록/상세와 발표 목록은 원본 쿼리에 삭제 필터가 없었으므로 추가하지 않는다.**

썸네일·배너 파일 경로는 `UploadedFileJpaEntity`를 **left join**해 얻는다(파일 미등록 이벤트도 목록에서 누락되지 않도록 inner join을 쓰지 않는다). `Projections.constructor`는 record 생성자로 직접 투영하므로 URL 변환을 투영식에 끼울 수 없어 fetch 직후 재조립한다.

### 이벤트 리스너 단위 테스트 12종 — 현재 동작 봉인 (공통 규칙)

리스너 테스트는 **리스너의 현재 동작을 봉인하는 순수 단위 테스트**다. 아래가 12개 파일 전부에 적용되는 공통 규칙이며, 개별 항목은 이 규칙에서 벗어나는 것만 아래에 따로 적는다.

- **스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 호출한다** — `AFTER_COMMIT` 발화 자체는 프레임워크 몫이라 검증 대상이 아니다. 스프링 배선을 검증하려고 `@SpringBootTest`를 붙이지 않는다.
- 협력자 없이 기록만 하는 리스너는 **무엇이 기록되는지**를 `ListenerLogCapture`로 확인한다.

**공통 유틸**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/shared/listener/ListenerLogCapture.java`

인프라 리스너 9개 중 6개는 협력자 없이 `log.info(...)`만 수행한다. 이런 리스너에서 "무엇을 하는지"는 곧 "무엇을 기록하는지"이므로, **로그를 관측하지 않으면 핸들러 본문을 통째로 지워도 통과하는 공허한 테스트만 남는다.** 그래서 Logback `ListAppender`를 대상 로거에 직접 붙여, 이벤트의 어떤 값이 기록에 반영되는지까지 봉인한다. **사용 후에는 반드시 `detach()`를 호출한다**(JUnit `@AfterEach`) — 떼지 않으면 같은 로거를 쓰는 다른 테스트가 실행될 때 이벤트가 계속 쌓인다.

공통 규칙만 적용되는 파일 — `coupon/listener/CouponEventListenerTest.java` · `member/listener/MemberEventListenerTest.java`(가입·탈퇴는 web-api와 admin-api 양쪽에서 트리거되지만 리스너 자체는 발행 경로를 알지 않는다) · `policy/listener/PolicyActivatedEventListenerTest.java`.

#### 개별 예외 — 공통 규칙 위에 추가로 봉인하는 것

| 대상 (`backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/...`) | 추가로 봉인하는 것 |
|---|---|
| `file/listener/FileUploadedEventListenerTest.java` | 기록되는 것은 **저장 경로**이지 표시용 URL이 아니다 — URL 변환은 조회 시점에 query DAO가 `FileUrlResolver`로 수행하므로 리스너가 경로를 그대로 남기는 것이 정상이다 |
| `mail/listener/MailVerificationEventListenerTest.java` | **이 리스너가 메일을 발송하지 않는 것이 정상**이라는 점을 함께 고정한다 — 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로, 발송은 `MailVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `sms/listener/SmsVerificationEventListenerTest.java` | 위와 동일한 이유로 **발송하지 않음**을 고정한다 — 발송은 `SmsVerificationService#issue`가 발급과 원자적으로 수행한다 |
| `point/listener/PointEventListenerTest.java` | **이 리스너가 잔액을 건드리지 않는 것이 정상**임을 고정한다 — 포인트 증감은 `PointLedgerService`가 이벤트 발행 **이전에** 이미 수행했고 리스너는 기록만 한다. **협력자를 주입받지 않는 생성자가 그 증거이며, 여기에 원장 서비스가 추가되면 이중 정산이 된다** |
| `member/listener/ReferralRegisteredEventListenerTest.java` | referral↔point 두 컨텍스트를 잇는 지점이라 검증 대상이 로깅이 아니라 **적립 2건과 보상 완료 전이가 모두, 그리고 그 순서대로 일어나는가**이다 |
| `notification/listener/ReviewOwnerReplyEventListenerTest.java` | review↔notification을 잇는 지점이라 검증 대상은 **답변 등록 이벤트가 리뷰 작성자 앞으로 알림을 적재하는가**이다. 수신자가 `reviewerMemberId`(작성자)여야 하고 이동 대상이 그 리뷰여야 한다. 가게명은 `ShopQueryDao`로 조회하므로 **조회가 비어 있는 경우까지 함께 봉인한다** — 알림 본문에 "null 사장님"이 새는 것을 막기 위함이다 |
| `payment/listener/PaymentEventListenerTest.java` | 로그만 남기는 다른 리스너와 달리 **실제 금전 효과**(포인트 증감)를 낸다. 따라서 "무엇을 기록하는가"가 아니라 **"어떤 조건에서 원장 서비스를 호출/미호출하는가"**를 검증한다 — 조건 분기가 잘못되면 적립이 이중으로 되거나 환급이 누락되며, `AFTER_COMMIT`이라 실패해도 재시도가 없다. 특히 **환불 요청 접수 시점에는 아무것도 하지 않는 것이 이 핸들러의 계약이다** — 접수 시점에 포인트가 움직이면 이후 취소가 확정될 때 `PaymentCancelledEvent`가 같은 금액을 다시 반영해 **이중 정산**이 된다 |
| `product/listener/ProductMenuReviewEventListenerTest.java` | 상품 평점·평가 수라는 **영속 상태**를 갱신하므로 "어떤 상품 id로 통계 갱신을 호출하는가"를 검증한다. **이벤트 3종 모두가 같은 재집계를 트리거해야 한다** — 하나라도 빠지면 `PRODUCT.rating`이 조용히 낡는다 |

### `MemberReviewCountQueryDaoTest` — 합산·병합·정렬 규칙 봉인

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/review/query/MemberReviewCountQueryDaoTest.java`
→ `MemberReviewCountQueryDao#mergeAndSort`

**이 테스트가 필수인 이유**: 이 DAO를 소비하는 `RankSettlementService`·`GradeSettlementService` 테스트는 포트를 fake로 주입하는 순수 단위 테스트라 DAO의 합산·병합·정렬 변경을 **전혀 잡지 못한다.** 병합·정렬을 쿼리에서 분리해 둔 것도 DB 없이 이 규칙을 검증하기 위해서다.

정렬 규칙: 건수 내림차순 → 마지막 작성 이른 순 → 회원 ID 오름차순.

### `ProductQueryDao#soldQuantityOf` — `Expressions.asNumber(서브쿼리)` 금지

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/product/query/ProductQueryDao.java`
→ `soldQuantityOf(...)`

**`numberTemplate(Long.class, "{0}", ...)`으로 감싼 것을 `Expressions.asNumber(subquery)`로 되돌리지 않는다.** `asNumber`는 반환 타입을 `Object`로 지워버린다 — 서브쿼리 자체는 `Long`을 보고하지만 `asNumber`를 거치면 `getType()`이 `Object`가 되고, `Projections.constructor`는 리플렉션으로 생성자를 찾으므로 **컴파일은 통과한 뒤 조회 시점에** 아래로 터진다.

```
com.querydsl.core.types.ExpressionException: No constructor found for ... class java.lang.Object
```

`popular-products` 500 장애가 실제로 이 계열이었고, `numberTemplate`으로 대상 타입을 명시적으로 고정해 수정했다. `@QueryProjection` → `Projections.constructor` 전환으로 **컴파일 게이트가 이미 사라진 상태**라(위 [읽기 계약 가드 2종](#읽기-계약-가드-2종은-이-모듈이-소유한다-챕터-09--application-common-module에서-이관) 절) 이 자리를 되돌리면 다시 런타임에만 드러난다.

### `ProductQueryDao#soldQuantityOf` — 수량 합은 `sumLong()`이다

**대상**: 위와 같음 → `soldQuantityOf(...)`의 `orderProductJpaEntity.quantity.sumLong()`

`sum()`으로 바꾸지 않는다. 이 저장소의 QueryDSL 포크(OpenFeign 6.11)에서 `sum()`이 `sumAggregate()`로 개명됐고, **수량 합은 `Integer` 범위를 넘길 수 있어 `Long` 집계가 맞다.**

### QueryDSL 투영 생성자는 "미사용"으로 보여도 삭제하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/**/query/*QueryDao.java`가 `Projections.constructor(...)`로 지목하는 모든 Result record

`Projections.constructor`는 **리플렉션으로 생성자를 찾으므로 정적 호출부가 0개**다. IDE·정적분석이 "사용되지 않는 생성자"로 표시하지만 삭제하면 조회 시점에 `No constructor found`로 터진다.

### `ShopQueryDao` 파일 별칭 4종 — 공용 별칭 재사용 금지

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopQueryDao.java`
→ `activeFile` · `contentBoardImageFile` · `menuCollectionImageFile` · `shopThumbnailFile`

`UPLOADED_FILE`을 목적별로 조인하므로 별칭 인스턴스를 목적마다 새로 만든다. **공용 `uploadedFileJpaEntity` 별칭을 재사용하면 다른 목적의 조인과 서로를 덮는다** — 메뉴모음컷 검수 목록은 `SHOP`도 함께 조인하는 경로라 특히 그렇다. 예외도 로그도 없이 값만 틀어지므로 이 별칭들을 공용 인스턴스로 되돌리지 않는다.

### `ShopQueryDao#findFoodTypeCategoryNames` — 아이콘 조인을 붙이지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopQueryDao.java`
→ `findFoodTypeCategoryNames(Long)`

정책 판정(채식 메뉴 등록 불가 카테고리 — product 컨텍스트)에 쓰이는 이름 집합만 뽑는다. 형제 메서드 `findFoodTypeAssignments`처럼 아이콘 파일을 조인하도록 "통일"하지 않는다 — **`activeImageFileId` 결측 시 inner join으로 카테고리가 조용히 누락돼 거절해야 할 요청이 통과한다.**

### `ShopQueryDao#findExposedMenuCollectionImages` — 승인 상태 필터를 호출부로 올리지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopQueryDao.java`
→ `findExposedMenuCollectionImages(Long)`

손님 화면용 메뉴모음컷의 승인 상태 필터는 이 투영이 소유한다. **필터를 소비 측(api 모듈)에 맡기면 새 소비 경로가 생길 때 조용히 빠져 대기·반려 이미지가 손님에게 노출된다.**

### `ShopSearchQueryDao#reviewCountsByShopId` — 두 필터를 함께 유지하고 짝 조회와 일치시킨다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopSearchQueryDao.java`
→ `reviewCountsByShopId(List<Long>)`

숨김(관리자 게시중단)과 사장님만보기를 **둘 다** 제외한다. **`ownerOnly`를 빼면 목록 카드의 리뷰 수만 늘고 가게 리뷰 목록에는 그 리뷰가 없어, 건수 차이로 비공개 리뷰의 존재가 새어나간다**(비로그인도 호출 가능한 경로다). `ReviewStatisticsQueryDao#countVisibleByShopId`와 조건이 **일치해야 하며, 한쪽만 고치면 같은 가게의 두 숫자가 어긋난다.**

### `ShopSearchQueryDao#deliveryAreaCovers` — 미설정 가게 통과와 null 무필터를 제거하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopSearchQueryDao.java`
→ `deliveryAreaCovers(Long)`

- **배달가능지역을 하나도 등록하지 않은 가게는 통과시킨다.** 이 예외를 "엄격하게" 없애면 미설정 가게가 배포 즉시 목록에서 전부 사라진다 — 기존 데이터 대부분이 0건이므로 사실상 서비스가 비는 것과 같다. 주문 접수 검사와 같은 원칙("정보를 안 넣은 것을 닫힌 것으로 보지 않는다")이다.
- **행정동이 `null`이면 필터를 걸지 않는다.** 좁힐 근거가 없을 때 감추는 것은 노출 축소일 뿐이다.
- 이 필터 자체를 없애면 고객이 **결제 마지막 단계에서야** 배달 불가를 안다(`ORDER_DELIVERY_AREA_NOT_COVERED`). `OrderPlacementService#validateDeliveryArea`와 같은 규칙을 유지한다.

### `ShopDeliveryTipQueryDao#findTipRanges` — 하한에 추가 배달팁을 더하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopDeliveryTipQueryDao.java`
→ `findTipRanges(List<Long>)` · `distanceUpperBound(ShopDeliveryTipSettingResult)` · `MAX_DELIVERY_DISTANCE_METERS`

- **추가 배달팁 4종(거리별·지역별·시간별·공휴일) 중 어느 것도 하한에 넣지 않는다.** 넣으면 실제로 달성 가능한 금액보다 높은 "최소 ○○원"을 광고하게 된다. **표시 가격은 실제보다 낮게 틀리는 편이 안전하지 높게 틀리면 안 된다.**
- **거리별 상한에 `DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND`(10,000원)를 그대로 쓰지 않는다.** 그러면 500m당 100원짜리 가게도 "최대 10,000원"으로 표기돼 거의 모든 가게가 같은 과장된 상한을 보인다.
- **이 산출은 현재 시각·고객 주소에 의존하지 않는다.** 목록은 정렬·캐시 대상이라 요청마다 값이 달라지면 안 되므로 "지금 이 주문에 붙는 금액"을 내지 않는다.
- 이 메서드가 하한/상한 산출 규칙의 **유일한 소유자**다(목록·카드·상세·팝업이 공유). 소비 측에 별도 산출을 만들지 않는다.

### `ReviewQueryDao#findMyReviews` ↔ `#findReviewsByMemberId` — 정책이 정반대인 쌍둥이 쿼리

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewQueryDao.java`
→ `findMyReviews(Long, PageQuery)` · `findReviewsByMemberId(Long, PageQuery)` · `visibleToCustomer()`

두 메서드는 쿼리가 거의 같지만 **사장님만보기(`ownerOnly`) 처리가 정반대다.**

- `findMyReviews`(마이페이지 = **본인**) — **사장님만보기 리뷰를 포함한다.** 이미 본인 한정 조회이므로 자기가 비공개로 쓴 리뷰도 보여야 하기 때문이다. 그래서 이 메서드는 `visibleToCustomer()` 헬퍼를 쓰지 않는다.
- `findReviewsByMemberId`(**타인** 프로필) — **사장님만보기 리뷰를 제외한다.** 작성자 본인에게만 보이는 리뷰이므로 타인 프로필에서는 보이면 안 된다.

**한쪽을 고칠 때 다른 쪽을 함께 고치지 말 것.** 쿼리가 닮았다는 이유로 "빠뜨린 것"으로 오해해 맞추면, 비공개 리뷰가 타인에게 새거나 본인이 자기 리뷰를 못 보게 된다.

**단 `hidden`(관리자 게시중단) 필터는 `findMyReviews`에서도 유지한다** — 게시중단은 정책 위반 제재라 사장님만보기보다 상위이며, **본인에게도 보이지 않는 것이 올바른 동작이다.**

뱃지 처리도 이 비대칭을 따른다. `findMyReviews`는 사장님만보기 리뷰를 포함하므로 구분을 위해 `ownerOnly`를 함께 뽑고, 타인 프로필은 `visibleToCustomer()`로 이미 걸렀으므로 **뱃지는 항상 false다.**

### `visibleToCustomer()` — where절과 count절 양쪽에 걸어야 한다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewQueryDao.java`
→ `visibleToCustomer()`

목록의 where절과 count절이 분리된 곳에서는 **양쪽 모두**에 걸어야 한다. **한쪽만 고치면 `totalElements`와 실제 목록 길이가 어긋나 프론트 무한스크롤이 빈 페이지로 깨진다.**

### `ReviewStatisticsQueryDao` — 상품 단위 집계 4종은 `PRODUCT.rating` 재집계용이 아니다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewStatisticsQueryDao.java`
→ `countVisibleByProductId` · `getAverageTasteRatingByProductId` · `getAverageAmountRatingByProductId` · `getAveragePriceRatingByProductId`

`PRODUCT.rating` 재집계의 근거는 **MENU_REVIEW로 완전히 이관되어 `MenuReviewStatisticsQueryDao`가 담당한다.** 이 DAO의 상품 집계는 **상품 상세 화면의 매장 리뷰 통계 응답**(`GET /api/products/v1/{id}/reviews/statistics`·평점대별 목록)이 계속 소비하는 **별개 계약이므로 남는다.**

**상품 평점 재집계 코드를 여기로 되돌리지 말 것.**

### `ReviewStatisticsQueryDao` ↔ `ShopReviewManagementQueryDao` — `hidden` 축의 의도된 비대칭

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewStatisticsQueryDao.java` · `.../ShopReviewManagementQueryDao.java`
→ 통계 DAO의 모든 집계 메서드 · `ShopReviewManagementQueryDao#tabPredicate(ReviewListTab)`

**통계 DAO의 모든 집계는 `hidden = false`로 숨김 리뷰를 제외한다.** 반면 점주 리뷰 목록(`ShopReviewManagementQueryDao`)은 차단 탭을 위해 숨김을 **포함**하므로, 두 화면의 건수가 **의도적으로 다르다** — 목록 `totalElements`가 20인데 대시보드 `totalReviewCount`가 17일 수 있다.

이 비대칭은 실수가 아니라 판단이다. 통계는 "내 가게가 손님에게 어떻게 평가되는가"를 답하는 지표이고 **게시중단된 리뷰는 손님에게 보이지 않으므로 평균·분포에 반영되면 안 된다.** 반대로 목록은 "내가 관리해야 할 리뷰"라서 차단된 것도 보여야 한다.

**다음 세션이 두 화면의 숫자가 다르다는 이유로 한쪽 필터를 맞추지 말 것** — 맞추면 차단된 악성 리뷰가 평점을 계속 끌어내리거나(통계에 포함), 점주가 차단 리뷰를 볼 수 없게 된다(목록에서 제외).

### `ReviewStatisticsQueryDao` — `ownerOnly` 축은 오버로드마다 정반대다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewStatisticsQueryDao.java`
→ `visibleToCustomer()` · `getRatingCounts(Long)` ↔ `getRatingCounts(Long, LocalDateTime, LocalDateTime)` · `getMonthlyReviewCounts(Long, int)` ↔ `getMonthlyReviewCounts(Long, LocalDateTime, LocalDateTime)`

위 `hidden` 축과 달리 **`ownerOnly`(사장님만보기)는 이 DAO 안에서 메서드마다 처리가 정반대다.**

- **고객용(제외)** — 기간 인자가 **없는** 메서드들. 가게 리뷰 수·재방문·항목별 평균 6종·별점 분포·연도별 월간 집계와 상품 집계 4종, 회원 리뷰 수가 여기 속하며 `visibleToCustomer()`로 두 축을 함께 건다.
- **점주용(포함)** — 기간 인자를 **받는** 메서드들(`from`/`to`). 점주는 자기 가게의 실제 피드백을 온전히 봐야 하므로 사장님만보기 리뷰도 통계에 포함된다. 이쪽은 `hidden`만 거르고 `ownerOnly`는 건드리지 않는다.

즉 `getRatingCounts`와 `getMonthlyReviewCounts`는 각각 오버로드가 2개인데 **기간 인자 없는 쪽=고객(두 축 제외), 기간 인자 있는 쪽=점주(hidden만 제외)** 로 동작이 반대다. IDE에서 나란히 보이므로 한쪽을 고칠 때 다른 쪽을 "빠뜨린 것"으로 오해하기 쉽다.

**두 화면의 숫자가 다르다는 이유로 한쪽에 맞추지 말 것** — 맞추면 비공개로 쓴 리뷰가 고객 화면 평점에 새어나가거나(고객용에 포함), 점주가 자기 가게 피드백의 일부를 볼 수 없게 된다(점주용에서 제외).

### `ShopReviewManagementQueryDao#tabPredicate` — 어느 탭에서도 `hidden`을 강제로 끄지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ShopReviewManagementQueryDao.java`
→ `tabPredicate(ReviewListTab)`

`ALL`은 조건 없음이며, **어느 탭에서도 `hidden`을 강제로 끄지 않는다.**

- `UNANSWERED` — 목록에 이미 left join된 사장님 답변이 없는 행이다.
- `BLINDED` — "게시중단 요청이 승인된 것"이 아니라 **리뷰가 실제로 숨겨진 것**을 기준으로 한다. 관리자가 요청 없이 직접 숨긴 리뷰도 점주에게는 차단된 리뷰이기 때문이다.
- `OWNER_ONLY` — 작성자가 비공개로 등록한 리뷰다. `BLINDED`와 **직교**하므로 한 리뷰가 두 탭에 동시에 나타날 수 있다(비공개 리뷰가 정책 위반이라 게시중단된 경우).

### `ShopReviewManagementQueryDao#createdAtLt` — 종료일은 다음날 00:00 미만이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ShopReviewManagementQueryDao.java`
→ `createdAtLt(LocalDate)`

**`loe(endDate.atStartOfDay())`로 바꾸지 않는다** — 그렇게 쓰면 **종료일 당일에 작성된 리뷰가 통째로 빠진다.**

### `ShopReviewManagementQueryDao#ratingEq` — 별점 필터는 내림 정수 기준이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ShopReviewManagementQueryDao.java`
→ `ratingEq(Integer)`

**4점 필터가 4.0~4.9를 포함해야** 별점 분포 통계(`ReviewStatisticsQueryDao#getRatingCounts`의 `floor`)와 **같은 집합을 가리킨다.** 정확 일치로 바꾸면 목록과 분포 그래프의 숫자가 어긋난다.

### `ShopReviewManagementQueryDao#hasImageEq` — `false`는 "필터 무시"가 아니다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ShopReviewManagementQueryDao.java`
→ `hasImageEq(Boolean)`

`false`는 **사진 없는 리뷰만**이다(`NOT EXISTS`). 미지정(`null`)이 전체를 뜻하므로 **`false`에 같은 의미를 주면 값 하나가 낭비된다.** web 목록의 `hasImage`와 같은 해석을 유지한다.

### `OrderQueryDao#findOrderProductOwnership` — left join이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/order/query/OrderQueryDao.java`
→ `findOrderProductOwnership(Long)`의 `leftJoin(orderJpaEntity)`

**inner join으로 묶지 않는다.** inner join으로 묶으면 주문이 사라진 주문 상품(`ORDER_PRODUCT.order_id`에 FK 제약이 없어 가능한 상태)이 "주문 상품 없음"으로 뭉뚱그려져, **소비 측이 원래 구분하던 `ORDER_NOT_FOUND`를 낼 수 없다.** 주문자 ID가 `null`인 것으로 그 상태를 구분해 넘긴다.

### `OrderQueryDao#findPayment` — `fetchOne`의 fail-loud를 유지한다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/order/query/OrderQueryDao.java`
→ `findPayment(OrderId)`

`PAYMENT.order_id`의 unique 제약이 깨져 동일 주문에 결제 행이 2건이 되면, **임의의 한 건을 조용히 고르는 대신 `fetchOne`으로 즉시 실패시킨다.** `fetchFirst`로 바꾸지 않는다 — 기존 `PaymentRepository#findByOrderId`와 동일한 fail-loud 시맨틱이다.

### `OrderQueryDao#withUnwrappedAmount` — 언랩을 없애면 api 모듈 규약이 깨진다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/order/query/OrderQueryDao.java`
→ `withUnwrappedAmount(PaymentProjection)`

이 언랩이 읽기 계약을 경계 타입(`Integer`)으로 유지해 **api 모듈이 `Amount.value()`를 호출하지 않게 한다.** 제거하고 Result에 `Amount`를 그대로 담으면 `apiModuleShouldBeDomainModelFree`가 다시 깨진다 — **이 자리가 그 규칙의 유일한 비-enum 위반이었다**(챕터 07).

### `@Embedded` record VO — 컴포넌트 선언 순서는 이름 알파벳 오름차순이다

**대상**: `backend/infrastructure/persistence/src/test/java/com/tastyhouse/infrastructure/shared/persistence/EmbeddedRecordComponentOrderTest.java`
→ `@Embedded`로 매핑되는 모든 record VO (`PhoneNumber` · `ProductDiscountInfo` · `OrderDeliveryDestination` · `VerificationCode` 등)

**새 embeddable record를 만들거나 기존 record에 컴포넌트를 끼워 넣을 때 선언 순서를 알파벳순으로 유지한다.** "읽기 좋은 순서"로 재배치하지 않는다.

Hibernate 6의 `Component#sortProperties()`는 embeddable 프로퍼티를 **이름순으로 정렬**하고, 정렬 결과가 record 컴포넌트 순서와 일치할 때만(`isSimpleRecord()`) 정렬을 건너뛴다. 선언 순서가 알파벳순이 아니면 `ComponentType#deepCopy`가 **정렬된 순서**로 읽은 값 배열을 canonical 생성자에 **선언 순서**대로 위치 기반 전달하므로 **값이 엉뚱한 파라미터로 들어간다.**

| 상황 | 결과 |
|---|---|
| 자리가 뒤바뀐 컴포넌트의 **타입이 다르면** | 런타임 예외 — `Could not instantiate entity ... argument type mismatch`. 실제 장애 선례: 주문 생성 시 `OrderDeliveryDestination`의 `lotAddress`(String)가 `distanceMeters`(Integer) 자리에 들어가 500이 났다 |
| **타입이 같으면** | **예외 없이 값만 조용히 뒤바뀐다.** 도로명↔지번 주소가 서로 바뀌어 저장되는 식이며, 테스트가 없으면 발견되지 않는다 |

**이 조용한 실패 때문에 사람 눈이 아니라 가드가 필요하다.** 가드는 엔티티 패키지를 클래스패스 스캔하므로 목록을 수동 관리하지 않는다.

`@AttributeOverride`의 `name`은 **컴포넌트명으로 매칭**되므로 선언 순서를 바꿔도 컬럼 매핑은 영향받지 않는다 — 즉 알파벳순 정렬은 DDL·컬럼 계약을 건드리지 않고 안전하게 지킬 수 있는 규약이다. "순서를 바꾸면 컬럼이 어긋날까 봐" 미루지 않는다.


### `ShopOrderNoticeQueryDao` 메서드 2종 — 노출 필터를 Service로 올리지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopOrderNoticeQueryDao.java`
→ `findOrderNotice` · `findVisibleOrderNotice`

점주용과 손님용을 **한 메서드로 합치고 게시중단 분기를 Service의 if 문으로 옮기지 않는다.** 손님 경로에서 필터를 빠뜨리면 게시중단된 문구가 그대로 노출되는 결함이 되는데, 쿼리 자체가 걸러내면 그 실수가 **물리적으로 불가능**해진다. 점주가 게시중단 건도 받는 것은 자기 문구가 왜 내려갔는지 봐야 하기 때문이며, 이 비대칭은 의도한 것이다.

### `ShopDeliveryAreaQueryDao#findShopLocation` — `ceo_id` 조건을 조회에서 빼지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopDeliveryAreaQueryDao.java`
→ `findShopLocation`

소유권을 **조회 조건으로 함께 건다.** 조회 서비스는 write 포트를 주입할 수 없어(`queryServicesShouldNotDependOnWritePorts`) `ShopOwnershipValidator`를 쓸 수 없고, 검증을 생략하면 **남의 가게 좌표를 읽는 IDOR**이 된다. 이 조건을 "중복 필터"로 보고 걷어내지 않는다.

### `ShopRiderGuideQueryDao` — web-api 경로에서 `SHOP_RIDER_GUIDE`를 조인하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopRiderGuideQueryDao.java`
→ 클래스 전체

라이더 안내는 **고객에게 노출되지 않는다.** 이 DAO는 ceo-api·admin-api의 query 서비스만 주입해 쓰며, web-api의 가게 상세·목록 조회는 이 테이블을 조인하지 않는다. "가게 정보를 한 번에 내려주자"는 취지로 web 경로에 조인을 추가하면 비노출 보장이 깨진다.

### `ShopRiderGuideQueryDao#findRiderGuide` — 라이더 안내 테이블은 `leftJoin`이다

**대상**: 같은 파일 → `findRiderGuide`

**미등록은 오류가 아니라 정상 상태다.** 등록 이력이 없어도 가게가 존재하면 결과를 반환해야 하므로 inner join으로 바꾸지 않는다.

### `ShopRiderGuideQueryDao#visitGuidePresenceEq` — `false`를 "미지정과 동일"로 두지 않는다

**대상**: 같은 파일 → `visitGuidePresenceEq`

`false`를 무필터로 처리하면 그 값이 응답을 전혀 바꾸지 않아 파라미터가 무의미해지고, **"문구 미등록 가게만" 조회할 방법도 사라진다.** 다른 boolean 필터와 동일하게 여집합으로 판정한다.

### `ShopRiderGuideQueryDao` 목록 — 픽업 위치 판정을 SQL 술어로 내리지 않는다

**대상**: 같은 파일 → `findRiderGuidePage` (중간 투영 `ShopRiderGuidePickupPresenceResult`)

"픽업 위치가 설정되었는가"를 select 절 술어로 투영하지 않고 **원본 컬럼을 읽어 Java에서 판정한다.** 판정 기준(도로명·위경도가 모두 채워졌는가)을 `ShopRiderGuide#hasPickupLocation`과 한 곳에서 일치시키기 위함이다. SQL로 내리면 두 판정이 조용히 갈린다.

### `ShopChangeHistoryQueryDao#createdAtOnDate` — 보관 하한을 DAO에서 제거하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopChangeHistoryQueryDao.java`
→ `createdAtOnDate`

서비스가 이미 400으로 거부한 뒤에도 6개월 보관 하한을 **항상 실어 보낸다.** 조회 대상 하루가 하한보다 뒤이므로 결과는 달라지지 않지만, 정책이 DAO에도 남아 **다른 호출자가 생겨도 6개월 밖 데이터가 새지 않는다.** 중복이라고 걷어내는 것이 이 안전망을 없앤다.

### `PaymentQueryDao#findPaymentByOrderId` — `fetchOne`을 `fetchFirst`로 바꾸지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/payment/query/PaymentQueryDao.java`
→ `findPaymentByOrderId`

`PAYMENT.order_id`에 unique 제약이 있어 주문당 결제는 최대 1건이다. **이 불변식이 깨지면 임의의 한 건을 조용히 고르는 대신 즉시 실패시킨다** — `PaymentRepository#findByOrderId`와 동일한 fail-loud 시맨틱을 유지한다.

### `PaymentQueryDao` — 호출부 없는 조회를 미리 만들지 않는다

**대상**: 같은 파일 → 클래스 전체

관리자 결제·환불 내역 조회는 **admin-api에 결제 소비자가 생길 때** 이 DAO에 메서드로 추가한다. 지금 미리 만들지 않는다.

### `MenuReviewStatisticsQueryDao` — `ownerOnly` 필터를 추가하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/menureview/query/MenuReviewStatisticsQueryDao.java`
→ 클래스 전체

**MENU_REVIEW에는 사장님만보기(`ownerOnly`) 개념이 없다.** 고객 노출 조건은 `hidden = false` 하나뿐이며, 두 축을 함께 거는 매장 리뷰 집계(`ReviewStatisticsQueryDao`)와 비교하며 "필터가 누락됐다"고 오해해 추가하지 않는다.

### `MenuReviewStatisticsQueryDao#getAverageRatingByProductId` — 없으면 `null`이다

**대상**: 같은 파일 → `getAverageRatingByProductId`

대상이 없을 때 0.0으로 대체하지 않는다. **"평점 0점"과 구분해야 한다.**

### `MenuReviewStatisticsQueryDao#countByMemberWithPeriod` — 정렬을 추가하지 않는다

**대상**: 같은 파일 → `countByMemberWithPeriod`

소비 측(`MemberReviewCountQueryDao`)이 REVIEW 집계와 병합한 **뒤에** 정렬하므로, 여기서 정렬해도 그 결과가 유지되지 않는다.

### `MenuReviewQueryDao#findWritableItemsByOrderId` — 상품 조인은 `leftJoin`이고 `deleted` 필터를 걸지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/menureview/query/MenuReviewQueryDao.java`
→ `findWritableItemsByOrderId`

inner join으로 바꾸면 상품 행이 사라지는 순간(소프트 삭제 후 필터, 혹은 향후 하드 삭제) 그 메뉴를 주문했던 회원의 **리뷰 작성 항목이 통째로 소멸한다.** 같은 이유로 여기에는 `deleted` 필터를 걸지 않으며, 평가 제외 판정에서도 **`ratingExcluded`가 null인 경우를 명시적으로 통과**시킨다(주문 스냅샷만으로 평가할 수 있어야 한다).

### `CouponQueryDao` 내 쿠폰 조회 — 원본 쿠폰의 삭제 필터를 추가하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/coupon/query/CouponQueryDao.java`
→ `findMemberCoupons` · `findAvailableMemberCoupons`

admin 목록·상세는 삭제된 쿠폰을 제외하지만, **내 쿠폰 조회는 이관 이전 동작을 그대로 보존해 필터링하지 않는다.** 일관성을 이유로 없던 필터를 넣으면 이미 발급된 보유분이 회원 쿠폰함에서 사라진다.

### `ProductFeedbackQueryDao` — 제보자 정보를 어떤 투영에도 담지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/product/query/ProductFeedbackQueryDao.java`
→ 클래스 전체

**점주가 특정 손님을 식별하면 보복 우려가 있다.** 제보의 목적은 정보 수정이지 손님 응대가 아니므로, `member_id`는 중복 방지 판정(write 포트)에만 쓰고 투영·응답 어디에도 싣지 않는다.

### `ProductFeedbackQueryDao` 메뉴 조인 — `leftJoin`을 유지한다

**대상**: 같은 파일 → `findFeedbackSummaries`

메뉴가 소프트 삭제돼도 제보는 남는다. inner join으로 바꾸면 **삭제된 메뉴에 대한 지적이 통째로 사라져** 점주가 원인을 파악할 근거를 잃는다.

### `ProductFeedbackQueryDao#MAX_CONTENTS_PER_GROUP` — 상한을 풀지 않고, 창 함수로 바꾸지 않는다

**대상**: 같은 파일 → `MAX_CONTENTS_PER_GROUP` · `findEtcContents`

무제한으로 실으면 제보가 많은 메뉴 하나가 응답을 뒤덮어 다른 메뉴의 지적이 묻힌다. 상한은 **자바에서 적용한다** — 그룹별 LIMIT은 표준 SQL로 표현할 수 없고, 창 함수를 쓰면 이 조회만 네이티브 SQL이 되어 컴파일 검증에서 벗어난다.

### `StorePriceVerificationQueryDao` — 가격표 이미지는 `left join`이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/product/query/StorePriceVerificationQueryDao.java`
→ `verificationProjection`

컬럼이 `NOT NULL`이라 정상 데이터라면 항상 맞지만, inner join으로 두면 **파일 행이 유실된 요청이 검수 목록에서 조용히 사라져** 처리 불가 상태가 된다.

### `StorePriceVerificationQueryDao` — 항목을 목록 조회에 조인하지 않는다

**대상**: 같은 파일 → `verificationProjection` · `findVerificationItems`

요청 1건에 메뉴가 N건 달리므로 목록에 조인하면 행이 부풀어 페이징이 깨진다. `itemCount`도 **스칼라 서브쿼리**여야 한다 — 조인 후 `GROUP BY`로 세면 페이징 대상 행이 부풀고 **항목이 0건인 요청이 조인에서 탈락한다.**

### `ShopChoiceQueryDao` 대표 상품 그룹핑 — `PRODUCT.shop_id`로 되돌리지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopChoiceQueryDao.java`
→ `productsByShopId`

메뉴-가게 N:M 도입으로 "이 가게 메뉴판에 무엇이 걸려 있는가"의 진실원은 **`PRODUCT_SHOP_LINK`**다. 그룹핑 키를 원본 컬럼으로 되돌리면 **다른 가게에서 불러온 메뉴가 그 가게 목록에 나타나지 않는다.**

### `BannerQueryDao` — 노출 조회는 `inner join`, 관리 조회는 `left join`이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/banner/query/BannerQueryDao.java`
→ `findVisibleBannersByType` · `findAllBanners` · `findDetailById`

이 비대칭을 한쪽으로 통일하지 않는다. inner로 통일하면 이미지 없는 배너가 **관리 화면에서 사라지고**, left로 통일하면 이미지 없는 배너가 **회원에게 노출된다.**

### `BugReportDetailProjection` — `@QueryProjection`을 쓰지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/bug/query/BugReportDetailProjection.java`
→ record 선언

어댑터 내부 타입이라 읽기 계약 패키지로 옮기지 않았지만, **`@QueryProjection`은 쓰지 않는다.** 그 어노테이션이 리포에 하나라도 남으면 "읽기 투영은 `Projections.constructor`로 한다"는 규칙에 예외가 생기고 Q타입 생성물이 다시 늘어난다.

### `BugReportQueryDao#toDetailResult` — 조립 팩토리를 포트 DTO에 두지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/bug/query/BugReportQueryDao.java`
→ `toDetailResult`

`BugReportDetailProjection`은 어댑터 내부 전용 타입이라 포트 DTO 쪽에 팩토리를 두면 **읽기 계약이 infra를 참조하게 된다.**

### `FileUrlResolver` — 캐싱을 도입하지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/file/query/FileUrlResolver.java`
→ `resolve` · `resolveAll`

`FileStoragePort#getFileUrl`은 네트워크·SDK·DB 접근이 없는 순수 문자열 변환이라 행 단위로 반복 호출해도 비용이 사실상 없다. **캐싱은 값비싼 연산에 쓰는 수단이며, 여기 도입하면 baseUrl 설정 변경 시 무효화 책임만 새로 생긴다.**

### `CeoQueryDao` — 인증·시드 조회를 이 DAO로 옮기지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/ceo/query/CeoQueryDao.java`
→ 클래스 전체

`findByUsername`/`existsByUsername`은 **불변식 검증 경로**이므로 write 포트에 잔류한다. 표현 목적 조회가 아니다.

### `CeoReplyPhraseQueryDao#findReplyPhrases` — 2차 정렬 키 `id`를 빼지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/ceo/query/CeoReplyPhraseQueryDao.java`
→ `findReplyPhrases`

삭제 후 `sort`를 재정렬하지 않아 **순번이 같은 행이 생길 수 있다.** 2차 키를 빼면 동률 행의 순서가 요청마다 달라진다.

### `AdminDongQueryDao` 경계 조회 — 경계 미보유 동을 목록에서 빼지 않는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/region/query/AdminDongQueryDao.java`
→ `findBoundariesWithinBoundingBox`

경계가 없다고 빼면 화면이 **"이 지역에 동이 없다"로 오해**하게 된다. 실제로는 좌표만 있고 경계 시드가 아직 안 들어온 정상 상태다.

### `MemberDeliveryAddressQueryDao` — 행정동은 `left join`이다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/member/query/MemberDeliveryAddressQueryDao.java`
→ `findByMemberId` · `regionNameExpression`

주소 문자열 매칭에 실패해 `admin_dong_id`가 null인 주소도 **목록에서 빠지면 안 된다.** 그 경우 `regionName`은 null로 내려가며, 이는 정상 동작이다.

### 영속 어댑터·엔티티 — 지우거나 되돌리면 안 되는 것

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/**/persistence/**`

아래는 "정상으로 보이는 정리"가 곧 결함이 되는 자리들이다. 대부분 **컴파일과 부팅은 통과하고 런타임·특정 데이터에서만 드러난다.**

#### getter 없는 필드를 "미사용"이라며 지우지 않는다

**대상**:
- `.../order/persistence/OrderProductOptionJpaEntity.java` → 옵션 스냅샷 4필드
- `.../review/persistence/ReviewTagJpaEntity.java` → `tagId`
- `.../region/persistence/AdminDongJpaEntity.java` → 바운딩박스 4컬럼
- `.../shop/persistence/ShopDeliveryAreaPolygonJpaEntity.java` → 집계 2컬럼
- `.../search/persistence/RecommendedKeywordJpaEntity.java` → `sortOrder`
- `.../shop/persistence/StationJpaEntity.java` → `stationName`
- `.../holiday/persistence/PublicHolidayJpaEntity.java` → `substitute`
- `.../reservation/persistence/ReservationSlotJpaEntity.java` → `version`

IDE·정적분석이 "assigned but never accessed" / "never used" / "never assigned"로 경고하지만 **전부 정상이다.** JPA가 flush 시 리플렉션으로 읽는 컬럼 매핑이거나(스냅샷·FK), QueryDSL `.orderBy(...)`·투영 전용이거나, NOT NULL 컬럼과 매핑돼야 `ddl-auto: validate`를 통과하거나, Hibernate가 관리하는 낙관적 락 버전이다.

**제거하면**: 주문 시점 옵션 스냅샷·태그 FK가 저장되지 않고, 정렬이 사라지며, 부팅이 스키마 검증에서 거부된다. `@SuppressWarnings("unused")`가 붙어 있는 자리도 같은 이유이며 **그 어노테이션을 떼지 않는다.**

바운딩박스·폴리곤 집계 컬럼에 **getter를 추가하지 않는다** — 경계에서 파생되는 값이라 getter를 두면 호출자 없는 죽은 코드가 된다. 값은 SQL 집계·점검 질의가 소비하며, 자바에서 필요해지는 시점에 질의와 함께 추가한다.

#### enum 컬럼의 `columnDefinition`을 떼지 않는다

**대상**: `.../ceo/persistence/CeoLoginHistoryJpaEntity.java` · `.../notification/persistence/NotificationJpaEntity.java` · `.../product/persistence/ProductFeedbackJpaEntity.java` · `.../product/persistence/ProductOptionGroupJpaEntity.java` · `.../product/persistence/ProductOptionGroupMergeHistoryJpaEntity.java` · `.../shop/persistence/ShopCeoAssignmentHistoryJpaEntity.java` · `.../shop/persistence/ShopChangeHistoryJpaEntity.java` · `.../shop/persistence/ShopDeliveryAreaAdjustmentRequestJpaEntity.java` · `.../shop/persistence/ShopDeliveryTipSettingJpaEntity.java` · `.../shop/persistence/ShopRequestIndexJpaEntity.java` · `.../shop/persistence/ShopRequestCommentJpaEntity.java` · `.../shop/persistence/ShopRiderGuideHistoryJpaEntity.java`

`@Enumerated(EnumType.STRING)`에서 **`columnDefinition = "VARCHAR(n)"`을 중복으로 보고 지우면 앱이 부팅하지 못한다.** Hibernate 6의 `MySQLDialect`가 네이티브 `ENUM(...)`을 기대해 `ddl-auto: validate`가 `wrong column type ... but expecting [enum (...)]`으로 거부한다(`BugReport` 장애 선례). `n`은 `backend/schema.sql`과 일치해야 하며, 스키마 쪽 길이를 바꾸면 여기도 함께 바꾼다.

#### `applyChanges`를 "일관성"을 이유로 추가하지 않는다

**대상**: append-only 이력·불변 사실 기록·replace-all 컬렉션·read-only 마스터 엔티티 전부. 대표 예 — `.../shop/persistence/ShopChangeHistoryJpaEntity.java` · `.../product/persistence/ProductOptionGroupMergeHistoryJpaEntity.java` · `.../product/persistence/ProductOptionGroupMergeExclusionJpaEntity.java` · `.../product/persistence/ProductFeedbackJpaEntity.java` · `.../product/persistence/StorePriceVerificationItemJpaEntity.java` · `.../payment/persistence/PaymentRefundJpaEntity.java` · `.../payment/persistence/TossPaymentRecordJpaEntity.java` · `.../rank/persistence/MemberReviewRankJpaEntity.java` · `.../review/persistence/ReviewBlindRequestAttachmentJpaEntity.java` · `.../shop/persistence/ShopNoticeImageJpaEntity.java`

`applyChanges`의 부재는 **update 경로가 존재하지 않는다는 구조적 표현**이다. 추가하면 "언젠가 바꿀 수 있다"는 잘못된 신호가 되고, **이력 행이 사후에 바뀌면 감사 근거로서의 가치가 사라진다.** 실제로 갱신 경로가 생길 때만 추가한다. 대응하는 write 어댑터(`*RepositoryImpl`)의 `save`에도 update 분기를 두지 않는다.

#### 복사 대상에서 뺀 필드를 `applyChanges`에 넣지 않는다

**대상**:
- `.../review/persistence/ReviewJpaEntity.java` · `.../review/persistence/ReviewMapper.java` → `ownerOnly`
- `.../product/persistence/ProductOptionGroupJpaEntity.java` → `groupType`

`ownerOnly`(사장님만보기)는 **등록 시에만 정해지고 전환이 불허**라 update 대상이 아니다. `groupType`은 유형 전환 경로를 두지 않기로 한 도메인 결정(`ProductOptionGroup.groupType`이 `final`인 이유)을 영속 계층에서도 지킨 것이다. **여기에 추가하면 전환이 가능하다는 잘못된 신호가 된다.**

반대로 **`ProductMapper#applyChanges`에서 필드를 빼지 않는다** — `ratingExcluded`·`deleted`·`composition`·`singleServing`·노출기간·`vegetarianType`·`weightText`는 점주 메뉴 관리 경로가 실제로 바꾸는 값이라, 하나라도 빠뜨리면 도메인에서 전이시킨 값이 저장되지 않고 **조용히 유실된다.** `ShopMenuCollectionImageJpaEntity#applyChanges`의 `sort`도 같은 이유로 뺄 수 없다(순서 변경은 승인 없이 즉시 반영되는 정상 경로다).

#### `save`의 PK 조회를 detached merge로 바꾸지 않는다

**대상**: `.../**/persistence/*RepositoryImpl.java` → `save`

detached 인스턴스를 그대로 `save`(merge)하면 **`@CreatedDate(updatable = false)` 감사 필드가 파손되고** 새 행이 중복 생성될 수 있다. managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush하는 형태를 유지한다.

#### `save`·삭제 경로의 PK 조회에 소프트 삭제 필터를 걸지 않는다

**대상**: `.../partnership/persistence/PartnershipRepositoryImpl.java` · `.../product/persistence/ProductRepositoryImpl.java` → `save` · `findByIdIncludingDeleted` · `.../rank/persistence/RankPeriodRepositoryImpl.java` · `.../rank/persistence/RankPrizeRepositoryImpl.java` → `delete`

삭제 전이를 저장하는 경로가 바로 이 자리이므로 **필터 없는 순수 PK 조회여야 한다.** 일반 로드용 필터 걸린 조회(`findById`)를 재사용하면 이미 삭제된 행을 다시 읽지 못해 **삭제가 영원히 실패하고 멱등 처리·상태 확인이 불가능해진다**(`RankPeriodRepositoryImpl#delete` 선례).

반대로 **일반 로드에서는 필터를 빼지 않는다** — `ProductJpaRepository`의 상속받은 `findById`에는 `deleted` 필터가 없으므로 일반 로드에는 `findByIdAndDeletedFalse`를 쓴다. 이 필터가 신규 주문·신규 메뉴평가 차단을 자동으로 성립시킨다.

#### replace-all 선행 삭제를 derived `deleteBy...`로 되돌리지 않는다

**대상**: `.../product/persistence/ProductAllergenJpaRepository.java` · `.../product/persistence/ProductExposureHourJpaRepository.java` · `.../shop/persistence/ShopDeliveryTipTierJpaRepository.java` · `.../shop/persistence/ShopDeliveryTipRegionJpaRepository.java` · `.../shop/persistence/ShopDeliveryTipHolidayJpaRepository.java` · `.../shop/persistence/ShopDeliveryAreaJpaRepository.java`

derived 삭제는 영속성 컨텍스트에 delete action만 큐잉하는데, **Hibernate의 기본 flush 순서는 action을 타입별로 묶어 insert를 delete보다 먼저 실행한다.** 같은 키를 재사용하는 교체는 **항상 유니크 키 중복으로 실패한다**(`uk_product_allergen_product_type`·`uk_product_exposure_hour`·`uk_shop_delivery_tip_tier`·`uk_shop_delivery_tip_region`·`uk_shop_delivery_tip_holiday_shop_id`).

**`clearAutomatically`도 함께 유지한다** — 벌크 연산은 1차 캐시를 우회하므로 삭제된 행이 캐시에 남아 뒤이은 조회를 오염시키거나, 재삽입이 이미 삭제된 엔티티를 보고 유니크 제약을 오판한다. `.../shop/persistence/ShopNoticeImageRepositoryImpl.java#deleteByShopNoticeId`와 `.../rank/persistence/MemberReviewRankRepositoryImpl.java#deleteByRankTypeAndBaseDate`의 캐시 비우기도 같은 이유로 제거하지 않는다.

#### `findFirstBy~`를 `findBy~`로 바꾸지 않는다

**대상**: `.../shop/persistence/ShopNoticeJpaRepository.java` → `findFirstByShopIdAndExposedIsTrueOrderByIdDesc`

노출 공지 1건 불변식은 도메인 서비스가 지킬 뿐 **DB 제약이 없다**(MySQL 부분 유니크 인덱스 미지원). `is_exposed = 1`이 2건 이상인 상태가 물리적으로 가능하며, 단건 시그니처는 그때 `IncorrectResultSizeDataAccessException`으로 **해당 가게의 공지 기능을 통째로 500으로 만든다.**

같은 이유로 `.../review/persistence/ReviewBlindRequestRepositoryImpl.java#findApprovedByReviewId`도 `fetchOne`을 쓰지 않는다 — 1회 제한이 애플리케이션 검사라 동시 요청에 이론상 뚫린다.

`.../shop/persistence/ShopOrderNoticeJpaRepository.java#findByShopId`가 단건 시그니처인 것은 `shop_id`에 컬럼 단위 유니크 제약이 있어서다. **그 유니크 제약을 제거하면 이 조회도 함께 깨진다.**

#### null 파라미터로 파생 쿼리를 합치지 않는다

**대상**: `.../product/persistence/ProductJpaRepository.java` → `findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc` · `.../product/persistence/ProductRepositoryImpl.java` → `findAllByShopIdAndCategoryId`

미분류 메뉴 조회를 `productCategoryId = null` 하나로 합치면 **null이 "조건 없음"으로 해석돼 가게의 모든 메뉴가 대상이 된다.**

#### 메뉴판 판정을 `PRODUCT.shop_id`로 되돌리지 않는다

**대상**: `.../product/persistence/ProductJpaRepository.java` → `countVisibleByShopLink` · `.../product/persistence/ProductRepositoryImpl.java` · `.../product/persistence/ProductPriceJpaRepository.java`

메뉴-가게 N:M 도입 이후 **"이 가게 메뉴판에 무엇이 걸려 있는가"의 진실원은 `PRODUCT_SHOP_LINK`다.** `PRODUCT.shop_id`로 세면 다른 가게에서 불러온 메뉴가 빠지고, 반대로 이 가게 메뉴판에 없는 원본 메뉴가 잘못 포함된다. `distinct`도 조인 형태가 바뀌어도 개수가 부풀지 않게 하는 방어이므로 지우지 않는다.

`ProductPriceJpaRepository`의 가게별 가격 조회 조건은 **`ProductQueryDao#findShopProductPrices`와 반드시 같아야 한다** — 갈리면 매장가격 뱃지가 두 화면에서 달라진다.

#### VO 승격을 `IdMapping` 없이 직접 호출하지 않는다

**대상**: `.../shared/persistence/IdMapping.java` → `vo` · `raw` · 모든 `*Mapper`

`XxxId.of(entity.getXxxId())`처럼 직접 호출하면 **컴파일은 통과하고, 그 FK가 실제로 null인 행을 읽을 때만 예외가 난다** — 빈 테이블이나 FK가 항상 채워진 샘플 데이터로는 잡히지 않는다. **nullable 여부와 무관하게 모든 매퍼가 이 헬퍼를 쓴다** — 컬럼별로 형태를 나누면 위험한 직접 호출이 흔해 보여 눈에 띄지 않게 된다.

#### FK 컬럼에 `@Convert`로 VO를 매핑하지 않는다

**대상**: `.../ceo/persistence/CeoReplyPhraseJpaEntity.java` → `ceo_id` · `.../menureview/persistence/MenuReviewJpaEntity.java` → 크로스 애그리거트 FK 전부

VO 매핑을 하면 QueryDSL이 `NumberPath<Long>` 대신 VO path를 생성해 **query DAO의 조인·투영이 깨진다.**

#### `OrderProductOptionJpaEntity.optionGroupType`을 enum으로 바꾸지 않는다

**대상**: `.../order/persistence/OrderProductOptionJpaEntity.java` → `optionGroupType`

주문 시점의 사실을 박제한 값이라 나중에 유형 enum에 상수가 추가되거나 이름이 바뀌어도 과거 주문 기록이 흔들리면 안 된다. **enum으로 매핑하면 알 수 없는 값에서 로드가 실패한다.**

#### 보증금 금액을 다른 금액 컬럼에 합치지 않는다

**대상**: `.../order/persistence/OrderProductJpaEntity.java` → `cupDepositAmount` · `.../order/persistence/OrderProductOptionJpaEntity.java` → `depositAmount`

`OrderProduct.cup_deposit_amount`는 `total_option_price`·`total_price`에 **포함되지 않는다** — 포함하면 주문 전체의 상품 금액으로 흘러들어 최소주문금액·쿠폰·포인트 기준액이 오염된다. `OrderProductOption.deposit_amount`도 `additional_price`와 **별도 항목**이며, **합치면 비과세 분리가 영구히 불가능해진다.**

`.../product/persistence/ProductOptionJpaEntity.java`의 `cupCount`는 금액이 아니라 **개수**를 저장한다 — 요율이 바뀌어도 마이그레이션이 필요 없으며, 환급 단위가 컵 개수라 금액만으로는 대체할 수 없다.

#### 배달 평점·평가 내용을 web-api 응답에 담지 않는다

**대상**: `.../review/persistence/ReviewJpaEntity.java` → `deliveryRating` · `deliveryComment`

**노출은 ceo-api 점주 리뷰 상세에만 한정된다** — web-api 응답에는 어떤 경로로도 담지 않으며(원문 규격: 고객 앱 미노출), `total_rating` 계산에도 넣지 않는다.

`.../product/persistence/ProductFeedbackJpaEntity.java`의 `memberId`도 같은 성격이다 — **중복 제보 판정에만 쓰며 점주 응답에는 절대 싣지 않는다.**

#### `ShopRequestIndexSyncAdapter`의 enum 승격 실패를 삼키지 않는다

**대상**: `.../product/persistence/ShopRequestIndexSyncAdapter.java`

포트 시그니처가 `String`인 것은 통합 상태 `ShopRequestStatus`가 shop 소유라 product 쪽 포트에 등장할 수 없기 때문이다. **승격 실패는 프로그래밍 오류(양쪽 enum이 어긋난 상태)이므로 `from(String)`의 400 변환에 맡기지 않고 그대로 전파시킨다** — 조용히 넘기면 인덱스가 원본과 어긋난 채 남는다.

이 기록은 **이벤트·`AFTER_COMMIT`이 아니라 원본 상태 전이와 같은 트랜잭션에서 동기 수행한다** — 기록 유실이 곧 "요청이 목록에서 사라짐"이기 때문이다. 리스너로 옮기지 않는다.

#### `GeoPolygonTextCodec`의 실패를 조용히 넘기지 않는다

**대상**: `.../shared/persistence/GeoPolygonTextCodec.java` → `decode`

형식이 깨진 입력은 `IllegalArgumentException`으로 실패시킨다 — **조용히 건너뛰면 도형의 일부가 사라진 채 복원되어, 점주가 그린 것과 다른 배달지역이 저장된 것처럼 보인다.** 저장 형식은 "경도 위도" 순서이고 `GeoPoint`는 (위도, 경도) 순서이므로 복원 시 뒤집는 자리를 지우지 않는다.

`MySQL GEOMETRY`·JSON으로 되돌리지 않는다 — 근거는 [코드 주석에서 이관된 설계 근거](#코드-주석에서-이관된-설계-근거)의 해당 절.

#### `AdminDongRepositoryImpl`의 두 방어선을 제거하지 않는다

**대상**: `.../region/persistence/AdminDongRepositoryImpl.java` → `synchronize` · `deactivateMissing`

- **모든 조회의 `is_active = 1` 필터** — 폐지 동은 시드가 삭제하지 않고 `is_active = 0`으로 남기므로(다른 테이블이 id로 참조 중이다) **이 필터가 유일한 방어선이다.** 빠지면 폐지된 행정동이 "검색 목록에는 안 뜨는데 등록 검증은 통과하고 주소 매칭에도 걸리는" 비대칭이 되살아난다.
- **빈 목록 동기화 차단** — 원천을 못 읽었을 때 마스터를 비우면 **전국 배달지역이 통째로 죽는다.**
- 동기화는 전량 삭제·재삽입이 아니라 **제자리 갱신(id 보존)** 이어야 한다 — 다른 테이블이 `id`를 참조한다.
- 바운딩박스는 호출자가 넘기지 않고 **경계에서 파생시킨다** — 두 값을 각각 받으면 어긋남이 조용히 저장된다. `toEntity`와 `applyChanges`가 같은 파생 헬퍼를 쓰는 것도 신규 행과 갱신 행의 저장 형태가 갈리지 않게 하기 위함이다.

#### `ReviewMapper`의 `product_id == 0` 분기를 지우지 않는다

**대상**: `.../review/persistence/ReviewMapper.java` → `toDomain`

`REVIEW.product_id`는 NOT NULL이지만 삭제된 `REVIEW_PRODUCT` 애그리거트의 레거시 값으로 `0`이 광범위하게 남아 있고, `ProductId` VO는 0을 거부한다. **이 분기를 지우면 그 행을 읽는 순간 예외가 난다.**

#### 위치 기반 전달이 많은 매퍼는 순서를 3중 대조한다

**대상**: `.../product/persistence/ProductNutritionMapper.java`(수치 14개) · `.../payment/persistence/TossPaymentRecordMapper.java`(필드 60여 개)

타입이 같아 **위치를 착각해도 컴파일은 통과하고 값만 조용히 뒤바뀐다.** 고칠 때는 도메인 getter 순서 · 엔티티 파라미터 순서 · 매퍼 호출 인자 순서를 하나씩 대조한다.

`.../product/persistence/ProductNutritionJpaEntity.java`의 14개 수치를 **`@Embedded` record로 묶지 않는다** — record 컴포넌트 선언 순서가 어긋나면 값이 조용히 뒤바뀌는데(`EmbeddedRecordComponentOrderTest`가 잡는 사고), 평면 필드는 `@Column`이 이름으로 매핑하므로 그 위험이 구조적으로 없다. `.../shop/persistence/ShopRiderGuideJpaEntity.java`의 픽업 위치 5개 컬럼도 같은 이유로 평면이다.

#### 예약 슬롯의 낙관적 락 배선을 바꾸지 않는다

**대상**: `.../reservation/persistence/ReservationSlotRepositoryImpl.java` · `.../reservation/persistence/ReservationSlotJpaRepository.java`

`@Version`만으로 동시 차감 충돌을 감지하므로 **별도 `@Lock`을 두지 않는다.** `save`·`flush`를 함께 감싸 `OptimisticLockConflictException`으로 번역하는 자리도 유지한다 — **도메인의 재시도 판별이 spring-orm 예외에 의존하지 않게** 하기 위함이다.

#### 모듈 auto-configuration의 두 설정을 건드리지 않는다

**대상**: `.../PersistenceModuleAutoConfiguration.java`

- **`infrastructure.redis` 제외** — 빼면 redis 모듈이 클래스패스에 있을 때 두 스캔이 같은 클래스를 중복 등록한다.
- **`before = JpaRepositoriesAutoConfiguration`** — 빼면 Boot 쪽 `@ConditionalOnMissingBean(JpaRepositoryConfigExtension)`이 물러나지 않는다.

`.../InfrastructurePersistenceConfig.java`는 JPA 스캔의 **단일 소유자**이며 `basePackageClasses`가 `com.tastyhouse.infrastructure` 이하 전체를 가리킨다 — 패키지를 옮기면 스캔 범위가 어긋나 부팅이 깨진다.

#### 알림 리스너의 `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 부분적으로 떼지 않는다

**대상**: `.../notification/listener/ReviewBlindApprovedEventListener.java` · `.../notification/listener/ReviewOwnerReplyEventListener.java` · `.../product/listener/ProductMenuReviewEventListener.java`

- `@Async`가 빠지면 호출 스레드에서 동기 실행되어 **알림 실패가 원본 API로 전파된다** — DB에는 반영됐는데 화면은 실패로 뜬다.
- `REQUIRES_NEW`가 빠지면 커밋될 트랜잭션이 없어 **리스너가 조용히 아무것도 남기지 않는다.**

`ProductMenuReviewEventListener`의 **`productId == null` 가드도 유지한다** — 컬럼이 NOT NULL이라는 이유로 지우면, 향후 발행 경로가 늘어 null이 실릴 때 그 예외가 `AFTER_COMMIT`에서 조용히 유실된다.

#### `PaymentEventListener#onRefundRequested`는 포인트를 건드리지 않는 것이 계약이다

**대상**: `.../payment/listener/PaymentEventListener.java` → `onRefundRequested`

이 이벤트는 환불 **접수** 시점이며 실제 금전 정산은 결제가 취소로 확정될 때 `PaymentCancelledEvent`가 수행한다. **여기서 포인트를 함께 움직이면 승인 전 요청만으로 잔액이 바뀌고, 이후 취소 확정 시 같은 금액이 두 번 반영된다.** 이 핸들러는 접수 사실만 남기며, DB를 쓰지 않으므로 `REQUIRES_NEW`도 열지 않는다.

`onPaymentCompleted`의 **현장 결제 한정 적립**도 계약이다 — PG 결제는 주문 접수 시점에 이미 처리됐다.

#### `ReferralRegisteredEventListener`의 처리 순서를 바꾸지 않는다

**대상**: `.../member/listener/ReferralRegisteredEventListener.java`

**적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다.

적립을 이 리스너에서 도메인 서비스로 되돌리지 않는다 — 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`다.

#### `ProductMenuReviewEventListener`가 REVIEW 이벤트를 다시 구독하게 하지 않는다

**대상**: `.../product/listener/ProductMenuReviewEventListener.java`

`PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔다. **두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다.**

#### 인증 리스너에 발송을 추가하지 않는다

**대상**: `.../mail/listener/MailVerificationEventListener.java` · `.../sms/listener/SmsVerificationEventListener.java`

이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다. 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.

#### 인증 테이블 RENAME 마이그레이션과 배포는 원자적이어야 한다

**대상**: `.../mail/persistence/MailVerificationJpaEntity.java` · `.../sms/persistence/SmsVerificationJpaEntity.java`

테이블·인덱스명이 `MAIL_VERIFICATION`·`SMS_VERIFICATION`으로 통일된 `alter.sql` RENAME 마이그레이션이 있고, **`ddl-auto=validate` 환경이므로 그 마이그레이션과 앱 배포를 따로 하면 부팅이 실패한다.**

#### `CachingProhibitedWordRepository`에 락을 추가하지 않는다

**대상**: `.../shop/persistence/CachingProhibitedWordRepository.java`

`AtomicReference`에 스냅샷을 통째로 담아 교체하므로 락이 필요 없다. 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하며, **중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.** TTL을 제거해 무기한 캐싱으로 바꾸지도 않는다 — 시드 갱신이 재기동 전까지 반영되지 않는다.

#### 테이블명의 `SHOP_` 접두를 소유 컨텍스트에 맞춰 바꾸지 않는다

**대상**: `.../product/persistence/StorePriceVerificationJpaEntity.java` · `.../product/persistence/StorePriceVerificationItemJpaEntity.java`

애그리거트가 product로 옮겨졌어도 **요청이 가게 단위로 접수되기 때문에** 테이블명을 유지하며, `@Table(name = ...)`로 명시 매핑한다.

#### `StorePriceVerificationItem.store_price`를 승인 시점 조회로 대체하지 않는다

**대상**: `.../product/persistence/StorePriceVerificationItemJpaEntity.java` → `store_price`

**승인은 요청 시점의 매장가를 쓴다.** 승인 시점에 현재 가격을 다시 읽으면 검수자가 보지 않은 값이 승인된다.

#### `ProductPriceJpaEntity.pickup_price_set_at`을 `updated_at`으로 대체하지 않는다

**대상**: `.../product/persistence/ProductPriceJpaEntity.java` → `pickup_price_set_at`

'매장가격 픽업' 뱃지가 **픽업가 설정 익일(영업일)** 부터 노출되는데, `updated_at`은 가격명·정렬만 바뀌어도 갱신되므로 **뱃지 노출 시점이 뒤로 밀린다.**

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거. 챕터 05에서 코드 주석을 제거하며 이관 -->

원문 주석은 코드에서 제거됐으므로, 여기가 각 쿼리 전략의 유일한 소재지다. 항목마다 **파일 경로 + 코드 요소명**을 앵커로 남긴다(줄 번호는 쓰지 않는다 — 코드가 바뀌면 즉시 틀리기 때문이다).

### `ProductQueryDao` — 쿼리 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/product/query/ProductQueryDao.java`

이 저장소에서 주석이 가장 많던 파일(497줄)이며, 아래 규칙들은 대부분 **한 번씩 사고를 내고 확정된 것**이다.

#### `ProductQueryDao` 클래스 역할

`product` 도메인 read 어댑터(CQRS query 측). 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하며 도메인 모델을 거치지 않으므로 write 포트(`ProductRepository` 등 9개)와 역할이 겹치지 않는다. 소비 모듈(web/admin-api·batch-module)의 `ProductQueryService`가 주입해 쓰며, 소비 모듈은 QueryDSL을 알지 않는다. 소비자별 메서드 분리는 아래와 같다.

| 소비자 | 메서드 |
|---|---|
| web | `findTodayDiscountProducts` · `findProductOptions` · `findProductsBatch` · `findProductImageUrls` · `findShopProducts` · `searchByKeyword` |
| admin | `findProducts`(관리 목록) · `findProductDetailById` · `findProductCategories` |
| ceo | `findProductManagementDetailById` · `findProductAvailability` |
| batch | `findFirstBbqSyncTarget` |

상품 대표 이미지 경로를 위해 file 도메인, 가게명을 위해 shop 도메인의 Q타입을 조인한다(같은 모듈 내 참조). 조인으로 얻은 저장 경로는 `FileUrlResolver`로 표시용 URL까지 변환해 Result에 담는데, `Projections.constructor`는 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어 **fetch 직후 재조립한다**(`withResolvedImageUrl` 계열).

#### 서브쿼리 별칭을 새로 만드는 이유 — 별칭 재사용은 조인을 조용히 망가뜨린다

→ `subProductImage` · `imageChangeRequestFile` · `productImageFile` · `subExposureHour` · `subCategoryProduct` · `subOptionGroupLink`

본 쿼리가 이미 쓰고 있는 Q타입 인스턴스를 서브쿼리에서 재사용하면 **조인이 서로를 덮거나 카운트가 조인된 1건으로 좁혀진다.** 예외도 로그도 없이 값만 틀리므로 반드시 별칭 인스턴스를 새로 만든다.

- `imageChangeRequestFile` — 본 쿼리가 `uploadedFileJpaEntity`를 대표 이미지 목적으로 이미 쓴다
- `subOptionGroupLink` — 본 쿼리가 링크 테이블을 조인하므로, 재사용하면 연결 메뉴 수가 항상 1이 된다

#### 판정 시각은 애플리케이션이 정한다 (`SERVICE_ZONE`)

→ `SERVICE_ZONE` · `nowInServiceZone()` · `exposedNow(LocalDateTime)`

노출 판정 술어가 `CURRENT_DATE`/`CURRENT_TIME`를 쓰지 않는 이유는 **DB 서버 타임존에 판정이 좌우되지 않게** 하기 위함이다. 호출부가 `LocalDateTime.now(ZoneId.of("Asia/Seoul"))`를 넣는다.

#### 인기 메뉴 (`findPopularProducts`)

→ `findPopularProducts` · `popularProductProjection` · `soldQuantityOf` · `orderableNow` · `POPULAR_PRODUCT_LIMIT` · `POPULAR_PRODUCT_WINDOW_DAYS`

**집계 테이블·배치를 두지 않고 실시간 조회로 처리한다.** 상품 판매량 집계 자산이 없었고(`POPULAR_KEYWORD`는 검색어 전용), 가게 단위·30일 창이면 `idx_orders_shop_id`·`idx_orders_created_at`로 좁혀지는 범위라 집계 자산을 새로 만들어 동기화 책임을 늘릴 이유가 없다.

집계 규칙:

- 원천은 `ORDERS` ⨝ `ORDER_PRODUCT`이며 **`COMPLETED` 주문만** 센다 — 취소·대기 주문이 순위를 흔들면 안 된다.
- 집계 창은 최근 `POPULAR_PRODUCT_WINDOW_DAYS`(30)일. 창이 없으면 오래 전 히트 메뉴가 순위를 영구히 점유한다.
- 순위는 `SUM(quantity)` 내림차순, **동수는 `product_id` 오름차순**으로 안정 정렬한다 — 타이브레이크가 없으면 같은 데이터에도 호출마다 순서가 달라져 화면이 흔들린다.

**두 갈래(사장님 추천 / 판매량)를 애플리케이션에서 합치는 이유**는 정렬 키가 서로 다르기 때문이다. 추천 자리는 판매량과 무관하게 우선하고(판매 이력이 0이어도 남는다) 나머지 자리만 판매량 순이므로, 한 쿼리의 `ORDER BY`로 표현하면 "추천이면서 판매량 0"인 항목이 뒤로 밀린다. 두 목록을 각각 뽑아 순서대로 이어 붙이면 그 규칙이 그대로 드러난다. **판매량 갈래에서 추천으로 이미 채운 메뉴를 제외하는 것은 SQL 술어로 넣어야 한다** — 뽑은 뒤 걸러내면 자리가 비는 만큼 결과가 모자란다.

양쪽 모두 `orderableNow`로 **판매중지·숨김·미노출 메뉴를 제외**한다. 주문할 수 없는 메뉴를 상단에 올리면 손님이 눌렀을 때 막힌다. 추천 갈래와 판매량 갈래는 `popularProductProjection()` **같은 투영을 공유**한다 — 따로 두면 한쪽만 고쳐져 같은 화면의 항목이 서로 다른 필드를 갖게 된다.

`soldQuantityOf`에서 `coalesce(0)`으로 감싸는 이유는 판매 이력이 없는 메뉴에서 스칼라 서브쿼리가 `NULL`이 되어 정렬과 `> 0` 비교가 모두 예상과 달라지기 때문이다. `shop_id`로 한 번 더 좁히는 것은 중복 조건이 아니다 — `ORDER_PRODUCT`는 가게를 모르므로 `ORDERS`를 통해서만 가게 범위를 걸 수 있고, 이 조건이 있어야 `idx_orders_shop_id`를 탄다.

#### 페이징 술어는 count와 content가 공유한다

→ `todayDiscountSearchable(LocalDateTime)` · `findTodayDiscountProducts` · 그 count 쿼리

따로 두면 한쪽만 고쳐져 페이징 `totalElements`가 어긋나고 마지막 페이지가 비는 사고가 난다. 오늘의 할인 count 쿼리는 목록 쿼리와 **같은 `innerJoin`(shop)·같은 where를 재현**한다 — 대표 이미지·파일 `leftJoin`은 "노출 중 최소 sort 1장"으로 좁혀져 상품당 최대 1행이라 행이 늘지 않으므로 count에서 생략하지만, 가게 조인은 `innerJoin`이라 짝이 없는 상품을 제외해 총 건수에 영향을 주므로 그대로 재현한다.

#### 노출 판정은 후처리가 아니라 술어여야 한다 (`exposedNow`)

→ `exposedNow(LocalDateTime)` · `dayTypeMatches` · `coversTime` · `coversAsOvernightTail`

**애플리케이션 후처리로 할 수 없다.** 목록에 페이징이 걸려 있어 20건을 fetch한 뒤 5건을 걸러내면 `totalElements`가 틀어지고 마지막 페이지가 비게 된다.

이 술어는 도메인의 `ProductExposureCalculator`와 **같은 결과를 내야 한다.**

- 기간: `start <= today`이고 `today <= end`. NULL이면 그 방향 제약 없음. 종료일은 **당일 포함**이다.
- 요일·시간대: 행이 **0건이면 제약 없음**(`notExists`).
- 행이 있으면 오늘 요일에 걸리는 행이 지금 시각을 덮거나, **전일 행이 자정을 넘겨** 지금 시각을 덮어야 한다. 전일 확인(`coversAsOvernightTail`)을 빠뜨리면 **01:00에 야식 메뉴가 사라진다.**
- `dayTypeMatches`는 요일 묶음과 개별 요일을 모두 보되 `HOLIDAY`는 **제외**한다 — 공휴일 판정이 이 술어에 없으므로, 공휴일 전용 메뉴는 계산기를 타는 상세 경로에서만 정확하다.

`visible`은 이 술어에 넣지 않는다 — 기존 쿼리들이 이미 각자 `visible.eq(true)`를 걸고 있고, 관리 화면은 숨김도 봐야 하므로 축을 분리해 둔다.

**두 축의 OR 그룹은 각각 지역 변수로 분리한다**(`startNotAfterToday` · `noHourRows` · `todayBranch` 등). 체이닝으로 이어 쓰면 `(A or B) and (C or D)`가 되는 것이 우연처럼 보이고, 조건을 하나 추가할 때 결합 순서가 조용히 바뀐다.

#### `notDeleted()` — 모든 조회에 거는 것이 정답이 아니다

→ `notDeleted()`

**정적 고정 조건**이라 동적 필터 헬퍼와 달리 절대 `null`을 반환하지 않는다 — null을 돌려주면 QueryDSL이 조건을 통째로 무시해 필터가 조용히 사라진다.

**리뷰 작성 가능 항목 조회처럼 `PRODUCT`를 INNER JOIN 하는 경로에 걸면**, 삭제된 메뉴를 주문했던 회원의 행이 통째로 사라져 **하드 삭제와 같은 데이터 손실**이 난다. 거는 곳과 걸지 않는 곳의 구분은 각 조회의 성격으로 판단한다.

#### 메뉴-가게 N:M — 진실원은 `PRODUCT_SHOP_LINK`다

→ `findShopProducts` · `findProductAvailability` · `existsProductInShop` · `findShopProductPrices` · `findProductOptionGroupsForManagement`

**`PRODUCT.shop_id`가 아니라 `PRODUCT_SHOP_LINK`로 조회한다.** 한 메뉴가 여러 가게 메뉴판에 노출될 수 있으므로 "이 가게 메뉴판에 무엇이 걸려 있는가"의 진실원은 링크 테이블이다. `shop_id`는 원본 소유 가게로 남아 다른 판정(메뉴명 중복·옵션그룹 소유권)에 계속 쓰이지만, **메뉴판 구성에는 쓰지 않는다.**

- **메뉴그룹·표시 순서도 링크에서 읽는다** — 같은 메뉴가 가게마다 다른 메뉴그룹·순서로 배치될 수 있어, 원본 컬럼(`PRODUCT.product_category_id`)을 쓰면 다른 가게 메뉴판에서 엉뚱한 그룹·순서로 보인다.
- **품절·노출·숨김은 메뉴가 소유하므로 링크로 분리하지 않는다**(`Product.soldOut`·`visible`) — 한 가게에서 품절하면 연결된 모든 가게에서 품절이다.
- 링크가 1개인 메뉴는 이 조회의 결과가 N:M 도입 이전과 완전히 동일하다 — 이 설계의 안전장치다.
- 옵션그룹의 가게 범위도 **링크를 거쳐 판정한다**(그룹 → 링크 → 메뉴 → 가게). 옵션그룹 행에도 `product_id`가 남아 있지만 그것은 1:N 시절의 잔재이며 N:M에서는 진실원이 아니다.

##### `existsProductInShop` — 소유권 판정은 동등 비교가 아니라 포함 관계다

→ `existsProductInShop(Long, Long)`

**메뉴의 가게와 대상 가게를 단순 동등 비교하던 방식을 이것으로 대체했다.** N:M 도입 전에는 "메뉴의 가게 == 내 가게"로 판정할 수 있었지만, 이제 한 메뉴가 여러 가게에 걸리므로 그 비교는 **포함 관계**여야 한다. 동등 비교를 남기면 연결된 가게의 점주가 자기 메뉴판의 메뉴를 열지 못한다. **컴파일러가 잡지 못하는 결함**이다.

- **원본 소유 가게(`PRODUCT.shop_id`)도 함께 인정한다** — 이관으로 모든 메뉴에 원본 링크가 생기지만, 링크가 아직 없는 메뉴(이관 직후 새로 만들어진 행 등)에서 원본 가게 점주가 잠기는 일을 막는 이중 안전장치다.
- **삭제된 메뉴는 없는 것으로 다룬다** — "메뉴 없음"과 "남의 가게 메뉴"를 호출부가 같은 `PRODUCT_NOT_FOUND`로 합쳐 존재 여부가 새지 않게 한다.

#### IDOR을 여는 반환 형태들

→ `findLinkedProductIdsByOptionGroup`(소유 상품 맵) · `findLinkedProductsByOptionGroupId` · `findVegetarianSetting`

- **소유 상품 맵의 값이 `Long`이 아니라 `Set<Long>`인 것이 핵심이다.** 링크 테이블 도입으로 한 그룹이 여러 메뉴에 연결되므로, 소유 상품을 단건으로 보면 "그 그룹의 임의의 한 메뉴"만 통과하고 나머지 메뉴의 옵션은 **예외도 로그도 없이 사라져** 장바구니 금액만 조용히 틀어진다. 개별·공통 그룹의 id 공간이 서로 겹칠 수 있으므로 결과 키는 `BatchOptionInfo#groupKey()`(공통 여부를 함께 인코딩한 키)다.
- `findLinkedProductsByOptionGroupId`가 메뉴의 `shopId`를 함께 반환하는 것은 **의도**다 — 옵션그룹은 자기 가게를 모르므로 호출부가 이 값으로 소유권을 역판정한다. **결과가 비면 소유 가게를 판정할 수 없다는 뜻이므로 호출부는 이를 "접근 불가"로 다뤄야 한다**(빈 목록을 "허용"으로 읽으면 IDOR이 열린다).
- `findVegetarianSetting`이 `shopId`를 함께 담는 이유는 소비 측(ceo-api)이 **이 메뉴가 정말 그 가게 것인지** 재확인해야 하기 때문이다 — 경로의 메뉴 id와 query의 가게 id가 서로를 검증하지 않으면 IDOR이 된다.

#### 관리 화면에는 `visible` 필터를 걸지 않는다

→ `findProductCategoriesForManagement` · `findProductOptionGroupsForManagement` · `findOptionsForManagement` · `findProductAvailability` · `findProductOptionAvailability` · `findProductImagesForManagement`

관리 화면이 **숨김 상태 자체를 조작하는 화면**이기 때문이다. 필터를 걸면 감춘(소프트 삭제된) 그룹·옵션을 다시 켤 방법이 영구히 사라진다. 손님 화면 조회(`findProductCategories` 등)와 쌍을 이루며, 손님 쪽만 `visible.eq(true)`를 건다.

- 메뉴그룹 관리 목록의 **메뉴 수는 삭제된 메뉴를 제외**한다. 이 값이 0이 아니면 그룹 삭제가 `PRODUCT_CATEGORY_HAS_PRODUCTS`로 거절되므로, 화면이 미리 안내할 수 있다.
- 삭제된 메뉴의 링크만 남은 옵션그룹은 목록에서 사라진다 — `notDeleted()`가 걸리기 때문이며 **의도된 동작**이다(그 그룹은 어느 살아있는 메뉴에서도 보이지 않는다).
- `findProductImagesForManagement`는 손님용 `findProductImageUrls`와 목적이 다르다 — 순서 변경·삭제 대상을 지목해야 하므로 **이미지 식별자**가 필요하고 숨김 상태도 보여야 한다.

#### 품절·숨김 필터는 그룹이 아니라 옵션 단위로 적용한다

→ `findNormalOptionsForAvailability` · `findCommonOptionsForAvailability` · `normalOptionMatchExists` · `commonOptionMatchExists` · `optionNameContains` · `soldOutOrHidden`

검색어·품절보기·숨김보기를 **그룹 단위로만 걸면** "치즈"를 검색했을 때 치즈 옵션을 가진 그룹의 **모든** 옵션이 함께 나와 검색이 사실상 무의미해진다. 반대로 **항목 단위 필터만 걸면** 옵션이 0개인 빈 그룹이 화면에 남는다. 그래서 두 겹으로 건다 — `*MatchExists` EXISTS 서브쿼리가 조건에 맞는 옵션을 하나라도 가진 그룹만 남기고, 그 안에서 `optionNameContains` 계열이 실제로 일치하는 항목만 남긴다.

`soldOutOrHidden` 계열이 헬퍼 하나로 묶여 있는 이유는 varargs `.where(...)`가 AND라 **OR을 표현할 수 없기** 때문이다. 단일 `BooleanExpression`으로 묶어 인자 하나로 넘긴다(위 [동적 where 조립 규칙](#querydsl-동적-where-조건-조립-규칙-booleanbuilder-대신-booleanexpression-varargs-헬퍼)의 예외 사유).

#### 공통 옵션그룹은 구조상 보증금 유형이 될 수 없다

→ `findCommonOptionGroups` · `findProductsBatch`(공통 옵션 분기) · `groupTypeNameOf`

공통 옵션그룹은 점주 CRUD 대상이 아니고 주문 검증 경로도 일반 옵션만 보므로, **구조상 보증금 유형이 될 수 없다.** 미러 테이블에 죽은 컬럼을 추가하는 대신 `NORMAL`을 하드코딩해 그 사실을 코드에 남긴다(`common=true`를 하드코딩하는 방식과 동일). 따라서 공통 옵션의 컵 보증금 관련 값은 항상 null이다.

`groupTypeNameOf`는 `null`을 `NORMAL`로 본다 — 기존 행은 DDL `DEFAULT 'NORMAL'`로 채워지지만 방어적으로 같은 기본값을 여기서도 쓴다.

#### 컵 보증금 금액은 저장값이 아니라 매번 계산한다

→ `findProductOptions` · `findProductsBatch` · `CupDepositPolicy`

`findProductOptions`와 `findProductsBatch`가 **같은 원천(`CupDepositPolicy`)으로 컵 개수에서 매번 계산**해야 메뉴판과 결제화면의 보증금이 갈리지 않는다.

#### 배치 조회 (N+1 회피)

→ `findProductOptions` · `findProductsBatch` · `findOptionsForManagement` · `findLinkedProductsByShop` · `findProductPricesByProductIds`

그룹·옵션·가격 행을 각각 배치(`in`) 조회해 N+1을 방지한다.

- **장바구니 배치 조회(`findProductsBatch`)는 존재하지 않거나 비활성인 상품을 결과에서 제외하지 않고 `available=false`로 남긴다** — 프론트가 "판매 종료" 안내를 띄울 수 있도록 하기 위함이며(쿠팡 `cartItemEnable` 방식), 요청한 `productId`의 **최초 등장 순서를 유지**한다. 그래서 모든 요청 `productId`를 먼저 맵의 키로 등록한 뒤 채운다.
- 옵션은 해당 상품에 **실제로 속하고** 조회에 성공한 경우에만 포함된다. 연결이 0건인 그룹(고아)은 소유 상품 set 자체가 없으므로 함께 걸러진다.
- `findLinkedProductsByShop`은 옵션그룹 연결 다이얼로그가 후보 그룹마다 `findLinkedProductsByOptionGroupId`를 개별 호출하던 N+1을 없앤다. **단일 가게 불변식**(옵션그룹은 한 가게에만 속한다) 덕분에, 이 가게의 메뉴로 조인을 걸면 결과가 곧 이 가게 옵션그룹 전체의 연결 목록이 된다.
- `findProductPricesByProductIds`는 장바구니·주문서용이다. 메뉴마다 `findProductPrices`를 부르면 항목 수만큼 쿼리가 나간다. **가격 행이 없는 메뉴는 결과에 등장하지 않는다** — 소비 측이 빈 목록으로 다루면 되고, 그때 화면은 기존 `PRODUCT.original_price` 경로로 표시된다(가격 행 도입 이전 데이터 호환).

#### 조인 fan-out을 접는 자리

→ `findProductOptionGroupsForManagement`(그룹당 1건 접기) · `findProductOptionAvailability`(메뉴명 모으기) · `findNutrition`

- 같은 그룹이 여러 메뉴에 연결돼 있으면 링크 `sort`가 달라 행이 여럿 나온다. **먼저 만난 행(= 가장 작은 sort)만 남겨** 그룹당 1건으로 접는다.
- 같은 옵션 그룹 id라도 연결 메뉴가 여러 건일 수 있어(1:N) 그룹 단위로 메뉴명을 모은다.
- **영양성분과 알레르기 성분은 함께 조인하지 않는다**(`findNutrition` / `findAllergenTypes` 분리) — 1:N을 함께 조인하면 성분 개수만큼 행이 늘어나 수치 14개가 중복 투영된다.

#### 옵션 정렬은 그룹이 아니라 링크가 갖는다

→ `findProductOptions` · `findCommonOptionGroups`

같은 그룹도 메뉴마다 진열 순서가 다를 수 있으므로, 정렬 키는 그룹 행이 아니라 링크 행에서 읽는다.

#### 알레르기 성분 정렬은 id 순이다 (알파벳순이 아니다)

→ `findAllergenTypes(Long)`

성분 코드 알파벳순이 **법령 열거 순서와 무관**해 화면 나열 순서가 고지 순서와 어긋나기 때문이다. 저장 순서(id 순)를 유지하면 점주가 체크한 순서(= 화면의 법령 순서)가 그대로 보인다.

#### `select`와 `Tuple.get`은 같은 표현식 인스턴스를 참조해야 한다

→ `findProductOptions` · `findProductsBatch` · `findProductOptionGroupsForManagement` · `representativeImageOf` 주변 조회

`NumberPath`·서브쿼리를 **지역 변수로 추출해** `select`와 `Tuple.get`이 같은 인스턴스를 보게 한다. 새로 만든 동등한 표현식을 `Tuple.get`에 넘기면 값을 찾지 못한다.

#### `findOptionGroupMergeCandidates` — 이 메서드만 네이티브 쿼리다

→ `findOptionGroupMergeCandidates(Long)` · `MERGE_CANDIDATE_SQL`

판정 기준(그룹명 + min/max + 옵션명·가격 집합 동일)이 파생 키 `GROUP BY`인데 **QueryDSL이 `GROUP_CONCAT`을 표현하지 못한다.** self-join으로 바꾸면 O(n²)이라 옵션그룹이 수십 개인 가게에서 급격히 느려진다.

- **서명 해싱은 하지 않는다** — 원시 `sig_payload`만 돌려주고 SHA-256은 도메인의 `ProductOptionGroupSignature`가 계산한다.
- `GROUP_CONCAT(... ORDER BY o.name, o.additional_price)`의 정렬 규칙은 `ProductOptionGroupSignature.payloadOf`와 **글자 단위로 일치**해야 한다. 옵션 `sort`는 그룹마다 다르지만 동일성에는 영향이 없다(같은 옵션을 다른 순서로 진열했을 뿐).
- `o.is_visible = 1` — 숨은(소프트 삭제된) 옵션이 동일성에 참여하면 눈에 똑같은 두 그룹이 **유령 옵션 하나 때문에** 다르다고 판정된다.
- `g.is_visible = 1` — 이미 합쳐졌거나 삭제된 그룹은 추천하지 않는다.
- `JOIN link/product` + `p.is_deleted = 0` — 관리 목록과 같은 가게 스코핑이며, "삭제된 메뉴에만 걸린 그룹은 사라진다"는 기존 동작을 그대로 재현한다. `GROUP BY g.id`가 링크 fan-out을 접는다.
- **`COUNT(o.id)`를 payload에 포함하는 것이 필수다** — `group_concat_max_len` 기본 1024바이트가 옵션 30개쯤에서 목록을 잘라 **서로 다른 그룹이 같다고 판정되는 조용한 오탐**을 만든다.

#### CQRS 교차 주입 금지가 조회 위치를 정한다

→ `findOptionGroupMergeExcludedSignatures` · `findProductPrices` · `findShopProductPrices` · `countVisibleProducts`

아래 조회들은 write 포트가 같은 데이터를 읽을 수 있는데도 이 DAO에 있다. 조회 서비스(`*QueryService`)가 write 포트를 주입하는 것을 ArchUnit `queryServicesShouldNotDependOnWritePorts`가 금지하기 때문이며, **표현 목적 경로는 이 DAO를 쓴다.**

| DAO 메서드 | 같은 데이터를 읽는 write 포트 | 반드시 조건이 일치해야 하는 이유 |
|---|---|---|
| `findProductPrices` | `ProductPriceRepository#findAllByProductId` | — |
| `findShopProductPrices` | `ProductPriceRepository#findAllByShopId` | 어긋나면 손님 화면과 점주 화면의 **매장가격 뱃지가 갈린다** |
| `countVisibleProducts` | `ProductRepository#countVisibleByShopId` | 조건(`visible = true` **이고** `deleted = false`)이 어긋나면 같은 가게의 뱃지가 점주·손님 화면에서 다르게 켜진다 |
| `findOptionGroupMergeExcludedSignatures` | exclusion write 포트 | 추천 목록을 만드는 것이 query 서비스라 write 포트를 주입할 수 없다 |

가격 행이 `shop_id`를 직접 들고 있지 않으므로 `findShopProductPrices`는 `PRODUCT_SHOP_LINK`로 조인해 노출 가게와 소프트 삭제를 함께 판정한다. **뱃지는 "이 가게에서 파는 메뉴들이 매장가와 같은가"를 묻는 것**이므로 판정 대상은 그 가게 메뉴판의 구성이며, 가격은 연결된 가게끼리 공유되므로 가격 행 자체는 메뉴 단위 그대로다.

#### 노출기간의 요일·시간 축은 write 포트로 읽는다

→ `findExposurePeriod(Long)`

요일·시간대 축은 판정 계산기가 **도메인 모델을 필요로 하므로** write 포트(`ProductExposureHourRepository`)를 통해 별도로 읽는다. 이 투영은 기간 축과 소유 가게만 담는다.

#### `BatchOptionInfo`는 DAO 밖으로 나가지 않는다

→ `BatchOptionInfo` · `BatchOptionInfo#groupKey()`

배치 조회 내부 계산용 `private` 중첩 record다. `new`로 직접 조립하는 내부 계산용이라 `Projections.constructor` 리플렉션 탐색을 거치지 않으므로 **투영 가드 2종의 대상이 아니다**(위 [읽기 계약 가드 2종](#읽기-계약-가드-2종은-이-모듈이-소유한다-챕터-09--application-common-module에서-이관) 절). 투영에 쓰려면 애초에 독립 파일로 분리해야 하고, 그 시점에 가드 대상이 된다.

### `<Ctx>DomainConfig` — 도메인 서비스 빈 등록 근거

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/*/config/*DomainConfig.java` (19개)

등록 위치 규칙 자체는 위 [규칙](#규칙) 절에 있다. 여기에는 **각 `@Bean`이 왜 도메인 서비스인가**(= 왜 애그리거트나 api 모듈이 아닌가)라는 판단 근거를 모은다. 전 config에 공통으로, 클래스 Javadoc은 "도메인 서비스는 `@Service` 없는 순수 POJO라 Spring이 스캔할 수 없으므로 새 POJO 도메인 서비스를 추가하면 여기에 `@Bean`을 추가한다"는 같은 문장이었다 — 규칙 절과 중복이라 옮기지 않는다.

#### 도메인 서비스로 뺀 판정 기준

주석들이 반복해 든 사유는 아래 네 가지다. 새 서비스를 만들 때도 이 기준으로 판단한다.

| 사유 | 뜻 | 예 |
|---|---|---|
| **집합 차원 불변식** | 행 하나만 보고는 판정할 수 없다 | `ShopNoticeExposureService`(가게당 노출 공지 1건) · `ShopMenuCollectionImageService`(최대 6·최소 1) · `ShopDeliveryAreaAdjustmentService`(진행 중 신청 중복 차단) |
| **크로스 애그리거트 원자성** | 여러 애그리거트가 한 트랜잭션에서 함께 바뀌어야 한다 | `ShopImageApprovalService`(요청 승인 + 이미지 반영) · `ShopPhoneNumberRegistryService`(대표번호 + 가게 애그리거트) · `ProductReviewStatsService` |
| **액터 무관 규칙** | 요청자(ceo)와 검수자(admin), 또는 admin CRUD와 batch 크롤링이 **같은 규칙**을 써야 한다 | `ProductRegistrationService` · `ShopOrderNoticeService` · `ShopRequestCancelService` |
| **컨텍스트 경계 파사드** | 소비 컨텍스트가 남의 모델·리포지토리를 직접 쓰지 않게 한다 | `ShopOrderContextService` · `OrderProductValidationService` |

복제하면 한쪽만 고쳐진다는 것이 공통 위험이다 — `ShopNextOpenTimeCalculator`가 요일별 영업시간 선택 규칙을 새로 짜지 않고 `ShopOperatingStatusCalculator`를 주입해 재사용하는 것도 같은 이유다(복제하면 요일 구분 추가 시 한쪽만 고쳐진다).

#### `ShopDomainConfig` — 개별 판단

**대상**: `.../shop/config/ShopDomainConfig.java`

- `prohibitedWordValidator` — **캐싱 데코레이터로 감싼 포트를 주입한다.** 검증기가 텍스트 검증마다 `findAll()`을 호출하므로 전량 로드가 매번 DB로 나가지 않게 한다. 금칙어는 SQL 시드 read-only 데이터라 정합성 리스크가 낮고, **캐싱을 어댑터 쪽에 두어 domain의 순수 POJO 검증기는 그대로 둔다.**
- `shopNextOpenTimeCalculator` — **product가 아니라 shop에 둔다.** 영업시간·휴무일 해석은 shop의 관심사이고, product 도메인 서비스가 `ShopBusinessHour`를 직접 참조하면 **컨텍스트 경계 위반**이다. 두 서비스의 조립은 ceo-api의 command service가 담당한다.
- `shopOrderAvailabilityService` — 주문 접수(`OrderPlacementService`)와 예약 생성(`ReservationBookingService`)이 **같은 규칙**을 쓰도록 검증을 이 서비스 하나에 모았다.
- `shopDeliveryAreaPolygonService` — 도형 원본과 그것을 환산한 행정동 집합이 **같은 트랜잭션에서 항상 일치**해야 한다. 환산을 비동기로 미루면 "저장은 됐는데 주문은 거절되는" 창이 생기고, **그 사이 등록 건수가 0이 되면 주문 접수의 지역 검사가 통째로 비활성된다.**
- `shopDeliveryAreaRadiusService` — 후보 행정동을 **write 포트로 읽는다.** 명령 경로가 infra query DAO를 주입하면 CQRS 교차 주입 금지 규칙에 걸린다. 거리 판정은 원 근사 다각형이 아니라 **하버사인 직선거리**로 한다.
- `shopOriginInfoService` — **금칙어 검수를 하지 않는다.** 원산지 본문은 마케팅 문구가 아니라 **법령이 요구하는 사실 표시**라, 검수로 저장을 막으면 표시 의무를 이행할 수 없게 된다.
- `shopChangeHistoryRecorder` / `shopCeoAssignmentRecorder` / `shopRequestIndexRecorder` — 변경을 수행하는 도메인 서비스가 **같은 트랜잭션에서 동기 호출**한다. **새 배정 경로나 새 요청 성격 애그리거트를 만들면 그 도메인 서비스에 이 Recorder를 배선해야 한다** — 배선 누락은 컴파일에 걸리지 않는다. `ShopChangeValueFormatter`는 상태 없는 static 유틸이라 빈으로 등록하지 않는다.
- `shopOrderNoticeService` — PUT 하나가 기존 행 유무에 따라 insert/update로 갈리므로 단일 애그리거트 연산이 아니고, 그 분기 규칙을 ceo·admin 두 api 모듈이 각자 갖지 않도록 도메인 서비스가 소유한다. 승인 절차가 없어 상태 전이가 `hidden` 하나뿐이라 서비스를 더 쪼갤 이유가 없다.
- `shopCeoAssignmentService` — 재배정을 `REVOKE` + `GRANT` **2행**으로 남긴다.

#### `ProductDomainConfig` — 개별 판단

**대상**: `.../product/config/ProductDomainConfig.java`

- `cupDepositPolicy` — 순수 계산기인데도 **빈으로 두는 이유는 요율을 단 한 곳에 두기 위함**이다. 점주 설정(ceo)·손님 메뉴판(web)·주문 금액 확정(order) 세 경로가 **같은 인스턴스를 주입받아야** "화면 금액과 결제 금액이 다른" 사고가 구조적으로 불가능해진다.
- `productDeletionService` — 삭제에도 숨김과 **같은 불변식**(노출 메뉴 ≥1 등)을 적용한다. **숨김만 막고 삭제를 열어두면 점주가 삭제로 우회해 빈 메뉴판을 만들 수 있다.**
- `productSortService` — `sort` 값을 클라이언트에서 받지 않고 **순서 있는 id 배열만 받아 서버가 0..N-1로 정규화**한다.
- `productOptionGroupLinkService` — **옵션그룹은 단일 가게에만 속한다**는 불변식을 강제해, 소유권 판정에서 ANY/ALL 구분이 사라지게 한다. 이 불변식이 `ProductQueryDao#findLinkedProductsByShop`의 단일 조회를 성립시킨다.
- `productOptionGroupMergeService` — 링크 재배치는 `ProductOptionGroupLinkService#relink`에 위임한다. UNIQUE 충돌 처리와 sort 불변식이 그 클래스 소유로 남아야 `renumber`를 공개하지 않아도 된다.
- `productExposureService` — 요일 묶음과 개별 요일의 **혼용을 금지한다.** 그 조합을 저장할 수 없게 하면 SQL 술어(`ProductQueryDao#exposedNow`)와 계산기(`ProductExposureCalculator`)가 갈릴 여지가 없다.
- `productNutritionService` — 영양성분과 알레르기를 **한 서비스가 소유한다.** 나누면 "영양성분만 저장되고 알레르기는 이전 값이 남은" 중간 상태가 손님 화면에 **잘못된 알레르기 표시**로 노출된다. 승인 워크플로가 없는 것은 점주(가맹본사)만이 아는 사실 정보여서 관리자가 검증할 근거가 없기 때문이다.
- `productPriceService` — **전체 교체(PUT) 의미론**이라 정렬·가격명 중복 같은 컬렉션 단위 불변식을 한 번에 판정한다. `sort=0` 행의 배달가를 `PRODUCT.original_price`에 **동기화**해 그 컬럼을 읽는 기존 수십 경로(주문·검색·오늘의할인·목록)의 동작을 그대로 유지한다. `StorePriceVerificationPort`를 받는 이유는 가격 변경으로 배달가 > 매장가가 되면 **그 자리에서** 가게 인증을 내려야 하기 때문이다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.

##### 승인 워크플로의 방향 비대칭

→ `productImageApprovalService` · `productRepresentativeApprovalService` · `productVegetarianApprovalService`

**등록·지정은 승인을 거치고, 순서 변경·삭제·해제는 즉시 반영된다.** 검수의 목적이 "부적합한 내용의 노출을 막는 것"이므로 내리는 방향에는 그 위험이 없기 때문이다. 채식 설정만은 신청조차 즉시 반영하지 않는데, **채식 표기가 알레르기·신념과 직결돼 잘못된 표기의 대가가 크기** 때문이다.

사장님 추천은 가게당 최대 6개·이미지 필수·최소 1개 유지 세 제약을 `ProductRepresentativeApprovalService`가 단독으로 소유한다. 세 번째 제약은 일괄 숨김(`ProductAvailabilityService`)이 이미 쓰는 `PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`를 재사용하므로, **두 경로가 같은 하한을 공유한다.**

### 전 `*QueryDao` 공통 — 반복되던 클래스 Javadoc

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/*/query/*QueryDao.java`

거의 모든 DAO의 클래스 Javadoc이 아래를 **글자만 바꿔 반복**하고 있었다. 전부 위 [`<ctx>/query/` 절](#ctxquery--read-어댑터-cqrs-query-측-개정됨--읽기-경로-포트화)이 이미 규칙으로 갖고 있는 내용이라 개별 이관하지 않고 여기 한 번만 적는다.

- 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하며 도메인 모델을 거치지 않으므로 write 포트와 역할이 겹치지 않는다.
- 소비 모듈의 `*QueryService`가 주입해 쓰며, **그 덕분에 api 모듈은 QueryDSL을 알지 않는다.**
- 도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 한 클래스에 둔다. 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다.
- 소비자별로 필요한 필드 셋이 달라 Result를 통합하지 않는다.
- 조인으로 얻은 저장 경로는 `FileUrlResolver`로 표시용 URL까지 변환해 Result에 담는다 — `Projections.constructor`는 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어 **fetch 직후 재조립한다**(`withResolvedXxx` 패턴).

**개별 DAO 문서에 이 문장들을 다시 쓰지 않는다.** 새 DAO를 만들 때도 마찬가지다 — 규칙 절이 이미 말하는 것을 클래스 Javadoc이 복창하던 것이 이 모듈 주석 7,244줄의 큰 몫이었다.

#### 반복되던 구현 관용구 4종

같은 이유로, 아래 관용구는 개별 DAO마다 설명이 붙어 있었으나 **한 번만 적는다.**

| 관용구 | 이유 |
|---|---|
| `@Convert` VO 컬럼의 raw `Long` path 헬퍼 (`shopThumbnailImageFileId()` · `memberProfileImageFileId()` · `shopStationId()` 등) | VO로 변환되는 컬럼을 QueryDSL에서 원시 타입으로 비교·조인하기 위해 별도 path를 만든다 |
| 같은 테이블 두 번 조인 시 **별칭 분리** (`replyToMember` 등) | 별칭을 재사용하면 조인이 서로를 덮는다. `ProductQueryDao`의 서브쿼리 별칭과 같은 사유다 |
| **count와 content의 술어·조인 공유** | 따로 두면 한쪽만 고쳐져 페이징 `totalElements`가 어긋나고 마지막 페이지가 빈다. **`innerJoin`은 count에서도 재현**해야 하고(짝이 없는 행을 제외하므로), 1:1 조인이라 행이 늘지 않으면 `countDistinct`는 필요 없다 |
| **파일 조인은 `leftJoin`** | 파일 미등록 행이 목록에서 통째로 누락되지 않게 한다 |

`withResolvedXxx` 재조립은 **record 재조립이 위치 기반**이므로 필드 선언 순서와 인자 순서를 하나씩 대조한다 — 타입이 같으면 뒤바뀌어도 컴파일이 통과한다.

### 개별 DAO — 그 DAO에만 있는 판단

아래는 위 공통 규칙으로 설명되지 않는 것들이다.

#### `ShopRequestQueryDao` — 인덱스 테이블 단독 조회와 반열림 날짜 구간

**대상**: `.../shop/query/ShopRequestQueryDao.java`

- **목록은 인덱스 테이블 단독으로 조회한다** — 유형별 원본을 UNION하지 않으므로 정렬·페이징·필터가 단일 테이블 인덱스로 해결되고, **유형이 늘어도 이 코드는 그대로다.** 진입 인덱스는 `(shop_id, created_at)`이며 기본 정렬 `created_at DESC, id DESC`가 이를 그대로 탄다.
- **상세는 인덱스와 원본을 함께 읽는다.** 인덱스에서 `requestType`/`sourceRequestId`를 얻어 유형별 원본을 별도 투영하며, **상태·반려 사유는 원본 값으로 응답한다**(인덱스는 파생 읽기모델이라 진실원이 아니다).
- **날짜 필터는 반열림 구간** `[startDate 00:00, endDate+1일 00:00)`으로 만든다 — `DATE(created_at)` 같은 **함수를 컬럼에 씌우면 인덱스를 타지 못한다.** 종료일도 `loe(endDate.atStartOfDay())`가 아니라 반열림 상한이어야 **종료일 당일 접수분이 빠지지 않는다.** 조회 기간 상한은 두지 않는다.
- `findComments`만 **작성순(ASC)**이다. 이 저장소의 목록 조회는 대체로 최신순인데 여기만 다른 것은 이 목록이 **대화**라서다 — 문의와 답변이 오간 순서대로 읽혀야 한다. 페이징하지 않는 것도 같은 이유다(요청 1건당 대화량이 적고 화면이 스레드를 통째로 보여준다).
- `commentCount()` 상관 서브쿼리는 `(shop_request_index_id, id)` 인덱스가 커버하며, **목록 size가 최대 100이라 행마다 실행돼도 비용이 낮다**(그래서 배치 조회로 바꾸지 않았다).
- 첨부 URL은 목록에서 join 없이 **존재 여부만** 담고, 상세에서만 완성한다. 응답에 `~FileId`를 노출하지 않는 규칙에 따라 URL로 변환해 내보낸다.

#### `ReviewManagementQueryDao` — 관리자는 전량 열람이 기본이다

**대상**: `.../review/query/ReviewManagementQueryDao.java`

web/공용 조회는 `ReviewQueryDao`에 있고 여기에는 관리 화면 전용 조회만 둔다. **관리 화면은 숨김 처리된 리뷰·댓글·답글까지 모두 봐야 하므로 `hidden` 필터를 걸지 않는다.**

`ownerOnlyEq`·`hidden` 두 축 모두 **필터를 강제하지 않고 검색 수단으로만 제공한다**(`null`이면 조건 없음 = 전체). 관리자에게는 전량 열람이 기본이기 때문이다.

댓글·답글 목록은 **회원 테이블을 join해 한 번에 투영한다** — 과거 조회 서비스가 도메인 모델을 읽은 뒤 작성자 닉네임을 별도 조회해 맵으로 붙이던 것을 대체했다.

#### `ReservationQueryDao` — 차단 상태의 단일 원천은 도메인이다

**대상**: `.../reservation/query/ReservationQueryDao.java`

- `existsBlockingReservation`의 차단 대상 상태는 도메인이 소유하므로 **`ReservationStatus.blockingStatuses()`를 그대로 참조한다.** 여기에 상태 목록을 복제하면 실제 차단 로직과 갈린다.
- `findSlotOccupancies`는 **행이 존재하는 슬롯만** 돌려준다. 행이 없는 시간대는 예약 0건이므로 결과에 없고, **소비 측이 전체 슬롯 목록과 병합해 기본 정원으로 채운다.**
- 가게·파일을 join으로 함께 투영해, 과거 예약을 도메인 모델로 읽은 뒤 가게를 건당 다시 조회하던 목록 크기만큼의 반복 조회를 없앴다.

#### `EventQueryDao` — 삭제 필터는 이관 이전 동작을 보존한다

**대상**: `.../event/query/EventQueryDao.java`

**삭제 필터링을 전 경로에 일괄 적용하지 않는다.** admin 관리 목록·상세와 당첨자 목록은 soft delete 분을 제외하고, **web 노출 목록·상세와 발표 목록은 원본 쿼리에 삭제 필터가 없었으므로 추가하지 않는다.** 일관성을 이유로 없던 필터를 넣으면 이관 전후 동작이 달라진다.

세 애그리거트(이벤트·당첨자·발표)의 조회를 한 DAO에 두며, 썸네일·배너는 **각각 별도 alias로 left join**해 추가 조회 없이 함께 투영한다.

#### `RankQueryDao` — 소프트 삭제 도메인

**대상**: `.../rank/query/RankQueryDao.java`

소프트 삭제 도메인이므로 **모든 조회 경로에 `deleted.isFalse()` 필터를 유지한다**(`EventQueryDao`와 달리 예외가 없다). `findActiveDuration`은 시작일이 가장 늦은 1건이고, `findMemberRank`가 비어 있으면 소비 측이 **0위 응답으로 대체**한다.

#### `MemberQueryDao` — 탈퇴 회원의 번호는 재사용 가능하다

**대상**: `.../member/query/MemberQueryDao.java`

- `existsByActivePhoneNumber`는 **탈퇴하지 않은 회원만** 본다. 탈퇴 회원의 번호는 재사용 가능하기 때문이다.
- `findMemberWithProfileImagesByIds`는 목록 화면이 작성자 정보를 합성할 때의 N+1을 없앤다 — 과거 단건 조회를 회원 수만큼 반복하던 것을 `in` 절로 대체했다.
- `findProfileImageUrl` 단건 조회가 따로 있는 이유는, 회원 상세 응답이 도메인 모델(`Member`)로 조립되는데 **프로필 이미지만 이 조회로 대체해 파일 단건 재조회를 없애기** 위해서다.
- `gender`는 응답까지 그대로 전달되는 표현용이라 도메인 enum이 아니라 **이름 문자열로 투영한다**(`stringValue()`).
- `existsByNickname` 같은 표현용 단건 판정은 write 포트가 아니라 이 어댑터가 답한다.

### `ShopQueryDao` — 가게 설정·관리 화면 조회 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopQueryDao.java`

#### 클래스 역할과 DAO 이분할

→ `ShopQueryDao`(클래스 선언)

`shop` 도메인 read 어댑터(CQRS query 측). 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하며 도메인 모델을 거치지 않으므로 write 포트(`ShopRepository`·`ShopDetailRepository` 등)와 역할이 겹치지 않는다. 소비 모듈(web/admin/ceo-api)의 `Shop*QueryService`가 주입해 쓰며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다. 구현하는 읽기 계약은 `ShopQueryPort`·`ShopBasicInfoQueryPort`·`ShopManagementQueryPort`·`ShopOwnerQueryPort` 4종이다.

**shop은 대형 도메인이라 공통 지침의 용도별 분리 허용에 따라 DAO를 둘로 나눈다.** 이 클래스는 *가게별 설정·관리 화면 조회*(전화번호·편의정보·콘텐츠보드·위생뱃지·이미지 변경요청·편의시설/음식유형 배정·배너·사진)를 담당하고, 목록·검색·베스트 등 **대형 조인은 `ShopSearchQueryDao`가 담당한다.**

소비자별 메서드는 CLAUDE.md 규칙대로 admin 마커 없이 순수 동작명을 쓰고, 비-admin 형제와 충돌할 때만 시그니처·`ById` 한정어로 구별한다.

#### 파일 테이블 별칭을 목적마다 새로 만드는 이유

→ `activeFile` · `contentBoardImageFile` · `menuCollectionImageFile` · `shopThumbnailFile`

`UPLOADED_FILE`을 여러 목적으로 조인하므로 목적별 별칭 인스턴스를 따로 둔다.

- `activeFile` — 카테고리의 활성/비활성 아이콘을 한 쿼리에서 함께 투영하기 위한 별칭
- `contentBoardImageFile` — 콘텐츠보드/이미지 변경요청의 이미지 조인용
- `menuCollectionImageFile` — 메뉴모음컷 조회용. **검수 목록은 `SHOP`도 함께 조인하므로 공용 `uploadedFileJpaEntity` 별칭을 재사용하면 다른 목적의 조인과 서로를 덮는다.**
- `shopThumbnailFile` — 가게 상세 조립 시 썸네일/상표 이미지 조회용

#### 포트에 선언하지 않은 infra 내부 조회 (`findShopName`)

→ `findShopName(Long)`

가게명 한 필드만 필요한 소비처(알림 본문 조립 등)를 위해 도메인 모델(`Shop`)을 통째로 로드하지 않는다. 그 소비처가 애그리거트 경계 밖(알림 리스너)이라 도메인 모델을 넘기면 컨텍스트가 결합되기 때문이다.

**포트에 선언되지 않은 infra 내부 조회다.** 유일한 소비처인 `ReviewOwnerReplyEventListener`가 같은 모듈에서 이 DAO를 구체 타입으로 주입하므로, application 계층이 소유할 계약이 아니다(`MemberReviewCountQueryPort` 선례와 같은 취급이며, 챕터 04의 포트 분할 대상에서 제외했다).

#### 도메인 모델 로드를 대체하지 않고 보완하는 조회

→ `findShopImageUrls(Long)` · `findVisibleDetailById(Long)` · `existsBookmark(Long, Long)`

- `findShopImageUrls` — 도메인 모델(`Shop`)은 다른 필드를 위해 계속 로드하되 **이미지 URL만 이 조회로 대체해 파일 단건 재조회를 없앤다.**
- `findVisibleDetailById` — 회원 노출용 가게 단건. 폐업·노출정지 가게는 투영되지 않으며, 가시성 조건(`permanentlyClosed=false`·`hidden=false`)은 write 포트 `ShopRepository#findVisibleById`와 **동일하게 유지한다.** 애그리거트를 로드해 표시 필드를 꺼내던 기존 형태를 한 번의 투영으로 대체한 것이다.
- `existsBookmark` — 표현용 단건 판정이라 write 포트가 아니라 이 어댑터가 답한다.
- `findManagementDetailById` — 관리 상세는 회원 노출용과 달리 **폐업·노출정지 가게도 조회된다.**

#### write 포트의 목록 조회와 공존하는 표현용 조회

→ `findBusinessHours(Long)` · `findBreakTimes(Long)` · `findClosedDays(Long)`

같은 데이터를 도메인 서비스도 읽지만 그쪽은 write 포트로 도메인 모델을 로드한다. **목적(불변식 검증 vs 화면 표현)과 반환 타입이 다르므로 중복이 아니다.**

#### 미설정 상태의 판정은 소비 측에 맡긴다

→ `findOriginInfo(Long)` · `findConvenienceInfo(Long)` · `findLatestOwnerMessage(Long)`

가게당 1건이며 미설정이면 `Optional.empty()`다. 원산지의 경우 점주 화면은 그때 빈 폼을, 손님 화면은 원산지 영역 숨김을 택하므로 **판정을 DAO가 하지 않는다.**

#### 이미지 변경요청은 유형 필터가 필수다

→ `findImageChangeRequests(Long, ShopImageType)`

이미지 유형별로 걸러 최근 요청 순으로 반환한다. **상표·대표이미지는 화면에서 각각 독립된 항목으로 "검수 대기 중" 배지를 표시하므로, 유형 필터 없이 반환하면 한쪽 유형의 PENDING 요청이 다른 쪽 배지까지 켠다.**

#### 관리 화면에는 노출 필터를 걸지 않는다

→ `findAllAmenityCategories()` · `findAllFoodTypeCategories()` · `findPhotoCategoryImages(Long)` · `findContentBoardPage(...)` · `findImageChangeRequestPage(...)` · `findMenuCollectionImageRequestPage(...)`

관리 화면은 미노출분까지 봐야 하므로 `visible` 필터를 걸지 않으며, 회원 화면용 `findVisibleFoodTypeCategories()`·`findVisibleAmenityCategories()`와 쌍을 이룬다. 사진 카테고리 이미지의 관리 목록은 **미노출 이미지도 함께 보여주고 그 상태를 표시해야 하므로** `visible`을 담은 `ShopPhotoCategoryImageManagementResult`를 돌려준다.

`menuCollectionImageStatusEq(ApprovalStatus)`처럼 상태 미지정(`null`)은 "전체"를 뜻하므로 술어를 붙이지 않는다.

#### 배정 목록은 소비 화면마다 투영이 다르다

→ `findAmenityAssignments(Long)` · `findAmenitiesWithCategory(Long)` · `findFoodTypeAssignments(Long)` · `findFoodTypeCategoryNames(Long)`

- `findAmenityAssignments` — 관리·설정 화면용. 카테고리 정보 포함
- `findAmenitiesWithCategory` — 회원 상세 화면용. **배정 식별자 없이 표시용 필드만**
- `findFoodTypeAssignments` — 관리 화면용. 아이콘 파일을 조인해 URL까지 완성

`findFoodTypeCategoryNames`는 `findFoodTypeAssignments`와 **목적이 다르다.** 이쪽은 *정책 판정*에 쓰이는 이름 집합만 필요하며(채식 메뉴 등록 불가 카테고리 판정 — product 컨텍스트가 소비자), 화면 표시명만 뽑는다. **아이콘 조인을 함께 끌고 오면 판정에 쓰이지 않는 파일 조회가 얹히고, `activeImageFileId` 결측 시 inner join으로 카테고리가 조용히 누락돼 거절해야 할 요청이 통과한다.**

#### 메뉴모음컷 — 상태 필터를 투영에 둔다

→ `findMenuCollectionImages(Long)` · `findExposedMenuCollectionImages(Long)`

- 점주 화면(`findMenuCollectionImages`)은 `sort` 순 **상태 무관 전량**이다. 대기·반려 건까지 내려보내는 이유는 원문 규격이 점주 화면에 검수 진행 상태를 보여주도록 규정하기 때문이다.
- 손님 화면(`findExposedMenuCollectionImages`)은 **승인분만** 본다. **상태 필터를 소비 측(api 모듈)이 아니라 이 투영에 두는 이유는, 필터를 호출부에 맡기면 새 소비 경로가 생길 때 조용히 빠져 대기·반려 이미지가 손님에게 노출될 수 있기 때문이다.**

#### 표시용 URL 변환은 fetch 직후 재조립한다

→ `withResolvedIconUrls(ShopFoodTypeCategoryResult)` 계열

`Projections.constructor`가 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어, 투영된 저장 경로를 fetch 직후 표시용 URL로 바꿔 재조립한다.

---

### `ShopSearchQueryDao` — 목록·검색 조회 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopSearchQueryDao.java`

#### 클래스 역할과 목록 조회의 기본 형태

→ `ShopSearchQueryDao`(클래스 선언) · `stationNamesByShopId` 이하 일괄 보강 조회

가게 목록·검색 read 어댑터(CQRS query 측). 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하므로 write 포트(`ShopRepository`)와 역할이 겹치지 않으며, 소비 모듈은 QueryDSL을 알지 않는다. `ShopSearchQueryPort`·`ShopSearchManagementQueryPort`를 구현한다. 이 클래스는 *목록·검색·베스트·즐겨찾기 등 대형 조인*을 담당하고, 가게별 설정·관리 화면 조회는 `ShopQueryDao`가 담당한다.

**목록 조회는 페이지 대상 가게를 먼저 뽑고 역·썸네일·음식유형·리뷰수·즐겨찾기수를 shopId 일괄 조회(in절)로 채우는 방식을 유지한다** — 컬렉션 필드(음식유형 다건)가 있어 단일 조인 투영으로는 카티전 곱이 생기기 때문이다.

#### 지도 마커 반경은 도메인 정책이 아니라 구현 세부다

→ `MAP_MARKER_RADIUS_METERS` · `findNearbyShops(BigDecimal, BigDecimal)`

지도 마커 조회 반경(200m). 위·경도 1도 ≈ 111km 근사로 사각 범위를 계산한다.

**도메인 정책이 아니라 지리 계산 구현 세부라 DAO에 잔류한다** — 이 값은 "가게가 어떠해야 하는가"가 아니라 "지도 뷰포트 질의를 어떤 사각 범위로 근사할 것인가"를 정하며, 정밀 거리 계산(하버사인)이나 공간 인덱스로 구현이 바뀌면 함께 사라진다. 도메인 어휘에 대응 개념이 없다.

`findNearbyShops`를 비롯해 `findBestShops`·`findLatestShops`·`findMyBookmarkedShops`는 모두 폐업·노출정지 가게를 제외한다. 베스트는 그에 더해 평점 없는 가게도 제외한다.

#### 리뷰 수 집계는 숨김과 사장님만보기를 둘 다 제외해야 한다

→ `reviewCountsByShopId(List<Long>)`

가게별 고객 노출 리뷰 수는 숨김(관리자 게시중단)과 사장님만보기를 **둘 다** 제외한다.

**`ownerOnly`를 빼먹으면 목록 카드의 리뷰 수만 늘고 정작 가게 리뷰 목록에는 그 리뷰가 없어 건수 차이로 비공개 리뷰의 존재가 새어나간다**(이 조회는 비로그인도 호출 가능한 경로다). 같은 이유로 `ReviewStatisticsQueryDao#countVisibleByShopId`와 **조건이 일치해야 한다** — 한쪽만 고치면 같은 가게의 두 숫자가 어긋난다.

#### 배달지역 필터는 노출과 주문 접수의 판정을 일치시킨다

→ `deliveryAreaCovers(Long)` · `findBestShops(Long, PageQuery)`

회원 배송지의 행정동을 배달하지 않는 가게를 목록에서 제외한다.

**이 필터가 없으면 고객은 결제 마지막 단계에서야 배달 불가를 안다**(`ORDER_DELIVERY_AREA_NOT_COVERED`) — 목록에 보이는 것과 주문할 수 있는 것이 어긋난다. 노출 시점과 주문 접수 시점(`OrderPlacementService#validateDeliveryArea`)이 같은 규칙을 쓰도록 두 판정을 일치시킨다.

**배달가능지역을 하나도 등록하지 않은 가게는 통과시킨다.** 주문 접수 검사와 같은 원칙이며("정보를 안 넣은 것을 닫힌 것으로 보지 않는다"), **이것이 없으면 미설정 가게가 배포 즉시 목록에서 전부 사라진다.** 기존 데이터의 대부분이 0건이므로 사실상 서비스가 비는 것과 같다.

행정동 인자가 `null`이면(비로그인·주소 미등록·행정동 미매칭) 필터를 걸지 않는다 — **좁힐 근거가 없을 때 감추는 것은 노출 축소일 뿐이다.**

#### 필터 집합 교집합과 목록 후처리

→ `intersect(Set<Long>, Set<Long>)` · `withResolvedImageUrlAndTipRange(...)` · `minDeliveryTip(...)` · `maxDeliveryTip(...)`

`intersect`는 두 필터 집합의 교집합을 내되, 한쪽이 없으면 다른 쪽을, 둘 다 없으면 `null`(필터 없음)을 돌려준다.

`withResolvedImageUrlAndTipRange`는 투영된 저장 경로를 표시용 URL로 바꾸고 배달팁 하한/상한을 채워 재조립한다. `Projections.constructor`가 생성자 직접 투영이라 두 변환 모두 투영식에 넣을 수 없어 fetch 직후 호출한다 — **배달팁 범위는 올림 계산이 섞여 SQL 집계로 표현되지 않는다**(`ShopDeliveryTipQueryDao#findTipRanges` 참고). 배달팁 설정이 없는 가게는 하한·상한 모두 0이다.

---

### `ShopDeliveryTipQueryDao` — 배달팁 조회·표기 산출 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/query/ShopDeliveryTipQueryDao.java`

#### 클래스 역할과 파트별 분리

→ `ShopDeliveryTipQueryDao`(클래스 선언) · `findSetting` · `findTiers` · `findRegionTips` · `findScheduleTips` · `findHolidayTipAmount`

가게 배달팁 read 어댑터(CQRS query 측). 점주 설정 화면과 고객 배달팁 팝업이 쓰는 표현용 조회를 담당한다 — write 포트 `ShopDeliveryTipRepository`는 불변식 검증·주문 접수 산출에 필요한 조회만 갖고, **화면용 조인 투영(지역 이름 조립 등)은 여기가 소유한다(CQRS 교차 주입 금지).**

**배달팁 5종을 한 번에 조회하는 단일 메서드를 두지 않고 파트별로 나눈 것은, 고객 팝업이 구간·설정만 필요로 하는 등 소비 지점마다 필요한 파트가 다르기 때문이다.** 소비 Service가 필요한 것만 조합한다.

- `findSetting` — 설정 헤더. 설정한 적이 없는 가게는 빈 `Optional`
- `findTiers` — 구간 순서(= 주문금액 오름차순)
- `findRegionTips` — 행정동 마스터를 조인해 표시용 이름까지 완성
- `findHolidayTipAmount` — 미설정이면 0. **미설정과 0원을 구분하지 않는 것이 이 팁의 규격이다**(0원 저장은 삭제로 해석된다)

#### 상한 표기용 최대 배달 거리는 도메인 값이 아니다

→ `MAX_DELIVERY_DISTANCE_METERS`

배달팁 **상한** 표기에서 가정하는 최대 배달 거리(5,000m). 기본배달거리 허용값의 최댓값(3km)의 곱절 남짓을 잡았다 — 기본배달거리를 3km로 잡은 가게도 상한이 0이 되지 않으면서, 100m당 300원짜리 최악 설정에서도 상한이 도메인 상한(`DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND`)에 닿아 그 이상 과장되지 않는다.

**도메인 정책이 아니라 표기용 가정이라 DAO에 잔류한다**(`ShopSearchQueryDao#MAP_MARKER_RADIUS_METERS`와 같은 성격) — "가게가 어디까지 배달해야 하는가"를 정하는 값이 아니라 "주소가 확정되기 전 상한을 어느 거리로 근사해 보여줄 것인가"를 정한다. 가게별 배달 반경이 데이터로 생기면 사라진다.

#### 거리별 상한은 도메인 상한을 그대로 쓰면 안 된다

→ `distanceUpperBound(ShopDeliveryTipSettingResult)`

거리별 할증은 원리상 거리에 비례해 무한히 커질 수 있어 그대로는 상한이 정의되지 않는다. **그렇다고 `DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND`(10,000원)를 그대로 쓰면, 500m당 100원짜리 가게도 목록에 "최대 10,000원"으로 표기돼 거의 모든 가게가 같은 과장된 상한을 보이게 된다.**

그래서 현실적인 최대 배달 거리 `MAX_DELIVERY_DISTANCE_METERS`까지 배달한다고 가정해 산출하고, 결과는 도메인이 이미 정한 추가 배달팁 상한으로 자른다 — 자르는 계산이 `ShopDeliveryTipSetting#calculateDistanceSurcharge`와 같은 식이라 확정 계산 결과가 이 상한을 넘지 않는다.

#### 배달팁 범위 산출은 N+1을 만들지 않는다

→ `findTipRanges(List<Long>)` · `findTipRange(Long)` · `findSettings(List<Long>)` · `collectAmounts(...)`

목록 행마다 조회하지 않고 `shopId` 목록으로 배달팁 4종을 **group-by 집계 4회(+헤더 1회)만 수행한 뒤 Java에서 합친다.** 거리별 상한이 SQL 집계로 표현되지 않는 올림 계산(`distanceUpperBound`)이라 스칼라 서브쿼리 한 방으로는 낼 수 없고, **쿼리 수가 가게 수와 무관하게 상수라 목록 규모가 커져도 비용이 늘지 않는다.**

`findSettings`는 여러 가게의 설정 헤더를 한 번에 읽으며(목록용), 설정 행이 없는 가게는 맵에 없다. `findTipRange`(단건)는 `findTipRanges`와 같은 규칙을 쓰고, 설정이 없으면 0/0이다.

`collectAmounts`가 `(shopId, min, max)` 3열 집계 결과를 **튜플 위치로 읽는 이유**는 집계 대상 테이블이 4종이라 표현식 객체를 키로 넘기면 호출부마다 같은 표현식을 두 번 써야 하기 때문이다 — 열 순서는 바로 위 `select(...)`에 고정돼 있다.

지역별 팁은 max에만 쓰이므로 `collectAmounts`가 채운 min 맵은 받기만 하고 버린다(`unusedMinRegionTips`) — 공용 헬퍼가 min/max를 함께 채우며, **집계 쿼리 비용은 동일하다.**

#### 지역 표기는 다른 DAO와 같은 형태여야 한다

→ `regionName()`

표시용 행정동 전체 이름(`"서울특별시 강남구 역삼1동"`)을 SQL에서 조립한다 — **`ShopDeliveryAreaQueryDao`와 같은 형태여야 두 화면의 지역 표기가 갈리지 않는다.**

### `ReviewQueryDao` — 쿼리 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewQueryDao.java`

#### `ReviewQueryDao` 클래스 역할

`review` 도메인 read 어댑터(CQRS query 측). 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하며 도메인 모델을 거치지 않으므로 write 포트(`ReviewRepository`)와 역할이 겹치지 않는다. 소비 모듈(web-api)의 리뷰 조회 서비스가 이 DAO를 주입해 쓰며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.

**도메인당 DAO 1개가 원칙이나 review는 대형 도메인이라 용도별로 분리했다.** admin(관리) 화면 전용 조회는 `ReviewManagementQueryDao`, 집계·통계 조회는 `ReviewStatisticsQueryDao`가 담당하고, 여기에는 web/공용 목록·상세 조회만 둔다.

#### 별칭을 새로 만드는 이유 — 같은 테이블을 두 번 조인한다

→ `replyToMember`

답글의 "누구에게 단 답글인지"(replyTo) 회원은 작성자 조인과 **같은 회원 테이블**이라 별칭을 분리해야 한다. 기본 별칭 하나로는 두 조인이 충돌한다.

#### 노출 조건 헬퍼 두 종 — 목록/집계용과 상세용이 다르다

→ `visibleToCustomer()` · `visibleToViewer(Long)`

`visibleToCustomer()`는 고객 목록·집계에 노출되는 리뷰 조건으로, 숨김(관리자 게시중단)과 사장님만보기를 **둘 다** 제외한다. **목록의 where절과 count절이 분리된 곳에서는 양쪽 모두에 걸어야 한다** — 한쪽만 고치면 `totalElements`와 실제 목록 길이가 어긋나 프론트 무한스크롤이 빈 페이지로 깨진다.

`visibleToViewer(Long)`는 리뷰 **상세**의 뷰어 기준 노출 조건으로, 사장님만보기 리뷰는 작성자 본인에게만 보인다. 비로그인(`viewerMemberId == null`)이거나 타인이면 사장님만보기 리뷰가 조회되지 않아 호출부가 `REVIEW_NOT_FOUND`(404)를 낸다. **403을 쓰지 않는 이유는 403이 "그 리뷰가 존재한다"는 사실을 노출하기 때문이다.**

조건이 OR이라 varargs `.where(a, b)`(AND)로는 표현할 수 없어 `BooleanExpression` 헬퍼로 만든다(`BooleanBuilder`는 프로젝트 금지 규약).

#### count 쿼리는 목록 쿼리의 조인을 재현한다

→ `countBestReviews()` · `countLatestReviews(Predicate)`

목록 쿼리와 **동일한 `innerJoin`(shop·station·member)을 재현해야 총 건수가 일치한다** — inner join은 짝이 없는 리뷰를 제외하므로 리뷰 테이블만 세면 값이 달라진다. 반면 프로필 이미지 `leftJoin`과 좋아요·댓글 수 스칼라 서브쿼리는 행 수를 바꾸지 않아 재현하지 않는다.

베스트 리뷰 쪽도 같은 기준이다 — 대표 이미지 조인은 "정렬값이 가장 작은 1장", 주문상품 조인은 `(orderId, productId)` 복합 동등으로 각각 리뷰당 최대 1행으로 좁혀지므로 행이 늘지 않아 count에서 생략하고, shop·station `innerJoin`만 그대로 재현한다.

**정렬(특히 추천순의 `groupBy`)은 총 건수와 무관하므로 count 쿼리에는 적용하지 않는다.** 추천순은 `groupBy(reviewJpaEntity.id, ...)`로 리뷰당 1행이 되므로 여기서 세는 리뷰 건수와 결과가 같다(기존 `fetch().size()`와 등가).

#### 정렬 정책

→ `applySort(JPAQuery, ReviewSortType)`

가게별·상품별 목록이 공유한다. 추천순은 좋아요 수 집계가 필요해 **별칭 조인 + `groupBy`가 따라붙고, 동수일 때는 최신순으로 갈린다.** 정렬 후보는 도메인 enum(`ReviewSortType`)이 소유하며 승격은 소비 모듈 Service가 한다.

#### N+1 회피 — 식별자를 모아 한 번에 조회한다

→ `findReviewedProductIds` · `findImageUrlsByReviewIds` · `findFirstImageUrlsByReviewIds`

주문 상세의 주문상품마다 `existsByOrderIdAndProductIdAndMemberId`를 호출하면 상품 수만큼 쿼리가 나가므로(N+1), 상품 식별자를 모아 `IN` 한 번으로 조회하고 소비 모듈이 메모리에서 판정하도록 한다. **입력이 비어 있으면 조회하지 않는다**(`findTagNamesByIds`·`findVisibleReplies`도 같다 — 빈 목록을 그대로 돌려준다).

이미지 조회도 같은 형태다. 저장 경로는 `FileUrlResolver`로 표시용 URL까지 변환한 뒤 돌려주며, 대표 이미지는 "정렬값이 가장 작은 1장"으로 좁힌다.

#### 투영 후 재조립 — `Projections.constructor`의 제약

→ `withResolvedImageUrl(SearchReviewItemResult)`

`Projections.constructor`는 생성자 직접 투영이라 **변환을 투영식에 넣을 수 없어** fetch 직후 호출해 저장 경로를 표시용 URL로 바꿔 재조립한다.

#### 댓글·답글 조회

→ `findComments(ReviewId)` · `findVisibleReplies(List<ReviewCommentId>)`

댓글 목록은 **숨김을 포함**해 최신순으로 돌려준다 — 기존 web 동작을 보존하기 위함이며, **답글만 숨김을 제외한다.** 관리 화면용 `ReviewManagementQueryDao#findCommentsIncludingHidden`과 달리 작성자 프로필 이미지 경로까지 함께 투영한다(web 응답이 프로필 이미지 URL을 포함하기 때문).

답글 대상 회원(replyTo)은 없을 수 있어 **leftJoin**으로 붙인다.

#### 크로스 도메인 `@Convert` VO 컬럼 우회

→ `shopStationId()` · `memberProfileImageFileId()`

`SHOP.station_id`(shop 도메인)·`MEMBER.profile_image_file_id`(member 도메인)는 `@Convert` VO 컬럼이라 QueryDSL이 VO path를 생성한다. raw `Long`으로 비교하기 위해 별도 path를 만들어 우회한다.

---

### `ReviewStatisticsQueryDao` — 집계 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ReviewStatisticsQueryDao.java`

#### `ReviewStatisticsQueryDao` 클래스 역할

리뷰 집계·통계 전용 read 어댑터(CQRS query 측). 가게/상품/회원 단위의 리뷰 수·평균 평점·평점 분포·월별 추이를 JPA 엔티티에서 직접 투영하며, 도메인 모델을 거치지 않으므로 write 포트(`ReviewRepository`)와 역할이 겹치지 않는다.

**도메인당 DAO 1개가 원칙이나 review는 대형 도메인이라 용도별로 분리했다.** 목록·상세 조회는 `ReviewQueryDao`, 관리(admin) 화면 전용 조회는 `ReviewManagementQueryDao`가 담당하고, 여기에는 집계·통계만 둔다.

소비자: web-api `ReviewQueryService`(가게 리뷰 통계 조합, 회원 리뷰 수)·`ProductQueryService`(상품 상세의 매장 리뷰 통계), ceo-api `ShopReviewQueryService`(점주 통계 대시보드 — 기간 오버로드 사용).

#### 기간 오버로드는 기존 오버로드의 한계 때문에 신설됐다

→ `getAverageTotalRating(Long, LocalDateTime, LocalDateTime)` · `getMonthlyReviewCounts(Long, LocalDateTime, LocalDateTime)` · `getMonthlyAverageRatings(...)`

- **총점 평균**이 기존 DAO에 없었던 이유는 가게 평점을 `Shop.rating` 비정규화 컬럼에서 읽었기 때문이다. 그 컬럼은 **전체 기간 누적값이라 "최근 6개월" 평균을 답할 수 없어** 여기서 직접 집계한다. 리뷰가 0건이면 `null`을 돌려준다(**0.0이 아니다** — "평점 0점"과 구분해야 한다).
- **월별 카운트**의 기존 오버로드 `getMonthlyReviewCounts(shopId, year)`는 키가 **월(1~12)**이라 연도를 걸쳐 있는 구간(최근 6개월)에서 작년 1월과 올해 1월이 **같은 키로 뭉개진다.** 그래서 기간 오버로드는 키를 `yyyy-MM`으로 바꾼다.
- **월별 평균 평점**은 신설이다. 기존에는 월별 *카운트*만 있었다.

#### 기간 구간은 반열림 `[from, to)`이다

→ `countBetween(Long, LocalDateTime, LocalDateTime)` · `countSince(Long, LocalDateTime)`

상한 없는 `countSince`와 나누는 이유는, **통계 응답의 13개 필드가 모두 같은 행 집합을 설명해야 하기 때문이다.** 월별 그래프는 이미 `[from, to)`로 집계하므로, 헤더 카운트가 상한 없이 집계되면 미래 시각 행(시계 오차·백필·관리자 보정)이 헤더에만 포함돼 **"그래프 합 ≠ 총 건수"** 가 된다. `countSince`는 180일 노출 게이트·최근 30일 카운트용이다.

#### 항목별 평균은 한 쿼리로 모은다

→ `getCategoryAverages(Long, LocalDateTime, LocalDateTime)`

항목별로 메서드를 6개 두면 통계 조회가 **쿼리 6번**이 된다. 같은 `WHERE`·같은 기간이라 한 쿼리에서 맛·양·가격·분위기·친절·위생을 함께 집계하는 것이 자연스럽다.

#### `yyyy-MM` 그룹 키와 인덱스

→ `yearMonthKey()`

`DATE_FORMAT`을 쓰면 인덱스를 타지 못하지만, **기간 조건(`created_at` 범위)이 이미 `idx_review_shop_id_created_at`로 대상을 좁힌 뒤 그룹핑에만 쓰이므로 문제되지 않는다.**

#### 정규화는 DAO가 아니라 소비 Service가 한다

→ `getRatingCounts(Long, LocalDateTime, LocalDateTime)`

평점 키 1~5를 항상 채우는 정규화는 소비 Service의 몫이다 — **DAO는 실제로 조회된 것만 담는다.**

---

### `ShopReviewManagementQueryDao` — 점주 리뷰 관리 쿼리 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/review/query/ShopReviewManagementQueryDao.java`

#### 클래스 역할 — `ReviewQueryDao`와 분리한 이유

점주 리뷰 관리(ceo) 전용 read 어댑터(CQRS query 측). 기존 `ReviewQueryDao`(web 소비)와 조회 용도가 다르다. **가장 큰 차이는 `hidden` 필터를 끄지 않는다는 점이다** — 점주는 차단 탭에서 숨겨진 리뷰를 봐야 하므로 web 목록(`hidden = false` 고정)과 같은 쿼리를 쓸 수 없다.

동적 조건은 `BooleanExpression` 헬퍼 + varargs `.where(...)`로 조립한다(`BooleanBuilder` 금지 — 프로젝트 공통 규약).

#### 다건 컬렉션은 본 쿼리에 join하지 않는다

→ `findShopReviews(...)` · `withCollections(...)` · `findImageUrlsByReviewIds` · `findProductNames`

리뷰 사진·주문 메뉴명은 리뷰당 다건이라 **join하면 행이 불어나 페이징 카운트가 어긋난다.** 페이지 하나를 먼저 뽑고 그 ID 집합으로 별도 조회한 뒤 위더로 채운다(**N+1이 아니라 페이지당 고정 2회**).

반대로 **사장님 답변은 `UNIQUE(review_id)`라 left join이 행을 늘리지 않으므로 본 쿼리에서 함께 투영한다**(미답변 탭 판정도 이 join의 `null` 여부로 한다).

이미지 URL은 `FileUrlResolver`로 표시용 URL까지 완성해 돌려준다 — **Result에 `~FileId`를 담지 않는다.**

주문 메뉴명은 `REVIEW`가 `order_id`와 `product_id`를 함께 갖지만 **주문 전체의 메뉴**를 보여준다 — 리뷰 대상 상품 하나만 보여주면 "무엇을 먹고 쓴 리뷰인지"를 알 수 없다. `order_id`가 `NULL`인 미인증 리뷰는 결과에 들어오지 않아 빈 목록이 된다.

#### 서브쿼리 별칭을 분리한다

→ `sortReviewLike` · `subReviewImage` · `subBlindRequest`

- `sortReviewLike` — 정렬용 좋아요 별칭. 존재 판정 서브쿼리와 **같은 테이블**이라 별칭을 분리한다.
- `subReviewImage` — 사진 존재 판정 서브쿼리 별칭. 목록 join(있으면 행이 불어남) 대신 **EXISTS로만 쓴다.**
- `subBlindRequest` — 최근 게시중단 요청 판정 서브쿼리 별칭.

#### 최신 게시중단 요청 1건은 상관 서브쿼리 2단으로 특정한다

→ `latestBlindRequestStatus()`

`id = (select max(id) ...)` 형태로 최신 1건을 특정한다 — **목록에 `REVIEW_BLIND_REQUEST`를 직접 join하면 요청을 여러 번 낸 리뷰에서 행이 불어난다.** 없으면 `null`이다.

게시중단 요청 이력(`findBlindRequestHistory`)은 최신순이며, **취소·반려된 과거 요청도 남긴다**(재요청 판단 근거).

#### 정렬 — `ONLY_FULL_GROUP_BY`

→ `applySort(JPAQuery, ReviewSortType)`

기존 `ReviewQueryDao#applySort`와 같은 정책이다(추천순은 좋아요 desc, 동수는 최신순). 추천순은 좋아요 수 집계가 필요해 `group by`가 붙는데, **투영에 든 모든 비집계 컬럼을 함께 묶어야 한다** — MySQL의 `ONLY_FULL_GROUP_BY`에서 하나라도 빠지면 쿼리가 거부된다.

#### 날짜 필터는 반열림 구간이다 — 함수를 컬럼에 씌우지 않는다

→ `createdAtLt(LocalDate)`

날짜 필터는 **반열림 구간** `[startDate 00:00, endDate+1일 00:00)`으로 만든다 — `DATE(created_at)`처럼 컬럼에 함수를 씌우면 `idx_review_shop_id_created_at`를 타지 못한다.

종료일 필터는 **다음날 00:00 미만**으로 만든다 — `loe(endDate.atStartOfDay())`로 쓰면 **종료일 당일에 작성된 리뷰가 통째로 빠진다.**

#### 사진 유무 필터는 join이 아니라 EXISTS다

→ `hasImageEq(Boolean)`

join이 아니라 `EXISTS`로 판정해 **행이 불어나지 않게 한다.** `false`는 "필터 무시"가 아니라 **사진 없는 리뷰만**이다(`NOT EXISTS`) — 미지정(`null`)이 전체를 뜻하므로 `false`에 같은 의미를 주면 값 하나가 낭비된다. web 목록의 `hasImage`와 같은 해석이다.

---

### `OrderQueryDao` — 쿼리 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/order/query/OrderQueryDao.java`

#### `OrderQueryDao` 클래스 역할

주문 read 어댑터(CQRS query 측). 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영하며 도메인 모델을 거치지 않으므로 write 포트(`OrderRepository`/`OrderProductRepository`/`OrderProductOptionRepository`)와 역할이 겹치지 않는다. 소비 모듈(web/admin-api)의 `OrderQueryService`가 이 DAO를 주입해 쓴다.

**소비자별 메서드 분리(공통 지침 패턴 3)**: 회원 화면용 `findOrders(MemberId, PageQuery)`, 관리자 화면용 `findOrders(OrderSearchCondition, PageQuery)` — **이름은 admin 마커 없이 순수 동작명을 쓰고 시그니처(회원 스코프 `MemberId` 유무)로 구별한다.** 상세 조회는 두 화면이 같은 필드 셋을 쓰므로 `findOrderDetail(OrderId)` 하나를 공유한다.

#### 이미지 — 같은 테이블 두 번 조인, 그리고 주문 시점 스냅샷

→ `ORDER_PRODUCT_IMAGE_FILE` · `withResolvedShopThumbnailImageUrl(OrderListItemResult)`

가게 대표 이미지(주문 목록)와 주문 상품 이미지 모두 `UPLOADED_FILE`을 join해 얻은 저장 경로를 `FileUrlResolver`로 표시용 URL까지 변환해 Result에 담는다. 두 이미지가 같은 테이블을 각각 join하므로 `ORDER_PRODUCT_IMAGE_FILE` 별칭을 따로 둔다 — **기본 별칭 하나로는 두 join이 충돌한다**(`EventQueryDao#findEventDetailById` 선례와 동일).

주문 상품은 **주문 시점의 `UPLOADED_FILE.id`를 스냅샷해 두므로**(=`ORDER_PRODUCT.image_file_id`), 이후 상품 대표 이미지가 교체돼도 과거 주문은 주문 당시 이미지를 그대로 보여준다.

URL 변환은 `Projections.constructor`가 생성자 직접 투영이라 투영식에 넣을 수 없어 **fetch 직후 재조립한다.**

#### 목록 쿼리

→ `findOrders(MemberId, PageQuery)` · `findOrders(OrderSearchCondition, PageQuery)`

- 회원 목록은 상품 라인을 join해 첫 상품명과 상품 종류 수를 집계하므로 **주문 단위로 `groupBy` 한다.**
- 관리 목록의 **결제 상태 조건은 where가 아니라 join 조건에 실어**, 결제가 없는 주문도 결제 상태 필터가 없을 때는 목록에 남도록 한다(기존 동작 보존).

#### 상세 조회는 회원 스코프를 검증하지 않는다

→ `findOrderDetail(OrderId)`

헤더에 가게명·가게 전화번호를 join하고, 상품 라인(각 라인의 선택 옵션 포함)과 결제 요약을 별도 조회해 덧붙인다. web-api(내 주문 상세)·admin-api(주문 관리 상세)가 공유한다.

**회원 스코프 검증은 하지 않는다** — 소유권 검증은 write 경로의 도메인 모델(`Order#validateOwnership`)이 담당하고, web-api `OrderQueryService`가 이 결과의 `memberId`를 요청 회원과 대조한다.

상품 라인(`findOrderProducts`)은 각 라인의 선택 옵션을 **한 번의 조회로 모아 라인별로 배분한다**(N+1 회피).

#### 결제 요약은 fail-loud다

→ `findPayment(OrderId)`

`PAYMENT.order_id`는 unique 제약이 있어 주문당 결제는 최대 1건이다. **이 불변식이 깨지면(동일 주문에 결제 행 2건) 임의의 한 건을 조용히 고르는 대신 `fetchOne`으로 즉시 실패시킨다** — 기존 `PaymentRepository#findByOrderId`와 동일한 fail-loud 시맨틱을 유지한다. 결제가 아직 없으면 `null`이다.

#### `Amount` VO 언랩이 읽기 계약을 경계 타입으로 유지한다

→ `withUnwrappedAmount(PaymentProjection)`

`PAYMENT.amount`가 `@Convert` 매핑이라 QueryDSL이 `SimplePath<Amount>`를 생성하므로 **투영은 VO로 받을 수밖에 없고**, `Projections.constructor`는 생성자 직접 투영이라 변환을 투영식에 넣을 수 없다. 그래서 fetch 직후에 푼다(`withResolvedShopThumbnailImageUrl`과 같은 형태).

**이 언랩이 읽기 계약을 경계 타입으로 유지해, api 모듈이 `Amount.value()`를 호출하지 않게 한다**(챕터 07 — `apiModuleShouldBeDomainModelFree`의 유일한 비-enum 위반이었다).

#### 접근 판정용 조회 2종

→ `findOrderProductOwnership(Long)` · `findOrderMemberId(Long)`

`findOrderProductOwnership`은 주문 상품 한 건의 소유·상품 식별 정보로 리뷰 작성 화면의 접근 판정에 쓴다. 주문 상품에서 주문으로 조인해 주문자 회원 ID까지 한 번에 가져오며, **애그리거트 둘(`OrderProduct` → `Order`)을 차례로 로드하던 기존 형태를 한 번의 투영으로 대체한다.**

`findOrderMemberId`는 주문의 주문자 회원 ID만 돌려준다. 상태를 바꾸지 않고 식별자만 대조하므로 표현 목적 조회다 — **주문 상태를 변경하는 경로의 소유권 검증은 그대로 write 포트가 담당한다.**

<!-- 챕터 05 — `*/query/*` 기타 DAO 39개. 분류 B(아키텍처 서술) 이관분. -->
<!-- 목적지: backend/infrastructure/persistence/AGENTS.md 의 "코드 주석에서 이관된 설계 근거" > "개별 DAO — 그 DAO에만 있는 판단" 아래에 이어 붙인다. -->

#### `AdminDongQueryDao` — 3단 lazy 트리와 SQL 조립 이름

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/region/query/AdminDongQueryDao.java`

- `AdminDongRepository`는 존재검증·주소 매칭용 write 포트라 배달가능지역·지역별 배달팁 설정 화면이 쓰는 **표현 목적 검색**을 담지 않는다. 그 조회를 이 DAO가 담당한다.
- 시도 → 시군구 → 동을 **3단 lazy 조회로 나눈다.** 전국 행정동이 3,600건을 넘어 전 계층을 한 번에 내리면 응답이 비대해지고 대부분이 화면에 쓰이지 않는다. **식별자(`adminDongId`·`code`)는 동 레벨에서만 채워진다** — 상위 두 레벨은 그룹핑 이름일 뿐 마스터 테이블에 자기 행이 없다.
- 목록 정렬은 주소 인덱스 `idx_admin_dong_name` 순서에 맞춰 **시/도 → 시군구 → 동** 순이다. 인덱스 순서와 어긋나게 바꾸지 않는다.
- 키워드 검색은 세 컬럼을 각각 비교하지 않고 **조립된 전체 이름 하나**를 부분 일치시킨다 — `"강남구 역삼"`처럼 시군구와 동을 이어 입력해도 걸리게 하기 위함이다.
- `regionName()`은 표시용 전체 이름(`"서울특별시 강남구 역삼1동"`)을 **SQL에서 조립**한다. 세 컬럼이 전부 NOT NULL이라 구분자 분기가 없어 단순 `concat` 체인으로 충분하며, 프론트가 세 조각을 받아 문자열을 조립하지 않게 한다. `ShopDeliveryAreaQueryDao`·`MemberDeliveryAddressQueryDao`가 같은 규칙을 쓴다.
- **경계를 보유하지 않은 동도 함께 내려보낸다.** 경계가 없다고 목록에서 빼면 화면이 "이 지역에 동이 없다"로 오해하게 되는데, 실제로는 좌표만 있고 경계 시드가 아직 안 들어온 정상 상태다.
- 후보 조회(`findCandidatesWithinBoundingBox`)는 표시용 이름까지 조립해 내려보내므로 조회 측이 이름을 얻으려고 다시 조회하지 않는다.

#### `ProductQueryDao`에서 갈라 나온 3개 DAO — 분리 사유

**대상**: `.../product/query/StorePriceVerificationQueryDao.java` · `.../product/query/ProductFeedbackQueryDao.java` · `.../product/query/ProductShopLinkQueryDao.java`

**`ProductQueryDao`에 메서드를 더하지 않고 DAO를 새로 둔다.** 그 클래스는 이미 2000줄이 넘고 메뉴·옵션·카테고리·승인요청 3종을 한 클래스가 떠맡고 있다. 아래 셋은 각각 **자체 테이블과 자체 조인 그래프**를 갖는 독립 조회 대상이라 별 파일로 두면 그 그래프가 한눈에 보인다. `ShopDeliveryAreaQueryDao`·`ShopDeliveryAreaAdjustmentQueryDao`가 shop 쪽에서 같은 이유로 분리돼 있다.

| DAO | 자체 조회 형태 |
|---|---|
| `StorePriceVerificationQueryDao` | `SHOP_STORE_PRICE_VERIFICATION` · `..._ITEM` 2테이블 |
| `ProductFeedbackQueryDao` | 메뉴 × 유형 `group by` 집계 |
| `ProductShopLinkQueryDao` | "점주 소유 가게 × 이 메뉴의 연결 여부" 조인 |

#### `StorePriceVerificationQueryDao` — 카디널리티가 항목 조회를 갈랐다

**대상**: `.../product/query/StorePriceVerificationQueryDao.java`

- 쓰기 포트 `StorePriceVerificationRepository`는 **불변식 검증용 조회만** 갖고, 표현 목적 투영(가게명 조인·항목 수 집계·파일 URL 완성)은 전부 이 DAO가 담당한다.
- **항목을 목록 조회에 합치지 않고 별 쿼리로 둔다.** 요청 1건에 메뉴가 N건 달리므로 목록에 조인하면 행이 부풀어 페이징이 깨진다 — 목록은 `itemCount` 집계만 담고 항목 자체는 상세에서 가져온다.
- `itemCount`는 **스칼라 서브쿼리**로 센다. 항목 테이블을 조인해 `GROUP BY`로 세면 페이징 대상 행이 부풀고, **항목이 0건인 요청이 조인에서 탈락한다.**
- 가격표 이미지는 컬럼이 `NOT NULL`이라 정상 데이터라면 항상 맞지만 **`left join`으로 둔다.** inner join이면 파일 행이 유실된 요청이 **검수 목록에서 조용히 사라져** 처리 불가 상태가 된다.
- 목록·상세가 투영을 공유한다. 따로 두면 한쪽만 고쳐져 같은 요청이 화면마다 다른 필드를 갖는다.
- `status`가 `null`이면 전체 조회다(상태 미지정 = "전체").

#### `ProductFeedbackQueryDao` — 페이징 단위가 집계 줄이다

**대상**: `.../product/query/ProductFeedbackQueryDao.java`

- **페이징을 집계 단위(메뉴 × 유형)로 한다.** 그것이 화면의 한 줄이기 때문이다 — 제보 건 단위로 페이징하면 한 집계 줄이 페이지 경계에서 쪼개져 건수가 잘못 보인다. 총계도 전체 건수가 아니라 **"집계 줄 수"**를 세며, `group by` 결과의 행 수라 `count(*)`로는 얻을 수 없다.
- 메뉴 조인은 **`leftJoin`이다.** 메뉴가 소프트 삭제돼도 제보는 남으므로 inner join으로 사라지게 하지 않는다 — 삭제된 메뉴에 대한 지적도 점주가 원인을 파악할 근거가 된다.
- 기준 시각(`since`)을 **파라미터로 받아 DAO가 시계를 직접 읽지 않게 한다.** 호출부가 같은 `now`로 목록·미확인 판정을 일관되게 맞춘다.
- `MAX_CONTENTS_PER_GROUP`(한 집계 줄에 싣는 `ETC` 서술 상한)을 무제한으로 풀지 않는다. 제보가 많은 메뉴 하나가 응답을 뒤덮어 다른 메뉴의 지적이 묻힌다. **메뉴별 상한은 자바에서 적용한다** — 그룹별 LIMIT은 표준 SQL로 표현할 수 없고, 창 함수를 쓰면 이 조회만 네이티브 SQL이 되어 컴파일 검증에서 벗어난다.
- 서술(`contents`)은 **`ETC` 유형에만 존재한다** — 다른 유형은 유형 자체가 내용이라 실을 것이 없다.
- `ETC` 서술은 집계 줄마다 따로 조회하지 않고 대상 메뉴를 모아 **한 번에 읽어 자바에서 나눈다**(N+1 회피).

#### `ProductShopLinkQueryDao` — 소유 가게 전체를 담아야 토글이 성립한다

**대상**: `.../product/query/ProductShopLinkQueryDao.java`

- **연결된 가게만 내려보내면 화면이 새 가게를 켤 수 없다.** 토글 목록의 원천이므로 소유 가게를 모두 담고 `linked`로 상태만 구분한다.
- 연결 링크와 그 링크가 가리키는 메뉴그룹을 **`left join`으로 붙인다** — 연결되지 않은 가게는 링크가 없고, 연결됐더라도 메뉴그룹이 비어 있을 수 있어 어느 쪽도 행을 떨어뜨려서는 안 된다.
- `findOwnedShopIds`는 연결 변경이 **본인 소유 가게에만** 허용되는지 판정하는 근거다. 도메인 서비스는 `ceoId`를 알지 못하므로(소유권은 ceo-api의 인가 관심사다) 호출부가 이 집합을 구해 넘긴다. **가게마다 `ShopOwnershipValidator`를 반복 호출하지 않는다** — 연결 목록이 여러 건이라 그만큼 가게 조회가 늘기 때문에 한 번에 읽어 집합으로 대조한다.

#### `ShopChoiceQueryDao` — shop의 세 번째 용도별 DAO, 가게에 종속되지 않는 조회

**대상**: `.../shop/query/ShopChoiceQueryDao.java`

- `ShopQueryDao`(가게별 설정·관리)·`ShopSearchQueryDao`(목록·검색)와 함께 shop 도메인의 **세 번째 용도별 DAO**다 — 가게에 종속되지 않는 **독립 조회**(에디터 추천 목록, 전역 태그·역 목록)를 담당한다.
- 에디터 추천 목록에서 **폐업·노출정지 가게의 추천은 제외한다.**
- 대표 상품 그룹핑 키는 `PRODUCT.shop_id`가 아니라 **`PRODUCT_SHOP_LINK`의 `shop_id`**다. 메뉴-가게 N:M 도입으로 "이 가게 메뉴판에 무엇이 걸려 있는가"의 진실원이 링크 테이블로 옮겨갔으므로, 원본 컬럼으로 묶으면 **다른 가게에서 불러온 메뉴가 그 가게 목록에 나타나지 않는다.**
- 상품 대표 이미지는 **노출 중 최소 `sort`** 이미지를 서브쿼리 별칭 `subProductImage`로 고른다.

#### `ShopRiderGuideQueryDao` — 고객 비노출과 boolean 필터의 여집합

**대상**: `.../shop/query/ShopRiderGuideQueryDao.java`

- **고객 비노출 보장**: 이 DAO는 ceo-api·admin-api의 query 서비스만 주입해 쓰며, web-api의 가게 상세·목록 조회는 `SHOP_RIDER_GUIDE`를 조인하지 않는다. 이 관계를 깨고 web 경로에서 조인하지 않는다.
- write 포트 `ShopRiderGuideRepository`에는 **도메인 불변식 판정에 필요한 3개 메서드만** 남기고, 관리자 목록·이력 조회는 이 DAO가 소유한다.
- 단건 조회는 **라이더 안내 테이블을 `left join`한다** — 등록 이력이 없어도 가게가 존재하면 결과를 반환한다("미등록"은 오류가 아니라 정상 상태다).
- 목록 정렬은 `updatedAt` 내림차순이다. 이 저장소의 기본 정렬은 대체로 `createdAt`인데 여기만 다른 것은 **최근 변경분부터 검수하는 실제 운영 순서**를 따르기 위함이다.
- **픽업 위치 설정 여부를 SQL 술어로 select 절에 투영하지 않고 원본 컬럼을 읽어 Java에서 판정한다.** 판정 기준(도로명·위경도가 모두 채워졌는가)을 `ShopRiderGuide#hasPickupLocation`과 한 곳에서 일치시키기 위함이다.
- `visitGuidePresenceEq`의 `false`를 "미지정과 동일"로 두지 않는다. 그러면 그 값이 응답을 전혀 바꾸지 않아 파라미터가 무의미해지고, **"문구 미등록 가게만" 조회할 방법이 사라진다.** 다른 boolean 필터와 동일하게 여집합으로 판정한다.
- `HISTORY_LIMIT`(관리자 검수 화면의 이력 노출 상한)을 넘는 분은 별도 페이징 엔드포인트를 두지 않고 필요해지면 추가한다.

#### `ShopDeliveryAreaQueryDao` — 소유권을 조회 조건으로 거는 IDOR 방어

**대상**: `.../shop/query/ShopDeliveryAreaQueryDao.java`

- `findShopLocation`은 **소유권을 조회 조건(`ceo_id`)으로 함께 건다.** 조회 서비스는 write 포트를 주입할 수 없어(`queryServicesShouldNotDependOnWritePorts`) `ShopOwnershipValidator`를 쓸 수 없는데, 검증을 생략하면 **남의 가게 좌표를 읽는 IDOR**이 된다. 조건을 쿼리에 넣으면 소유하지 않은 가게는 결과가 비어 자연스럽게 차단된다. 이 조건을 빼지 않는다.
- 소유 가게가 아니거나 좌표가 없으면 **예외를 던진다** — 좌표 없이는 7km 상한의 기준점이 없어 미리보기 자체가 성립하지 않는다.
- 반면 `findPolygon`은 **미설정이 정상 상태**이므로 빈 `Optional`을 반환한다. 404 판단을 호출 측에 시키지 않는다.
- 배달가능지역 목록은 **등록 순(`id` 오름차순)**이다.
- 출처별 행정동 집합(`findAdminDongIdsBySource`)·지역별 배달팁 참조 집합(`findRegionTipAdminDongIds`)은 도형 미리보기가 각각 "닫히는 동"·"닫을 수 없는 동"을 미리 보여주는 입력이다.

#### `ShopNoticeQueryDao` — 본문·이미지 2단 조회

**대상**: `.../shop/query/ShopNoticeQueryDao.java` (중간 투영 `ShopNoticeRow` · `ShopNoticeManagementRow` · `ShopNoticeImageResult` 포함)

- 공지 본문과 첨부 이미지를 **두 쿼리로 나눠** 읽고 `shopNoticeId`로 묶는다 — 1:N 조인으로 한 번에 읽으면 공지 행이 이미지 수만큼 중복되어 **페이징 카운트가 어긋난다.** 이 2단 조립 형태가 고유하므로 `ShopQueryDao`에 섞지 않고 별도 DAO로 둔다.
- 그래서 본문만 담는 중간 투영(`ShopNoticeRow`·`ShopNoticeManagementRow`)을 먼저 투영한 뒤 이미지 URL을 붙여 최종 Result로 재조립한다.
- 점주 화면 목록은 **노출중 공지를 맨 위로, 그다음 최근 등록 순**이다. web 노출 조회는 `exposed = true AND hidden = false` 최대 1건이다.
- 첨부 이미지는 `sortOrder` 오름차순으로 묶는다.

#### `ShopOrderNoticeQueryDao` — 노출 조건 분기를 Service로 올리지 않는다

**대상**: `.../shop/query/ShopOrderNoticeQueryDao.java`

- 주문안내는 가게당 1건 단독 행이고 조인 대상이 없어 `ShopQueryDao`의 대형 조립 메서드들과 성격이 다르므로 별도 DAO로 둔다.
- **메서드가 둘로 나뉜 이유는 노출 조건이 소비자에 따라 다르기 때문이다.** 점주는 자기 문구가 내려갔다는 사실과 그 사유를 봐야 하므로 게시중단 건도 받고, 손님은 게시중단 건을 아예 받지 않는다. **이 분기를 Service의 if 문으로 옮기지 않는다** — 손님 경로에서 필터를 빠뜨리면 게시중단된 문구가 그대로 노출되는 결함이 되고, 쿼리 자체가 걸러내면 그 실수가 물리적으로 불가능해진다.
- 두 조회가 같은 컬럼 묶음을 읽으므로 투영을 한 곳에 둔다 — 복제하면 필드 추가 시 한쪽만 고쳐진다.

#### `ShopChangeHistoryQueryDao` — 보관 하한을 DAO에도 남긴다

**대상**: `.../shop/query/ShopChangeHistoryQueryDao.java`

- 날짜 필터는 **반열림 구간** `[changedDate 00:00, 다음날 00:00)`이다 — `DATE(created_at) = ?`처럼 컬럼에 함수를 씌우면 인덱스를 타지 못한다.
- **보관 하한(6개월)을 서비스가 이미 400으로 거부한 뒤에도 항상 실어 보낸다.** 조회 대상 하루가 하한보다 뒤이므로 결과는 달라지지 않지만, **정책이 DAO에도 남아 다른 호출자가 생겨도 6개월 밖 데이터가 새지 않는다.** 중복이라고 걷어내지 않는다.

#### `ShopCeoAssignmentHistoryQueryDao` · `CeoLoginHistoryQueryDao` — 이력 DAO 공통

**대상**: `.../shop/query/ShopCeoAssignmentHistoryQueryDao.java` · `.../ceo/query/CeoLoginHistoryQueryDao.java`

- 날짜 필터는 **반열림 구간** `[startDate 00:00, endDate+1d 00:00)`이다 — `DATE(created_at) BETWEEN ...`처럼 컬럼에 함수를 씌우면 인덱스를 타지 못한다. **종료일은 그날 하루를 포함해야 하므로 다음날 00:00 미만**으로 건다.
- 정렬은 `created_at DESC, id DESC`다.
- `ShopCeoAssignmentHistoryQueryDao`는 `SHOP`을 **`leftJoin`으로** 이어 `shopName`까지 투영한다 — 이력 행은 append-only라 **가게가 나중에 폐업해도 남으므로** inner join이면 행이 사라진다.

#### `PaymentQueryDao` — 호출부 없는 조회를 미리 만들지 않는다

**대상**: `.../payment/query/PaymentQueryDao.java`

- 소비 모듈이 실제로 쓰는 조회 둘만 갖는다 — 주문별 결제 조회(회원의 결제 확인 화면)와 PK 조회(command 커밋 후 응답 조립용 재조회). **관리자 결제·환불 내역 조회는 admin-api에 결제 소비자가 생길 때 추가한다**(호출부 없는 조회를 미리 만들지 않는다).
- 주문별 조회는 회원 스코프 검증에 쓸 주문의 `memberId`를 함께 투영한다. **주문이 없으면 결제도 조회되지 않으므로(inner join)** 소비 모듈은 "주문 없음"과 "결제 없음"을 결과 부재로 함께 처리한다.
- `PAYMENT.order_id`에 unique 제약이 있어 주문당 결제는 최대 1건이다. **이 불변식이 깨지면 임의의 한 건을 조용히 고르는 대신 `fetchOne`으로 즉시 실패시킨다** — `PaymentRepository#findByOrderId`와 동일한 fail-loud 시맨틱이다. `fetchFirst`로 바꾸지 않는다.
- 환불 요청 단건 조회는 **소유권을 다시 대조하지 않는다.** 요청 시점에 이미 검증됐고 이 조회는 그 직후 재조회이기 때문이다.
- 두 조회 경로가 같은 필드 셋을 쓰므로 투영을 공유한다.

#### `MemberFollowQueryDao` — 뷰어 팔로우 여부의 비로그인 처리

**대상**: `.../member/follow/query/MemberFollowQueryDao.java`

`viewerMemberId`가 주어지면 각 항목에 뷰어의 팔로우 여부를 함께 투영하고, **비로그인(`null`)이면 모두 `false`로 둔다.** 팔로잉·팔로워 목록이 같은 규칙을 쓴다. `existsFollow` 같은 표현용 단건 판정은 write 포트가 아니라 이 어댑터가 답한다.

#### `MemberDeliveryAddressQueryDao` — 행정동 미매칭 주소를 떨어뜨리지 않는다

**대상**: `.../member/query/MemberDeliveryAddressQueryDao.java`

- 행정동은 **`left join`이다** — 주소 문자열 매칭에 실패해 `admin_dong_id`가 null인 주소도 목록에서 빠지면 안 된다. 그 경우 `regionName`은 null로 내려간다(concat 결과 전체가 null이 된다).
- 목록 정렬은 **기본 배송지 먼저, 그다음 등록 순**이다.
- `findDefaultAdminDongId`는 가게 목록·검색이 "이 회원에게 배달되는 가게만" 남기려고 쓰는 값이라 주소 전체가 필요 없다. **목록 조회 경로마다 도는 질의이므로 컬럼 하나만 투영한다.** 기본 배송지가 없거나 매칭 실패로 null이면 비어 있고, **호출부는 그 경우 필터를 걸지 않는다.**
- `regionNameExpression()`은 도메인 모델 `AdminDong#fullName()`과 **같은 규칙(공백 하나 join)**이다. 두 곳이 어긋나면 화면 표기가 갈린다.

#### `CouponQueryDao` — 원장 삭제와 보유 쿠폰의 필터가 다르다

**대상**: `.../coupon/query/CouponQueryDao.java`

**삭제된 쿠폰(soft delete)은 admin 목록·상세에서 제외한다. 반면 내 쿠폰 조회는 이관 이전 동작을 그대로 보존해 원본 쿠폰의 삭제 여부를 필터링하지 않는다**(이미 발급된 보유분은 계속 보인다). 일관성을 이유로 내 쿠폰 쪽에 없던 필터를 넣지 않는다 — 넣으면 회원 쿠폰함에서 쿠폰이 사라진다.

admin 목록(`findAllCoupons`)과 web 내 쿠폰 목록(`findMemberCoupons`/`findAvailableMemberCoupons`)은 **메서드명이 아니라 시그니처로 구분한다.** 내 쿠폰 목록 두 메서드는 투영·조인을 공유하고 where 절만 각자 덧붙인다.

#### `MenuReviewStatisticsQueryDao` — 필터 축이 매장 리뷰와 다르다

**대상**: `.../menureview/query/MenuReviewStatisticsQueryDao.java`

- 소비자가 둘이다 — **상품 평점 재집계**(`ProductReviewStatisticsAdapter`가 도메인 포트 `ProductReviewStatisticsPort`를 구현하며 위임. `PRODUCT.rating`의 **유일한 근거**가 이 집계다)와 **랭킹·회원등급 기간 집계**(`MemberReviewCountQueryDao`가 REVIEW 집계와 합산).
- **고객 노출 조건은 `hidden = false` 하나뿐이다.** MENU_REVIEW에는 사장님만보기(`ownerOnly`) 개념이 없다. 매장 리뷰 집계(`ReviewStatisticsQueryDao`)가 두 축을 함께 거는 것과 다르므로, **두 DAO를 비교하며 "필터가 누락됐다"고 오해해 `ownerOnly`를 추가하지 않는다.**
- 평균 평점은 대상이 없으면 `null`이다 — **"평점 0점"과 구분해야 하므로 0.0이 아니다.**
- 기간 집계는 반열림 구간 `[startDate, endDate)`이며 **정렬하지 않는다.** 소비 측이 REVIEW 집계와 병합한 **뒤에** 정렬해야 하므로 여기서 정렬해도 유지되지 않는다.

#### `MenuReviewQueryDao` — 삭제된 메뉴도 평가할 수 있어야 한다

**대상**: `.../menureview/query/MenuReviewQueryDao.java`

- 상품 조인은 **반드시 `leftJoin`이다.** inner join이면 상품 행이 사라지는 순간(소프트 삭제 후 필터, 혹은 향후 하드 삭제) 그 메뉴를 주문했던 회원의 **리뷰 작성 항목이 통째로 소멸한다.** 여기에는 `deleted` 필터를 걸지 않는다 — 삭제된 메뉴라도 이미 주문한 회원은 평가할 수 있어야 한다.
- 같은 이유로 평가 제외 판정에서 **`ratingExcluded`가 null인 경우를 명시적으로 통과시킨다.** 상품 행이 없으면(leftJoin 미스) null이라 `isFalse()`만으로는 걸러지며, 주문 스냅샷만으로 평가할 수 있어야 한다.
- 상품 이미지는 **주문 시점 스냅샷(`ORDER_PRODUCT.image_file_id`)**을 쓴다 — 이후 상품 이미지가 바뀌어도 주문 당시 본 메뉴를 그대로 보여주기 위함이다.
- 집계(상품 평점 재집계·기간 집계)는 용도가 달라 `MenuReviewStatisticsQueryDao`가 담당한다.

#### `FileUrlResolver` — 왜 `FileQueryDao`가 아닌가, 왜 캐싱하지 않는가

**대상**: `.../file/query/FileUrlResolver.java`

- 저장 경로 → 표시용 URL 변환의 **단일 실행 지점(read 측 어셈블러)**이다. 과거에는 각 api 모듈 `FileService`의 `getUrlByPath`를 조회 Service마다 호출해 응답을 조립했고, 그 결과 같은 변환이 **60여 곳에 흩어져 모듈별 `FileService`가 서로 다르게 드리프트**했다. 변환을 read 어댑터 안으로 들여오면 Result가 이미 URL을 담은 채 나오므로 api 모듈에서 변환 호출 자체가 사라진다.
- **`FileQueryDao`가 아니라 resolver인 이유**: 파일은 자체 조회 화면이 없어 `file` 도메인의 read model이 아니다. 각 도메인 query DAO가 `uploaded_file`을 join해 흡수하며, 여기 있는 것은 그 DAO들이 공유하는 변환기 하나뿐이다.
- 변환 규칙 자체는 도메인 출력 포트 `FileStoragePort`가 소유한다(S3는 baseUrl 연결, Firebase는 경로 인코딩 + `?alt=media`). **스토리지 구현을 infra가 알지 않도록 포트만 주입받으며**, 이는 헥사고날에서 driven 어댑터가 도메인 포트를 사용하는 정상 형태다.
- **캐싱하지 않는다.** `FileStoragePort#getFileUrl`은 네트워크·SDK·DB 접근이 없는 순수 문자열 변환이라 행 단위로 반복 호출해도 비용이 사실상 없다. 캐싱은 값비싼 연산에 쓰는 수단이며, 여기 도입하면 **baseUrl 설정 변경 시 무효화 책임만 새로 생긴다.**
- 경로가 없으면(파일 미첨부, left join 미스) `null`을 돌려준다. 컬렉션 변환은 **변환할 수 없는 항목을 제외하므로 결과 크기가 입력보다 작을 수 있고**, 맵 변환은 입력 순서를 보존한다.

#### `BannerQueryDao` — 이미지 필수 여부가 조인 종류를 정한다

**대상**: `.../banner/query/BannerQueryDao.java`

회원 노출 목록은 **이미지가 필수라 파일을 `inner join`**하고, 관리 목록·상세는 **이미지가 없을 수 있어 `left join`**한다. 이 비대칭을 한쪽으로 통일하지 않는다 — inner로 통일하면 이미지 없는 배너가 관리 화면에서 사라지고, left로 통일하면 이미지 없는 배너가 회원에게 노출된다.

노출 조회는 유형 일치 + `visible=true` + **현재 시각이 노출 기간 안**의 세 조건을 함께 건다. 관리 조회는 비노출·노출기간 만료 배너를 포함한다.

#### `BugReportQueryDao` · `BugReportDetailProjection` — 2단 조립과 `@QueryProjection` 비채택

**대상**: `.../bug/query/BugReportQueryDao.java` · `.../bug/query/BugReportDetailProjection.java`

- `BugReportDetailResult`는 별도 테이블에서 모으는 `imageFileIds` 목록을 포함하므로 **한 번의 투영으로 만들 수 없다.** `BugReportDetailProjection`이 `BUG_REPORT` 한 행의 스칼라 필드만 받고, DAO가 이미지 ID를 별도 조회로 합쳐 최종 결과를 조립한다.
- **어댑터 내부 타입이지만 `@QueryProjection`을 쓰지 않는다.** 그 어노테이션이 리포에 하나라도 남으면 "읽기 투영은 `Projections.constructor`로 한다"는 규칙에 예외가 생기고 Q타입 생성물이 다시 늘어난다.
- 조립 팩토리를 포트 DTO 쪽에 둘 수 없다 — 두면 **읽기 계약이 infra를 참조하게 된다.**
- 목록은 첨부 이미지 개수를 **서브쿼리 count**로 함께 투영한다.

#### `SearchQueryDao` — write 포트가 없는 읽기 전용 애그리거트

**대상**: `.../search/query/SearchQueryDao.java`

추천 검색어는 **이 DAO의 조회가 유일한 접근 경로라(읽기 전용 애그리거트) write 포트 자체가 없다.** 인기/추천 두 애그리거트의 조회를 한 클래스에 두며, 검색 키워드 조회는 web 노출 전용이라 admin 소비자가 없어 메서드가 각각 하나씩만 있다.

키워드 집계(`findTopKeywordsSince`)는 **소비자가 도메인**(인기 검색어 갱신 서비스)이라 결과를 `SearchKeywordCountAdapter`가 도메인 값 타입으로 옮겨 담아 전달한다. `TOP_KEYWORD_LIMIT`이 인기 검색어로 노출하는 상위 키워드 수다.

#### `GeoRingsResolver` — 인코딩 형식을 api가 알지 않게 하는 read 측 변환기

**대상**: `.../shared/query/GeoRingsResolver.java`

- **왜 별도 빈인가**: 좌표 인코딩 형식은 영속 계층의 지식이라 `GeoPolygonTextCodec`이 `..persistence..`에 있는데, api 모듈은 그 패키지에 의존할 수 없다(ArchUnit `shouldNotDependOnInfrastructurePersistence`). 그렇다고 api가 인코딩 형식을 알게 하면 **저장 형식이 바뀔 때 api까지 함께 고쳐야 한다.**
- 그래서 `FileUrlResolver`와 같은 형태를 취한다 — **read 측이 소비자가 바로 쓸 수 있는 형태까지 완성해서 내려보낸다.** api는 도메인 기하 타입만 받고 저장 형식을 알지 않는다.
- 소비 모듈은 이 클래스가 아니라 계약인 `GeoRingsQueryPort`를 주입한다 — 읽기 경로 포트화로 api 모듈은 `com.tastyhouse.infrastructure..query..`에 의존하지 않는다.
- **경계 미보유·도형 미설정은 정상 상태다** — 각각 빈 목록과 `null`을 돌려주며 예외로 다루지 않는다.

#### `CeoQueryDao` — 인증·시드 조회는 write 포트에 잔류한다

**대상**: `.../ceo/query/CeoQueryDao.java`

인증·시드 멱등성에 쓰이는 단건 조회(`findByUsername`/`existsByUsername`)는 **불변식 검증 경로이므로 이 DAO가 아니라 write 포트에 잔류한다.** 표현 목적 조회가 아니므로 여기로 옮기지 않는다. 이 DAO가 갖는 것은 가게 배정용 Select 드롭다운을 채우는 전체 점주 목록뿐이다.

#### `CeoReplyPhraseQueryDao` — 정렬 2차 키가 필요한 이유

**대상**: `.../ceo/query/CeoReplyPhraseQueryDao.java`

- **페이징이 없다** — 점주당 5건 상한이라 한 번에 전부 내려주는 편이 단순하고, 페이지 파라미터를 두면 프론트가 쓰지 않을 분기를 떠안는다.
- 정렬은 `sort ASC, id ASC`다. **`id`를 2차 키로 두는 이유는 삭제 후 `sort`를 재정렬하지 않아 순번이 같은 행이 생길 수 있기 때문이다** — 동률일 때 등록순으로 안정 정렬된다. 2차 키를 빼면 순서가 요청마다 달라진다.

#### `PaymentProjection` — VO를 그대로 받는 infra 내부 중간 투영

**대상**: `.../order/query/PaymentProjection.java`

읽기 계약 `OrderPaymentResult`는 경계 타입 `Integer`를 싣지만, `PAYMENT.amount`가 `@Convert` 매핑이라 QueryDSL이 생성하는 path는 `SimplePath<Amount>`다. 그래서 **투영 단계에서는 VO로 받고 `OrderQueryDao#withUnwrappedAmount`가 fetch 직후 언랩한다.**

#### 그 밖의 개별 판단

| DAO | 판단 |
|---|---|
| `ShopDeliveryAreaAdjustmentQueryDao` | 가게별 신청 이력은 **가게당 건수가 적고 화면이 시트 안 목록이라 페이징하지 않는다.** 검수 화면 목록만 페이징한다. 동의서 파일은 `UPLOADED_FILE`을 `left join`해 URL까지 완성하므로 소비 Service가 fileId로 재조회하지 않으며 **응답에 `~FileId`가 노출되지 않는다.** `ShopQueryDao`에 합치지 않은 것은 그 DAO가 이미 가게 설정 전반과 이미지 변경요청까지 담아 비대하기 때문이며, `ShopDeliveryAreaQueryDao` 선례를 따른다. |
| `MemberReferralQueryDao` | 내가 추천한 회원 목록은 **최근 등록순**이다. |
| `FaqQueryDao` | **도메인당 DAO 1개 원칙에 따라 항목·카테고리 두 애그리거트를 한 클래스에 둔다.** 관리 조회(`findAllCategories`·`findAllFaqs`·상세)는 비노출분을 포함하고, 회원 조회(`findVisibleCategories`·`findVisibleFaqs`)는 노출분만 본다. `findVisibleFaqs`는 `categoryId`가 null이면 전체 카테고리 대상이다. |
| `NoticeQueryDao` | 소비 모듈은 이 DAO가 아니라 계약 `NoticeQueryPort`를 주입하므로 **api 모듈은 QueryDSL도 이 어댑터의 존재도 알지 않는다.** 관리 조회는 비노출 공지를 포함한다. |
| `PolicyQueryDao` | **정책 조회는 노출 제한이 없어**(활성/비활성 모두 공개 조회 가능) admin/web 구분이 필요하지 않으므로 메서드가 하나씩만 있다. `findByTypeAndVersion`은 **과거 버전 열람용이라 현행 여부를 따지지 않는다.** 상세 두 메서드가 투영을 공유한다. |
| `PointQueryDao` | 포인트 계정이 없는 회원이면 잔액 조회가 비어 있고 **소비 측에서 0으로 대체한다.** 전체 이력(`findPointHistories`)과 페이징 검색(`findPointHistoryPage`)은 시그니처로 구분하며, web의 내 포인트 내역 화면이 **페이징 없이 전체를 소비한다.** |
| `NotificationQueryDao` | 알림 목록은 최신순, 미읽음 개수는 헤더 배지용 집계다. |
| `PartnershipQueryDao` | 제휴 신청 조회는 **관리자만 소비한다**(web-api는 신청 생성만 한다). 상세는 삭제되지 않은 신청만 투영한다. |
| `BugReportQueryDao` | 버그 제보 조회도 **관리자만 소비한다**(web-api는 제보 등록만 한다). |

### 영속 어댑터·엔티티·매퍼 — 공통 구현 전략

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/**/persistence/**`

이 모듈의 `<ctx>/persistence/` 아래 500여 파일은 **엔티티 · 매퍼 · `*RepositoryImpl`(write 어댑터) · `*JpaRepository`** 네 종류로 이뤄지며, 대부분의 판단이 파일마다 반복된다. 아래는 그 반복되는 규칙을 한 벌로 모은 것이고, 특정 파일에만 해당하는 예외는 그 뒤 소절에 따로 적는다.

#### 영속 모델을 도메인 모델과 분리하는 이유

→ `*JpaEntity` · `*Mapper`

JPA 엔티티(`XxxJpaEntity`)는 DB 매핑(테이블·컬럼·감사 필드)만 담당하고 비즈니스 행위를 갖지 않는다. 도메인 모델은 프레임워크-프리를 유지해야 하므로 **도메인↔엔티티 변환 책임을 infrastructure 쪽 `XxxMapper`가 전담**한다. 매퍼의 방향은 셋으로 고정돼 있다.

| 메서드 | 경로 | 비고 |
|---|---|---|
| `toDomain(entity)` | 조회 | 엔티티를 도메인 모델로 재구성한다 |
| `toEntity(domain)` | 신규 저장 | 식별자 없는 상태로 만든다. 엔티티의 package-private `create(...)` 팩토리를 통해서만 호출한다 |
| `applyChanges(entity, domain)` | 갱신 | managed 엔티티에 변경 필드를 복사한다 |

**`applyChanges`가 없는 엔티티는 update 경로가 존재하지 않는다는 구조적 표현이다.** append-only 이력(`ShopChangeHistory`·`ShopCeoAssignmentHistory`·`ShopRequestComment`·`ShopRiderGuideHistory`·`CeoLoginHistory`·`PointHistory`·`ProductOptionGroupMergeHistory`·`ProductOptionGroupMergeExclusion`), 불변 사실 기록(`ProductFeedback`·`StorePriceVerificationItem`·`PaymentRefund`·`TossPaymentRecord`·`MemberWithdrawal`·`ReviewBlindRequestAttachment`·`ShopNoticeImage`), replace-all로 통째 교체되는 컬렉션(`ProductAllergen`·`ProductExposureHour`·배달팁 구간·지역별·시간별), read-only 마스터(`PublicHoliday`·`AdminDong`·`ProhibitedWord`·`RecommendedKeyword`)가 그 대상이다. **여기에 `applyChanges`를 추가하는 것은 "언젠가 바꿀 수 있다"는 잘못된 신호를 남기는 일이므로, 실제로 갱신 경로가 생길 때만 추가한다.**

#### 저장은 detached merge가 아니라 load-copy-save다

→ `*RepositoryImpl#save`

id가 없으면 insert, 있으면 **PK로 managed 엔티티를 조회(같은 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해 dirty checking으로 flush**한다. detached 인스턴스를 그대로 `save`(merge)하면 `@CreatedDate(updatable = false)` 감사 필드가 파손되고, 경우에 따라 새 행이 중복 생성된다. **이 경로를 merge로 바꾸지 않는다.**

- 갱신 경로가 없는 어댑터(append-only·불변 애그리거트)는 update 분기 자체를 두지 않는다.
- 갱신 경로가 없는데도 id 분기가 남아 있는 곳(`ShopBookmark`·`ShopClosedDay`·`ShopAmenity`·`ShopFoodType`·`ShopOrderMethod`·`ShopBannerImage`·`Tag`)은 존재 시 재조회만 수행한다.
- **PK 조회에 소프트 삭제 필터를 걸지 않는다** — 삭제 전이를 저장하는 경로가 바로 이 `save`이기 때문이다. 필터를 걸면 삭제가 영원히 실패한다(`PartnershipRepositoryImpl`·`ProductRepositoryImpl`·`RankPeriodRepositoryImpl#delete`·`RankPrizeRepositoryImpl#delete`).

#### write 어댑터에는 표현 목적 조회를 두지 않는다

→ `*RepositoryImpl`

CQRS 분리(공통 지침 패턴 4)로 목록·검색·상세 같은 **표현 목적 read는 전부 같은 모듈의 `<ctx>/query/*QueryDao`로 이관**됐고, write 어댑터에는 도메인 모델 단건 로드·중복 검증·저장·삭제만 남는다. 그 결과 대부분의 `*RepositoryImpl`은 QueryDSL이 필요 없어 `JPAQueryFactory`를 주입하지 않는다.

**write 포트에 남은 조회는 "불변식 판정에 필요한 것"이라는 기준으로 남긴 것이다.** `FaqCategoryRepositoryImpl#existsActiveItemsByCategoryId`(삭제 불변식), `ShopDetailRepositoryImpl`의 영업시간·휴게시간·정기휴무 목록(휴게시간 범위 검증·정기휴무 개수 제한·영업 상태 판정), `ProductPriceRepositoryImpl`의 가격 교체·인증 반영 판정, `ShopDeliveryTipRepositoryImpl#findRegionTipAdminDongIds`(일괄 삭제의 원자적 차단)가 그 예다. 표현용으로도 쓰인다는 이유만으로 DAO로 옮기지 않는다.

`FaqCategoryRepositoryImpl#existsActiveItemsByCategoryId`의 메서드명 "Active"는 **노출 여부가 아니라 미삭제**를 뜻한다 — 삭제되지 않은 항목이면 비노출이어도 존재로 본다(전환 이전 동작 보존).

#### 크로스 애그리거트 FK는 raw `Long`이고, VO 변환은 `IdMapping`이 전담한다

→ `IdMapping#vo` · `IdMapping#raw` · 각 `*Mapper`

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shared/persistence/IdMapping.java`

엔티티의 FK 컬럼은 `@Convert`로 VO를 매핑하지 않고 **raw `Long`으로 둔다** — VO 매핑을 하면 QueryDSL이 `NumberPath<Long>` 대신 VO path를 생성해 query DAO의 조인·투영이 깨진다(`CeoReplyPhraseJpaEntity.ceo_id`·`MenuReviewJpaEntity`의 FK 전부가 이 이유로 raw다).

승격·언패킹은 예외 없이 `IdMapping`을 거친다.

- **왜 필요한가**: 모든 `XxxId` VO는 compact constructor에서 null을 거부한다. 반면 일부 FK는 nullable이다(`Shop.ceo_id` 점주 미배정, `BugReport.assignee_admin_id` 미배정, `MemberDeliveryAddress.admin_dong_id` 행정동 매칭 실패, `ShopRequestIndex.attachment_file_id`). 매퍼에서 `CeoId.of(entity.getCeoId())`처럼 직접 호출하면 **컴파일은 통과하고, 그 FK가 실제로 null인 행을 읽을 때만 예외가 난다** — 빈 테이블이나 FK가 항상 채워진 샘플 데이터로는 잡히지 않는 결함이다.
- `raw(...)`도 쓰기 방향에서 대칭적으로 필요하다 — NOT NULL 컬럼이라도 도메인 모델이 아직 미배정 상태를 null VO로 들고 있을 수 있어 `domain.getCeoId().value()`가 NPE로 실패한다.
- **nullable 여부와 무관하게 모든 매퍼가 이 헬퍼를 통일해서 쓴다.** "NOT NULL이면 직접 호출, nullable이면 헬퍼"처럼 컬럼별로 형태를 나누면 작성자·리뷰어가 매번 nullable 여부를 확인해야 하고, 위험한 직접 호출 형태가 흔해 보여 눈에 띄지 않게 된다. NOT NULL 컬럼에서는 null 분기가 죽은 코드가 될 뿐이지만 nullable 컬럼에서는 유일하게 안전한 경로다.
- `Xxx.of(...)` 위임 규칙(DTO 조립 규칙)의 예외가 아니다 — `vo(raw, CeoId::of)`의 메서드 레퍼런스가 `of()` 팩토리를 경유하므로 `new`는 여전히 팩토리 내부에만 남는다.

#### enum 컬럼은 `@Enumerated(STRING)` + `columnDefinition` 병기가 필수다

→ `CeoLoginHistoryJpaEntity` · `NotificationJpaEntity` · `ProductFeedbackJpaEntity` · `ProductOptionGroupJpaEntity.groupType` · `ProductOptionGroupMergeHistoryJpaEntity` · `ShopCeoAssignmentHistoryJpaEntity` · `ShopChangeHistoryJpaEntity` · `ShopDeliveryAreaAdjustmentRequestJpaEntity.status` · `ShopDeliveryTipSettingJpaEntity` · `ShopRequestIndexJpaEntity` · `ShopRequestCommentJpaEntity` · `ShopRiderGuideHistoryJpaEntity`

`@Enumerated(EnumType.STRING)`에 **`columnDefinition = "VARCHAR(n)"`을 함께 적지 않으면** Hibernate 6의 `MySQLDialect`가 네이티브 `ENUM(...)` 컬럼을 기대해 `ddl-auto: validate`가 `wrong column type ... but expecting [enum (...)]`으로 **부팅을 거부한다**(`BugReport` 장애 선례). `n`은 `backend/schema.sql`의 길이와 일치해야 한다 — 알려진 값은 `CeoLoginHistory` result/failureReason 20, `ShopCeoAssignmentHistory` actionType 20, `ShopChangeHistory` category/changeType 40 · actionType/actorType 20, `ShopRequestIndex` requestType 40 · status 20이다.

#### 감사 필드 상속은 DB 컬럼이 정한다

→ `BaseEntity` 상속 여부

`created_at`·`updated_at`이 NOT NULL이면 **도메인 모델에 감사 필드가 없더라도** `BaseEntity`를 상속한다 — 감사 리스너가 값을 채워야 하기 때문이며, 매퍼는 그 두 값을 도메인으로 옮기지 않는다(`ProductShopLinkJpaEntity`·`ProductOptionGroupLinkJpaEntity`·`ProductCommonOptionGroupLinkJpaEntity`·`ProductExposureHourJpaEntity`).

반대로 테이블에 감사 컬럼이 없거나 소비처가 없으면 상속하지 않는다(`PublicHolidayJpaEntity`·`AdminDongJpaEntity`·`MailVerificationJpaEntity`·`SmsVerificationJpaEntity`·`ReviewTagJpaEntity`·`ReviewBlindRequestAttachmentJpaEntity`·`ProhibitedWordJpaEntity`). `MailVerification`·`SmsVerification`은 `created_at`만 있고 `updated_at`이 없어서, `ReviewBlindRequestAttachment`는 불변 애그리거트라 감사 시각을 소비하지 않아서다.

`ShopRequestCommentJpaEntity`는 수정 경로가 없어 `updated_at`이 항상 `created_at`과 같지만, `BaseEntity` 규약을 맞추려고 컬럼을 둔다.

#### getter 없는 쓰기 전용 필드를 지우지 않는다

→ `OrderProductOptionJpaEntity`의 옵션 스냅샷 4필드 · `ReviewTagJpaEntity.tagId` · `AdminDongJpaEntity`의 바운딩박스 4컬럼 · `ShopDeliveryAreaPolygonJpaEntity`의 집계 2컬럼 · `RecommendedKeywordJpaEntity.sortOrder` · `StationJpaEntity.stationName` · `PublicHolidayJpaEntity.substitute` · `ReservationSlotJpaEntity.version`

IDE가 "assigned but never accessed" / "never used" / "never assigned"로 경고하지만 **전부 정상이며 제거하면 동작이 깨진다.** 각각의 근거는 다르다.

- **insert 전용 스냅샷** — JPA가 flush 시 리플렉션으로 읽는 컬럼 매핑이다. 지우면 주문 시점 옵션 스냅샷·태그 FK가 저장되지 않는다.
- **정렬·조회 전용** — QueryDSL `.orderBy(...)`에서만 참조하고 `Projections.constructor` 투영에는 포함하지 않거나(`RecommendedKeywordJpaEntity.sortOrder`), 투영으로만 조회돼 엔티티→도메인 재구성 매퍼가 없다(`StationJpaEntity`).
- **스키마 정합 전용** — 도메인이 소비하지 않지만 NOT NULL 컬럼과 매핑돼야 `ddl-auto: validate`를 통과한다(`PublicHolidayJpaEntity.substitute`).
- **파생 값** — 바운딩박스·폴리곤 집계는 경계에서 파생되는 값이라 getter를 두면 호출자 없는 죽은 코드가 된다. 값은 SQL 집계·점검 질의가 소비한다. **필요해지는 시점에 질의와 함께 추가한다.**
- **낙관적 락 버전** — 애플리케이션이 대입하지 않고 Hibernate가 flush 시점에 검증·증가시킨다.

#### replace-all 교체의 선행 삭제는 벌크 `@Modifying` delete여야 한다

→ `ProductAllergenJpaRepository#deleteAllByProductId` · `ProductExposureHourJpaRepository#deleteAllByProductId` · `ShopDeliveryTipTierJpaRepository#deleteByShopId` · `ShopDeliveryTipRegionJpaRepository#deleteByShopId` · `ShopDeliveryTipHolidayJpaRepository#deleteByShopId` · `ShopDeliveryAreaJpaRepository#deleteByShopIdAndSource`

**derived `deleteBy...`로 되돌리지 않는다.** derived 삭제는 영속성 컨텍스트에 delete action만 큐잉하는데, **Hibernate의 기본 flush 순서는 action을 타입별로 묶어 insert를 delete보다 먼저 실행한다.** 따라서 같은 키를 재사용하는 교체(같은 알레르기 성분을 다시 체크, 같은 `day_type`·`tier_order`·`admin_dong_id`를 유지한 채 금액만 변경)에서 **항상 유니크 키 중복으로 실패한다.** 관련 제약은 `uk_product_allergen_product_type`·`uk_product_exposure_hour`·`uk_shop_delivery_tip_tier`·`uk_shop_delivery_tip_region`·`uk_shop_delivery_tip_holiday_shop_id`다.

derived 삭제는 또한 대상을 먼저 조회한 뒤 건별로 삭제하므로 수백 건에서 쿼리가 그만큼 늘어난다 — 폴리곤 재저장은 매번 이 삭제로 시작한다.

**`clearAutomatically`를 함께 붙이는 이유**: 벌크 연산은 1차 캐시를 우회하므로, 삭제된 행이 캐시에 남아 뒤이은 조회를 오염시키거나 재삽입이 이미 삭제된 엔티티를 보고 유니크 제약을 오판한다. 같은 이유로 `ShopNoticeImageRepositoryImpl#deleteByShopNoticeId`(QueryDSL bulk delete)와 `MemberReviewRankRepositoryImpl#deleteByRankTypeAndBaseDate`도 삭제 후 1차 캐시를 비운다 — 후자는 같은 트랜잭션에서 곧바로 같은 기준일 랭킹을 새로 적재하므로 캐시에 남은 행이 적재분과 충돌한다.

#### 소프트 삭제 필터는 조회 성격으로 갈린다

→ `ProductJpaRepository#findByIdAndDeletedFalse` · `ProductRepositoryImpl#findById` · `ProductRepositoryImpl#findByIdIncludingDeleted`

`ProductJpaRepository`의 파생 쿼리 대부분에 `AndDeletedFalse`가 붙어 있다. **상속받은 `findById`에는 그 필터가 없으므로** 일반 로드에는 `findByIdAndDeletedFalse`를 쓰고, 삭제·저장 경로만 필터 없는 `findById`를 쓴다.

일반 로드(`ProductRepositoryImpl#findById`)에 필터를 걸어 두면 **신규 주문·신규 메뉴평가 차단이 자동으로 성립**한다. 반면 삭제 자신은 필터 없는 순수 PK 조회(`findByIdIncludingDeleted`)로 대상을 읽어야 한다 — `findById`를 재사용하면 이미 삭제된 행을 다시 읽지 못해 **멱등 처리와 상태 확인이 불가능**해지고 삭제가 영원히 실패한다(`RankPeriodRepositoryImpl#delete` 선례).

`ProductCategoryRepositoryImpl#delete`만 **하드 삭제**다 — 메뉴그룹은 주문·리뷰가 참조하지 않고, 소속 메뉴가 남아 있으면 도메인 서비스가 `PRODUCT_CATEGORY_HAS_PRODUCTS`로 먼저 막으므로 고아 데이터가 생기지 않는다. `Product` 자신이 소프트 삭제인 이유는 스키마에 FK 제약이 0개이기 때문이다.

#### null 파라미터를 "조건 없음"으로 해석시키지 않는다

→ `ProductJpaRepository#findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc` · `ProductRepositoryImpl#findAllByShopIdAndCategoryId`

미분류 메뉴 조회를 `productCategoryId = null`로 합치지 않고 **별도 파생 메서드로 가른다.** 하나로 합치면 null이 "조건 없음"으로 해석돼 가게의 모든 메뉴가 대상이 된다.

#### 단건 시그니처는 DB가 1건을 보장할 때만 쓴다

→ `ShopNoticeJpaRepository#findFirstByShopIdAndExposedIsTrueOrderByIdDesc` · `ShopOrderNoticeJpaRepository#findByShopId` · `ReviewBlindRequestRepositoryImpl#findApprovedByReviewId`

**`findFirstBy~`를 `findBy~`로 바꾸지 않는다.** 노출 공지 1건 불변식은 도메인 서비스가 지키고 DB 제약이 없다(MySQL이 부분 유니크 인덱스를 지원하지 않는다). 따라서 `is_exposed = 1`이 2건 이상인 상태가 물리적으로 가능한데, 단건 시그니처는 그때 `IncorrectResultSizeDataAccessException`으로 **해당 가게의 공지 기능을 통째로 500으로 만든다.** 최신 1건을 결정적으로 고르면 다음 `expose` 호출이 나머지를 자연스럽게 정리한다.

반대로 `ShopOrderNotice`는 `shop_id`에 조건 없는 컬럼 단위 유니크 제약이 있어 MySQL로 표현되므로 단건 시그니처가 안전하다. **그 유니크 제약이 동시 요청 두 건이 각각 "기존 행 없음"을 읽고 둘 다 insert하는 경합 창을 닫는다** — 도메인 서비스의 선행 조회는 "정상 수정 경로로 유도하는 편의"만 담당한다.

`ReviewBlindRequestRepositoryImpl#findApprovedByReviewId`도 같은 형태다 — 1회 제한 덕분에 `APPROVED`는 리뷰당 최대 1건이지만 그 제한이 애플리케이션 검사라 동시 요청에는 이론상 뚫린다. 따라서 `fetchOne`(2건이면 예외) 대신 최신 1건을 취해 조회가 실패하지 않게 한다.

#### 위치 기반 전달이 많은 매퍼는 3중 대조가 규약이다

→ `ProductNutritionMapper` · `TossPaymentRecordMapper` · `ProductNutritionJpaEntity`

`ProductNutrition`의 수치 14개와 `TossPaymentRecord`의 필드 60여 개는 **타입이 같아 위치를 착각해도 컴파일이 통과하고 값만 조용히 뒤바뀐다.** 이 파일들을 고칠 때는 **도메인 필드/getter 순서 · 엔티티 `create`/`reconstitute` 파라미터 순서 · 매퍼가 넘기는 인자 순서**를 하나씩 대조한다.

`ProductNutritionJpaEntity`가 14개 수치를 `@Embedded` record로 묶지 않고 평면 필드로 두는 것도 같은 이유다 — record 컴포넌트 선언 순서가 어긋나면 값이 조용히 뒤바뀌는데(`EmbeddedRecordComponentOrderTest`가 잡는 사고), 평면 필드는 `@Column`이 이름으로 매핑하므로 그 위험이 구조적으로 없다. `ShopRiderGuideJpaEntity`의 픽업 위치 5개 컬럼도 같은 판단으로 평면이다 — VO로 묶으려면 record여야 하고 컴포넌트 선언 순서가 알파벳 오름차순이어야 하는 제약이 붙는데 얻는 것이 없다.

#### 스냅샷 컬럼은 FK가 아니라 복사다

→ `OrderJpaEntity`의 배달 목적지 7컬럼 · 수령 예약시간 2컬럼 · `OrderProductOptionJpaEntity`의 옵션 스냅샷 · `ShopDeliveryAreaPolygonJpaEntity.center_*` · `StorePriceVerificationItemJpaEntity.store_price`

주문 시점 값을 FK가 아니라 복사해 박제한다 — 회원이 주소록을 수정·삭제해도 과거 주문의 배달팁 산출 근거가 사라지면 안 되기 때문이다. 배달이 아닌 주문은 7컬럼이 전부 null이라 모두 nullable이며, 즉시 주문은 예약시간 2컬럼이 null이다(기존 주문 행이 전부 null이라 "즉시 주문"으로 정확히 해석되는 무손상 마이그레이션). 포장 주문은 슬롯이 단일 시각이라 두 컬럼 값이 같다.

`OrderProductOptionJpaEntity.optionGroupType`은 **도메인 enum이 아니라 `String`으로 매핑한다** — 주문 시점의 사실을 박제한 값이라 나중에 유형 enum에 상수가 추가되거나 이름이 바뀌어도 과거 주문 기록이 흔들리면 안 되며, enum으로 매핑하면 알 수 없는 값에서 로드가 실패한다.

`ShopDeliveryAreaPolygonJpaEntity.center_*`는 저장 시점 가게 좌표의 스냅샷이라 현재 좌표와 다를 수 있다 — 가게가 이전하면 그 차이로 "배달지역 재설정 필요"를 감지한다.

`StorePriceVerificationItemJpaEntity.store_price`를 항목이 직접 들고 있는 이유는 **승인이 요청 시점의 매장가를 쓴다**는 규칙 때문이다 — 승인 시점에 현재 가격을 다시 읽으면 검수자가 보지 않은 값이 승인된다.

#### 비정규화 컬럼은 조인을 줄이려는 것이지 진실원이 아니다

→ `ProductFeedbackJpaEntity.shopId` · `ProductRepresentativeRequestJpaEntity.shop_id` · `ProductShopLinkJpaEntity`

`ProductFeedback.shop_id`는 제보 시점의 가게이며, 점주 목록 조회를 `PRODUCT` 조인 없이 처리하기 위한 비정규화다. `ProductRepresentativeRequest.shop_id`를 요청 행이 직접 들고 있는 것은 개수 제한이 가게 단위 불변식이라 메뉴를 거치지 않고 가게별 대기 건수를 세야 하기 때문이다.

`ProductShopLink`(메뉴↔가게 N:M)는 **`PRODUCT.shop_id`를 대체하지 않는다** — 그 컬럼은 원본 소유 가게로 남고 이 테이블은 "어느 가게 메뉴판에 노출되는가"만 담는다. `UNIQUE(product_id, shop_id)`가 같은 메뉴를 같은 가게에 두 번 연결하는 것을 DB 차원에서 막는다. 메뉴판 노출 판정(`ProductJpaRepository#countVisibleByShopLink`·`ProductRepositoryImpl`·`ProductPriceJpaRepository`)은 전부 링크를 통해야 한다 — `PRODUCT.shop_id`로 세면 다른 가게에서 불러온 메뉴가 빠지고, 반대로 이 가게 메뉴판에 없는 원본 메뉴가 잘못 포함된다. `distinct`는 조인 형태가 바뀌어도 개수가 부풀지 않게 하는 방어다.

`ProductPriceJpaRepository`의 가게별 가격 조회 조건은 **`ProductQueryDao#findShopProductPrices`와 반드시 같아야 한다** — 갈리면 매장가격 뱃지가 두 화면에서 달라진다.

#### 컨텍스트 경계를 건너는 것은 얇은 포트 어댑터가 흡수한다

→ `ReplyPhraseProhibitedWordValidatorAdapter` · `StorePriceVerificationAdapter` · `ShopRequestIndexSyncAdapter` · `MemberGradeReviewCountAdapter` · `MemberReviewCountAdapter` · `ProductReviewStatisticsAdapter` · `KeywordCountAdapter`

domain의 `ContextBoundaryTest`가 타 컨텍스트의 `service`·`model` 직접 import를 금지하므로, 도메인 서비스는 포트만 알고 실제 결합은 어댑터가 흡수한다. **어댑터는 규칙을 복제하지 않고 위임만 한다.**

- `ReplyPhraseProhibitedWordValidatorAdapter` — 검수 규칙 자체는 shop 컨텍스트의 `ProhibitedWordValidator`에 그대로 위임한다. 주입받는 빈은 `ShopDomainConfig`가 캐싱 데코레이터로 감싸 등록한 것이라 검증마다 금칙어 전량을 다시 읽지 않는다.
- `StorePriceVerificationAdapter` — 인증 요청 애그리거트는 product 소유지만(승인의 본체가 `PRODUCT_PRICE`를 채우는 일이므로) 인증 ON/OFF는 가게 단위 상태라 `SHOP`에 있다. 이 플래그만 좁은 포트로 뽑는다. `@Repository`가 아니라 `@Component`인 이유는 도메인 write 포트 구현이 아니라 출력 포트 어댑터이기 때문이다.
- `ShopRequestIndexSyncAdapter` — 통합 인덱스와 그 기록자는 shop 소유다. **상태 문자열을 여기서 enum으로 승격하며, 승격 실패는 프로그래밍 오류(양쪽 enum이 어긋난 상태)이므로 `from(String)`의 400 변환에 맡기지 않고 그대로 전파시킨다** — 조용히 넘기면 인덱스가 원본과 어긋난 채 남는다. 기록은 원본 상태 전이와 **같은 트랜잭션**에서 동기 수행된다(이벤트·`AFTER_COMMIT`을 쓰지 않는 이유는 기록 유실이 곧 "요청이 목록에서 사라짐"이기 때문이다).
- 집계 조회 어댑터 4종(`MemberGradeReviewCountAdapter`·`MemberReviewCountAdapter`·`ProductReviewStatisticsAdapter`·`KeywordCountAdapter`)은 집계 조회 자체를 소유 도메인의 QueryDao에 두고, **그 결과를 소비 도메인이 이해하는 값 타입으로 옮겨 담는 변환만** 담당한다. 덕분에 소비 도메인 서비스는 read model이나 QueryDSL을 알지 않는다. member와 rank가 같은 DAO를 공유하면서도 포트·값 타입을 컨텍스트별로 나눈 것은 컨텍스트 순환을 피하기 위해서다.

#### `GeoPolygonTextCodec` — 폴리곤을 `LONGTEXT`에 담는다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shared/persistence/GeoPolygonTextCodec.java`
→ `encode` · `encodeRings` · `decode` · `decodeRings` · `COORDINATE_SCALE`

인코딩 형식은 **링 구분 `;` · 점 구분 `,` · 점 내부는 `"경도 위도"`(공백 1칸) · 소수점 6자리 고정**이다. 정밀도는 위경도 저장 컬럼(`DECIMAL(9,6)`)과 맞췄다.

**MySQL `GEOMETRY`를 쓰지 않는 이유**: 공간 인덱스가 이득을 주는 질의가 설계상 없다(폴리곤 조회는 항상 `WHERE shop_id = ?` 단건이다). 반면 `hibernate-spatial`+JTS 의존, dialect 교체, SRID 4326 축순서 함정, 자기교차 도형에서의 `ST_Contains` 미정의 동작을 모두 떠안게 된다. domain은 production 의존이 0개로 강제되어 JTS `Geometry`를 도메인에 둘 수도 없다.

**JSON을 쓰지 않는 이유**: 이 모듈에 Jackson이 보장되지 않고 리포에 JSON 컬럼 선례가 없다. `String.split`만으로 끝나는 형식이라 의존을 늘릴 이유가 없다.

**좌표 순서가 "경도 위도"인 이유**: GeoJSON·WKT 등 공간 데이터 표준이 `(x, y) = (경도, 위도)` 순서를 쓰므로 저장 형식이 그 관례를 따른다. 다만 **API 경계에서는 `{latitude, longitude}` 객체로 주고받아** 순서 혼동을 없앤다 — 이 코덱은 그 경계 안쪽(영속 계층)에만 존재하며, `GeoPoint` 복원 시 순서를 뒤집는다.

**형식이 깨진 입력은 `IllegalArgumentException`으로 실패시킨다** — 조용히 건너뛰면 도형의 일부가 사라진 채 복원되어, 점주가 그린 것과 다른 배달지역이 저장된 것처럼 보인다. 다만 `decodeRings`는 값이 없으면 빈 목록을 반환한다(행정동 경계는 단계적으로 투입되므로 미보유가 정상 상태다).

도형 좌표를 도메인이 알지 않도록 **형식 지식을 이 코덱에 가두고**, `ShopDeliveryAreaPolygonMapper`·`AdminDongMapper`가 위임한다.

#### `CachingProhibitedWordRepository` — 캐싱은 도메인이 아니라 어댑터에 둔다

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shop/persistence/CachingProhibitedWordRepository.java`
→ `TTL` · `Snapshot`

`ProhibitedWordValidator`는 텍스트 검증 때마다 `ProhibitedWordRepository#findAll()`을 호출하는데, 점주 입력(가게소개·찾아오는길 등) 저장 경로마다 금칙어 테이블을 통째로 다시 읽는 것이 낭비다. **검증기는 domain의 순수 POJO라 스프링 `@Cacheable`을 붙일 수 없으므로**, 캐싱을 write 포트 어댑터를 감싸는 데코레이터로 구현하고 빈 등록 지점(`ShopDomainConfig`)에서 주입한다 — 검증기·도메인 서비스 코드는 그대로다.

금칙어는 SQL 시드로 관리되는 read-only 데이터(Java 계층에 생성·수정 경로가 없다)라 정합성 리스크가 낮다. 그래도 **무기한 캐싱은 시드 갱신이 재기동 전까지 반영되지 않으므로 TTL(10분)을 둬서 자연히 만료시킨다.**

`AtomicReference`에 (적재 시각, 목록) 스냅샷을 통째로 담아 교체하므로 **락이 필요 없다.** 만료 직후 동시 호출이 겹치면 적재가 중복될 수 있으나 결과가 같은 read-only 조회라 무해하다 — 중복 적재를 막는 락이 주는 이득보다 락 경합 비용이 크다.

#### `AdminDongRepositoryImpl` — 행정동 마스터 동기화

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/region/persistence/AdminDongRepositoryImpl.java`
→ `synchronize` · `SAVE_BATCH_SIZE` · `deactivateMissing`

쓰기는 `synchronize`(동기화 배치 전용) 하나뿐이며 건별 저장 경로가 없다.

- **모든 조회가 `is_active = 1`로 통일돼 있다.** 과거 이 어댑터의 `existsById`·`findByDongNameMatch`는 활성 여부를 거르지 않는 반면 `AdminDongQueryDao`는 걸러, 통폐합돼 폐지된 행정동이 **검색 목록에는 안 뜨는데 등록 검증은 통과하고 주소 매칭에도 걸리는** 비대칭이 있었다. 폐지 동은 시드가 삭제하지 않고 `is_active = 0`으로 남기므로(다른 테이블이 id로 참조 중이다) **이 필터가 유일한 방어선이다.**
- **전량 삭제·재삽입이 아니라 제자리 갱신(id 보존)** 인 이유도 다른 테이블이 `id`를 참조하기 때문이다. 원천에서 사라진 동은 삭제하지 않고 `deactivate()`로 폐지 처리한다.
- **빈 목록 동기화는 `IllegalArgumentException`으로 막는다** — 원천을 못 읽었을 때 마스터를 비우면 전국 배달지역이 통째로 죽는다.
- `SAVE_BATCH_SIZE = 500` — 3,500여 건을 한 영속성 컨텍스트에 쌓으면 경계 문자열(행당 평균 4KB, 최대 64KB)까지 함께 메모리에 머물러 힙이 불필요하게 커진다.

`AdminDongJpaEntity`·`AdminDongMapper` 쪽 규칙.

- 좌표·경계 컬럼은 전부 nullable이다 — 시드가 단계적으로 투입되므로(코드·좌표 먼저, 경계는 나중) 미보유 행이 정상 상태이며, 기존 행에 무해하게 추가하기 위한 조건이기도 하다.
- **바운딩박스는 호출자가 넘기지 않고 경계에서 파생시킨다** — 경계와 박스가 어긋나면 프리필터가 실제 경계와 다른 후보를 내놓는데, 두 값을 각각 받으면 그 어긋남이 조용히 저장될 수 있다. 경계가 사라지면 낡은 박스가 남지 않도록 null까지 그대로 반영한다.
- `toEntity`와 `applyChanges`는 **같은 파생 헬퍼를 쓴다** — 한쪽만 바뀌면 신규 행과 갱신 행의 저장 형태가 갈린다.
- 대표점은 **위경도가 모두 있을 때만** `GeoPoint`로 승격한다(하나만 있으면 좌표로서 의미가 없다). 경계는 빈 문자열·null 모두 빈 목록으로 정규화한다.
- `AdminDongJpaRepository#findAllWithinBoundingBox`는 배달지역 환산의 후보 프리필터이며 `idx_admin_dong_center`를 탄다. 대표점이 없는 행은 좌표 비교가 `NULL`이 되어 자동으로 빠진다 — 판정 근거가 없는 동을 후보에 넣어도 "판정 불가"로 분류될 뿐이다. `findExistingIds`는 일괄 등록의 존재 검증이 건별 조회를 돌지 않도록 식별자만 투영한다.

#### 낙관적 락은 슬롯 예약에만 있고, 예외는 프레임워크-프리로 번역한다

→ `ReservationSlotJpaEntity.version` · `ReservationSlotRepositoryImpl#save` · `ReservationSlotJpaRepository#findByShopIdAndSlotDateAndSlotTime`

`@Version`만으로 동시 차감 충돌을 감지하므로 **별도 `@Lock`을 두지 않는다.** managed 엔티티의 `@Version`이 flush 시 검증·증가되므로 load-copy-save가 낙관적 락 동작을 그대로 보존한다. `save`의 dirty checking 변경은 명시적 `flush` 시점에 검증되므로 충돌도 거기서 나며, `save`·`flush`를 함께 감싸 `OptimisticLockConflictException`으로 번역한다 — **도메인의 재시도 판별이 spring-orm 예외에 의존하지 않게** 하기 위함이다.

#### 승인 상태를 별도 요청 테이블로 분리하지 않은 판단

→ `ShopMenuCollectionImageJpaEntity.status` · `ShopMenuCollectionImageJpaEntity#applyChanges`

메뉴모음컷의 승인 상태가 별도 요청 테이블이 아니라 이미지 행에 있는 이유는 **검수 대상이 "이 이미지 자체"라 요청과 결과물이 1:1**이기 때문이다 — 분리하면 승인 시 행을 옮겨 담아야 하고, 점주가 승인 전부터 관리하는 `sort`가 그 순간 흔들린다.

그래서 `applyChanges`가 `sort`도 복사한다 — **순서 변경은 승인 없이 즉시 반영되는 정상 경로**라 상태 전이만 복사하면 순서 변경이 조용히 유실된다. `ShopMenuCollectionImageJpaRepository#findAllByShopIdOrderBySortAsc`가 상태 무관 전량을 읽는 것도 같은 맥락으로, 개수 제한·최소 1개 유지 같은 집합 불변식이 대기·반려 건까지 포함해 판정된다.

#### 배달팁 5종을 한 어댑터·한 매퍼가 담당하는 이유

→ `ShopDeliveryTipRepositoryImpl` · `ShopDeliveryTipMapper`

write 포트 `ShopDeliveryTipRepository`가 5종을 한 인터페이스로 묶었으므로 매퍼도 하나에 모은다 — 타입마다 파일을 쪼개면 같은 어댑터가 매퍼 5개를 import하게 되고, 5종이 함께 바뀌는 변경(예: FK 매핑 방식 전환)이 5개 파일에 흩어진다.

이 어댑터는 `ShopDeliveryTipRegionLookup`도 함께 구현한다 — **두 포트가 같은 테이블(`SHOP_DELIVERY_TIP_REGION`)을 읽으므로 어댑터를 쪼개면 같은 쿼리가 두 곳에 생긴다.** 포트를 나눈 것은 소비자(`ShopDeliveryAreaService`)의 의존을 좁히기 위함이지 저장소를 나누기 위함이 아니다.

`ShopDeliveryTipSettingJpaEntity`가 거리별 설정(기본배달거리·할증 단위·할증액)을 별도 테이블로 쪼개지 않고 인라인한 이유는 `UNIQUE(shop_id)` 행 하나가 **거리별↔지역별 배타성의 물리적 단일 소유자**가 되게 하기 위해서다.

`ShopDeliveryAreaJpaEntity.source`가 필요한 이유는 폴리곤 재저장 시 **도형에서 파생된 행만 골라 교체**하기 위해서다 — 구분이 없으면 점주가 손으로 추가한 행까지 함께 지워진다. DDL이 `VARCHAR(20) NOT NULL DEFAULT 'MANUAL'`이라 기존 행이 자동으로 채워지고 구버전 백엔드로 롤백해도 INSERT가 계속 성공하며, 매퍼는 `source`가 null인 행을 `MANUAL`로 본다(배포 전환 구간 방어).

#### 레거시 값을 VO 승격에서 걸러내는 자리

→ `ReviewMapper#toDomain`

`REVIEW.product_id`는 컬럼이 NOT NULL이지만, 삭제된 `REVIEW_PRODUCT` 애그리거트의 레거시 값으로 `0`이 광범위하게 남아 있다. `ProductId` VO는 0을 양수가 아니라며 거부하므로 **0을 null과 동일하게 "상품 미상"으로 취급해 승격을 건너뛴다.**

#### 모듈 스캔·auto-configuration

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/PersistenceModuleAutoConfiguration.java` · `.../InfrastructurePersistenceConfig.java`

- 이 모듈이 앱의 runtimeClasspath에 있으면 자동 활성화된다("클래스패스 존재 = 활성화"). 앱은 `@Import`하지 않고 `build.gradle`에서 `runtimeOnly`로만 의존하며, 끄려면 `spring.autoconfigure.exclude`를 쓴다.
- **`infrastructure.redis`를 컴포넌트 스캔에서 제외한다** — Redis 빈의 등록 주체는 `RedisModuleAutoConfiguration` 하나로 일원화한다. 제외하지 않으면 redis 모듈이 클래스패스에 있을 때 두 스캔이 같은 클래스를 중복 등록한다.
- **`before = JpaRepositoriesAutoConfiguration`이 필요하다** — 스캔 안 `InfrastructurePersistenceConfig`의 `@EnableJpaRepositories`가 같은 deferred 단계에서 Boot보다 먼저 처리돼야 Boot 쪽 `@ConditionalOnMissingBean(JpaRepositoryConfigExtension)`이 물러난다.
- `InfrastructurePersistenceConfig`가 JPA 스캔의 **단일 소유자**다. domain은 이 모듈을 의존하지 않으므로(의존 방향: infrastructure → domain) domain에 이 패키지를 문자열로 선언할 수 없고, Spring Boot 공식 권장대로 엔티티를 소유한 모듈이 스스로 스캔 설정을 선언한다. `basePackageClasses`로 `com.tastyhouse.infrastructure` 이하 전체를 타입 세이프하게 지정한다. domain이 100% JPA-free로 전환되며 `@EnableJpaAuditing`·`@EnableTransactionManagement` 전역 설정도 이 클래스로 병합됐고, `BaseEntity`의 `@CreatedDate`/`@LastModifiedDate`가 이 설정으로 채워진다.

#### 리스너 배치가 `infrastructure:persistence`인 이유 (개별 사유)

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/**/listener/*.java`

리스너 작성 규칙 일반은 이 문서의 [`<ctx>/listener/`](#ctxlistener--도메인-이벤트-리스너) 절에 있다. 여기에는 **"왜 특정 api 모듈이 아니라 이 모듈이 소유하는가"의 리스너별 근거**만 적는다 — 공통 답은 "특정 api 모듈에 두면 다른 모듈이 같은 이벤트를 발행할 때 후속 처리가 조용히 누락된다"이고, 각 리스너의 발행 경로가 그 근거다.

| 리스너 | 발행 경로가 여럿인 근거 |
|---|---|
| `CouponEventListener` | 발급은 admin(수동)·이벤트 경유(가입·추천 보상), 사용은 web(주문 결제) |
| `MemberEventListener` | 가입·탈퇴가 web-api(본인)와 admin-api(관리자 강제 탈퇴) 양쪽 |
| `PointEventListener` | web(주문 결제)·admin(수동 조정)·이벤트 경유(결제 취소·추천 보상) |
| `PaymentEventListener` | 지금은 web-api뿐이지만 admin-api의 환불·관리 경로가 같은 이벤트를 발행하게 되어도 포인트 연동이 누락되면 안 된다 |
| `ReferralRegisteredEventListener` | 추천 등록이 일반 가입과 소셜 가입(4종) 어느 경로에서도 발생한다 |
| `ProductMenuReviewEventListener` | 평가는 web-api에서 등록되지만 admin-api의 숨김·삭제로도 통계가 바뀐다 |
| `ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` | 지금은 admin/ceo 경로뿐이지만, **알림 적재는 행위 주체가 아니라 "그 일이 일어났다"는 사실에 반응해야 한다** |
| `PolicyActivatedEventListener` | 활성화 자체가 특정 액터에 묶이지 않는 도메인 불변식(`PolicyActivationService`)이다 |
| `MailVerificationEventListener` · `SmsVerificationEventListener` | 도메인별 분리 원칙 — 한 리스너가 여러 도메인 이벤트를 구독하면 한 도메인의 변경이 다른 도메인의 리스너 파일을 건드린다 |

개별 판단으로 따로 남길 것.

- **`ReferralRegisteredEventListener` — 순서가 중요하다. 적립 먼저, 보상 완료 전이는 그 다음이다.** 전이가 먼저 커밋되면 "완료로 표시됐지만 포인트는 없는" 추천 관계가 남아 적립 실패 건을 상태로 식별할 수 없게 된다. 지금 순서라면 적립 실패 시 추천 관계가 `PENDING`에 머물러 재처리 대상으로 남는다. 과거에는 `ReferralRegistrationService`가 point 애그리거트와 리포지토리를 직접 주입해 적립을 재구현했으나, 적립 시맨틱(잔액 증가 + EARNED 이력 + 적립 이벤트)의 단일 원천은 `PointLedgerService`여야 하므로 컨텍스트를 잇는 책임을 리스너로 옮겼다. `AFTER_COMMIT`이라 이 핸들러가 실패해도 추천 등록은 롤백되지 않는데, **추천 등록과 보상 적립의 결합을 끊기 위해 의도적으로 감수한 트레이드오프**다.
- **`PaymentEventListener` — 적립액 계산의 단일 원천은 `PaymentConfirmationService#calculateEarnedPoint`다**(주문에 기록되는 적립 포인트와 실제 적립액이 갈리지 않도록). 리스너는 이벤트 수신과 트랜잭션 경계만 담당한다.
- **`ReviewBlindApprovedEventListener` · `ReviewOwnerReplyEventListener` — `@Async` + `AFTER_COMMIT` + `REQUIRES_NEW` 3종 세트를 반드시 함께 단다.** `AFTER_COMMIT`만 달면 호출 스레드에서 동기 실행되어 알림 실패가 원본 API로 전파된다 — DB에는 반영됐는데 화면은 실패로 뜨는, "알림이 실패해도 원본은 유효하다"는 판단과 정면으로 어긋나는 상태가 된다. `REQUIRES_NEW`가 없으면 커밋될 트랜잭션이 없어 리스너가 조용히 아무것도 남기지 않는다. 동기가 아니라 리스너인 근거는 「크로스 컨텍스트 후처리」 판정표의 "후처리가 실패하면 원본도 없던 일이 되어야 하는가?"에 **아니오**이기 때문이며, 인증코드 발송이 동기인 것과 반대 방향이다.
- **`ReviewBlindApprovedEventListener`에 가게명 조회가 없는 것은 의도적이다** — 게시중단 안내 문구가 가게명을 노출하지 않는다. 고객에게 필요한 정보는 재노출 예정일이지 어느 가게가 요청했는지가 아니며, **요청 주체를 알리면 리뷰 작성자와 점주 사이의 분쟁을 부추길 수 있다.**
- **`ProductMenuReviewEventListener`의 구독 대상은 REVIEW가 아니라 MENU_REVIEW 이벤트다.** `PRODUCT.rating`의 근거가 MENU_REVIEW로 완전히 옮겨갔으므로 구독도 하나만 남는 것이 맞다 — 두 리스너가 같은 `ProductReviewStatsService`를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서 사라진다. `ReviewCreatedEvent`/`ReviewDeletedEvent`는 발행만 남고 소비자가 0이다. **`productId == null` 가드는 유지한다** — MENU_REVIEW의 `product_id`는 NOT NULL이지만 이벤트 record가 VO를 담고 있어 향후 발행 경로가 늘 때 null이 실릴 수 있고, 그 예외는 `AFTER_COMMIT` 리스너에서 조용히 유실된다.
- **`MailVerificationEventListener` · `SmsVerificationEventListener`는 발송을 담당하지 않는다.** 이 이벤트는 인증 **완료** 시점이고 발송은 **발급** 시점에 필요하므로 시점이 다르다 — 발송은 `MailVerificationService#issue`·`SmsVerificationService#issue`가 발급과 원자적으로 수행한다.
- **`PolicyActivatedEventListener`는 재동의 요청·개정 고지 발송을 아직 담당하지 않는다** — 발송 대상이 전체 회원이라 요청 스레드에서 처리할 수 없고 배치·큐 설계가 선행돼야 한다. 그때까지는 전이 사실만 남겨, 어떤 정책이 언제 현행이 됐는지가 발행 지점 밖에서도 관측 가능하게 한다.

#### `SpringDomainEventPublisher` — 발행 포트 어댑터

**대상**: `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shared/event/SpringDomainEventPublisher.java`

domain의 `DomainEventPublisher` 포트를 Spring `ApplicationEventPublisher`에 위임한다. `@TransactionalEventListener`/`@EventListener` 기반 리스너가 그대로 수신한다.

#### 테이블명이 소유 컨텍스트와 어긋나는 자리

→ `StorePriceVerificationJpaEntity` · `StorePriceVerificationItemJpaEntity`

애그리거트가 product 컨텍스트로 옮겨졌어도 **테이블명의 `SHOP_` 접두를 유지한다** — 요청이 가게 단위로 접수되기 때문이며 소유 컨텍스트와는 별개다. 이름을 바꾸지 않으므로 `@Table(name = ...)`로 명시 매핑한다.

#### 채널 어휘로 통일된 테이블·인덱스명

→ `MailVerificationJpaEntity` · `SmsVerificationJpaEntity`

테이블·인덱스명은 채널 도메인 어휘에 맞춰 `MAIL_VERIFICATION`·`SMS_VERIFICATION`과 `idx_mail_verification_*`·`idx_sms_verification_*`로 통일돼 있다(`alter.sql`의 RENAME 마이그레이션). **`ddl-auto=validate` 환경이므로 그 마이그레이션과 앱 배포는 원자적으로 수행해야 한다.** 반면 `email`·`phone_number` 컬럼은 채널이 아니라 검증 대상 값 자체라 이름을 유지한다.

#### 그 밖에 근거를 남길 자리

- **`CeoReplyPhraseJpaEntity.content`가 `TEXT`가 아니라 `VARCHAR(1000)`인 이유** — 상한 1,000자가 확정돼 있어 DB가 직접 보증한다. `schema.sql`의 길이와 일치시킨다.
- **`MenuReviewJpaEntity`에 `review_id` 컬럼이 없는 것은 의도적이다** — 메뉴 평가는 매장 리뷰를 참조하지 않는다. 작성 근거는 `UNIQUE(order_product_id)`가 주문 항목당 1건을 물리적으로 보증한다.
- **`ProductOptionGroupMergeExclusionJpaEntity`** — `group_signature`는 SHA-256 hex 64자 고정이라 `CHAR(64)`다. `UNIQUE(shop_id, group_signature)`가 재클릭 멱등성을 물리적으로 보장하며, 같은 서명을 다시 제외하려는 요청은 서비스가 `findByShopIdAndGroupSignature`로 먼저 걸러 멱등하게 처리한다(유니크 제약이 최종 방어선).
- **`ProductPriceJpaEntity.pickup_price_set_at`을 별도 컬럼으로 두는 이유** — '매장가격 픽업' 뱃지가 **픽업가 설정 익일(영업일)** 부터 노출되기 때문이다. 감사 필드 `updated_at`으로 대체할 수 없다(가격명·정렬만 바뀌어도 갱신되므로 뱃지 노출 시점이 뒤로 밀린다).
- **`ProductOptionJpaEntity.cupCount`는 금액이 아니라 개수를 저장한다** — 요율(300원)이 바뀌어도 이 컬럼을 마이그레이션할 필요가 없다.
- **`ShopDeliveryTipScheduleJpaEntity.day_type`은 `DayType`을 재사용하되 `HOLIDAY`는 저장되지 않는다** — 공휴일은 전용 애그리거트가 담당하며 그 금지는 도메인 모델 `ShopDeliveryTipSchedule#of`가 강제한다.
- **`ProductImageRepositoryImpl#save`에 갱신 분기가 생긴 경위** — 과거에는 무조건 insert였다(기존 행을 갱신하는 경로가 없었다). 이미지 순서 변경이 생기면서 detached 인스턴스를 그대로 `save`하면 감사 필드가 파손되고 새 행이 중복 생성되므로 분기를 뒀다. `ProductImageJpaRepository#findAllByProductIdOrderBySortAsc`가 정렬을 보장하는 것도 순서 변경(replace-all)과 "맨 뒤 sort" 산출이 집합 전체를 보기 때문이다.
- **`StorePriceVerificationRepositoryImpl`이 두 JPA 리포지토리를 감싸는 이유** — 요청 본체와 항목이 같은 애그리거트 경계에서 함께 저장·조회되므로 한 포트(한 구현)가 담당한다. 항목은 접수 시 한 번 저장되고 변경되지 않으므로 update 분기가 없고, 별도 조회 경로(`findAllItemsByVerificationId`)가 있어 저장 결과를 반환하지 않는다.
- **`UploadedFileRepositoryImpl`은 순수 pass-through이며 update 경로가 없다** — 표현 목적 파일 조회(응답 URL)는 이 어댑터를 거치지 않고, 각 도메인 query DAO가 `uploaded_file`을 join하고 `FileUrlResolver`가 URL로 변환한다.
- **`ShopOwnerMessageHistoryJpaRepository#findFirstByShopIdOrderByIdDesc`** — append-only 이력이라 최신 행이 곧 현재 노출 문구다.
- **`SearchKeywordLogJpaRepository`** — 키워드별 검색 수 집계는 타입 없는 `Object[]` 튜플을 돌려주던 네이티브 쿼리 대신 `SearchQueryDao`의 QueryDSL 투영이 담당한다.
- **`RecommendedKeywordJpaEntity`에 도메인 모델·write 포트·매퍼를 두지 않는다** — 조회 경로가 CQRS query 측으로 이관돼 이 엔티티에서 Result DTO로 직접 투영하므로 전부 미사용이 되어 제거됐다.
- **`PublicHolidayRepositoryImpl`·`AdminDong` 캘린더는 read-only 마스터라 저장·삭제 경로가 없다** — 캘린더는 `insert.sql` 시드가 소유한다.
