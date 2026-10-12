# ADR-0044: JpaRepository에 메서드를 선언하지 않고 QueryDSL 어댑터로 옮긴다

- 상태: 채택
- 결정 커밋: `65da13f9a` (2026-10-08)
- 대체한 결정: —

## 맥락

- **`JpaRepository`**: Spring Data JPA가 제공하는 리포지토리 인터페이스입니다. `findByUsername` 같은 메서드 이름만 선언하면 쿼리를 만들어 주고(파생 쿼리), `@Query`·`@Modifying`으로 JPQL을 직접 쓸 수도 있습니다.
- **QueryDSL**: 쿼리를 자바 코드로 조립하는 도구입니다. 이 저장소의 조회는 대부분 `JPAQueryFactory`로 씁니다.
- **write 포트 / QueryPort**: 명령 경로가 쓰는 아웃바운드 포트(`port.out.write`)와 조회 유스케이스가 쓰는 읽기 포트(`{Ctx}QueryPort`)입니다.

결정 전에는 `JpaRepository` 57개가 메서드 116개를 선언하고 있었습니다. 같은 조건(`deleted = false`, `shopId` 필터)이 "메서드 이름"과 "QueryDSL" 두 문법으로 갈라져 있었습니다.

## 이전 규칙

### backend/infrastructure/jpa/AGENTS.md(당시 persistence) — `CeoQueryAdapter` 봉인·주석 이관 근거

"`findByUsername`/`existsByUsername`은 불변식 검증 경로이므로 write 포트에 잔류한다. 여기로 옮기지 않는다." 인증·시드 멱등성에 쓰이는 단건 조회는 조회 DAO가 아니라 write 포트에 남았습니다.

### backend/application-core/AGENTS.md — `queryServicesShouldNotDependOnWritePorts` carve-out 3건

조회 유스케이스 서비스는 write 포트를 주입하지 않는다는 규칙에 확정 carve-out 3건이 있었습니다.

| FQN | 근거 |
|---|---|
| `ShopDeliveryTipViewQueryService` (web) | 배달팁 계산 경로가 도메인 서비스에 넘길 애그리거트 로드에 write 포트를 쓴다 — 도메인 계산 입력 |
| `AdminUsernameExistsQueryService` | 인증 계정의 존재 확인(`existsByUsername`, 시드 멱등성 확인)에 write 포트를 쓴다(원문은 Load/Save 분리 후 이름 `AdminLoadPort`로 적혀 있었다). 표현 목적 read model이 없는 원시값 반환 경로 |
| `CeoOwnerUsernameExistsQueryService` | 위 admin과 같은 인증 조회 경로 |

당시 문서는 이 3건을 "각 앱에서 그대로 승계한 확정 판정이며, 이관 대상이 아니다"라고 적었습니다. 세 carve-out은 유스케이스 분리(ADR-0043)로 각각 `ShopQueryService`·`AdminQueryService`·`CeoOwnerQueryService`(당시 이름)에서 write 포트를 실제로 쓰는 연산의 서비스로 1:1 이전된 것입니다.

## 결정

`infrastructure:jpa`의 `XxxJpaRepository`에는 메서드를 선언하지 않습니다. `JpaRepository<E, Long>` 상속 메서드만 씁니다. 조건 있는 조회·삭제는 그 쓰임으로 위치를 정합니다.

| 항목 | before | after |
|---|---|---|
| 리포지토리 선언 메서드 | 57개 리포지토리에 116개(파생 쿼리·`@Query`·`@Modifying`) | 0개 — 같은 패키지 `XxxPersistenceAdapter`의 `JPAQueryFactory` 조회로 이관 |
| 도메인 모델 반환·불변식 검증 조회 | 리포지토리 파생 쿼리 | `XxxPersistenceAdapter`(예: `AdminPersistenceAdapter#findByUsername`·`#existsByUsername`) |
| 원시값을 돌려주는 조회 유스케이스(아이디 중복확인) | write 포트(당시 `AdminPersistencePort` — Load/Save 분리 후 이름은 `AdminLoadPort`) | `AdminQueryPort`·`CeoOwnerQueryPort`(구현은 `..query..`의 QueryAdapter) |
| `queryServicesShouldNotDependOnWritePorts` carve-out | 3건 | 1건(`ShopDeliveryTipViewQueryService`) |
| 가드 | 없음 | jpa `LayerRulesTest#jpaRepositoriesShouldNotDeclareMethods` |

## 근거

- 같은 조건이 두 문법으로 갈라지면 한쪽만 고쳐지는 일이 생깁니다. 조회 문법을 QueryDSL 하나로 모읍니다.
- admin·ceo 아이디 중복확인 서비스는 원시값을 돌려주는 조회 유스케이스라 write 포트가 아니라 QueryPort가 맞습니다. 같은 `existsByUsername`을 `AdminCreateService`·`CeoCreateService`가 생성 중복검사에 쓰므로 write 포트 메서드도 남습니다. 같은 행을 읽는 메서드가 두 포트에 있는 것은 목적이 달라 허용됩니다.
- QueryAdapter로 일원화하지 않는 이유: `..query..`는 domain을 볼 수 없고, persistence → query 의존은 금지이며, 명령 서비스는 QueryPort를 주입하지 못합니다.

## 결과·감수한 손실

- 동작은 바뀌지 않습니다. 파생 delete는 읽은 뒤 `deleteAll`, `@Modifying`은 flush→delete→clear 순서를 유지하고, 파생 쿼리의 null→IS NULL 의미도 `isNull()` 헬퍼로 보존했습니다.
- **대가**: 파생 쿼리는 부팅 때 검증됐지만 QueryDSL은 실행 시점에만 검증됩니다. 조건을 고치면 해당 엔드포인트를 한 번 호출해 확인해야 합니다.
- 상속 메서드 `flush()` 호출은 허용됩니다(선언 금지 규칙은 선언만 막음).

### 후속 결정 — 조회 서비스의 write 포트 간접 읽기 4건을 QueryPort로 이관 (커밋 `7af2291c1`, 2026-10-09)

이 결정은 조회 서비스가 write 포트를 **직접** 주입하는 경우만 정리했습니다. 이후 조회 서비스가 **도메인 서비스를 거쳐** write 포트를 읽고 그 값을 화면에 내보내던 간접 경로 4건도 QueryPort로 옮겼습니다. 동작은 바뀌지 않습니다(쿼리 조건·정렬·소유 검증 `PRODUCT_NOT_FOUND` 404를 그대로 옮김).

| 경로 | before | after |
|---|---|---|
| ceo 상품 가격 목록 | `ProductPriceQueryService` → `ProductPriceService#findPrices` → `ProductPricePersistencePort#findAllByProductId` | `ProductOwnerQueryPort#findPrices` → `ProductOwnerPriceView` |
| ceo 상품 노출 시간 | `ProductExposureQueryService` → `ProductExposureService#findHours` → `ProductExposureHourPersistencePort#findAllByProductId` | `ProductOwnerQueryPort#findExposureHours` → `ProductExposureHourResult`. 같은 서비스의 노출 여부(`ProductExposureService#evaluate`)는 계산 입력이라 그대로 둠 |
| ceo 가격 검수 최신 건 | `ShopStorePriceVerificationQueryService` → `StorePriceVerificationOwnerReader#readLatest`(이후 위임 래퍼 제거로 삭제) → `StorePriceVerificationPersistencePort#findLatestByShopId` | 신설 `StorePriceVerificationOwnerQueryPort#findLatestByShopId` → `StorePriceVerificationOwnerLatestResult` |
| web 내 등급 | `MemberMyGradeQueryService` → `MemberGradeService#getMyGrade` → `MemberReviewRankPersistencePort#findLatestByMemberIdAndRankType` | `RankQueryPort#findLatestReviewCount`로 리뷰 수만 읽고 `MemberGrade` 계산은 조회 서비스가 함. `MemberGradeService`는 삭제 |

- **쿼리는 글자 그대로 옮깁니다.** 원본에 `orderBy`가 없으면 추가하지 않습니다(노출 시간 목록). 정렬을 고치는 것은 동작 변경이라 별도 결정입니다.
- **소유 검증의 의미를 바꾸지 않습니다.** 가격 목록은 원래 `product.shopId` 일치 + 미삭제가 아니면 404였습니다. 같은 조건을 갖는 `ProductOwnerQueryPort#findExposurePeriod`의 `shopId`를 비교해 재현했고, shop **링크** 기준인 `existsProductInShop`은 의미가 달라 쓰지 않았습니다.
- **이 판정은 ArchUnit으로 강제하지 못합니다.** "서비스가 write 포트로 읽은 값이 화면으로 나가는가"는 정적으로 판정할 수 없어 리뷰가 지킵니다. 조회 유스케이스에 도메인 서비스를 새로 주입할 때 위 표로 확인합니다.

## 영향받은 문서

- `backend/CLAUDE.md` · JpaRepository 메서드 선언 금지 규칙 (조건 있는 조회는 QueryDSL 어댑터가 소유) · 쓰임별 위치 표·의미 보존·대가
- `backend/CLAUDE.md` · write 포트 잔류 판정 기준 (domain repository에 남길 조회의 경계)
- `backend/infrastructure/jpa/AGENTS.md` · 봉인·가드 목록 > `jpaRepositoriesShouldNotDeclareMethods` — JpaRepository에 메서드를 선언하지 않는다
- `backend/infrastructure/jpa/AGENTS.md` · "`CeoQueryAdapter` — 인증 로드·생성 중복검사는 write 포트에 남는다" · 인증 로드는 write 포트, 아이디 중복확인은 QueryPort
- `backend/application-core/AGENTS.md` · `queryServicesShouldNotDependOnWritePorts` carve-out 3건 — 목록에 새 항목을 추가하지 않는다 · 제목은 앵커 보존용, 남은 carve-out 1건
