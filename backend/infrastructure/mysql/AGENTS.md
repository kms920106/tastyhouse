<!-- Parent: ../../AGENTS.md -->

# infrastructure:mysql

**MySQL 접속 기술(드라이버 + 커넥션 풀 + 연결 설정)을 소유하는 모듈**(`java-library`). **자바 코드가 없다.** 가진 것은 의존 선언(`build.gradle`)과 설정 파일(`application-mysql.yml`) 둘뿐이다. 도메인 포트를 구현하지 않는다는 점은 `restclient`와 같다. `restclient`가 HTTP 연결 코어이듯 이 모듈은 DB 연결 코어라 driven 쪽으로 센다.

> **용어**
> - **커넥션 풀(HikariCP)**: DB 연결을 미리 만들어 두고 요청마다 빌려주는 장치. 연결 수립(TCP + 인증)은 수~수십 ms가 걸려 요청마다 새로 만들면 느리다. Spring Boot의 기본 풀 구현이 HikariCP다.
> - **Connector/J**: MySQL 공식 JDBC 드라이버(`com.mysql:mysql-connector-j`). `data-source-properties`는 이 드라이버에 그대로 전달되는 옵션이다.

## 신설 배경

`infrastructure:redis`가 Redis 연결 설정을 `application-redis.yml`로 소유하는 것처럼, MySQL 연결 설정도 그 기술의 모듈이 소유하게 했다("모듈이 소비하는 설정은 모듈이 소유한다"). 이전에는 조립 모듈 `infrastructure:persistence`가 드라이버와 datasource를 직접 갖고 있었고, 커넥션 풀 설정은 하나도 없어 전부 Hikari 기본값으로 떴다.

| 항목 | before | after (현행) |
|---|---|---|
| `mysql-connector-j` 선언 | `persistence/build.gradle` `runtimeOnly` | **이 모듈** `runtimeOnly` |
| `HikariCP` 선언 | 없음(`spring-boot-starter-data-jpa`가 전이로 실음) | **이 모듈** `runtimeOnly` — 풀 설정을 소유하는 모듈이 풀 구현도 선언한다(버전은 Spring Boot BOM) |
| `spring.datasource.{url,username,password,driver-class-name}` | `application-infrastructure.yml` | **`application-mysql.yml`** (값 불변) |
| `spring.sql.init.mode: always` | `application-infrastructure.yml` | **`application-mysql.yml`** (DataSource 초기화 설정이라 함께 이동. classpath에 `schema*.sql`/`data*.sql`이 없어 지금도 no-op) |
| `spring.datasource.hikari.*` | 없음(Hikari 기본값) | 아래 표 — **동작 변경** |
| 로딩 | persistence yml이 직접 소유 | persistence의 `application-persistence.yml`이 `classpath:application-mysql.yml`을 import |

JPA(`infrastructure:jpa`)와 MyBatis(`infrastructure:mybatis`, admin-api 전용)는 같은 DataSource를 쓴다. 이 모듈은 둘 중 어느 쪽에도 속하지 않고, 조립 모듈 `persistence`가 jpa와 함께 싣는다. admin-api는 persistence를 거쳐 이 모듈을 받으므로 MyBatis도 같은 풀을 쓴다.

## 소유물

```
backend/infrastructure/mysql/
  build.gradle                                runtimeOnly mysql-connector-j + runtimeOnly HikariCP
  AGENTS.md
  src/main/resources/application-mysql.yml    아래 키 목록
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다(fat jar 안 이름 `mysql-0.0.1-SNAPSHOT.jar`). `bootJar`는 비활성이고 plain jar다. 자바 패키지가 없으므로 `PackageRootTest`도 없다.

## `application-mysql.yml` 키와 근거

모든 수치는 `${환경변수:기본값}` 형태다.

### 커넥션 풀 (`spring.datasource.hikari.*`)

| 키 | 값 | Hikari 기본값 | 근거 |
|---|---|---|---|
| `pool-name` | `${spring.application.name}-hikari` | `HikariPool-1` | 로그·JMX에서 어느 앱의 풀인지 구분한다(`web-api-hikari`, `admin-api-hikari` …). 4앱 모두 `spring.application.name`을 선언한다 |
| `maximum-pool-size` | `${DB_POOL_MAX_SIZE:5}` | 10 | 보수적으로 5에서 시작한다. 트래픽이 적은 admin·ceo·batch는 기본값으로 충분하고, web-api만 배포할 때 올린다(아래 "앱별 조정") |
| `minimum-idle` | `${DB_POOL_MIN_IDLE:5}` | = max | HikariCP 권장인 고정 크기 풀이다. 트래픽이 몰릴 때 연결을 새로 만드는 지연을 없앤다. max와 같으면 `idle-timeout`이 의미가 없어 설정하지 않는다. **max를 올릴 때 같이 올린다** |
| `connection-timeout` | `${DB_POOL_CONNECTION_TIMEOUT_MS:3000}` | 30000 | 풀이 고갈됐을 때 30초를 기다리면 톰캣 스레드가 묶여 장애가 번진다. 3초 안에 실패시킨다(fail-fast) |
| `validation-timeout` | `${DB_POOL_VALIDATION_TIMEOUT_MS:1000}` | 5000 | 연결 생존 확인 상한. `connection-timeout`보다 작아야 한다 |
| `max-lifetime` | `${DB_POOL_MAX_LIFETIME_MS:1800000}` | 1800000 | 기본값과 같지만 운영 DB 값과 대조할 수 있게 명시했다. **MySQL `wait_timeout`·인프라(LB·프록시) idle timeout보다 수 초 이상 짧아야 한다.** 운영 DB가 30분보다 짧으면 이 값을 내린다 |
| `keepalive-time` | `${DB_POOL_KEEPALIVE_TIME_MS:60000}` | 0(끔) | 고정 풀이라 유휴 연결이 늘 존재한다. 방화벽·NAT·LB가 유휴 TCP를 조용히 끊으면 다음 사용 시 실패하므로 1분마다 핑한다. 최소 30000이고 `max-lifetime`보다 작아야 한다 |
| `leak-detection-threshold` | `${DB_POOL_LEAK_DETECTION_MS:0}` | 0(끔) | 운영에서는 끈다. 로컬에서 연결 반납 누락을 진단할 때만 `10000` 등으로 켠다. batch의 장기 작업은 경고 오탐을 낸다 |

### 드라이버 옵션 (`spring.datasource.hikari.data-source-properties.*`)

| 키 | 값 | 드라이버 기본값 | 근거 |
|---|---|---|---|
| `connectTimeout` | `${DB_CONNECT_TIMEOUT_MS:3000}` | 0(무제한) | TCP 연결 수립 상한. DB가 응답하지 않을 때 연결 생성이 무한정 걸리는 것을 막는다 |
| `socketTimeout` | `${DB_SOCKET_TIMEOUT_MS:0}` | 0(무제한) | **기본은 기존 동작 유지(무제한).** 켜면 그 시간을 넘는 쿼리가 끊긴다. batch·admin의 장기 쿼리를 보호하려고 기본을 0으로 두고, 네트워크 단절 방어가 필요한 앱(web-api)만 켠다 |
| `cachePrepStmts` | `true` | false | PreparedStatement 파싱 결과를 연결별로 캐시(클라이언트 측) |
| `prepStmtCacheSize` | `250` | 25 | HikariCP MySQL 권장값 |
| `prepStmtCacheSqlLimit` | `2048` | 256 | Hibernate가 만드는 긴 SQL도 캐시되게 한다 |
| `useLocalSessionState` | `true` | false | autocommit·격리 수준을 서버에 매번 묻지 않는다 |
| `cacheResultSetMetadata` | `true` | false | 결과 메타데이터 캐시 |
| `cacheServerConfiguration` | `true` | false | 연결 생성 때마다 서버 변수를 조회하지 않는다 |
| `elideSetAutoCommits` | `true` | false | 상태가 같으면 `SET autocommit`을 보내지 않는다 |
| `rewriteBatchedStatements` | `true` | false | JDBC 배치 INSERT를 multi-value 한 문장으로 바꾼다. IDENTITY 키 엔티티는 Hibernate가 배치하지 않으므로 현재 JPA 경로에는 영향이 없다 |
| `maintainTimeStats` | `false` | true | 쓰지 않는 시간 통계 수집을 끈다 |

### 그 외

| 키 | 값 | 비고 |
|---|---|---|
| `spring.datasource.url`·`username`·`password` | `${DB_URL}`·`${DB_USERNAME}`·`${DB_PASSWORD}` | 기본값이 없다. 미설정이면 기동이 실패한다(의도) |
| `spring.datasource.driver-class-name` | `com.mysql.cj.jdbc.Driver` | |
| `spring.sql.init.mode` | `always` | 초기화 스크립트가 classpath에 없어 no-op |

## 의도적으로 넣지 않은 설정

| 설정 | 넣지 않은 이유 |
|---|---|
| `useServerPrepStmts=true` | 서버 측 PreparedStatement는 연결마다 MySQL 서버 메모리를 점유한다. 4앱 × 연결 수 × 250이 MySQL `max_prepared_stmt_count`(기본 16,382)에 근접하고, 스케일아웃하면 `Can't create more than max_prepared_stmt_count`로 실패한다. 클라이언트 측 캐시(`cachePrepStmts`)만 쓴다 |
| `auto-commit: false` + `hibernate.connection.provider_disables_autocommit` | 트랜잭션 없이 실행되는 조회 DAO·MyBatis 경로의 의미가 바뀐다 |
| URL 파라미터(`connectionTimeZone`·`characterEncoding` 등) | `DB_URL` 환경변수 소관이다. 시간 값 해석이 바뀌는 동작 변경이라 이 모듈이 덮어쓰지 않는다 |
| `idle-timeout` | `minimum-idle == maximum-pool-size`(고정 풀)이면 동작하지 않는다 |

## 앱별 조정 (환경변수로만)

드라이버·드라이버 최적화·keepalive·max-lifetime·connectTimeout은 4앱 공통이다. 앱마다 다른 것은 풀 크기와 socketTimeout뿐이다.

| 앱 | `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | `DB_SOCKET_TIMEOUT_MS` |
|---|---|---|
| web-api | 10 이상(풀 사용량 모니터링 후 결정) | 60000 |
| admin-api | 기본 5 | 기본 0 |
| ceo-api | 기본 5 | 기본 0 |
| batch-module | 기본 5 | 기본 0(켜지 않는다 — 행정동 경계·BBQ 적재 등 장기 작업) |

연결 총합(Σ 인스턴스 수 × `DB_POOL_MAX_SIZE`)은 MySQL `max_connections`(기본 151)보다 충분히 작아야 한다.

**앱 `application.yml`에 `spring.datasource.hikari.*`를 적어도 반영되지 않는다.** Spring Boot에서 `spring.config.import`로 불러온 문서는 불러온 쪽 문서보다 우선한다. 앱 yml → `application-persistence.yml` → `application-mysql.yml` 순으로 import되므로, 이 파일의 값이 앱 yml의 같은 키를 이긴다. 조정하는 방법은 다음 셋이다.

- **앱별**: 기동할 때 환경변수를 준다. 예: `DB_POOL_MAX_SIZE=10 DB_POOL_MIN_IDLE=10 DB_SOCKET_TIMEOUT_MS=60000 java -jar web-api/build/libs/web-api-0.0.1-SNAPSHOT.jar`
- **또는 커맨드라인 인자**: `--spring.datasource.hikari.maximum-pool-size=10`
- **전 앱 공통**: `backend/.env`에 `DB_POOL_*`를 적는다. 앱 yml이 `.env`를 마지막에 import하므로 placeholder가 그 값으로 해석된다.

실효 설정은 `--logging.level.com.zaxxer.hikari.HikariConfig=DEBUG`로 기동하면 로그에 키별로 찍힌다.

## Dependencies

### External
- `com.mysql:mysql-connector-j` (**runtimeOnly**) — JPA·MyBatis 공용 드라이버.
- `com.zaxxer:HikariCP` (**runtimeOnly**) — 커넥션 풀. `spring-boot-starter-data-jpa`·`mybatis-spring-boot-starter`도 전이로 싣지만, 이 모듈이 풀 설정을 소유하므로 풀 구현도 직접 선언한다(전이 경로가 바뀌어도 설정 키가 고아가 되지 않게).

### 이 모듈을 의존하는 쪽
- `infrastructure:persistence` (**runtimeOnly**) — jpa와 함께 조립하고, `application-persistence.yml`이 `classpath:application-mysql.yml`을 import한다. 앱 4개는 이 모듈을 직접 의존하지 않는다.
