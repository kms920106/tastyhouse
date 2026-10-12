# ADR-0035: application *ApplicationConfig를 지우고 스캔 설정을 각 앱 부트스트랩의 중첩 설정으로 옮긴다

- 상태: 채택
- 결정 커밋: `deae9a8e0` (2026-10-03)
- 대체한 결정: —

## 맥락

- **컴포넌트 스캔(`@ComponentScan`)**: "이 패키지 아래에서 빈이 될 클래스를 찾아라"는 Spring 지시.
- **마커 스캔**: 스캔에 "특정 마커 애노테이션(`@WebApp` 등)이 붙은 클래스만"이라는 include 필터를 건 것. 당시 4개 앱이 `application` 모듈 하나를 공유해서, 앱마다 뜰 빈을 마커로 갈랐다.
- **컴포지션 루트**: 앱을 조립하는 단 한 곳. "어떤 부품을 이 앱에 붙일지"는 실행 앱 모듈(`web-api`·`admin-api`·`ceo-api`·`batch-module`)만 안다는 저장소 규칙이 있다.

공유 모듈 `application` 안에 앱마다 하나씩 `WebApplicationConfig`·`AdminApplicationConfig`·`CeoApplicationConfig`·`BatchApplicationConfig`가 있었다. 마커 하나만 빼고 모양이 같았고, 각 앱이 `@Import(XxxApplicationConfig.class)`로 가져왔다. 공유 모듈이 "어느 앱이 어떤 마커를 싣는가"라는 앱 조립 지식을 갖고 있었다.

## 이전 규칙

### backend/CLAUDE.md — 모듈 등록 컨벤션

- 앱은 `{App}ApplicationConfig` 하나만 `@Import`했고, 그 클래스가 `application` 모듈 안에 있었다.
- `application`이 auto-configuration을 갖지 않는 예외였던 사유는 "`{App}ApplicationConfig`가 `@Import`되는 대상이라 자기 등록 대상이 아니다"였다.

### backend/CLAUDE.md — 앱 마커 규칙 / 앱이 가질 수 있는 조립 코드의 상한 / 비채택 대안과 재고 조건

- 마커 include 필터를 건 `@ComponentScan`이 `application` 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig`에 있었고 앱이 `@Import`했다.
- 앱 조립 코드의 상한은 `@Import({App}ApplicationConfig)` 한 줄(+ yml)이었다.
- 부트스트랩 모듈 분리를 채택하지 않은 이유를 "`@SpringBootApplication` + `@Import` 두 줄뿐"이라고 표현했다.

### backend/AGENTS.md — Module Dependency Graph (그래프 아래 모듈 등록 항목)

- 챕터 02 auto-configuration 전환 뒤 앱의 `@Import`는 그 앱 정체성인 `{App}ApplicationConfig` 하나만 남았고, 그 클래스가 `application` 모듈에 있다고 적었다.

### backend/application-core/AGENTS.md — 챕터 03 빈 배선

- 4개 `*ApplicationConfig`의 include 필터가 앱별 스캔 범위를 정했다.

## 결정

| 항목 | before | after |
|---|---|---|
| 앱의 `@Import` | `@Import({App}ApplicationConfig.class)` 1줄 | 없음(0줄) |
| 스캔 `@ComponentScan`의 위치 | `application` 모듈의 `{Web,Admin,Ceo,Batch}ApplicationConfig` 4개 | 각 앱 부트스트랩 안의 `static class ApplicationLayerScanConfig` 4개 |
| `application` 모듈이 아는 것 | 앱별 마커 조합(앱 조립 지식) | 없음(당시에는 마커 애노테이션만 제공) |
| 앱 조립 코드 상한 | `@Import` 1줄 + yml | 부트스트랩 중첩 `ApplicationLayerScanConfig` 1개 + yml |
| 회귀 가드 | 없음 | 앱별 `ApplicationLayerScanConfigTest` 4개 + application `LayerRulesTest#applicationShouldNotDeclareComponentScan`. 단정은 application testFixtures `ApplicationLayerScanAssertions`로 공유 |
| 동작 | — | 변경 없음(스캔 대상·필터·빈 집합 동일) |

## 근거

- 앱 조립 지식은 컴포지션 루트 규칙상 실행 앱 모듈의 것이다. 공유 모듈이 그것을 갖는 상태를 없앤다.
- **부트스트랩에 `@ComponentScan`을 직접 달지 않고 static 중첩 클래스로 둔 이유(Spring 6.1 함정)**: Spring Framework 6.1.5 `ConfigurationClassParser`는 설정 클래스에 직접 붙은 `@ComponentScan`을 먼저 모으고, 하나라도 있으면 메타 애노테이션(`@SpringBootApplication` 안의 `@ComponentScan`)을 무시한다. 직접 달면 앱 자기 패키지 스캔과 Boot의 `TypeExcludeFilter`·`AutoConfigurationExcludeFilter`가 조용히 사라진다. static 중첩 `@Configuration`은 별도 설정 클래스로 처리되어 이 문제가 없다.

## 결과·감수한 손실

- 4앱 변경 전후 싱글톤 빈 집합이 설정 클래스 이름 1건을 빼고 동일함을 기동 로그 diff로 확인했다.
- 기동 시 `com.tastyhouse.application` 클래스패스 스캔이 2회 돈다(중첩 클래스 처리 경로 + 앱 기본 스캔). 두 번째는 `ClassPathBeanDefinitionScanner.isCompatible`로 건너뛰어 빈은 중복되지 않는다. 기동 비용만 미세하게 늘었다.
- 설정 클래스 자신의 빈 이름이 `webApiApplication.ApplicationLayerScanConfig`처럼 바뀌었다(주입하는 곳 없음).
- `AppOwnership`을 `com.tastyhouse.architecture` 패키지로 옮겼다.
- 이후 [ADR-0036](0036-app-marker-removal.md)이 마커를 없애면서 `ApplicationLayerScanConfig`는 include 필터 없는 `@ComponentScan(basePackages = "com.tastyhouse.application")`가 됐다. "부트스트랩의 static 중첩 설정 클래스가 스캔을 소유한다"는 형태는 현재 규칙으로 남았다.

## 영향받은 문서

- `backend/CLAUDE.md` · "모듈 등록 컨벤션 (auto-configuration — 챕터 02)" · 앱은 `@Import`를 갖지 않고, application 계층 스캔은 각 앱 부트스트랩의 static 중첩 `ApplicationLayerScanConfig`가 소유한다.
- `backend/CLAUDE.md` · "스캔 설정" · 중첩 클래스 형태를 유지하는 이유는 Spring 6.1 함정이다.
- `backend/CLAUDE.md` · "앱이 가질 수 있는 조립 코드의 상한" · 상한은 부트스트랩 중첩 스캔 클래스 2개 + `{앱}-application` 의존 1줄 + yml이다.
- `backend/CLAUDE.md` · "비채택 대안과 재고 조건" · 부트스트랩 모듈 분리를 채택하지 않은 이유와 재고 조건.
- `backend/application-core/AGENTS.md` · "빈 배선 — 앱 부트스트랩의 중첩 스캔 설정" · 이 모듈은 스캔 설정을 갖지 않는다.
- `backend/web-api/AGENTS.md`·`backend/admin-api/AGENTS.md`·`backend/ceo-api/AGENTS.md`·`backend/batch-module/AGENTS.md` · "`ApplicationLayerScanConfigTest` — 부트스트랩 스캔 설정의 오기입·직접 선언을 막는다" · 부트스트랩 직접 스캔·`@Import`를 막는 회귀 가드.
