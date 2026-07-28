# core-module → domain-module 전환 작업 (공통 지침)

> 모든 작업 파일(`NN-*.md`)을 실행하기 전에 이 문서를 반드시 먼저 읽을 것.
>
> **[개정]** query DAO의 소유가 "소비 모듈(api)" → **infrastructure-module**로 변경되었다. api 모듈(web/admin/ceo/batch)은 **QueryDSL을 절대 알면 안 된다**(`com.querydsl.*` import·`@QueryProjection` 선언 금지). 또한 소비 모듈의 application 서비스는 도메인당 **CQRS 분리**(`{도메인}CommandService`/`{도메인}QueryService`)로 확정되었다. 구 패턴으로 이미 전환된 notice는 `10-notice.md`가 **재작업 문서**로 개정되어 있다.

## 목표 구조 (확정)

```
domain-module (← core-module 리네이밍, 마지막 Phase에서)
  · domain/model·vo·event·repository(write 포트만)·domain/service(불변식·정책)
  · domain/<ctx>/port (외부 어댑터용 출력 포트)
  · shared, exception — 프레임워크-프리(최종적으로 spring-tx/spring-orm·querydsl-core·apt 제거)
     ↑ implementation
infrastructure-module
  · <ctx>/persistence — write 어댑터(JpaEntity/Mapper/JpaRepository/RepositoryImpl — write 전용으로 축소)
  · <ctx>/query — read 어댑터(CQRS): {도메인}QueryDao(@Repository) + Result DTO(@QueryProjection) + SearchCondition
  · <ctx>/listener — 크로스커팅 이벤트 리스너
  · DomainServiceConfig(도메인 서비스 빈 등록), SpringDomainEventPublisher, QueryDslConfig
     ↑ implementation
web-api / admin-api / ceo-api / batch-module
  · 도메인당 CQRS 분리 application 서비스 + presentation(request/response)
    - {도메인}CommandService(@Transactional): domain write 포트·도메인 서비스만 주입 — 생성/수정/삭제/상태전이
    - {도메인}QueryService(@Transactional(readOnly = true)): infra {도메인}QueryDao만 주입 — 조회 + Response 조립(private 매퍼)
    - 조회만 있는 도메인은 QueryService만 두고, 컨트롤러는 필요한 서비스를 각각 주입
  · QueryDSL 절대 금지: com.querydsl.* import·@QueryProjection 선언·infra ..persistence.. import 금지
    (infra 중 ..query..만 import 허용 — ArchUnit + grep으로 강제)
external-api → domain-module의 port 구현(현행 유지, import 경로만 변경)
```

## 판정 기준 (use case 분류)

| 분류 | 정의 | 행선지 |
|---|---|---|
| (A) 액터 특화 command | 한 애그리거트만 다루거나, 특정 액터 워크플로 | 소비 모듈의 `{도메인}CommandService`로 병합(3단→2단, 패턴 2) |
| (B) read model | QueryService + Result DTO | **infrastructure-module `<ctx>/query/` DAO + DTO**로 이관. 소비 모듈 `{도메인}QueryService`가 DAO를 직접 주입(패턴 3) |
| (C) 불변식 오케스트레이션 | 한 트랜잭션에서 2+ 애그리거트 타입을 load & save | domain `domain/service/` POJO로 하강 — **모듈로 복제 금지** |
| (D) 무상태 정책 | Calculator/Validator | domain `domain/service/`로 단순 이동 |
| (E) 이벤트 리스너 | @EventListener/@TransactionalEventListener | infrastructure-module `<ctx>/listener/`로 이동(특정 api 모듈에 두면 다른 모듈 트리거 시 누락됨) |
| 출력 포트 | `application/port/**` | domain `domain/<ctx>/port/`로 이동 |

## write 포트 잔류 판정 기준 (패턴 4 보조)

domain repository(write 포트)에 **남기는** 메서드:
- 반환/파라미터가 **도메인 모델·VO·원시값**(엔티티, `Optional<엔티티>`, boolean, long)이고,
- **command 경로 또는 도메인 서비스의 트랜잭션 안**에서 소비되는 것 — `findById`/`save`/`delete`/`existsByX`(중복 검증)/`findByNaturalKey`(단건 로드)/검증용 `countByX`/락 획득용 조회.

infra `<ctx>/query/` DAO로 **보내는** 메서드:
- Result DTO·`PageResult` 반환, 조인 투영, 목록·검색·페이징 등 **표현 목적 read** 일체.

판정 질문: **"이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"** — 그렇다면 write 포트 잔류(예: 50-file의 `findFilePath` 값 반환형, 15-admin의 username 단건 조회), 아니라면(화면 조립용) query DAO.

## 공통 패턴

**패턴 1 — 도메인 서비스 (분류 C/D)**: `@Service`/`@Transactional` 금지, 순수 POJO. repository 포트(domain) 주입 가능. 이벤트 발행은 `core/shared/event/DomainEventPublisher` 포트 사용(Phase 0에서 신설). 빈 등록은 infrastructure-module `DomainServiceConfig`에 `@Bean` 추가.

**패턴 2 — 모듈 application 서비스 CQRS 분리 (분류 A)**: 기존 web/admin/ceo의 단일 facade Service(예: `adminapi/shop/ShopService`)를 `{도메인}CommandService`(@Transactional — domain write 포트·도메인 서비스 주입, core CommandService 로직 흡수)와 `{도메인}QueryService`(@Transactional(readOnly = true) — infra `{도메인}QueryDao` 주입, Response 조립 private 매퍼 보유)로 **분해**한다. 조회만 있는 도메인은 QueryService만 둔다. Long→VO 승격·String→enum 승격 등 기존 경계 규칙은 유지. CommandService는 `..query..`를, QueryService는 write 포트를 서로 주입하지 않는다.

**패턴 3 — infra query DAO (분류 B)**: infrastructure-module에 `infrastructure/{도메인}/query/{도메인}QueryDao.java`(@Repository) 신설. 같은 모듈의 `JPAQueryFactory`(`config/QueryDslConfig` 빈)와 `QXxxJpaEntity`를 사용해 투영. **도메인당 DAO 1개, 소비자별 메서드 분리**(예: admin용 `findAllNotices`/web용 `findVisibleNotices` — CLAUDE.md 규칙대로 admin 마커 없이 순수 동작명, 충돌 시 시그니처·`ById` 한정어). 대형 도메인(shop/review 등, 대략 400줄 초과)만 용도별 DAO 분리 허용(예: `ShopSearchQueryDao`). Result DTO(`@QueryProjection` 유지 — infra에 apt 있음)·SearchCondition은 **infra query 패키지 소유**로 이동. admin 전용 Result가 비-admin 형제와 **같은 패키지에 공존해 충돌하면 `Management` 한정어를 유지·부여**한다(과거 "모듈 분리로 충돌이 사라지면 한정어 제거 가능" 문구는 폐지 — 이제 항상 같은 패키지다). 필드 셋이 다른 admin/web Result는 통합하지 않는다(과잉 노출 방지). **소비 모듈이 실제 쓰는 메서드·필드만 이관** — 안 쓰는 것은 삭제.

**패턴 4 — write 포트 순수화**: domain repository 인터페이스에서 read 메서드(`application.dto.result` 반환)를 제거하고 엔티티 반환 CRUD만 남김(위 "write 포트 잔류 판정 기준" 적용). infra RepositoryImpl의 해당 조회 코드는 **같은 모듈의 `<ctx>/query/` DAO(패턴 3)**로 이동.

## 절대 규칙

1. **gradle build 실행 금지** — 검증은 IDE/LSP diagnostics(컴파일 오류 0)로.
2. **커밋·롤백 금지(NO_COMMIT_OR_ROLLBACK)** — 작업 완료 시 추천 커밋 메시지만 제시(`{type}({scope}): 한글 요약`).
3. 답변·문서는 한국어.
4. 자기 도메인 파일 밖의 코드는 **컴파일을 깨뜨린 지점 복구 목적으로만** 수정(크로스 도메인 import 갱신 등). 다른 도메인의 구조 개편은 하지 말 것.
5. 도메인 완료 기준: 해당 도메인의 core `application/` 패키지가 비어(삭제되어) 있고, 전 모듈 LSP 오류 0.
6. DB 스키마·DDL(create.sql/alter.sql) 무변경 — 이 전환은 순수 코드 재배치.
7. **api 모듈(web/admin/ceo/batch)에 QueryDSL 도입 금지** — `com.querydsl.*` import·`@QueryProjection` 선언 0건 유지. ArchUnit 규칙(`com.querydsl..`·`..infrastructure..persistence..` 의존 금지)과 grep으로 검증.

## 실행 순서 (그룹 내 병렬 가능, 그룹 간 순차)

| 순서 | 파일 | 비고 |
|---|---|---|
| 0 | `00-phase0-scaffolding.md` | **모든 작업의 선행 필수** |
| 1 | `10-notice`(**재작업 — 그룹 1 중 최우선**, apt 롤백·ArchUnit 규칙 포함) `11-faq` `12-banner` `13-policy` `14-partnership` `15-admin` `16-bug` `17-search` `18-ceo` | 단순·저결합 |
| 2 | `20-verification` `21-member` `22-point` `23-coupon` `24-event` `25-rank` `26-reservation` | 이벤트·VO 공유 |
| 3 | `30-review` `31-shop` `32-product` | 대형·크로스 조인 |
| 4 | `40-order` `41-payment` | 원자 동기화 클러스터 — **같은 AI가 순서대로 수행 권장** |
| 5 | `50-file` | 허브 — 전 도메인 완료 후 |
| 6 | `99-finalize.md` | 리네이밍·의존 정리·문서 |
