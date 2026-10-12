# ADR-0051: MySQL 드라이버·datasource·커넥션 풀을 infrastructure:mysql로 옮긴다

- 상태: 채택
- 결정 커밋: `34edab8d2` (2026-10-09)
- 대체한 결정: [ADR-0047](0047-jpa-module-split.md)(드라이버·datasource 소유 부분)

> 커밋 `34edab8d2`의 메시지는 `456db1b13`의 메시지를 글자 그대로 복사한 것이라 mysql 언급이 없습니다. 이 커밋이 mysql 모듈 분리라는 것은 `backend/settings.gradle`에 `include 'infrastructure:mysql'` 한 줄이 추가된 것으로 판별합니다.

## 맥락

- **HikariCP**: Spring Boot의 기본 커넥션 풀입니다. DB 연결을 미리 만들어 두고 요청마다 빌려줍니다.
- **Connector/J**: MySQL 공식 JDBC 드라이버(`com.mysql:mysql-connector-j`)입니다.

[ADR-0047](0047-jpa-module-split.md) 이후 MySQL 드라이버와 datasource 설정은 조립 모듈 `infrastructure:persistence`가 직접 들고 있었습니다. 커넥션 풀 설정은 하나도 없어 Hikari 기본값(풀 이름 `HikariPool-1`, 풀 고갈 시 30초 대기, keepalive 없음, 소켓 타임아웃 무제한)으로 떴습니다. `infrastructure:redis`가 Redis 연결 설정을 `application-redis.yml`로 소유하는 것처럼, MySQL 연결도 자기 모듈이 소유하게 하려는 것이 출발점이었습니다(`docs/tasks/mysql-module/backend.md`).

## 이전 규칙

### backend/infrastructure/persistence/AGENTS.md · backend/infrastructure/jpa/AGENTS.md

- "드라이버·datasource는 조립 모듈 persistence가 소유한다." MySQL 드라이버는 JPA·MyBatis가 함께 쓰므로 persistence 조립 모듈이 `runtimeOnly 'com.mysql:mysql-connector-j'`로 가졌습니다.
- jpa 문서의 설정 파일 절 제목은 "datasource·provider 키는 `infrastructure:persistence`의 `application-persistence.yml`"이었습니다. `application-persistence.yml`이 `spring.config.import: classpath:application-jpa.yml`·`spring.datasource.*`(URL·계정·`com.mysql.cj.jdbc.Driver`)·`spring.sql.init.mode`·provider 키를 담았습니다.
- 나눈 기준은 "JPA를 쓸 때만 의미 있는 키는 JPA 구현 모듈이, JPA·MyBatis가 함께 쓰는 DB 접속(datasource·드라이버)과 구현 선택 스위치는 조립 모듈이 갖는다"였습니다. persistence 문서의 근거는 "JPA(`infrastructure:jpa`)와 MyBatis(`infrastructure:mybatis`)가 같은 커넥션 풀을 공유하고 `JpaTransactionManager` 하나가 두 기술을 같은 트랜잭션으로 묶으므로, 드라이버·접속 정보는 어느 한 구현 모듈의 것이 아니다 — 그래서 구현들을 묶는 조립 모듈이 갖는다"였습니다.

### CLAUDE.md — 리포지토리 구조 (모듈 수 연대기)

38개 시점의 `infrastructure:persistence`는 "코드 없는 조립 모듈(jpa + MySQL 드라이버 조립, datasource·provider 키 소유)"이었고 include는 37줄이었습니다. 이 결정으로 persistence는 "jpa + mysql을 조립하고 provider 키만 갖게" 되었고 include는 38줄이 됐습니다.

## 결정

MySQL 드라이버·datasource·커넥션 풀 설정을 신설 모듈 `infrastructure:mysql`(`application-mysql.yml`)로 옮기고, persistence는 jpa와 mysql을 조립합니다.

| 항목 | before | after |
|---|---|---|
| 모듈 수 | 38 | 39 (`include 'infrastructure:mysql'`) |
| 드라이버 선언 | `persistence/build.gradle` `runtimeOnly 'com.mysql:mysql-connector-j'` | `mysql/build.gradle`. persistence는 `runtimeOnly project(':infrastructure:mysql')` |
| HikariCP 선언 | 없음(starter-data-jpa가 전이로 실음) | `mysql/build.gradle` `runtimeOnly 'com.zaxxer:HikariCP'` |
| `spring.datasource.*`·`spring.sql.init.mode` | persistence yml | `application-mysql.yml` |
| persistence yml import | `application-jpa.yml` | `application-mysql.yml`, `application-jpa.yml` |
| `spring.datasource.hikari.*` | 없음(Hikari 기본값) | 고정 풀 기본 5, `connection-timeout` 3000, `keepalive-time` 60000, 풀 이름 `{앱}-hikari`, PS 캐시 등 |
| 앱 4개 `build.gradle`·`application.yml` | — | 불변 |

## 근거

- 연결 설정은 그 기술 모듈이 소유한다는 선례(`infrastructure:redis`의 `application-redis.yml`)를 MySQL에도 적용합니다.
- 운영 기본값으로 뜨던 커넥션 풀에 실무 설정(3초 fail-fast, keepalive, PS 캐시 등)을 줍니다. 각 값의 근거는 `backend/infrastructure/mysql/AGENTS.md`에 있습니다.

## 결과·감수한 손실

- API·엔드포인트·에러코드·DB 스키마는 그대로입니다. **커넥션 풀 설정만 동작이 바뀌었습니다**(Hikari 기본값 → 명시 설정).
- "드라이버·접속 정보는 어느 구현 모듈의 것도 아니다"라는 근거는 그대로이고, 위치만 조립 모듈에서 그 아래 DB 연결 코어로 내려갔습니다. persistence가 mysql을 조립하므로 MyBatis를 쓰는 앱도 같은 풀을 받습니다.
- `spring.sql.init.mode: always`는 초기화 스크립트가 없어 no-op입니다.
- 이 시점 persistence에 남았던 provider 키는 [ADR-0052](0052-banner-write-primary.md)에서 삭제돼, 지금 persistence는 jpa + mysql 조립만 합니다.
- mysql 모듈은 자바 코드가 없는 DB 연결 코어로, driven 어댑터 수에 셉니다.

## 영향받은 문서

- `backend/infrastructure/mysql/AGENTS.md` · 신설 배경 / `application-mysql.yml` 키와 근거 / 의도적으로 넣지 않은 설정
- `backend/infrastructure/persistence/AGENTS.md` · mysql 모듈 분리 — 드라이버·datasource를 `infrastructure:mysql`로 / 왜 드라이버·datasource는 mysql 모듈에 두나
- `backend/infrastructure/jpa/AGENTS.md` · 설정 파일 (`src/main/resources/application-jpa.yml`) / Dependencies > External
- `CLAUDE.md` · 리포지토리 구조 · 모듈 39(mysql 신설), driven 18
