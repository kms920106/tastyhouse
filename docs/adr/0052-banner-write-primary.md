# ADR-0052: 같은 포트를 여러 기술이 구현하면 둘 다 등록하고 쓰는 쪽에 @Primary를 단다(provider 키 삭제)

- 상태: 채택
- 결정 커밋: `f91ff6659` (2026-10-09)
- 대체한 결정: [ADR-0045](0045-banner-mybatis-provider-pilot.md)

## 맥락

- **`@Primary`**: 같은 타입의 빈이 여러 개일 때, 단일 주입 지점에 기본으로 들어갈 빈을 지정하는 Spring 애노테이션입니다.
- **`@ConditionalOnProperty`**: 설정 키 값에 따라 빈을 등록하거나 건너뛰는 조건 애노테이션입니다.

[ADR-0045](0045-banner-mybatis-provider-pilot.md)로 banner 쓰기는 JPA·MyBatis 두 구현 중 provider 키(`persistence.banner.write.provider`)로 하나만 빈으로 등록했습니다. 이 방식은 기술 구현을 추가하는 컨텍스트마다 yml·메타데이터·조건 애노테이션·조건 테스트를 한 묶음씩 반복해야 했습니다.

## 이전 규칙

### backend/CLAUDE.md — 영속 포트 기술 중립 규칙

"같은 포트를 여러 모듈이 구현하면 provider 속성으로 하나만 등록한다 — 각 구현 클래스에 `@ConditionalOnProperty(name = "persistence.{ctx}.write.provider", havingValue = "{기술}")`를 달고, `matchIfMissing = true`는 기본 구현에만 둔다. 선례: banner(`persistence.banner.write.provider=jpa|mybatis`, 기본 jpa). provider 키는 조립 모듈 `infrastructure:persistence`의 `application-persistence.yml`이 소유한다."

### backend/infrastructure/jpa/AGENTS.md · persistence/AGENTS.md

- 조립 모듈 `application-persistence.yml`이 `persistence.banner.write.provider`(`${BANNER_WRITE_PROVIDER:jpa}`)를 가졌고, `META-INF/additional-spring-configuration-metadata.json`도 있었습니다.
- 조건 테스트 `BannerJpaProviderConditionTest`·`BannerMyBatisProviderConditionTest`가 provider 값별 등록을 검증했습니다.
- 메타데이터 파일 경로는 `infrastructure/persistence/src/main/resources/META-INF/additional-spring-configuration-metadata.json`이었고, provider 키 하나의 IDE 자동완성용이었습니다.
- persistence 문서의 "구현 모듈을 바꾸거나 추가하는 절차"는 "같은 포트를 새 구현도 구현한다면 provider 키(`persistence.{ctx}.write.provider`)를 이 yml에 추가하고, 기본 구현에만 `matchIfMissing = true`를 둔다"였습니다.

### backend/CLAUDE.md — 영속 포트 기술 중립 규칙 > 가드 (이 결정 시점의 형태)

이 결정이 도입한 admin-api 가드의 처음 형태는 이후 [ADR-0053](0053-notice-mybatis-legacy.md)에서 넓어졌습니다.

| 항목 | 이 결정 시점 | ADR-0053 이후 |
|---|---|---|
| 메서드명 | `eachSharedWritePortHasExactlyOnePrimaryImplementation` | `eachSharedOutboundPortHasExactlyOnePrimaryImplementation` |
| 수집 범위 | `.port.out.write` 쓰기 포트만 | `.port.out` 전체(조회 포트 포함) |
| 공존 포트 | `BannerLoadPort`·`BannerSavePort` | notice 4개 |
| 테스트 위치 근거 | admin-api — "JPA·MyBatis가 함께 실리는 유일한 앱" | admin-api 그대로(web-api에도 함께 실리지만 결과가 같아 복제하지 않음) |
| 모듈 등록 테스트 | `BannerJpaPersistenceAdapterRegistrationTest`·`BannerMyBatisPersistenceAdapterRegistrationTest` | 삭제하고 notice 등록 테스트 4개로 교체 |

선례 경로는 `.../jpa/banner/persistence/BannerJpaPersistenceAdapter.java`(`@Primary`)·`.../mybatis/banner/BannerMyBatisPersistenceAdapter.java`(없음)였습니다.

## 결정

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

## 근거

- 원문의 "왜 바꿨나": 컨텍스트마다 같은 묶음(yml·메타데이터·조건 2개·테스트 2개)을 반복하는 비용이 기술 공존의 이점보다 컸습니다.
- 비활성 구현도 포트를 `implements`하므로, 포트 시그니처가 바뀌면 컴파일 에러로 드러나 썩지 않는다는 이점은 그대로 남습니다.
- `@ConditionalOnBean`류는 여전히 쓰지 않습니다([ADR-0038](0038-autoconfiguration-imports-removal.md)).

## 결과·감수한 손실

- **런타임 전환(설정만 바꿔 재기동)이 불가능해졌습니다.** MyBatis는 파일럿이라 런타임 전환 요구가 없다고 보고 감수했습니다.
- 동작 변경: `BANNER_WRITE_PROVIDER` 환경변수가 무효가 됐고, admin-api에 MyBatis 빈이 주입 없이 함께 뜹니다. HTTP·DB는 변경이 없습니다.
- 가드: admin-api `PersistencePrimaryRulesTest`가 쓰기 포트별로 구현이 2개 이상이면 `@Primary`가 정확히 1개인지 검사합니다.
- 대상이었던 banner MyBatis 구현은 다음 날 [ADR-0053](0053-notice-mybatis-legacy.md)에서 삭제됐습니다. 그러나 "둘 다 등록하고 쓰는 쪽에 `@Primary`" 규칙은 notice 쓰기·조회 포트 4개로 이어져 지금도 현재 규칙입니다. 같은 결정에서 가드 수집 범위가 쓰기 포트에서 `port.out` 전체로 넓어졌습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 영속 포트 기술 중립 규칙 (포트는 JPA·MyBatis 어느 쪽으로도 구현될 수 있어야 한다) · 두 구현 모두 `implements` + 쓰는 쪽 `@Primary`, 가드
- `backend/CLAUDE.md` · 모듈 등록 컨벤션 > 모듈별 설정 클래스 인벤토리 · `MyBatisModuleConfig` 행(조건 없음, `@Primary`가 고름)
- `backend/CLAUDE.md` · 모듈 지도 · mybatis 어댑터는 함께 뜨지만 주입되지 않음
- `CLAUDE.md` · 리포지토리 구조 · `infrastructure:persistence`는 jpa + mysql 조립만, `@Primary`가 주입 대상을 고른다
- `backend/infrastructure/persistence/AGENTS.md` · 소유물 / 왜 코드 없는 모듈이 됐나 · provider 키 없음
- `backend/infrastructure/jpa/AGENTS.md` · 봉인·가드 목록 > notice JPA 구현이 대표(`@Primary`)다 — 등록 테스트 2개
- `backend/infrastructure/mybatis/AGENTS.md` · 전환 방법 / 봉인·가드 목록 > MyBatis 어댑터에 `@Primary`를 붙이지 않는다 — 등록 테스트 2개
- `backend/admin-api/AGENTS.md` · "`PersistencePrimaryRulesTest` — 여러 기술이 함께 구현하는 아웃바운드 포트는 `@Primary`가 정확히 1개 (banner-write-primary → notice-mybatis-legacy)"
- `backend/AGENTS.md` · Subdirectories
