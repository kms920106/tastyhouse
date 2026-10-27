<!-- Parent: ../../AGENTS.md -->

# infrastructure:persistence

DB 영속 계층을 **한 벌로 묶어 앱에 노출하는 조립 모듈(스타터)**(`java-library`). **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-persistence.yml`) 둘뿐이다. 형태는 `infrastructure:file-storage`(→ firebase)·`infrastructure:pg`(→ tosspayments)와 같다.

> **용어**
> - **조립 모듈(스타터)**: 도메인 포트를 하나도 구현하지 않고, "이 앱에 어떤 구현 모듈을 싣는가"만 선언하는 모듈. `spring-boot-starter-data-jpa`가 Hibernate를 골라 싣는 것과 같은 역할이다.
> - **구현 모듈**: 포트를 실제 기술로 구현한 모듈. DB 쪽은 `infrastructure:jpa`(JPA·QueryDSL — 쓰기 어댑터 + 조회 DAO 전부)와 `infrastructure:mybatis`(notice 쓰기·조회 MyBatis 레거시 구현 — JPA가 `@Primary`라 주입되지 않는다) 둘이다.
> - **DB 연결 코어**: `infrastructure:mysql` — MySQL 드라이버·커넥션 풀(HikariCP)·접속 설정을 소유한다. 구현 모듈이 아니라 그 아래에서 JPA·MyBatis가 함께 쓰는 DataSource를 만든다.
> - **`@Primary`**: 여러 후보 빈 중 단일 주입 때 우선 선택되는 쪽을 표시하는 Spring 애노테이션. 같은 포트의 구현이 여럿이면 모두 빈으로 등록되고 `@Primary`가 붙은 구현이 주입된다. 이 모듈은 그 선택에 관여하지 않는다.

## 왜 코드 없는 모듈이 됐나 (jpa 모듈 분리)

이 모듈은 원래 JPA·QueryDSL 코드 전부(main 569개, test 포함 675개 java)를 가진 driven 어댑터였다. 그 코드는 신설 모듈 `infrastructure:jpa`로 `git mv` 됐고, 이 모듈에는 조립 선언만 남았다. 구현 모듈의 설계·규칙·가드는 전부 `../jpa/AGENTS.md`가 소유한다.

| 항목 | before | after (현행) |
|---|---|---|
| 자바 코드 | JPA 엔티티·어댑터·조회 DAO·가드 테스트 전부 | **없음** (테스트·ArchUnit도 없음) |
| `build.gradle` 의존 | `:domain`·`:application-core`·`spring-boot-starter-data-jpa`·QueryDSL·`mysql-connector-j` | **`runtimeOnly project(':infrastructure:jpa')` + `runtimeOnly project(':infrastructure:mysql')` 두 줄** (mysql 모듈 분리 전에는 두 번째 줄이 `runtimeOnly 'com.mysql:mysql-connector-j'`) |
| `application-persistence.yml` | datasource + `spring.jpa.*` + 구현 선택 키 + 로그 레벨 한 벌 | `application-mysql.yml`·`application-jpa.yml` import만 (`spring.jpa.*`는 jpa 모듈로, datasource·`spring.sql.init`은 mysql 모듈로) |
| 설정 파일명 | `application-infrastructure.yml` (모듈명과 불일치) | **`application-persistence.yml`** — 다른 모듈(`redis`·`mysql`·`jpa` 등)처럼 `application-{모듈명}.yml`로 정렬. 앱 4개의 import 줄만 바뀌었고 동작은 불변 |
| 자바 패키지 | `com.tastyhouse.infrastructure.persistence..` | 없음 — 코드가 `com.tastyhouse.infrastructure.jpa..`로 갔다 |
| 앱 4개(web·admin·ceo·batch) | `runtimeOnly project(':infrastructure:persistence')` + `classpath:application-persistence.yml` import | **불변** — 동작도 불변 |

앱 쪽이 바뀌지 않은 것이 이 형태의 이점이다. 앱은 "DB에 저장한다"까지만 알고 "JPA로", "MySQL로"는 모른다.

## mysql 모듈 분리 — 드라이버·datasource를 `infrastructure:mysql`로

`infrastructure:redis`가 Redis 연결 설정을 `application-redis.yml`로 소유하듯, MySQL 접속 기술도 자기 모듈이 소유한다. 이 모듈은 jpa와 mysql을 묶는 조립 역할만 한다. 커넥션 풀 설정(HikariCP)과 그 근거는 `../mysql/AGENTS.md`가 소유한다([결정 0051](../mysql/AGENTS.md#decision-0051)).

| 항목 | before | after (현행) |
|---|---|---|
| MySQL 드라이버 | 이 모듈 `runtimeOnly 'com.mysql:mysql-connector-j'` | `infrastructure:mysql`이 선언, 이 모듈은 `runtimeOnly project(':infrastructure:mysql')` |
| `spring.datasource.{url,username,password,driver-class-name}` | 이 모듈 yml | `application-mysql.yml` (값 불변) |
| `spring.sql.init.mode: always` | 이 모듈 yml | `application-mysql.yml` (값 불변 — 초기화 스크립트가 없어 no-op) |
| 커넥션 풀 설정 | 없음(Hikari 기본값) | `application-mysql.yml`의 `spring.datasource.hikari.*` — **동작 변경** |
| 앱 4개 | — | 불변 — mysql 모듈을 직접 의존하지 않고 이 모듈을 거쳐 전이로 받는다 |

## 소유물

```
backend/infrastructure/persistence/
  build.gradle                                       runtimeOnly jpa + runtimeOnly mysql
  AGENTS.md
  src/main/resources/application-persistence.yml  아래 키 목록
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 리소스만 실린다. `bootJar` 비활성 + plain jar(실행 단위가 아니다).

**`application-persistence.yml`이 소유하는 키**

| 키 | 값 | 용도 |
|---|---|---|
| `spring.config.import` | `classpath:application-mysql.yml`, `classpath:application-jpa.yml` | 하위 모듈 설정 중첩 로드 — datasource·커넥션 풀은 mysql 모듈이, `spring.jpa.*`(`ddl-auto: validate`·naming·`open-in-view: false`)와 hibernate 로그 레벨은 jpa 모듈이 소유한다 |

## 왜 드라이버·datasource는 mysql 모듈에 두나

- **JPA·MyBatis가 같은 DataSource를 쓴다.** web-api·admin-api에서는 JPA(`infrastructure:jpa`)와 MyBatis(`infrastructure:mybatis`)가 같은 커넥션 풀을 공유하고, `JpaTransactionManager` 하나가 두 기술을 같은 트랜잭션으로 묶는다. 드라이버·접속 정보는 어느 한 구현 모듈의 것이 아니므로, 두 구현 아래의 DB 연결 코어 `infrastructure:mysql`이 갖는다. 이 모듈이 mysql을 조립하므로 web-api·admin-api의 MyBatis도 같은 풀을 받는다.
- **이 모듈은 구현 선택 스위치를 갖지 않는다.** 같은 포트의 구현 선택은 설정이 아니라 코드(JPA 어댑터의 `@Primary`)가 한다 — 컨텍스트마다 설정 키·메타데이터·조건 애노테이션·조건 테스트를 반복하지 않기 위해서다([결정 0052](#decision-0052)). 규칙은 `backend/CLAUDE.md`의 "영속 포트 기술 중립 규칙", 현재 사례(notice 쓰기·조회)는 `../jpa/AGENTS.md`의 "notice 쓰기·조회 — JPA 구현과 MyBatis 구현의 공존"과 `../mybatis/AGENTS.md`에 있다.

## mybatis는 조립하지 않는다

이 모듈은 `infrastructure:jpa`·`infrastructure:mysql`만 `runtimeOnly`로 싣는다. `infrastructure:mybatis`는 **web-api·admin-api가 직접** `runtimeOnly`로 의존한다(각 앱 `application.yml`에 `classpath:application-mybatis.yml` import도 함께). MyBatis 자동 설정(`mybatis-spring-boot-starter`·`@MapperScan`·XML 파싱)은 필요한 앱(notice를 쓰는 web-api·admin-api)에서만 켜져야 하기 때문이다. 이 모듈에 mybatis를 넣으면 4앱 전부(ceo·batch 포함)의 클래스패스에 실려 그 판단이 무너진다("클래스패스 존재 = 활성화" — `backend/CLAUDE.md`).

## 구현 모듈을 바꾸거나 추가하는 절차

**두 곳을 항상 쌍으로 바꾼다.** 한쪽만 바꾸면 빈은 있는데 설정이 없거나(`spring.jpa.*` 누락 → 기본값으로 떠서 `ddl-auto` 검증이 꺼짐 등), 설정은 있는데 구현 빈이 없어 포트 주입이 실패한다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:jpa')` 옆에(또는 대신) `runtimeOnly project(':infrastructure:{새 구현}')`
2. `application-persistence.yml`의 `spring.config.import`: `classpath:application-jpa.yml` 옆에(또는 대신) `classpath:application-{새 구현}.yml`

DB 연결 코어(mysql)를 다른 DB로 바꿀 때도 같은 쌍이다 — `runtimeOnly project(':infrastructure:mysql')`과 `classpath:application-mysql.yml`을 함께 바꾼다.

같은 포트를 새 구현도 구현한다면 이 yml은 고치지 않는다. 두 구현 모두 포트를 `implements`하고 쓰는 쪽에만 `@Primary`를 붙인다(`backend/CLAUDE.md`의 "영속 포트 기술 중립 규칙"). 특정 앱에서만 켜야 하는 구현(mybatis처럼)은 여기가 아니라 그 앱의 `build.gradle`·`application.yml`에 둔다.

## 가드

**없다.** 코드 없는 스타터라 검사할 클래스가 없다 — `file-storage`·`pg`와 같은 상태다. 배선의 증명은 기동 성공이다: 앱이 이 모듈만 의존하는데 jpa 모듈이 전이로 실리지 않으면 write·read 포트 구현 빈이 없어 컨텍스트 로딩이 실패하고, mysql 모듈(드라이버·datasource 설정)이 빠지면 DataSource 생성에서 실패한다.

## Dependencies

### Internal
- `infrastructure:jpa` (**runtimeOnly**) — JPA 어댑터·조회 DAO 전부. `runtimeOnly`라 이 모듈을 `runtimeOnly`로 의존하는 앱의 `runtimeClasspath`에만 전이로 실리고, compileClasspath에는 나타나지 않는다(헥사고날 컴파일 게이트 유지).
- `infrastructure:mysql` (**runtimeOnly**) — MySQL 드라이버·HikariCP·`application-mysql.yml`. JPA·MyBatis 공용.

### 이 모듈을 의존하는 쪽
- `web-api`·`admin-api`·`ceo-api`·`batch-module` — 각자 `runtimeOnly project(':infrastructure:persistence')` + `application.yml`의 `classpath:application-persistence.yml` import.

## 결정 경위

이 문서가 소유한 규칙이 **왜 지금 모양이 됐는지**를 결정 단위로 적습니다. 본문은 현재 규칙만 담고, "예전에는 어땠고 왜 바뀌었는가"는 이 절에만 둡니다. 이 절은 항상 문서의 마지막 `##` 절이며, 항목은 번호 순입니다. 작성 규칙은 루트 `CLAUDE.md`의 "규칙 문서 개정 규칙" 절에 있습니다.

<a id="decision-0045"></a>

### 0045. banner 쓰기를 JPA·MyBatis 별도 모듈로 구현하고 provider 키로 하나만 등록한다

- 상태: 대체됨(→ [결정 0052](#decision-0052))
- 결정 커밋: `79cebac26` (2026-10-09)
- 대체한 결정: —
- 원문: 삭제 전 git 이력의 `docs/adr/0045-banner-mybatis-provider-pilot.md`

banner 쓰기를 JPA·MyBatis 두 모듈이 모두 구현하고, 설정 키로 하나만 빈으로 등록합니다.

| 항목 | before (파일럿) | after |
|---|---|---|
| MyBatis 어댑터 위치 | `infrastructure:persistence` 안 | 신설 모듈 `infrastructure:mybatis` (모듈 36 → 37) |
| JPA 쓰기 어댑터 | 삭제됨 | `BannerJpaPersistenceAdapter`로 복원 |
| 구현 선택 | 없음(MyBatis 하나) | `persistence.banner.write.provider=jpa\|mybatis`(기본 jpa) + 각 구현의 `@ConditionalOnProperty`. `matchIfMissing = true`는 기본 구현에만 |
| 잘못된 provider 값 | — | 구현 0개 → admin-api 기동 실패 |
| MyBatis 자동 설정이 켜지는 앱 | 4앱 전부 | admin-api만(`runtimeOnly :infrastructure:mybatis`) |
| 어댑터 이름 | `BannerPersistenceAdapter` / `BannerMapper` | 기술 한정어: `BannerJpaPersistenceAdapter` · `BannerMyBatisPersistenceAdapter`, 변환기 `BannerJpaMapper` · `BannerRowMapper` |
| provider 키 소유 | — | `application-persistence.yml` |

<a id="decision-0052"></a>

### 0052. 같은 포트를 여러 기술이 구현하면 둘 다 등록하고 쓰는 쪽에 @Primary를 단다(provider 키 삭제)

- 상태: 채택
- 결정 커밋: `f91ff6659` (2026-10-09)
- 대체한 결정: [결정 0045](#decision-0045)
- 원문: 삭제 전 git 이력의 `docs/adr/0052-banner-write-primary.md`

#### 맥락

- **`@Primary`**: 같은 타입의 빈이 여러 개일 때, 단일 주입 지점에 기본으로 들어갈 빈을 지정하는 Spring 애노테이션입니다.
- **`@ConditionalOnProperty`**: 설정 키 값에 따라 빈을 등록하거나 건너뛰는 조건 애노테이션입니다.

[결정 0045](#decision-0045)로 banner 쓰기는 JPA·MyBatis 두 구현 중 provider 키(`persistence.banner.write.provider`)로 하나만 빈으로 등록했습니다. 이 방식은 기술 구현을 추가하는 컨텍스트마다 yml·메타데이터·조건 애노테이션·조건 테스트를 한 묶음씩 반복해야 했습니다.

#### 이전 규칙

##### backend/CLAUDE.md — 영속 포트 기술 중립 규칙

"같은 포트를 여러 모듈이 구현하면 provider 속성으로 하나만 등록한다 — 각 구현 클래스에 `@ConditionalOnProperty(name = "persistence.{ctx}.write.provider", havingValue = "{기술}")`를 달고, `matchIfMissing = true`는 기본 구현에만 둔다. 선례: banner(`persistence.banner.write.provider=jpa|mybatis`, 기본 jpa). provider 키는 조립 모듈 `infrastructure:persistence`의 `application-persistence.yml`이 소유한다."

##### backend/infrastructure/jpa/AGENTS.md · persistence/AGENTS.md

- 조립 모듈 `application-persistence.yml`이 `persistence.banner.write.provider`(`${BANNER_WRITE_PROVIDER:jpa}`)를 가졌고, `META-INF/additional-spring-configuration-metadata.json`도 있었습니다.
- 조건 테스트 `BannerJpaProviderConditionTest`·`BannerMyBatisProviderConditionTest`가 provider 값별 등록을 검증했습니다.
- 메타데이터 파일 경로는 `infrastructure/persistence/src/main/resources/META-INF/additional-spring-configuration-metadata.json`이었고, provider 키 하나의 IDE 자동완성용이었습니다.
- persistence 문서의 "구현 모듈을 바꾸거나 추가하는 절차"는 "같은 포트를 새 구현도 구현한다면 provider 키(`persistence.{ctx}.write.provider`)를 이 yml에 추가하고, 기본 구현에만 `matchIfMissing = true`를 둔다"였습니다.

##### backend/CLAUDE.md — 영속 포트 기술 중립 규칙 > 가드 (이 결정 시점의 형태)

이 결정이 도입한 admin-api 가드의 처음 형태는 이후 [결정 0053](../mybatis/AGENTS.md#decision-0053)에서 넓어졌습니다.

| 항목 | 이 결정 시점 | 결정 0053 이후 |
|---|---|---|
| 메서드명 | `eachSharedWritePortHasExactlyOnePrimaryImplementation` | `eachSharedOutboundPortHasExactlyOnePrimaryImplementation` |
| 수집 범위 | `.port.out.write` 쓰기 포트만 | `.port.out` 전체(조회 포트 포함) |
| 공존 포트 | `BannerLoadPort`·`BannerSavePort` | notice 4개 |
| 테스트 위치 근거 | admin-api — "JPA·MyBatis가 함께 실리는 유일한 앱" | admin-api 그대로(web-api에도 함께 실리지만 결과가 같아 복제하지 않음) |
| 모듈 등록 테스트 | `BannerJpaPersistenceAdapterRegistrationTest`·`BannerMyBatisPersistenceAdapterRegistrationTest` | 삭제하고 notice 등록 테스트 4개로 교체 |

선례 경로는 `.../jpa/banner/persistence/BannerJpaPersistenceAdapter.java`(`@Primary`)·`.../mybatis/banner/BannerMyBatisPersistenceAdapter.java`(없음)였습니다.

#### 결정

같은 포트를 여러 기술 구현이 구현하면 두 구현 모두 포트를 `implements`하고, 쓰는 쪽에만 `@Primary`를 붙입니다. 조건 애노테이션과 설정 키는 두지 않습니다.

| 항목 | before (provider 키 스위치) | after (`@Primary`) |
|---|---|---|
| 구현 선택 | `persistence.banner.write.provider` + 두 어댑터의 `@ConditionalOnProperty`(JPA만 `matchIfMissing = true`) | 두 어댑터 모두 조건 없음, JPA 어댑터에만 `@Primary` |
| 설정·메타데이터 | yml 4줄 + 메타데이터 json | 둘 다 삭제 — `infrastructure:persistence`는 키를 소유하지 않는다 |
| 테스트 | 조건 테스트 2개 | 등록 테스트 `BannerJpaPersistenceAdapterRegistrationTest`·`BannerMyBatisPersistenceAdapterRegistrationTest` + admin-api 가드 `PersistencePrimaryRulesTest` |
| admin-api에 뜨는 banner 쓰기 빈 | 1개(선택된 쪽) | 2개(JPA가 주입되고 MyBatis는 주입되지 않음) |
| 전환 방법 | 환경변수 `BANNER_WRITE_PROVIDER=mybatis`로 기동 | `@Primary`를 MyBatis 어댑터로 옮기고 재빌드(환경변수는 무효) |
| 새 컨텍스트에 기술 구현 추가 비용 | yml 4줄 + 메타데이터 항목 + 조건 애노테이션 2개 + 조건 테스트 2개 | 새 어댑터의 `implements` + 활성 쪽 `@Primary` 1줄 |
| 메서드별 기술 혼용 | — | 금지 — 같은 포트는 `@Primary`로 하나만 |

#### 근거

- 원문의 "왜 바꿨나": 컨텍스트마다 같은 묶음(yml·메타데이터·조건 2개·테스트 2개)을 반복하는 비용이 기술 공존의 이점보다 컸습니다.
- 비활성 구현도 포트를 `implements`하므로, 포트 시그니처가 바뀌면 컴파일 에러로 드러나 썩지 않는다는 이점은 그대로 남습니다.
- `@ConditionalOnBean`류는 여전히 쓰지 않습니다([결정 0038](../../AGENTS.md#decision-0038)).

#### 결과·감수한 손실

- **런타임 전환(설정만 바꿔 재기동)이 불가능해졌습니다.** MyBatis는 파일럿이라 런타임 전환 요구가 없다고 보고 감수했습니다.
- 동작 변경: `BANNER_WRITE_PROVIDER` 환경변수가 무효가 됐고, admin-api에 MyBatis 빈이 주입 없이 함께 뜹니다. HTTP·DB는 변경이 없습니다.
- 가드: admin-api `PersistencePrimaryRulesTest`가 쓰기 포트별로 구현이 2개 이상이면 `@Primary`가 정확히 1개인지 검사합니다.
- 대상이었던 banner MyBatis 구현은 다음 날 [결정 0053](../mybatis/AGENTS.md#decision-0053)에서 삭제됐습니다. 그러나 "둘 다 등록하고 쓰는 쪽에 `@Primary`" 규칙은 notice 쓰기·조회 포트 4개로 이어져 지금도 현재 규칙입니다. 같은 결정에서 가드 수집 범위가 쓰기 포트에서 `port.out` 전체로 넓어졌습니다.
