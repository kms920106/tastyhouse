# ADR-0047: JPA·QueryDSL 코드를 infrastructure:jpa로 옮기고 persistence를 코드 없는 조립 모듈로 만든다

- 상태: 채택
- 결정 커밋: `4178929c3` (2026-10-09)
- 대체한 결정: —

## 맥락

- **조립 모듈(스타터)**: 자바 코드 없이 구현 모듈을 `runtimeOnly`로 묶어 앱에 노출하는 모듈입니다. 선례는 `file-storage → firebase`, `pg → tosspayments`입니다.
- **driven 어댑터**: application의 아웃바운드 포트를 실제 기술로 구현하는 모듈입니다.

[ADR-0045](0045-banner-mybatis-provider-pilot.md)로 banner 쓰기의 MyBatis 구현은 별도 모듈(`infrastructure:mybatis`)이 됐습니다. 그런데 같은 포트의 JPA 구현은 "persistence"라는 공용 이름의 모듈에 섞여 있어 이름이 비대칭이었습니다. 또 DB 연결 정보와 MySQL 드라이버는 JPA와 MyBatis가 함께 쓰는 것이라, 어느 한 기술 모듈이 아니라 공용 모듈이 소유하는 편이 맞았습니다(`docs/tasks/jpa-module-split/backend.md`). 참고 구조는 board-project의 `adapter-out-persistence-jpa` / `-mybatis` 분리입니다.

## 이전 규칙

### backend/infrastructure/persistence/AGENTS.md(현 jpa/AGENTS.md)

- `infrastructure:persistence`가 JPA·QueryDSL 코드 전부(엔티티·쓰기 어댑터·조회 DAO·가드 테스트)를 가진 driven 어댑터였습니다.
- 자바 패키지 루트는 `com.tastyhouse.infrastructure.persistence..`, 진입 설정은 `InfrastructurePersistenceConfig`였습니다.
- 설정은 `application-persistence.yml` 한 벌(datasource + `spring.jpa.*` + provider 키)이었고, MySQL 드라이버도 이 모듈의 `runtimeOnly`였습니다.

### backend/CLAUDE.md — infrastructure 패키지 규칙 (ArchUnit 패키지 술어 함정)

[ADR-0037](0037-infrastructure-package-root.md)로 루트에 `persistence` 세그먼트가 생겨, `"com.tastyhouse.infrastructure..persistence.."`가 `...persistence.order.query`까지 매칭했습니다. 그래서 persistence `LayerRulesTest`의 술어는 `"com.tastyhouse.infrastructure.persistence..persistence.."`·`"com.tastyhouse.infrastructure.persistence..query.."`처럼 루트를 끝까지 적어야 했습니다. 앱(`{web,admin,ceo}-api`·`batch-module`) `LayerRulesTest`의 persistence 금지 술어는 `"com.tastyhouse.infrastructure.persistence.."`였습니다. `persistence.order.persistence`처럼 `persistence`가 두 번 나오는 이름도 감수했습니다.

## 결정

JPA·QueryDSL 코드와 테스트를 신설 모듈 `infrastructure:jpa`로 옮기고, `infrastructure:persistence`는 코드 없는 조립 모듈로 바꿉니다.

| 항목 | before | after |
|---|---|---|
| Gradle 좌표 / 디렉터리 | `:infrastructure:persistence` / `backend/infrastructure/persistence/` | `:infrastructure:jpa` / `backend/infrastructure/jpa/` |
| 자바 패키지 루트 | `com.tastyhouse.infrastructure.persistence..` | `com.tastyhouse.infrastructure.jpa..` (하위 `<ctx>/persistence`·`<ctx>/query`·`config`·`shared.*` 불변) |
| 진입 설정 클래스 | `InfrastructurePersistenceConfig` | `JpaModuleConfig` |
| `infrastructure:persistence` | driven 어댑터(JPA 코드 전부) | 조립 모듈 — `runtimeOnly ':infrastructure:jpa'` + `runtimeOnly 'com.mysql:mysql-connector-j'` |
| 설정 yml | `application-persistence.yml` 한 벌 | `application-jpa.yml`(jpa: `spring.jpa.*`·hibernate 로그) + `application-persistence.yml`(persistence: datasource·`spring.sql.init`·provider 키, `application-jpa.yml` import) |
| MySQL 드라이버 | 이 모듈 `runtimeOnly` | persistence 조립 모듈(JPA·MyBatis 공용) |
| 앱 4개 의존·yml import | `runtimeOnly ':infrastructure:persistence'` | 불변 |
| 모듈 수 | 37 | 38 |

## 근거

- JPA 구현과 MyBatis 구현을 `infrastructure:jpa` / `infrastructure:mybatis`로 대칭되게 둡니다.
- 나눈 기준: JPA를 쓸 때만 의미 있는 키는 JPA 구현 모듈이, JPA·MyBatis가 함께 쓰는 DB 접속(datasource·드라이버)과 구현 선택 스위치는 조립 모듈이 갖습니다.
- 루트가 `com.tastyhouse.infrastructure.jpa`라 루트에 `persistence` 세그먼트가 없어져 ArchUnit 술어 오매칭 함정이 해소됩니다.

## 결과·감수한 손실

- 앱 4개의 의존·yml import와 동작·HTTP·DB는 변경이 없습니다.
- 현행 술어는 jpa `LayerRulesTest`가 `"com.tastyhouse.infrastructure.jpa..persistence.."`·`"..jpa..query.."`, 앱 4개 `LayerRulesTest#shouldNotDependOnInfrastructurePersistence`(메서드명 유지)가 `"com.tastyhouse.infrastructure.jpa.."`입니다. 루트를 끝까지 적는 원칙은 유지합니다 — 줄이면 다른 영속 기술 모듈의 패키지까지 대상에 들어올 수 있습니다.
- mybatis `LayerRulesTest`의 금지 패키지도 `"com.tastyhouse.infrastructure.jpa.."`입니다.
- 코드(main·test)와 모듈 문서(`backend/infrastructure/persistence/AGENTS.md` → `backend/infrastructure/jpa/AGENTS.md`)는 `git mv`로 함께 옮겼습니다. `settings.gradle` include는 37줄이 됐습니다.
- `persistence.order.persistence` 이중 이름은 `jpa.order.persistence`가 되어 사라졌습니다.
- 이 결정 이전의 과거 시점 서술에 나오는 `persistence` 모듈명·패키지·`InfrastructurePersistenceConfig`는 지금 이름(`jpa`·`JpaModuleConfig`)으로 읽습니다.
- 드라이버·datasource 소유는 [ADR-0051](0051-mysql-module-split.md)이 `infrastructure:mysql`로 옮겼고, provider 키는 [ADR-0052](0052-banner-write-primary.md)가 삭제했습니다. 지금 persistence는 jpa + mysql 조립만 합니다.
- 작업 중 600여 파일의 패키지를 기계적으로 옮겼습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 모듈 지도 (모듈 재편 완료 + application 모듈 통합 + external 분리 + 앱 모듈 재분리) · `infrastructure:jpa`와 조립 모듈 persistence
- `backend/CLAUDE.md` · infrastructure 패키지 규칙 (루트 = 모듈명) · jpa 루트·ArchUnit 술어는 루트를 끝까지 적는다
- `backend/CLAUDE.md` · 패키지 최상위 지도 · `com.tastyhouse.infrastructure.jpa..` 행
- `backend/infrastructure/jpa/AGENTS.md` · infrastructure:jpa / 규칙 / 설정 파일 (`src/main/resources/application-jpa.yml`) / Dependencies
- `backend/infrastructure/jpa/AGENTS.md` · 봉인·가드 목록 > `LayerRulesTest` 패키지 술어 — 루트 `com.tastyhouse.infrastructure.jpa`를 끝까지 적는다
- `backend/infrastructure/persistence/AGENTS.md` · 왜 코드 없는 모듈이 됐나 (jpa 모듈 분리)
- `backend/AGENTS.md` · Subdirectories · `infrastructure/jpa`
- `CLAUDE.md` · 리포지토리 구조 · 모듈 38(jpa 신설)
