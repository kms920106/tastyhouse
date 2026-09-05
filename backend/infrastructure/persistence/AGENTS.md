<!-- Parent: ../../AGENTS.md -->

# infrastructure:persistence

> **경로 이동 (챕터 05)**: 이 모듈은 `infrastructure-module/`에서 **`infrastructure/persistence/`로 이동**했고 Gradle 좌표는 `:infrastructure:persistence`다. 자바 패키지(`com.tastyhouse.infrastructure..`)·클래스명(`InfrastructureModuleConfig`·`InfrastructurePersistenceConfig`)·`application-infrastructure.yml`은 **전부 불변**이므로, 아래 본문의 패키지 경로는 그대로 유효하다. 형제 모듈 `infrastructure:redis`가 Redis를(`../redis/AGENTS.md`), `infrastructure:external`이 외부 연동 코어를(`../external/AGENTS.md`) 소유하고, 실제 외부 어댑터는 `infrastructure:{firebase,aws,oauth,payment,messaging,crawling}`이 기술별로 나눠 갖는다 — 전부 driven 어댑터다.
>
> 재편 이유는 `infrastructure` 아래를 **기술별로** 나누기 위해서다 — 모듈 이름이 곧 "infrastructure = DB"라는 암묵 전제가 되지 않게 한다.

`domain-module`의 순수 도메인 모델을 영속화하고, 읽기 계약 패키지 `com.tastyhouse.application..port.out`이 선언한 읽기 포트를 구현하는 **인프라 어댑터 모듈**. 헥사고날 아키텍처에서 `domain-module`이 선언한 포트(`<ctx>/repository/XxxRepository` write 포트, `shared/event/DomainEventPublisher`)를 JPA/QueryDSL/Spring으로 구현하고, 그 읽기 포트(`{Ctx}QueryPort`)도 함께 구현한다. 외부 연동 모듈들이 파일/OAuth/PG 어댑터를 담당하는 것과 같은 원리로 DB 어댑터를 domain 밖으로 분리해 "domain은 프레임워크를 모른다"를 모듈 경계로 강제한다.

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

- **패키지 루트는 `com.tastyhouse.infrastructure`** — web/admin/ceo/batch의 `scanBasePackages`에 이 패키지가 등록되어 있어야 빈(RepositoryImpl·QueryDao·Listener·Config)이 인식된다. JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)뿐 아니라 **JPA Auditing(`@EnableJpaAuditing`)·트랜잭션 관리(`@EnableTransactionManagement`) 전역 설정도 이 모듈의 `InfrastructurePersistenceConfig`가 `basePackageClasses`(타입 세이프)로 스스로 선언**한다. domain-module은 이 모듈을 의존하지 않아 컴파일 타임에 이 패키지를 볼 수 없으므로, 엔티티·리포지토리를 소유한 모듈이 스스로 선언하는 것이 Spring Boot 공식 권장과 일치한다.
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
  - 과거에는 모듈 루트의 `DomainServiceConfig` 하나가 17개 컨텍스트의 `@Bean` 55개를 전부 조립했으나(959줄), 모든 도메인 작업이 이 한 파일을 수정해 리포지토리에서 가장 충돌이 잦은 파일이 되어 컨텍스트별로 분할했다. `InfrastructureModuleConfig`가 `com.tastyhouse.infrastructure` 전체를 `@ComponentScan`하므로 앱 쪽 변경 없이 자동 등록된다.
  - **빈 이름(= `@Bean` 메서드명)은 바꾸지 않는다** — `@Qualifier` 참조가 깨질 수 있다.
  - **컨텍스트 분류가 애매한 빈**(여러 컨텍스트 서비스를 파라미터로 받는 것)은 **반환 타입이 속한 컨텍스트**의 config에 둔다.
  - member의 하위 컨텍스트(`follow`·`referral`) 빈은 `member/config/MemberDomainConfig`에 함께 둔다(별도 파일로 쪼개지 않음).
  - **모듈 진입점인 `InfrastructureModuleConfig`·`InfrastructurePersistenceConfig`는 모듈 루트에 그대로 둔다**(apps가 `@Import`하므로 경로 변경 금지). `<ctx>/config/` 규칙은 신설 도메인 서비스 config에만 적용된다.
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

> **`ReadContractSingleOwnerTest`는 챕터 04에서 삭제됐다.** 같은 FQCN이 두 모듈에 정의되는 것을 막던 가드인데, 공유 계약 55개가 `domain-module`에서 `application`으로 돌아오며 split package 자체가 사라졌다. 이제 같은 모듈 안의 중복 정의는 컴파일 에러라 가드가 필요 없다.

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

이 모듈이 실제로 구현·소비하는 datasource/hibernate(`ddl-auto`)/mysql driver/`spring.sql.init` 등 JPA·DB 설정을 이 모듈의 `application-infrastructure.yml`이 소유한다(과거 `core-module`의 `application-core.yml`이었으나, 도메인 모듈이 JPA-free로 전환되며 이 모듈로 이동·리네이밍됨). 실행 모듈(`web-api`/`admin-api`/`ceo-api`/`batch-module`)의 `application.yml`이 `spring.config.import: classpath:application-infrastructure.yml`로 로딩하며, 이는 외부 연동 모듈이 각자 소유하는 `application-external.yml`(`infrastructure:external`)·`application-firebase.yml`(`infrastructure:firebase`)·`application-payment.yml`(`infrastructure:payment`)·`application-messaging.yml`(`infrastructure:messaging`)·`application-crawling.yml`(`infrastructure:crawling`)·`application-aws.yml`(`infrastructure:aws`)과, `application-redis.yml`(`infrastructure:redis` 소유)·`application-logging.yml`(logging-module 소유)과 동일한 패턴이다.

## Dependencies

### Internal
- `domain-module` (api) — 도메인 모델·write 포트·출력 포트·`shared/page`·`shared/event`·`shared/exception`·`exception` 참조
- `application` (implementation) — 읽기 계약(`{Ctx}QueryPort`·Result·SearchCondition)을 구현·투영하기 위해 의존한다(QueryDao가 그 인터페이스를 `implements`). 챕터 04로 공유 계약 55개까지 이 모듈로 돌아와, 읽기 계약은 전부 이 한 의존으로 보인다

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (**implementation** — 소비 모듈 전이 차단. OpenFeign 포크는 CVE-2024-49203 대응이며 패키지명 `com.querydsl.*` 유지, 6.x부터 jpa는 `:jakarta` classifier 없이 jakarta 기본·apt만 `:jakarta` 유지) + `querydsl-apt` annotationProcessor

<!-- MANUAL: -->
