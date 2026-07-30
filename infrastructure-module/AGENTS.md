# infrastructure-module

core-module의 순수 도메인 모델을 영속화하는 **persistence 어댑터 모듈**. 헥사고날 아키텍처에서 `core-module`이 선언한 Repository 포트(`domain/repository/XxxRepository` 인터페이스)를 JPA/QueryDSL로 구현한다. `external-api`가 파일/OAuth 어댑터를 담당하는 것과 같은 원리로, DB 어댑터를 core 밖으로 분리해 "core는 JPA 구현을 모른다"를 모듈 경계로 강제한다.

## 패키지 구조

```
com.tastyhouse.infrastructure.<도메인>.persistence/
├── XxxJpaEntity.java       @Entity — DB 매핑 전용(비즈니스 행위 없음), core.shared.entity.BaseEntity 상속
├── XxxMapper.java          도메인 ↔ 엔티티 변환 (package-private, toDomain/toEntity/applyChanges)
├── XxxJpaRepository.java   Spring Data JpaRepository<XxxJpaEntity, Long>
└── XxxRepositoryImpl.java  @Repository — core의 XxxRepository 구현 (QueryDSL 위임)
```

## 규칙

- **패키지 루트는 `com.tastyhouse.infrastructure`** — web-api/admin-api의 `scanBasePackages`에 이 패키지가 등록되어 있어야 빈이 인식된다. JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)은 이 모듈 패키지 루트의 `InfrastructurePersistenceConfig`가 `basePackageClasses`(타입 세이프)로 스스로 선언한다 — core의 `DatabaseConfig`에 이 패키지를 문자열로 넣지 않는다(core는 이 모듈을 의존하지 않아 컴파일 타임에 패키지를 볼 수 없음).
- **웹/관리 API는 이 모듈을 `runtimeOnly`로만 의존**한다 — 컴파일 타임에 어댑터 구현을 보지 못하게 해 은닉한다.
- **JPA 엔티티(`XxxJpaEntity`)는 영속 전용**: 행위 메서드를 두지 않고, 신규 생성용 정적 팩토리 `create(...)`와 update 복사용 `applyChanges(...)`만 둔다. 감사 필드는 `BaseEntity`(`@MappedSuperclass`)에서 상속.
- **저장 시맨틱은 load-copy-save**: `save(domain)`에서 id null이면 insert, id 있으면 managed 엔티티 조회 후 `applyChanges` 복사. detached merge 금지(감사 필드 파손 방지).
- **Q타입 생성 위치 주의**: `QXxxJpaEntity`는 이 모듈에서 생성(엔티티가 여기 있으므로). `@QueryProjection` result DTO의 `QXxxResult`는 그 DTO가 있는 모듈에서 생성된다 — CQRS 전환이 끝난 도메인은 Result DTO가 이 모듈 `<ctx>/query/`에 있어 여기서 생성되고, 아직 전환되지 않은 도메인은 core `application/dto/result`에 남아 core-module에서 생성된다. 양쪽 build.gradle 모두 QueryDSL apt를 유지한다.
- **QueryDSL은 이 모듈 안에 갇힌다**: `querydsl-jpa`는 `api`가 아니라 `implementation`으로 의존해 소비 모듈(web/admin/ceo/batch)에 전이 노출되지 않는다. api 모듈은 `<ctx>/query/`의 DAO와 Result DTO만 주입·import하며, `com.querydsl..`·`..persistence..` 의존은 각 api 모듈 `architecture/LayerRulesTest`(ArchUnit)가 차단한다.
- **엔티티 enum 매핑**은 core AGENTS.md와 동일: `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "VARCHAR(n)")`, `ORDINAL` 금지.

reference 구현: `notice` 도메인 — write 어댑터 `notice/persistence/`(`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl` — 단건 로드·저장만), read 어댑터 `notice/query/`(`NoticeQueryDao` + `NoticeManagementListItemResult`/`NoticeListItemResult`/`NoticeDetailResult`/`NoticeSearchCondition`).

## `<ctx>/query/` — read 어댑터 (CQRS query 측)

표현 목적 조회(목록·검색·페이징·상세)는 write 포트(`XxxRepository`)가 아니라 이 패키지의 `{도메인}QueryDao`(`@Repository`)가 담당한다. DAO는 같은 모듈의 `JPAQueryFactory`와 `QXxxJpaEntity`로 JPA 엔티티에서 Result DTO로 **직접 투영**하며(도메인 모델을 거치지 않음), Result DTO·SearchCondition도 이 패키지가 소유한다.

- **도메인당 DAO 1개, 소비자별 메서드 분리**: admin용/web용 메서드를 한 DAO에 둔다. 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다(`findAllNotices`=비노출 포함 전체 / `findVisibleNotices`=노출분만). 대형 도메인(shop/review 등, 대략 400줄 초과)만 용도별 DAO 분리를 허용한다.
- **Result 이름 충돌 시 `Management` 한정어**: admin 전용 Result가 비-admin 형제와 같은 패키지에 공존해 충돌하면 `Management`를 부여한다(`NoticeManagementListItemResult` vs `NoticeListItemResult`). 필드 셋이 다른 admin/web Result는 통합하지 않는다(과잉 노출 방지).
- **write 포트 잔류 판정**: "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?" — 그렇다면 write 포트에 남기고(`findById`/`existsByX`/락 획득용 조회), 화면 조립용이면 이 DAO로 보낸다.
- **소비 모듈이 실제 쓰는 메서드·필드만 이관**한다(미사용은 삭제).

**대형 도메인 용도별 DAO 분리 reference: `shop`** — 소비 모듈 3개(web/admin/ceo)가 함께 쓰는 최대 도메인이라 DAO를 용도별로 3개로 나눴다.

| DAO | 담당 |
|---|---|
| `ShopQueryDao` | 가게별 설정·관리 조회(전화번호·편의정보·콘텐츠보드·위생뱃지·이미지 변경요청·편의시설/음식유형 카테고리·배정·배너·사진) |
| `ShopSearchQueryDao` | 목록·검색 대형 조인(지도 마커·베스트·최신·키워드 검색·즐겨찾기·관리 목록) |
| `ShopChoiceQueryDao` | 가게에 종속되지 않는 독립 조회(에디터 추천 목록·전역 태그·역 목록) |

- 목록 조회는 페이지 대상 가게를 먼저 뽑고 역·썸네일·음식유형·리뷰수·즐겨찾기수를 shopId 일괄 조회(in절)로 채운다 — 컬렉션 필드(음식유형 다건)가 있어 단일 조인 투영은 카티전 곱이 생기기 때문이다.
- **필드 셋이 달라 Result를 통합하지 않은 사례**: 사진 카테고리 이미지 조회는 회원용 `ShopPhotoCategoryImageResult`(노출분 표시용)와 관리용 `ShopPhotoCategoryImageManagementResult`(`visible` 포함 — 관리 화면은 미노출 이미지도 상태와 함께 보여줘야 함)로 나뉜다. 같은 패키지에 공존해 충돌하므로 `Management` 한정어를 부여했다.
- **write 포트 잔류 판정이 갈린 사례**: `findBusinessHoursByShopId`·`findBreakTimesByShopId`·`findClosedDaysByShopId`·`findByShopId`(임시중지·임시휴무)는 표현용으로도 쓰이지만 **휴게시간 범위 검증·정기휴무 개수 제한·영업 상태 판정**이라는 불변식에 필요하므로 write 포트(`ShopDetailRepository` 등)에 남겼다. 반면 Result DTO를 반환하던 카테고리·배정·배너·사진 목록은 전부 DAO로 보냈다.

## 설정 파일 (`src/main/resources/application-infrastructure.yml`)

이 모듈이 실제로 구현·소비하는 datasource/hibernate(`ddl-auto`)/mysql driver/`spring.sql.init` 등 JPA·DB 설정을 이 모듈의 `application-infrastructure.yml`이 소유한다(과거 `core-module`의 `application-core.yml`이었으나, core-module이 100% JPA-free로 전환되며 이 모듈로 이동·리네이밍됨). `web-api`/`admin-api`의 `application.yml`이 `spring.config.import: classpath:application-infrastructure.yml`로 로딩하며, 이는 이미 참조 중인 `application-external.yml`(external-api 소유)과 동일한 패턴이다.

## Dependencies

### Internal
- `core-module` (api) — 도메인 모델·Repository 인터페이스·result DTO·`BaseEntity` 참조

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (api, OpenFeign 포크 — CVE-2024-49203 대응. 패키지명 `com.querydsl.*` 유지, 6.x부터 jpa는 `:jakarta` classifier 없이 jakarta 기본·apt만 `:jakarta` 유지)
- Lombok

<!-- MANUAL: -->
