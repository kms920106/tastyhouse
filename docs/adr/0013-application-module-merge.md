# ADR-0013: 앱별 application 모듈 4개를 application 하나로 합친다(챕터 01)

- 상태: 대체됨(→ [ADR-0036](0036-app-marker-removal.md))
- 결정 커밋: `68d160710` (2026-09-04)
- 대체한 결정: [ADR-0008](0008-per-app-application-modules.md)

## 맥락

[ADR-0008](0008-per-app-application-modules.md)로 앱마다 `{app}-application` 모듈을 세웠다. 목적은 앱 간 수평 의존을 빌드가 막는 **컴파일 게이트**였다.

- **컴파일 게이트**: 금지된 의존을 테스트가 아니라 컴파일 에러로 막는 것. 모듈 클래스패스에 대상이 없으면 import 자체가 실패한다.

application 모듈 통합 프로그램은 그 분리가 실제로 값을 했는지 다시 따졌다.

## 이전 규칙

[ADR-0008](0008-per-app-application-modules.md) — application 계층은 `{web,admin,ceo,batch}-application` 4모듈로 물리 분리하고, 읽기 계약도 소비 앱 수에 따라 앱 모듈·`domain`에 나눠 둔다([ADR-0009](0009-read-contracts-split-by-consumer.md)).

### `backend/AGENTS.md` — Purpose

통합 전 서술은 "application 계층은 앱별 모듈로 물리 분리됐다 (모듈 재편 프로그램, 챕터 01~06)"였다 — 도메인당 CQRS 쌍을 `{web|admin|ceo|batch}-application`이 소유하고, 모듈 경계가 "계층 × 앱" 2차원이 되어 계층 위반이 ArchUnit 사후 검출이 아니라 컴파일 에러가 된다고 적었다. 앱 모듈은 챕터 01~04에서 각각 batch·web·admin·ceo 순으로 하나씩 세워졌다.

### `backend/AGENTS.md` — Module Dependency Graph (통합 기간의 표기)

- 4개 api 모듈이 같은 `application` 모듈을 의존하므로 "자기 앱의 UseCase만 주입"은 빌드가 아니라 각 모듈 `LayerRulesTest`의 `adaptersShouldOnlyUseOwnAppUseCases`가 강제한다(챕터 01 신설)고 적었다. 앱 마커 제거([ADR-0036](0036-app-marker-removal.md)) 후 각 앱이 자기 `{앱}-application`만 의존해 빌드가 강제하게 되면서 이 규칙은 삭제됐고, 다른 앱 모듈이 섞이는 것은 각 앱의 `ApplicationModuleClasspathTest`가 막는다.
- `application ─→ domain` 간선 옆에 "공유 읽기 계약 55개도 여기 있다(앱 단독 271개는 이 모듈 소유)"라는 주석이 있었다.

### `CLAUDE.md` — 리포지토리 구조 (모듈 수 연대기)

통합 후 서술은 "읽기 계약은 전부 `application` 모듈이 소유하고, 그 결과 `com.tastyhouse.application` 최상위 패키지를 한 모듈이 단독으로 갖는다"였다. 앱 마커 제거 이후 이 패키지는 코어 + 앱 모듈 4개가 나눠 갖는 split package가 됐다(단 `port.out` 계약은 앱 전용 SPI 포트를 빼면 전부 코어 소유).

## 결정

| 항목 | before | after |
|---|---|---|
| Gradle 모듈 | `{web,admin,ceo,batch}-application` 4개 | `application` 1개(모듈 수 17 → 14) |
| 자바 패키지 | `com.tastyhouse.{app}application` + `com.tastyhouse.application.<ctx>.port.out` | 이 챕터에서는 **불변**(평탄화는 챕터 03 — [ADR-0014](0014-package-flattening-app-markers.md)) |
| "자기 앱의 UseCase만 주입" 강제 | 빌드 | 각 api 모듈 `LayerRulesTest#adaptersShouldOnlyUseOwnAppUseCases`(이 챕터 신설) |

**이 챕터의 범위는 Gradle 모듈만 4 → 1이다.** 뒤 챕터에서 동명 클래스 182건 개명(02) → 패키지 평탄화 + 앱 마커 애노테이션(03) → 공유 읽기 계약 55개 복귀(04)가 이어졌다.

## 근거

`backend/application-core/AGENTS.md` "앱 축 통합 (챕터 01) — 폐기됨" 원문의 세 근거:

- **컴파일 게이트가 사실상 없었다.** `infrastructure/persistence/build.gradle`이 4개 application 모듈을 전부 `implementation`으로 의존하므로 모든 실행 jar에 4개 jar가 이미 들어 있었다(admin-api fat jar `BOOT-INF/lib/` 실측). 앱 분할이 실제로 준 게이트는 application → application 한 방향뿐이었고, 그것은 ArchUnit이 패키지로 이미 막고 있었다.
- **소유권 연쇄가 부채를 낳았다.** 읽기 계약을 앱 모듈이 소유하게 하면서 수평 의존을 피하려고 공유 계약을 `domain`으로 올리고 `application-common-module`을 해체했다(챕터 05·07·09). 그 결과가 split package 5모듈과 가드 3종(`ReadContractSingleOwnerTest`·`ReadContractPurityTest`·`RuleAnchorTest`의 소유 모듈 필터)이다.
- **중복이 컸고 이득이 없었다.** web·admin·ceo의 `LayerRulesTest`는 규칙 16종이 이름·본문까지 동일했다(diff는 carve-out 이름과 `because` 문구뿐). `gradle.properties`가 비어 있어 병렬 빌드 이득도 없었다.

## 결과·감수한 손실

- 모듈이 하나가 되면서 앱 간 수평 의존을 빌드가 막지 못하게 됐다. 그 자리를 ArchUnit(`AppIsolationTest`·`adaptersShouldOnlyUseOwnAppUseCases`)이 채웠다.
- 계층 축 분할(application이 infra를 모른다)은 그대로 유효했다.
- **대체**: 이후 `port.out` 계약을 전부 코어에 두자 persistence가 코어만 의존하게 되어 첫째 근거가 성립하지 않게 됐다. 그래서 `{web,admin,ceo,batch}-application` 4모듈이 앱 전용 빈만 담는 형태로 돌아왔다([ADR-0036](0036-app-marker-removal.md)). 둘째 근거(소유권 연쇄)는 계약을 코어가 전부 가지므로 재발하지 않고, 셋째 근거(규칙 중복)는 아키텍처 테스트를 코어 한 곳에 두어(`testImplementation`으로 앱 모듈 4개를 봄) 피했다.
- 둘째 근거가 지적한 split package·가드 3종 부채는 챕터 04에서 공유 계약 55개를 통합 `application`으로 되돌리며 통째로 사라졌다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · "앱 축 통합 (챕터 01) — 폐기됨" · 통합이 폐기됐다는 포인터
- `backend/application-core/AGENTS.md` · "앱 모듈 재분리 — 앱 소속은 Gradle 모듈이 표현한다" · 현재 규칙
- `backend/CLAUDE.md` · "앱 모듈 경계 규칙 (앱 마커 제거 — 앱 소속은 Gradle 모듈이 표현한다)" > "앱 모듈을 나눠도 컴파일 게이트가 동작하는 이유" · persistence가 코어만 의존하므로 게이트가 동작
- `backend/CLAUDE.md` · "인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략" · 계층 축 분할은 유효
- `backend/AGENTS.md` · "Module Dependency Graph" · application 계층 5개
- `CLAUDE.md` · "리포지토리 구조" · application 계층 5개(코어 + 앱 모듈 4)
