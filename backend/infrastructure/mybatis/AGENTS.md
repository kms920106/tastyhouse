<!-- Parent: ../../AGENTS.md -->

# infrastructure:mybatis

> **(번복됨 — notice-mybatis-legacy)** 이 모듈은 원래 banner(배너) 쓰기 포트의 MyBatis 파일럿 구현을 담았다. notice-mybatis-legacy 작업에서 banner MyBatis 코드를 전부 삭제하고(banner는 JPA 구현 하나로 원복), **notice(공지사항) 레거시 MyBatis 구현(쓰기 + 조회)** 으로 교체했다. 아래 본문은 교체 후 기준으로 다시 썼다. 교체 전후 대조는 [교체 이력](#교체-이력-notice-mybatis-legacy) 절에 있다.

**MyBatis로 구현한 영속 어댑터를 담는 모듈(`java-library`).** 지금은 notice 컨텍스트의 아웃바운드 포트 4개 — 쓰기 `NoticeLoadPort`·`NoticeSavePort`, 조회 `NoticeQueryPort`·`NoticeManagementQueryPort` — 의 MyBatis 구현이 있다. 같은 포트 4개를 `infrastructure:jpa`의 JPA 구현(`NoticeJpaPersistenceAdapter`·`NoticeJpaQueryAdapter`)도 갖고 있다. 두 기술의 구현은 조건 없이 모두 빈으로 등록되고, **`@Primary`가 붙은 JPA 구현이 주입된다** — 이 모듈의 구현은 web-api·admin-api에 함께 뜨지만 쓰이지 않는다.

notice는 예전에 MyBatis로 구현돼 있었고 지금은 JPA만 쓴다. 이 모듈은 그 **레거시 구현을 지우지 않고 남겨 둔 것**이다 — 포트가 바뀌면 컴파일 에러로 드러나 함께 유지되고, 필요하면 `@Primary` 한 줄을 옮겨 되돌릴 수 있다.

## 용어 풀이

- **포트**: application 모듈의 인터페이스. 서비스는 이것만 안다. notice의 포트는 다음 4개다.
  - 쓰기: `application/.../notice/port/out/write/{NoticeLoadPort,NoticeSavePort}`(`findActiveById`, `save`) — 도메인 모델 `Notice`를 주고받는다.
  - 조회: `application/.../notice/port/out/{NoticeQueryPort,NoticeManagementQueryPort}`(`findVisibleNotices`, `findAllNotices`·`findDetailById`) — 도메인을 모르는 `*Result` record를 돌려준다.
- **어댑터**: 포트를 실제 기술로 구현한 클래스. 이 모듈의 `NoticeMyBatisPersistenceAdapter`(쓰기)와 `NoticeMyBatisQueryAdapter`(조회)가 MyBatis로 구현한다.
- **JPA(ORM)**: 자바 객체(엔티티)를 테이블에 자동으로 대응시킨다. SQL을 직접 쓰지 않고, 엔티티 값을 바꾸면 트랜잭션 끝에 UPDATE가 나간다(변경 감지).
- **MyBatis(SQL 매퍼)**: SQL을 XML에 직접 쓰고, 결과 행을 자바 객체로 옮긴다. 무엇이 실행되는지 SQL 그대로 보인다.
- **1차 캐시**: JPA가 한 트랜잭션 안에서 로드한 엔티티를 기억하는 공간. MyBatis는 이것을 모르므로 같은 행을 두 기술로 섞어 쓰면 값이 어긋날 수 있다 — 이 모듈은 `@Primary`로 포트마다 한쪽만 주입받고 한 포트의 메서드를 두 기술로 나누지 않아 이를 피한다.
- **`@Primary`**: 같은 타입의 빈이 여러 개일 때, 하나만 주입받는 자리(생성자 인자 `NoticeLoadPort noticeLoadPort` 등)에 어느 빈을 넣을지 지정하는 Spring 애노테이션. 여러 빈을 모으는 `List<NoticeLoadPort>` 주입에는 적용되지 않아 두 구현이 모두 들어간다(현재 그런 주입 0건).
- **LIKE 이스케이프**: `LIKE '%검색어%'`에서 검색어 안의 `%`·`_`는 와일드카드로 해석된다. 사용자가 입력한 `50%`를 글자 그대로 찾으려면 앞에 이스케이프 문자(이 저장소는 `!`)를 붙이고 SQL에 `ESCAPE '!'`를 적어야 한다.

## 왜 이 모듈이 있나

1. **포트는 그대로 두고 구현 기술만 바꿀 수 있다는 것을 실제 코드로 보이기 위해서다.** notice 서비스(web `NoticeListQueryService`, admin `NoticeCreateService`·`NoticeUpdateService`·`NoticeDeleteService`·`NoticeManagementListQueryService`·`NoticeManagementDetailQueryService`)는 JPA든 MyBatis든 한 줄도 바뀌지 않는다.
2. **레거시 구현을 지우지 않고 유지하기 위해서다.** 비활성 구현도 포트를 `implements`하므로 포트가 바뀌면 컴파일 에러로 함께 고쳐진다. 되돌릴 때는 `@Primary` 한 줄을 옮기고 재빌드한다.
3. **MyBatis를 필요한 앱에만 싣기 위해서다.** 처음 파일럿에서는 MyBatis가 `infrastructure:persistence` 안에 있어 4앱 전부에서 MyBatis 자동 설정이 켜졌다. 모듈을 나누면서 이 모듈을 의존하는 앱에서만 켜진다(backend/CLAUDE.md "클래스패스 존재 = 활성화"). notice를 쓰는 앱이 web(공개 목록)과 admin(관리 CRUD)이므로 **web-api·admin-api 두 앱**이 의존한다. ceo·batch에는 공지 기능이 없어 싣지 않는다.

참고한 구조: board-project(헥사고날 멀티모듈 예제 프로젝트)의 `adapter-out-persistence-jpa`·`-mybatis` 분리. 다른 점은 board-project가 포트마다 한 기술만 구현(JPA=쓰기, MyBatis=조회)하는 반면, 여기서는 **같은 포트를 두 모듈이 모두 구현**하고 `@Primary`로 고른다는 것이다. JPA 모듈 `infrastructure:jpa`에 어댑터 100여 개가 함께 있어 web-api·admin-api가 그 모듈을 뺄 수 없기 때문이다.

## 패키지 구조

```
com.tastyhouse.infrastructure.mybatis/
├── MyBatisModuleConfig.java                  @MapperScan(basePackageClasses = MyBatisModuleConfig.class, annotationClass = Mapper.class)
└── notice/
    ├── persistence/                          쓰기 — 도메인 모델 Notice를 쓴다
    │   ├── NoticeMyBatisPersistenceAdapter   NoticeLoadPort·NoticeSavePort 구현 — 조건 없이 등록, @Primary 없음(주입되지 않음)
    │   ├── NoticeMyBatisMapper               MyBatis SQL 인터페이스(@Mapper): selectActiveById · insert · update
    │   ├── NoticeRow                         조회 행(record, XML <constructor>의 이름 기반 매핑)
    │   ├── NoticeWriteRow                    쓰기 행(INSERT 생성 키를 setId로 돌려받는다)
    │   └── NoticeRowMapper                   행 ↔ 도메인 변환
    └── query/                                조회 — 도메인을 모른다
        ├── NoticeMyBatisQueryAdapter         NoticeQueryPort·NoticeManagementQueryPort 구현 — 조건 없이 등록, @Primary 없음
        │                                     (containsPattern: !·%·_ 이스케이프 + Locale.ROOT 소문자화)
        └── NoticeQueryMyBatisMapper          MyBatis SQL 인터페이스(@Mapper): countVisible · selectVisible ·
                                              countManagement · selectManagement · selectDetailById (*Result record 직접 반환)
src/main/resources/
├── application-mybatis.yml                   mybatis.mapper-locations: classpath*:mapper/**/*.xml
└── mapper/notice/
    ├── NoticeMyBatisMapper.xml               namespace = …mybatis.notice.persistence.NoticeMyBatisMapper
    └── NoticeQueryMyBatisMapper.xml          namespace = …mybatis.notice.query.NoticeQueryMyBatisMapper
```

- 루트 패키지는 `com.tastyhouse.infrastructure.mybatis`다(backend/CLAUDE.md "infrastructure 패키지 규칙").
- 컨텍스트 아래를 jpa 모듈처럼 `{ctx}.{persistence|query}`로 나눈다. 쓰기와 조회가 함께 있기 때문이다. 두 패키지의 경계는 `LayerRulesTest`가 강제한다(아래 봉인 목록).
- 이름 규칙(backend/CLAUDE.md "아웃바운드 포트·어댑터 네이밍 규칙"의 예외 표):
  - 이 저장소에서 `XxxMapper`는 도메인 변환기다. 그래서 MyBatis SQL 인터페이스는 `XxxMyBatisMapper`, 행 ↔ 도메인 변환기는 `XxxRowMapper`로 짓는다.
  - 같은 포트를 JPA 구현과 함께 가지므로 어댑터에 기술 한정어를 붙인다 — 쓰기 `NoticeMyBatisPersistenceAdapter`, 조회 `NoticeMyBatisQueryAdapter`(`{Ctx}{기술}QueryAdapter`).
  - 조회 SQL 인터페이스는 `NoticeQueryMyBatisMapper`(`{Ctx}QueryMyBatisMapper`)다. 어댑터와 어순이 다른 이유는 `MyBatisMapper` 접미어를 유지해 도메인 변환기 `…Mapper`와 한눈에 구분하기 위해서다.
- 조회 쪽은 `Row` 중간 타입 없이 application의 `*Result` record(`NoticeListItemResult`·`NoticeManagementListItemResult`·`NoticeDetailResult`)를 XML `<constructor>`로 직접 만든다. 조회 어댑터는 도메인을 모르므로 변환할 도메인 모델이 없다.

## 전환 방법

| 하고 싶은 것 | 방법 |
|---|---|
| MyBatis로 바꾸기 | `@Primary`를 JPA 어댑터 2개(`backend/infrastructure/jpa/src/main/java/com/tastyhouse/infrastructure/jpa/notice/persistence/NoticeJpaPersistenceAdapter.java`·`.../notice/query/NoticeJpaQueryAdapter.java`)에서 MyBatis 어댑터 2개(`NoticeMyBatisPersistenceAdapter`·`NoticeMyBatisQueryAdapter`)로 옮긴다. **등록 테스트 4개의 기대값도 함께 뒤집는다** — jpa 모듈의 `NoticeJpaPersistenceAdapterRegistrationTest`·`NoticeJpaQueryAdapterRegistrationTest`, 이 모듈의 `NoticeMyBatisPersistenceAdapterRegistrationTest`·`NoticeMyBatisQueryAdapterRegistrationTest`. 그다음 web-api·admin-api를 재빌드한다(`내리기 → 빌드 → 다시 띄우기`) |
| JPA로 되돌리기 | `@Primary`와 테스트 기대값을 원래대로 돌리고 재빌드 |
| 쓰기만 또는 조회만 옮기기 | 가능하다. 포트 묶음(쓰기 2개 / 조회 2개) 단위로 `@Primary`가 갈린다. 단, 한 포트 묶음 안의 메서드를 두 기술로 나누지 않는다(1차 캐시 문제) |
| 둘 다 붙이거나 둘 다 떼기 | 컨텍스트는 뜨지만 그 포트를 주입하는 시점에 실패한다 → admin-api 가드 `PersistencePrimaryRulesTest`가 빌드 단계에서 막는다 |

MyBatis 구현은 이 모듈을 의존하는 앱에서만 쓸 수 있다. 지금은 **web-api·admin-api 두 앱**이다(각 앱 `build.gradle`의 `runtimeOnly project(':infrastructure:mybatis')` + `application.yml`의 `classpath:application-mybatis.yml` import).

## JPA 구현과의 비교 (notice)

### 쓰기 (`NoticeLoadPort`·`NoticeSavePort`)

| 항목 | JPA (`infrastructure:jpa`) | MyBatis (이 모듈) |
|---|---|---|
| 클래스 | `NoticeJpaPersistenceAdapter` + `NoticeJpaMapper` + `NoticeJpaRepository` + `NoticeJpaEntity` | `NoticeMyBatisPersistenceAdapter` + `NoticeRowMapper` + `NoticeMyBatisMapper` + `NoticeRow`/`NoticeWriteRow` + XML |
| `findActiveById` | QueryDSL `selectFrom … where id = ? and deleted = false` | XML `selectActiveById`(`WHERE id = ? AND is_deleted = false` — 같은 조건) |
| 신규 저장 | `noticeJpaRepository.save(entity)` — IDENTITY 키를 엔티티가 받음 | `insert`(`useGeneratedKeys`) — 키를 `NoticeWriteRow#setId`로 받음 |
| 수정 저장 | `findById`(삭제 여부와 무관)로 managed 엔티티를 로드해 값 복사 → 변경 감지로 UPDATE | `UPDATE NOTICE SET … WHERE id = ?` 한 문장. `created_at`은 SET에서 제외. `is_deleted` 조건을 걸지 않는다(JPA가 삭제 여부와 무관하게 `findById`를 쓰기 때문) |
| 없는 id 수정 | `IllegalStateException("존재하지 않는 공지사항입니다: …")` | 영향 행 0이면 같은 예외·같은 메시지 |
| `created_at`/`updated_at` | `BaseEntity`의 감사(auditing)가 앱 시각으로 채움 | 어댑터가 `LocalDateTime.now()`로 채움 |
| 값이 안 바뀐 저장 | UPDATE 생략 → `updated_at` 그대로 | 항상 UPDATE → `updated_at` 갱신 (**알려진 의미 차이**) |

### 조회 (`NoticeQueryPort`·`NoticeManagementQueryPort`)

| 항목 | JPA (`NoticeJpaQueryAdapter`) | MyBatis (`NoticeMyBatisQueryAdapter` + `NoticeQueryMyBatisMapper`) |
|---|---|---|
| 공개 목록 `findVisibleNotices` | `deleted = false AND visible = true`, `id DESC`, offset/limit | `countVisible`·`selectVisible` — 같은 조건, `ORDER BY id DESC LIMIT … OFFSET …` |
| 관리 검색 `findAllNotices` | `titleContains`/`contentContains`(`containsIgnoreCase` → `lower(col) like ? escape '!'`), `visibleEq` | 어댑터의 `containsPattern`이 `!`·`%`·`_` 앞에 `!`를 붙이고 `Locale.ROOT`로 소문자화한 `%…%` 패턴을 만들고, XML이 `LOWER(col) LIKE #{pattern} ESCAPE '!'`로 쓴다. 빈 문자열·공백만 있는 검색어는 조건 생략(`StringUtils.hasText`) — JPA와 같다 |
| 관리 상세 `findDetailById` | `id = ? AND deleted = false` | `selectDetailById` — 같은 조건 |
| 결과 매핑 | `Projections.constructor(*Result.class, …)` | XML `resultMap`의 `<constructor>`(이름 기반)로 `*Result` record 직접 생성 |
| 실행 SQL 확인 | p6spy에 엔티티 별칭(`nje1_0.` 등)이 붙은 SQL | p6spy에 XML 그대로 `SELECT id, title, content, …` |

### 기술 일반

| 항목 | JPA | MyBatis |
|---|---|---|
| 장점 | 엔티티만 고치면 SQL이 따라옴, 스키마 검증(`ddl-auto: validate`) | SQL이 눈에 보이고 복잡한 쿼리를 그대로 쓸 수 있음 |
| 단점 | 변경 감지·1차 캐시를 알아야 함 | 감사 필드·소프트 삭제 조건·LIKE 이스케이프를 직접 써야 함, 스키마 검증 없음 |
| 런타임 실행 여부 | 실제로 실행됨(`@Primary`) | **실행되지 않음.** XML 정합성은 `*MapperXmlTest`가 빌드 시점에 검증하지만, 컬럼 의미 오류처럼 DB에서만 드러나는 문제는 DB 통합 테스트가 없어 잡히지 않는다 |

## 배선

- `MyBatisModuleConfig`(`@Configuration(proxyBeanMethods = false)`, `@ComponentScan` 없음)는 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 등록된다. `@MapperScan`은 지우지 않는다 — MyBatis 기본 매퍼 스캔은 앱의 auto-config 패키지(`com.tastyhouse.webapi`·`com.tastyhouse.adminapi`)만 본다.
- 트랜잭션 매니저를 따로 만들지 않는다. 앱의 `JpaTransactionManager`가 같은 DataSource 커넥션을 바인딩하므로 MyBatis가 서비스의 `@Transactional`에 그대로 참여한다.
- `mapper-locations`는 `classpath*:`여야 한다. `classpath:`는 첫 `mapper/` 디렉터리 하나만 찾아, 앞선 jar에 같은 디렉터리가 생기면 부팅은 성공하고 첫 쿼리에서 `BindingException`이 난다.

## Dependencies

### Internal
- `domain` (implementation) — 도메인 모델 `Notice`·VO `NoticeId`(쓰기 어댑터만 쓴다)
- `application` (implementation) — 구현할 포트 4개와 조회 결과 `*Result`·`NoticeSearchCondition`·`PageQuery`·`PageResult`(`..port.out..`만 참조)

### External
- `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3` (implementation — Spring Boot 3.2.x 호환 라인)
- MySQL 드라이버는 이 모듈이 선언하지 않는다 — DB 연결 코어 `infrastructure:mysql`이 소유하고, 조립 모듈 `infrastructure:persistence`가 그것을 `runtimeOnly`로 싣는다(JPA·MyBatis 공용 드라이버라 어느 한 구현 모듈의 것이 아니다. `../mysql/AGENTS.md`). web-api·admin-api가 persistence를 의존하므로 이 모듈과 함께 실린다.
- datasource(`spring.datasource.*`, 커넥션 풀 포함)는 `infrastructure:mysql`의 `application-mysql.yml`이 소유한다. MyBatis도 JPA와 같은 Hikari 풀을 쓴다. 이 모듈의 `application-mybatis.yml`은 MyBatis 자체 설정만 갖는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### MyBatis 어댑터에 `@Primary`를 붙이지 않는다 — 등록 테스트 2개

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/notice/persistence/NoticeMyBatisPersistenceAdapterRegistrationTest.java` → `registersUnconditionallyAndServesBothPorts`·`yieldsToPrimaryImplementation` · `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/notice/query/NoticeMyBatisQueryAdapterRegistrationTest.java` → 같은 이름의 두 메서드 · `backend/infrastructure/mybatis/src/main/java/com/tastyhouse/infrastructure/mybatis/notice/persistence/NoticeMyBatisPersistenceAdapter.java` → 클래스 선언의 `implements NoticeLoadPort, NoticeSavePort`(`@Primary` 없음) · `backend/infrastructure/mybatis/src/main/java/com/tastyhouse/infrastructure/mybatis/notice/query/NoticeMyBatisQueryAdapter.java` → 클래스 선언의 `implements NoticeQueryPort, NoticeManagementQueryPort`(`@Primary` 없음)

원문 취지: 조건 없이 등록되고 같은 빈 하나가 묶음의 두 포트를 모두 구현한다 — 비활성 구현도 포트를 `implements`해야 포트가 바뀔 때 컴파일 에러로 드러난다(`implements`를 지우거나 주석 처리하지 않는다). `@Primary`가 붙은 다른 구현이 함께 있으면 그쪽이 주입된다 = MyBatis는 대표가 아니다. jpa 모듈의 `NoticeJpaPersistenceAdapterRegistrationTest`·`NoticeJpaQueryAdapterRegistrationTest`(`winsOverNonPrimaryImplementation`)와 짝이다. **이 두 클래스에 `@Primary`를 붙이지 않는다**(전환할 때만 JPA에서 옮겨 온다) — 둘 다 붙으면 primary 중복으로 주입이 실패한다(반증 확인: 이 테스트들과 admin-api `PersistencePrimaryRulesTest`가 실패한다). 단언은 `getBean` 시점에 둔다.

### `NoticeMyBatisMapperXmlTest` · `NoticeQueryMyBatisMapperXmlTest` — XML 정합성

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/notice/persistence/NoticeMyBatisMapperXmlTest.java` · `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/notice/query/NoticeQueryMyBatisMapperXmlTest.java` → `parsesWithMatchingConstructors`·`everyMapperMethodHasStatement`

원문 취지: MyBatis 구현은 `@Primary`가 아니라 런타임에 한 번도 실행되지 않으므로, XML 오류를 기동이나 요청이 대신 잡아 주지 않는다. 이 테스트는 `XMLMapperBuilder`로 DB 없이 XML을 파싱해 namespace가 매퍼 인터페이스와 맞는지, 매퍼 인터페이스의 모든 메서드에 같은 id의 statement가 있는지, `resultMap`의 `<constructor>`가 대상 record 생성자와 맞는지(미완성 resultMap·statement 0건)를 검증한다. 매퍼 메서드나 record 컴포넌트를 바꾸면 XML도 함께 고친다. SQL 의미(컬럼 이름 오타 등)는 검증하지 못한다.

### `NoticeMyBatisQueryAdapterTest` — LIKE 패턴 이스케이프

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/notice/query/NoticeMyBatisQueryAdapterTest.java` → `blankKeywordHasNoPattern`·`wrapsLowercasedKeyword`·`escapesWildcardsAndEscapeChar` · `NoticeMyBatisQueryAdapter` → `containsPattern`

원문 취지: JPA `containsIgnoreCase`와 같은 결과를 내기 위해 검색어의 `!`·`%`·`_` 앞에 이스케이프 문자 `!`를 붙이고 `Locale.ROOT`로 소문자화한다. XML의 `ESCAPE '!'`와 이스케이프 문자를 함께 바꿔야 한다(한쪽만 바꾸면 `50%` 검색이 모든 행에 걸린다).

### `architecture/PackageRootTest` · `architecture/LayerRulesTest`

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage` · `topLevelClassesShouldNotBePublic`, 같은 패키지 `LayerRulesTest.java` → `shouldNotDependOnApiModules` · `shouldNotDependOnOtherPersistenceAdapters` · `queryShouldNotDependOnDomain` · `persistenceShouldNotDependOnQuery`

원문 취지:
- 모든 클래스는 `com.tastyhouse.infrastructure.mybatis..`에 있고 최상위 클래스는 public이 아니다.
- `shouldNotDependOnApiModules`: application은 `..port.out..`만 참조한다(유스케이스를 침범하지 않는다).
- `shouldNotDependOnOtherPersistenceAdapters`: `com.tastyhouse.infrastructure.jpa..`를 의존하지 않는다 — 영속 어댑터 모듈끼리는 서로 모르고, "같은 포트의 JPA 구현과 MyBatis 구현은 @Primary로만 갈린다". 이 모듈의 클래스패스에 jpa 모듈이 없어 지금은 위반이 컴파일조차 안 되지만, 의존이 추가되는 회귀를 막는 방어선이라 지우지 않는다.
- `queryShouldNotDependOnDomain` (notice-mybatis-legacy 신설): `..query..`는 `com.tastyhouse.domain..`을 의존하지 않는다 — 조회 어댑터는 domain-free 읽기 계약만 구현하고, 도메인 모델을 쓰는 것은 영속 어댑터(`XxxMyBatisPersistenceAdapter`)뿐이다. jpa 모듈의 같은 규칙과 짝이다.
- `persistenceShouldNotDependOnQuery` (notice-mybatis-legacy 신설): `..persistence..`는 `..query..`를 의존하지 않는다 — write 어댑터는 read model을 의존하지 않는다(read → write 단방향).

## 교체 이력 (notice-mybatis-legacy)

| 항목 | before (banner 파일럿) | after (notice 레거시) |
|---|---|---|
| 구현 대상 포트 | `BannerLoadPort`·`BannerSavePort` (쓰기 2개) | `NoticeLoadPort`·`NoticeSavePort`·`NoticeQueryPort`·`NoticeManagementQueryPort` (쓰기 2개 + 조회 2개) |
| 패키지 | `mybatis.banner` (쓰기만 있어 나누지 않음) | `mybatis.notice.persistence` / `mybatis.notice.query` |
| 클래스 | `BannerMyBatisPersistenceAdapter`·`BannerMyBatisMapper`·`BannerRow`·`BannerWriteRow`·`BannerRowMapper` | 위 [패키지 구조](#패키지-구조)의 7개 |
| XML | `mapper/banner/BannerMyBatisMapper.xml` | `mapper/notice/NoticeMyBatisMapper.xml`·`mapper/notice/NoticeQueryMyBatisMapper.xml` |
| JPA 짝 | `BannerJpaPersistenceAdapter`(`@Primary`) | `NoticeJpaPersistenceAdapter`·`NoticeJpaQueryAdapter`(둘 다 `@Primary`). banner JPA는 `BannerPersistenceAdapter`로 이름을 되돌리고 `@Primary`를 뗐다(구현이 하나라 한정어가 필요 없다) |
| 의존 앱 | admin-api | web-api·admin-api |
| 가드 | 등록 테스트 1개, `LayerRulesTest` 2규칙 | 등록 테스트 2개, XML 파싱 테스트 2개, 패턴 테스트 1개, `LayerRulesTest` 4규칙 |

banner 파일럿 시절의 이전 이력(`persistence.banner.write.provider` 키로 하나만 등록하던 방식 → banner-write-primary에서 `@Primary`로 전환)은 `docs/tasks/README.md`의 `banner-write-provider-switch`·`banner-write-primary` 행에 있다. 동작 원리(조건 없이 둘 다 등록, `@Primary`가 주입 대상을 고름)는 그대로 이어받았다.
