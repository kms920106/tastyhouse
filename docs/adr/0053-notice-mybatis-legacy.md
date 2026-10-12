# ADR-0053: mybatis 모듈의 banner 파일럿을 지우고 notice 레거시 MyBatis 구현(쓰기+조회)을 둔다

- 상태: 채택
- 결정 커밋: `8ea00a525` (2026-10-10)
- 대체한 결정: [ADR-0045](0045-banner-mybatis-provider-pilot.md)(banner MyBatis 구현)

## 맥락

- **레거시 구현**: 지금 주입되지는 않지만 포트를 계속 구현하며 유지되는 대체 구현입니다. 포트가 바뀌면 컴파일 에러로 함께 고쳐지므로 썩지 않고, `@Primary` 한 줄을 옮기면 되돌릴 수 있습니다.
- **조회 포트(`{Ctx}QueryPort`)**: 조회 유스케이스가 쓰는 읽기 전용 아웃바운드 포트입니다.

[ADR-0045](0045-banner-mybatis-provider-pilot.md)·[ADR-0052](0052-banner-write-primary.md)를 거치며 `infrastructure:mybatis`는 banner **쓰기** 포트 2개만 구현했고 admin-api만 의존했습니다. 기술 중립 규칙도 쓰기 포트에만 적용됐습니다.

## 이전 규칙

### backend/CLAUDE.md — 모듈 지도

"`infrastructure:mybatis` — MyBatis 어댑터(파일럿 — banner 쓰기만, admin-api만 의존). 같은 포트를 JPA와 함께 구현하고 `@Primary`(JPA) 쪽만 주입." 공존은 banner 쓰기뿐이었고 admin에서만 떴습니다.

### backend/CLAUDE.md — infrastructure 패키지 규칙

"mybatis: `{루트}.{컨텍스트}`(지금은 `banner` 하나 — 쓰기만 있어 `persistence`/`query`로 나누지 않는다. 조회를 추가하면 jpa 모듈처럼 나눈다)."

### backend/CLAUDE.md — 아웃바운드 포트·어댑터 네이밍 규칙

기술 공존 예시는 banner였습니다(`BannerJpaPersistenceAdapter`·`BannerMyBatisPersistenceAdapter`·`BannerJpaMapper`·`BannerRowMapper`).

### backend/CLAUDE.md — 영속 포트 기술 중립 규칙

"**write 포트**(`application/<ctx>/port/out/write/`)와 그것을 쓰는 application 코드는 영속 기술의 어휘·타입·예외를 모른다." `@Primary` 가드(`eachSharedWritePortHasExactlyOnePrimaryImplementation`)는 `.port.out.write` 쓰기 포트만 수집했습니다.

### backend/admin-api/AGENTS.md · backend/infrastructure/jpa/AGENTS.md · backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md

- admin-api는 "JPA·MyBatis 구현이 함께 실리는 유일한 앱"이었습니다.
- jpa 문서의 공존 절 제목은 "banner 쓰기 — JPA 구현과 MyBatis 구현의 공존"이었고, 대표 구현 봉인은 `BannerJpaPersistenceAdapterRegistrationTest`였습니다.
- domain 문서의 구현 1개 기본형 예시는 notice(`NoticeLoadPort`·`NoticeSavePort`, 어댑터 `NoticePersistenceAdapter`)였습니다.
- notice JPA 어댑터 이름은 구현 1개 기본형인 `NoticePersistenceAdapter`·`NoticeQueryAdapter`였고 `@Primary`가 없었습니다(jpa 문서 `<ctx>/query/` 절의 reference 구현도 `NoticeQueryAdapter`).

### backend/infrastructure/persistence/AGENTS.md · backend/infrastructure/mysql/AGENTS.md

- persistence 용어 절의 "구현 모듈" 중 `infrastructure:mybatis`는 "banner 쓰기 MyBatis 구현"이었습니다.
- "mybatis는 조립하지 않는다" 절은 `infrastructure:mybatis`를 "admin-api가 직접" `runtimeOnly`로 의존한다고 적었습니다. 별도 모듈로 뗀 이유(커밋 `79cebac26`: "MyBatis 자동 설정(`mybatis-spring-boot-starter`·`@MapperScan`·XML 파싱)은 필요한 앱에서만 켜지게 한다")는 그대로이고, 당시 그 앱이 admin-api 하나였을 뿐입니다.
- mysql 문서는 MyBatis를 "admin-api 전용"으로 적었습니다.

## 결정

`infrastructure:mybatis`의 시연 대상을 banner 쓰기에서 notice 쓰기·조회로 바꾸고, 기술 중립 규칙을 아웃바운드 포트 전체로 넓힙니다.

| 항목 | before (banner 파일럿) | after (notice 레거시) |
|---|---|---|
| 구현 대상 포트 | `BannerLoadPort`·`BannerSavePort`(쓰기 2개) | `NoticeLoadPort`·`NoticeSavePort`·`NoticeQueryPort`·`NoticeManagementQueryPort`(쓰기 2 + 조회 2) |
| mybatis 패키지 | `mybatis.banner`(나누지 않음) | `mybatis.notice.persistence` / `mybatis.notice.query` |
| JPA 짝 | `BannerJpaPersistenceAdapter`(`@Primary`) | `NoticeJpaPersistenceAdapter`·`NoticeJpaQueryAdapter`(둘 다 `@Primary`). banner JPA는 `BannerPersistenceAdapter`·`BannerMapper`로 원복, `@Primary` 제거 |
| 의존 앱 | admin-api | web-api·admin-api |
| 기술 중립 규칙 적용 대상 | 쓰기 포트(`port/out/write/`)만 | 아웃바운드 포트 전체(`port/out/` — 쓰기 + 조회) |
| 기술 공존 시 조회 어댑터 이름 | 규칙 없음 | `{Ctx}{기술}QueryAdapter`, 매퍼 `{Ctx}QueryMyBatisMapper` |
| `@Primary` 가드 | `eachSharedWritePortHasExactlyOnePrimaryImplementation`(`port.out.write`) | `eachSharedOutboundPortHasExactlyOnePrimaryImplementation`(`port.out` 전체) |
| 모듈 등록 테스트 | `Banner{Jpa,MyBatis}PersistenceAdapterRegistrationTest` | notice 등록 테스트 4개로 교체 |
| mybatis 가드 | 등록 테스트 1개, `LayerRulesTest` 2규칙 | 등록 테스트 2개, 매퍼 XML 파싱 테스트 2개, 패턴 테스트 1개, `LayerRulesTest` 4규칙(`queryShouldNotDependOnDomain`·`persistenceShouldNotDependOnQuery` 포함) |

## 근거

- `backend/infrastructure/mybatis/AGENTS.md`의 "왜 이 모듈이 있나": 포트는 그대로 두고 구현 기술만 바꿀 수 있다는 것을 실제 코드로 보이고, 레거시 구현을 지우지 않고 유지하며, MyBatis를 필요한 앱에만 싣기 위해서입니다.
- notice는 쓰기·조회 포트를 모두 가져 조회 포트까지 시연할 수 있습니다. notice를 쓰는 앱이 web(공개 목록)과 admin(관리 CRUD)이라 두 앱이 의존합니다. ceo·batch에는 공지 기능이 없어 싣지 않습니다.
- banner는 MyBatis 구현이 사라지면 구현이 하나라 기술 한정어가 필요 없어져 원래 이름으로 돌아갑니다.

## 결과·감수한 손실

- 동작 변경은 없습니다(API·스키마·도메인 불변, `GET /api/notices/v1` 응답 동일). 모든 포트에 계속 JPA 구현이 주입됩니다.
- notice 서비스 6개(web `NoticeListQueryService`, admin `NoticeCreateService`·`NoticeUpdateService`·`NoticeDeleteService`·`NoticeManagementListQueryService`·`NoticeManagementDetailQueryService`)는 한 줄도 바뀌지 않았습니다.
- MyBatis SQL은 런타임에 실행되지 않으므로 XML 파싱·SQL 렌더링 테스트로만 검증됩니다.
- web-api에도 jpa·mybatis가 함께 실리지만, ArchUnit은 그 앱의 테스트 클래스패스만 읽어 web·admin 어디에 둬도 결과가 같으므로 `PersistencePrimaryRulesTest`는 admin-api에만 둡니다.
- domain 문서의 구현 1개 기본형 예시는 notice가 공존 예외 사례가 되어 banner로 바뀌었습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 영속 포트 기술 중립 규칙 (포트는 JPA·MyBatis 어느 쪽으로도 구현될 수 있어야 한다) · 아웃바운드 포트 전체 적용, notice 선례, 가드
- `backend/CLAUDE.md` · 아웃바운드 포트·어댑터 네이밍 규칙 · 기술 공존 예외(notice 예시, `{Ctx}{기술}QueryAdapter`)
- `backend/CLAUDE.md` · infrastructure 패키지 규칙 (루트 = 모듈명) · mybatis `{루트}.{컨텍스트}.{persistence|query}`
- `backend/CLAUDE.md` · 모듈 지도 · mybatis = notice 레거시 구현, web·admin 의존
- `CLAUDE.md` · 리포지토리 구조 · `mybatis`는 notice MyBatis 레거시 구현(쓰기+조회), web-api·admin-api 의존
- `backend/infrastructure/mybatis/AGENTS.md` · 왜 이 모듈이 있나 / 패키지 구조 / JPA 구현과의 비교 (notice) / 전환 방법
- `backend/infrastructure/jpa/AGENTS.md` · notice 쓰기·조회 — JPA 구현과 MyBatis 구현의 공존 / 봉인·가드 목록 > notice JPA 구현이 대표(`@Primary`)다 — 등록 테스트 2개
- `backend/infrastructure/persistence/AGENTS.md` · mybatis는 조립하지 않는다
- `backend/admin-api/AGENTS.md` · "`PersistencePrimaryRulesTest` — 여러 기술이 함께 구현하는 아웃바운드 포트는 `@Primary`가 정확히 1개 (banner-write-primary → notice-mybatis-legacy)" · admin에 두고 web에 복제하지 않는 이유
- `backend/domain/src/main/java/com/tastyhouse/domain/AGENTS.md` · Common Patterns · 기본형 예시 banner
