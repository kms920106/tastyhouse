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

`infrastructure:redis`가 Redis 연결 설정을 `application-redis.yml`로 소유하듯, MySQL 접속 기술도 자기 모듈이 소유한다. 이 모듈은 jpa와 mysql을 묶는 조립 역할만 한다. 커넥션 풀 설정(HikariCP)과 그 근거는 `../mysql/AGENTS.md`가 소유한다([ADR-0051](../../../docs/adr/0051-mysql-module-split.md)).

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
- **이 모듈은 구현 선택 스위치를 갖지 않는다.** 같은 포트의 구현 선택은 설정이 아니라 코드(JPA 어댑터의 `@Primary`)가 한다 — 컨텍스트마다 설정 키·메타데이터·조건 애노테이션·조건 테스트를 반복하지 않기 위해서다([ADR-0052](../../../docs/adr/0052-banner-write-primary.md)). 규칙은 `backend/CLAUDE.md`의 "영속 포트 기술 중립 규칙", 현재 사례(notice 쓰기·조회)는 `../jpa/AGENTS.md`의 "notice 쓰기·조회 — JPA 구현과 MyBatis 구현의 공존"과 `../mybatis/AGENTS.md`에 있다.

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
