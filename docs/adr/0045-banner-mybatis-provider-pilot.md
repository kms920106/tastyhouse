# ADR-0045: banner 쓰기를 JPA·MyBatis 별도 모듈로 구현하고 provider 키로 하나만 등록한다

- 상태: 대체됨(→ [ADR-0052](0052-banner-write-primary.md))
- 결정 커밋: `79cebac26` (2026-10-09)
- 대체한 결정: —

## 맥락

- **MyBatis**: SQL을 XML에 직접 쓰고 결과를 객체에 매핑하는 영속 기술입니다. JPA와 달리 엔티티 상태 추적이 없습니다.
- **영속 포트 기술 중립**: application의 포트와 서비스가 JPA·MyBatis 어느 쪽의 어휘·타입·예외도 모르게 해서, 어댑터만 바꿔 기술을 갈아끼울 수 있게 하는 규칙입니다.

앞선 banner MyBatis 파일럿(`docs/tasks/mybatis-banner-pilot/`)은 banner 쓰기 어댑터를 MyBatis로 교체해 "포트·서비스 무변경, 어댑터만 교체"를 실증했습니다. 다만 파일럿에서는 MyBatis 어댑터가 `infrastructure:persistence` 안에 있어 4앱 전부에서 MyBatis 자동 설정이 켜졌고, JPA 쓰기 어댑터는 삭제된 상태였습니다(커밋 메시지·`backend/infrastructure/mybatis/AGENTS.md`).

## 이전 규칙

### backend/CLAUDE.md — 아웃바운드 포트·어댑터 네이밍 규칙

어댑터 이름은 1:1 대칭이었습니다(`XxxPersistencePort` ↔ `XxxPersistenceAdapter`, 변환기 `XxxMapper`). 같은 포트를 여러 기술이 구현하는 경우의 규칙은 없었습니다.

### backend/CLAUDE.md — 모듈 지도

모듈 36개. MyBatis 전용 모듈은 없었습니다.

## 결정

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

## 근거

- MyBatis를 필요한 앱에만 싣기 위해 모듈을 나눴습니다. 모듈을 의존하는 앱에서만 자동 설정이 켜집니다("클래스패스 존재 = 활성화").
- 두 모듈 모두 포트를 완전히 구현하게 해, 포트·서비스를 바꾸지 않고 기술을 전환할 수 있음을 유지했습니다(포트·서비스 변경 없음).
- `matchIfMissing = true`를 둘 다 두면 속성이 없을 때 구현이 2개 등록돼 `NoUniqueBeanDefinitionException`이 납니다. 잘못된 값이면 구현이 0개가 되어 기동이 실패하는데, 조용히 틀린 쪽이 켜지는 것보다 낫다고 봤습니다(원문). `@ConditionalOnBean`류는 쓰지 않았습니다.
- 구현이 둘이면 이름만으로 기술을 구분해야 해서 `{Ctx}{기술}PersistenceAdapter`로 짓습니다. `{Ctx}MyBatisMapper`는 MyBatis SQL 인터페이스 이름이라 변환기에 쓰지 않습니다.

## 결과·감수한 손실

- 포트·서비스는 바뀌지 않았습니다.
- 당시 루트 `CLAUDE.md` 리포지토리 구조의 모듈 수 연대기는 이 결정을 "banner 쓰기의 MyBatis 구현을 담는 `infrastructure:mybatis`가 신설돼 37개(include 36줄 — JPA 구현과 함께 같은 포트를 구현하고 `persistence.banner.write.provider`로 하나만 등록)"로 적었습니다. 이후 JPA 모듈 분리(38개)·MySQL 모듈 분리(39개) 때도 `infrastructure:persistence`가 이 provider 키를 소유한다고 서술됐고, `@Primary` 전환으로 키가 삭제된 뒤 persistence는 jpa + mysql 조립만 하게 됐습니다(모듈 수 39는 불변).
- provider 키 방식은 같은 날 [ADR-0052](0052-banner-write-primary.md)가 `@Primary`로 대체했습니다(키·조건 애노테이션·메타데이터·조건 테스트 삭제). banner의 MyBatis 구현 자체는 [ADR-0053](0053-notice-mybatis-legacy.md)에서 삭제되고 MyBatis 시연 대상은 notice로 바뀌었습니다.
- 기술 공존 시의 한정어 네이밍 예외(`{Ctx}{기술}PersistenceAdapter`)는 지금도 유효하며, 예시만 notice로 바뀌었습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 아웃바운드 포트·어댑터 네이밍 규칙 (`LoadPort`·`SavePort` ↔ `PersistenceAdapter`, `QueryPort` ↔ `QueryAdapter`) · 같은 포트를 여러 기술이 구현할 때의 기술 한정어 예외
- `backend/CLAUDE.md` · 영속 포트 기술 중립 규칙 (포트는 JPA·MyBatis 어느 쪽으로도 구현될 수 있어야 한다) · 지금은 `@Primary` 방식([ADR-0052](0052-banner-write-primary.md))
- `backend/CLAUDE.md` · 모듈 지도 · `infrastructure:mybatis`
- `CLAUDE.md` · 리포지토리 구조 · `infrastructure:mybatis` 신설로 모듈 37
- `backend/infrastructure/mybatis/AGENTS.md` · 왜 이 모듈이 있나 · MyBatis를 필요한 앱에만 싣는다
- `backend/infrastructure/persistence/AGENTS.md` · mybatis는 조립하지 않는다
