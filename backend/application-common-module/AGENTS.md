# application-common-module

읽기 경로(CQRS query 측)의 **계약**만 소유하는 프레임워크-프리 `java-library` 모듈. 인터페이스(`{Ctx}QueryPort`)와 그 입출력 타입(`*Result`/`*SearchCondition`)을 여기 두고, 구현(QueryDSL DAO)은 `infrastructure-module`의 `<ctx>/query/`가 담당한다. 읽기 경로 포트화(챕터 04)로 신설됐다.

## 신설 배경

챕터 03까지 api 모듈(web/admin/ceo/batch)의 `{도메인}QueryService`는 `infrastructure-module`의 `{도메인}QueryDao`(구현 클래스)를 컴파일 타임에 직접 주입했다. api 모듈이 `implementation project(':infrastructure-module')`로 의존해야 했던 이유가 이것이었고, 은닉은 `..persistence..`(write 어댑터)·`com.querydsl..` 금지 ArchUnit 규칙이 대신했다 — 그래도 `..query..`(DAO·Result·SearchCondition) import는 허용돼 있었다.

이 구조를 완전 매핑 전략(인바운드 포트 도입과 같은 결)으로 역전하려면 읽기 쪽에도 인터페이스가 필요했다. 그런데 **infra가 구현해야 하는 인터페이스는 api 모듈 안에 둘 수 없다** — 빌드 그래프가 api → infra 단방향이고, infra의 `LayerRulesTest#shouldNotDependOnApiModules`가 infra → api 역방향 의존을 막기 때문이다. `domain-module`에 두는 방안도 기각됐다 — Result record 184개가 표현 목적 투영(화면 조립용 DTO)이라 순수 도메인에 섞이면 [write 포트 잔류 판정 기준](../CLAUDE.md#write-포트-잔류-판정-기준-domain-repository에-남길-조회의-경계)이 흐려진다. 남은 선택지는 **계약 전용 신규 모듈**뿐이었다.

이 판단은 [인바운드 포트를 도입한다](../CLAUDE.md#인바운드-포트usecase-인터페이스를-도입한다--완전-매핑-전략-채택-과거-결정의-명시적-번복) 절이 "컨텍스트별 모듈 분할은 하지 않는다"고 선언하면서도 이 모듈 하나만 **단일 예외**로 둔 이유이기도 하다 — 25개 컨텍스트 각각을 모듈로 쪼개는 것과 달리, api 모듈 4개가 공유해야 하는 인터페이스를 위한 모듈 1개 신설은 빌드 그래프 복잡도를 거의 늘리지 않는다.

## 소유 범위

- **`{Ctx}QueryPort` 인터페이스** (73개): 조회 계약. 메서드명은 [query DAO 소유 규칙](../CLAUDE.md#query-daoqueryport결과-dtosearchcondition-소유-규칙-개정--읽기-계약은-application-common-module-구현은-infrastructure-module)의 관례(admin 마커 없는 순수 동작명)를 그대로 승계한다.
  - **DAO와 1:1이 아니다**(챕터 04). 한 DAO의 public 표면이 여러 앱의 조회를 담고 있으면 [소비자별 분할 규칙](../CLAUDE.md#조회-포트-소비자별-분할-규칙-포트명은-반환-result-계열을-승계--챕터-04)에 따라 앱별 인터페이스로 쪼개고, **DAO 하나가 그 포트들을 전부 `implements`** 한다(예: `ShopQueryDao` → `ShopQueryPort`·`ShopBasicInfoQueryPort`·`ShopManagementQueryPort`·`ShopOwnerQueryPort`). 투영 본문은 복제되지 않으므로 늘어나는 것은 선언뿐이다.
  - **포트명은 반환 `Result` 계열을 승계한다** — `Management`(관리 화면)·`Owner`(점주 관리 화면, 형제가 `Management`를 점유했을 때) 한정어 사용법은 위 규칙 문서를 따른다.
  - **application 소비자가 없는 조회는 포트에 두지 않는다** — infra 내부 전용은 DAO의 평범한 public 메서드로 남긴다(`ShopQueryDao#findShopName`).
- **`*Result` record** (184개+): 조회 결과 반환 타입. [결과 DTO 접미어 규칙](../CLAUDE.md#결과-dto-접미어-규칙-result로-통일-dto-금지)의 `Result` 접미어·`Management` 한정어 규칙을 그대로 따른다. **`public` 필수** — `Projections.constructor`가 리플렉션으로 생성자를 찾으므로 package-private이면 컴파일은 통과하고 호출 시점에만 500이 난다.
- **`*SearchCondition` record** (20개+): 포트 메서드의 동적 검색 조건 파라미터. 필드는 HTTP 경계에서 넘어온 원시타입(`String`/`Long`/`Boolean`)이다.
- **포트 2종** (도메인 전용이 아닌 것): `com.tastyhouse.application.shared.port.out.GeoRingsPort`(저장된 도형 문자열을 도메인 기하 타입으로 해독 — 인코딩 형식은 영속 계층 지식이라 `infrastructure-module`의 `GeoRingsResolver`가 구현한다. 조회가 아니라 변환만 있어 이름에 `Query`를 붙이지 않았다), `com.tastyhouse.application.product.port.out.ProductBatchItem`(배치 조회 포트 메서드의 입력 타입이라 이 모듈로 이동).

## 패키지 규칙

```
com.tastyhouse.application.<ctx>.port.out
├── {Ctx}QueryPort.java        읽기 포트 인터페이스
├── {용도}Result.java          조회 결과 record (public 필수)
└── {도메인}SearchCondition.java  검색 조건 record
```

컨텍스트 slug는 `domain-module`의 바운디드 컨텍스트명을 그대로 따른다(`notice`·`shop`·`order` 등). 대형 도메인은 용도별로 포트를 더 나눈다(예: `shop`의 `ShopQueryPort`/`ShopSearchQueryPort`/`ShopChoiceQueryPort` — 대응 DAO 분리와 짝을 이룬다). 여기에 더해 **같은 DAO의 계약도 소비 앱별로 갈린다**(`ShopQueryPort`/`ShopManagementQueryPort`/`ShopOwnerQueryPort`).

## 프레임워크-프리인 이유

`domain-module`과 동일한 컴파일 게이트를 적용한다 — 루트 `build.gradle`의 spring 주입 블록(`configure(subprojects.findAll { it.name != 'domain-module' && it.name != 'application-common-module' })`)이 이 모듈을 domain-module과 함께 **제외**하고, 바로 아래 `project(':application-common-module')` 블록에서 `java` + `io.spring.dependency-management`(BOM으로 버전 고정만, 의존 추가 없음)만 적용한다. 그 결과 이 모듈의 컴파일 클래스패스에 `org.springframework.*`가 없어 **`import org.springframework.stereotype.Component;` 한 줄이 컴파일 에러**가 된다.

- **왜 프레임워크가 없어야 하는가**: 이 모듈이 선언하는 것은 "무엇을 조회할 수 있는가"라는 계약이지 "어떻게 구현하는가"가 아니다. 프레임워크 의존을 허용하면 계약이 특정 런타임(Spring)에 묶이고, api 모듈이 이 모듈을 통해 간접적으로 QueryDSL·JPA 세부사항을 다시 들여다볼 여지가 생긴다.
- **`org.springframework.boot` 플러그인을 적용하지 않으므로 `bootJar` 태스크 자체가 없다** — `domain-module`과 동일하게 `bootJar { enabled = false }`를 쓰면 스크립트 평가 에러가 난다. 실행 가능한 산출물이 아니라 plain jar만 생성한다(`application-common-module/build.gradle`의 `jar { enabled = true; archiveClassifier = '' }`).
- **테스트도 spring-free**: `spring-boot-starter-test` 대신 실제로 쓰는 `junit-jupiter`·`assertj-core`만 선언한다.

## 의존

- **`api project(':domain-module')`** 하나뿐. `PageQuery`/`PageResult`(domain의 `shared/page`)를 포트 메서드의 페이징 입출력 타입으로 참조하기 위함이다. `api`로 노출해야 이 모듈을 의존하는 api 모듈이 그 타입을 함께 볼 수 있다.
- **QueryDSL 의존은 없다.** 이 모듈의 Result record는 `@QueryProjection`을 달지 않는다 — `infrastructure-module`의 DAO가 `Projections.constructor(XxxResult.class, ...)`로 투영한다. querydsl-apt를 이 모듈에 붙이는 것은 QueryDSL이 infra 밖으로 새는 것을 막아 온 확정 결정([api 모듈 QueryDSL·infra 전면 금지 규칙](../CLAUDE.md#api-모듈-querydslinfra-전면-금지-규칙-archunit-강제--챕터-04로-완료))의 역행이라 **금지**한다.

## `Projections.constructor` 전환 시 주의사항

Result record가 이 모듈로 이동하며 `@QueryProjection`(컴파일 타임 검증)을 쓸 수 없게 됐다. `infrastructure-module`의 DAO는 select 절을 아래처럼 조립한다.

```java
// before (챕터 03까지, infrastructure-module 소유 시절): @QueryProjection 생성자
.select(new QNoticeManagementListItemResult(notice.id, notice.title, notice.content, notice.visible, notice.createdAt))

// after (챕터 04, application-common-module 소유): Projections.constructor
.select(Projections.constructor(NoticeManagementListItemResult.class,
    notice.id, notice.title, notice.content, notice.visible, notice.createdAt))
```

- **`Projections.constructor`는 리플렉션으로 런타임에 생성자를 찾는다.** 대상 record가 `public`이 아니면(package-private) 컴파일은 통과하고 **그 쿼리가 처음 호출되는 순간에만 500**이 난다 — 이 저장소에는 `ShopRiderGuidePickupPresenceResult`가 이 함정에 빠져 admin "라이더 안내 검수" 목록 조회가 전부 500이 났던 실제 사고 선례가 있다.
- **select 절 인자 개수·타입·순서가 record 생성자와 일치해야 한다.** 불일치도 컴파일에 걸리지 않고 런타임에만 드러난다. 전환하는 select 절마다 record 컴포넌트 순서와 select 인자 순서를 하나씩 대조하고, 컨텍스트 전환 직후 해당 조회 경로의 기존 테스트를 실행한다.
- **가드 테스트가 이 컴파일 게이트 상실을 보완한다**: `infrastructure-module`의 `ProjectionConstructorMatchingTest`가 select 절의 인자 개수·타입 나열이 대상 record의 public 생성자 시그니처와 일치하는지 소스 스캔으로 검증한다. `QueryResultRecordVisibilityTest`(같은 목적의 기존 가드, public 여부만 검사)는 스캔 대상 패키지를 `com.tastyhouse.application..port.out..`으로 옮겨 이 모듈의 Result를 대상으로 삼는다.
- **테스트가 없는 DAO는 생성자 매칭 스모크 테스트를 추가한다**: 위 두 가드가 정적 검증을 하더라도, 실제 쿼리 실행 경로(빈 결과가 아니라 실제 행을 반환하는 경로)의 회귀 테스트가 없으면 500 여부를 CI에서 잡을 수 없다.
- **`FileUrlResolver` 재조립(`withResolvedXxx` 패턴)은 무수정**이다 — DAO가 fetch 직후 Result를 재조립하는 로직은 Result의 소유 모듈이 바뀌어도 그대로 동작한다([파일 URL 조립 위치 규칙](../CLAUDE.md#파일-url-조립-위치-규칙-query-dao가-fileurlresolver로-완성) 참고).

## 소비 배선

- `infrastructure-module` — `implementation`으로 의존. `{Ctx}QueryDao`가 이 모듈의 `{Ctx}QueryPort`를 `implements`하고, Result record를 `Projections.constructor`로 투영한다.
- `web-api`/`admin-api`/`ceo-api`/`batch-module` — `implementation`으로 의존. `{도메인}QueryService`(또는 batch의 `*SchedulerService`)가 이 모듈의 `{Ctx}QueryPort` 인터페이스를 주입하며, `infrastructure-module`의 DAO 구현체나 `com.tastyhouse.infrastructure..` 패키지는 전혀 import하지 않는다(각 모듈 `LayerRulesTest`가 강제. 챕터 04의 임시 장치 `shouldNotDependOnInfrastructureQuery`는 챕터 05에서 제거됐다).

## Dependencies

### Internal
- `domain-module` (api) — `PageQuery`/`PageResult`(`shared/page`) 참조용

### External
- 없음(production 의존 0개). 테스트: `junit-jupiter`, `assertj-core`

<!-- MANUAL: -->
