# ADR-0033: 서비스 간 협력도 UseCase 인터페이스로 주입하고 구체 클래스를 주입하지 않는다

- 상태: 채택
- 결정 커밋: `9b0582c33` (2026-10-01)
- 대체한 결정: —

## 맥락

**UseCase 인터페이스**(인바운드 포트, `port.in`)는 컨트롤러가 application을 부르는 진입 계약입니다([ADR-0005](0005-inbound-port-full-mapping.md)). 서비스 클래스(`*QueryService`·`*CommandService`)가 이를 구현합니다.

챕터 03에서 인바운드 포트를 도입할 때, 스펙 문언은 "`QueryService`의 public 메서드를 인터페이스로 전사한다"였습니다. 그런데 public 메서드에는 컨트롤러가 부르는 것과 **다른 서비스가 부르는 협력용**이 섞여 있었습니다. 협력용 메서드는 도메인 모델이나 infra `*Result`를 그대로 주고받았기 때문에, 인터페이스로 올리면 `commandRecordsShouldBeBoundaryTyped`(`port.in`의 domain·infrastructure 의존 금지)에 걸렸습니다. 그래서 협력 서비스는 구체 클래스를 주입하는 길을 택했고, 그 결과 UseCase를 구현한 서비스를 다른 서비스가 구체 타입으로 주입하는 곳이 28건(24개 파일) 생겼습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — `QueryUseCase`에는 컨트롤러 표면만 올린다 — 협력용 public 메서드를 전사하지 않는다

스펙 §3의 목적은 "컨트롤러 주입 타입 교체"이므로, 인터페이스에는 컨트롤러 표면만 올리고 협력용 메서드는 구체 클래스에 그대로 둔다. 이러면 조회 로직 diff 0을 유지하면서 두 ArchUnit 규칙을 모두 만족한다. 이것은 규칙을 무르게 하는 것이 아니라, 인바운드 포트가 애초에 겨냥한 표면(컨트롤러 경계)에 대상을 한정하는 것이다.

당시 걸리던 협력용 메서드는 9개였고(챕터 03 시점에는 `MemberQueryService#getMember` 포함 10개였으나, 그 경로가 `MemberAuthService`의 `memberRepository` 직접 로드로 바뀌어 사라졌다), 전부 다른 서비스가 호출했다.

| 메서드 | 반환 | 호출부 |
|---|---|---|
| `AdminQueryService#findByUsername` | `Optional<Admin>` | `TokenService`·`UserDetailsService` |
| `CeoOwnerQueryService#findByUsername` | `Optional<Ceo>` | 위와 동일 |
| `ProductQueryService`의 `findPopularProducts`·`findShopProductCategories`·`findShopProducts`·`searchByKeyword` | `*Result`·`PageResult` | `ShopQueryService`·`SearchQueryService` |
| `ReviewQueryService`의 `findMyReviews`·`findShopReviewStatistics`·`findShopReviewsByRating` | 위와 동일 | 상동 |

판별법: `port/in` 인터페이스 파일에 `com.tastyhouse.domain.`·`com.tastyhouse.infrastructure.` import가 생기면 잘못 올린 것이다(carve-out 제외). carve-out은 처음에 `domain.exception..`(에러 계약)과 `domain.shared.page..`(페이징 계약) 둘이었습니다(실측: `port/in` 전체의 domain import는 `domain.exception` 602건 · `domain.shared.page` 43건, 그 밖은 0건). 이후 덩어리 01로 페이징 계약이 `com.tastyhouse.application.shared.port.out.page`로 옮겨가 페이징 43건은 domain import가 아니게 됐고, carve-out은 `domain.exception..` 하나가 됐습니다. 새 조회를 추가할 때 컨트롤러가 부르지 않는 메서드라면 `*QueryService`에만 두고, **협력 서비스는 인터페이스가 아니라 구체 클래스를 주입해 쓴다.**

원래 표는 `findByUsername` 호출부에 "시더"도 적었다. 하지만 이것은 당시에도 틀린 서술이었다. `AdminSeeder`·`CeoSeeder`는 `existsByUsername`만 호출한다.

## 결정

UseCase를 구현한 클래스는 application 안에서도 구체 타입으로 주입하지 않습니다.

| 항목 | before | after |
|---|---|---|
| 서비스 간 협력 주입 | 구체 `XxxService` 주입(28건, 24개 파일) | `port.in` UseCase 인터페이스 주입(0건) |
| 이미 UseCase에 있던 메서드 호출(11개 파일) | 구체 타입으로 주입 | 주입 타입만 인터페이스로 교체 |
| 반환 타입이 domain-free인 협력 메서드 9개 | 구체 클래스에만 존재 | `ReviewQueryUseCase`·`ProductQueryUseCase`에 선언(본문 불변) |
| 도메인 타입을 주고받는 단순 위임(`Admin`/`CeoQueryService#findByUsername`, `MemberCommandService#signUp`·`signUpSocial`·`saveSocialAccount`) | 구체 클래스에 두고 협력 서비스가 호출 | 삭제. 호출부가 write 포트·`MemberRegistrationService`·`PasswordEncoder`를 직접 주입 |
| 트랜잭션 경계 | 위임 대상 서비스의 클래스 `@Transactional`이 제공 | 호출부로 같은 속성을 옮김(`CredentialLoginService#signUp`에 `@Transactional`, `Admin`/`CeoTokenService#refresh`에 `readOnly`) |
| 컨트롤러 UseCase 필드명(web-api·ceo-api 69곳) | `*Service` | `*UseCase` |
| 가드 | — | `servicesShouldDependOnUseCasesNotImplementations`, `useCaseFieldsShouldBeNamedUseCase`(5개 모듈), `commandServicesShouldNotDependOnQueryPorts`에 `*QueryUseCase` 추가 |

## 근거

- 협력에 필요한 것이 domain-free 타입이면 UseCase에 선언해도 `port.in` 경계 규칙을 어기지 않습니다. 그러니 구체 주입을 할 이유가 없습니다.
- 도메인 타입을 쓰는 위임은 UseCase에 올릴 수 없습니다. `port.in` 규칙에 걸리고, 비밀번호 해시를 담은 `Admin`/`Ceo` 도메인을 컨트롤러가 닿는 인터페이스로 노출하게 되기 때문입니다. 그래서 위임을 없애고 실제 협력자(write 포트·도메인 서비스)를 직접 주입합니다.
- `commandServicesShouldNotDependOnQueryPorts`에 `*QueryUseCase`를 추가해, 협력 주입을 UseCase로 바꾸면서 생길 수 있는 CQRS 우회를 막습니다.

## 결과·감수한 손실

- HTTP 응답·DB·에러코드는 바뀌지 않았습니다. `refresh`의 트랜잭션 범위는 조회 한 번에서 메서드 전체로 넓어졌지만, 나머지 작업이 Redis 호출이라 동작은 같습니다.
- UseCase에 올린 협력 메서드는 컨트롤러 표면이 아닙니다. 컨트롤러가 이런 메서드를 새로 호출하려면 그 화면 계약이 맞는지 먼저 확인해야 합니다.
- 도메인 타입 위임을 걷어낸 호출부가 직접 주입한 협력자는 `AdminPersistencePort`/`CeoPersistencePort`, 마커 없는 도메인 서비스 `MemberRegistrationService`, `PasswordEncoder`, `MemberSocialAccountPersistencePort`입니다(전부 당시 이름 — write 포트는 이후 [ADR-0046](0046-write-port-load-save-split.md)으로 Load/Save 분리). `CredentialLoginService#signUp`의 `@Transactional`은 회원 저장과 추천 등록의 원자성을 지키기 위한 것입니다.
- UseCase에 올린 domain-free 협력 메서드 9개는 `ReviewQueryService`의 `findShopReviewsByRating`·`findShopReviewStatistics`·`countVisibleReviewsByMemberId`·`findReviewedProductIds`·`findMyReviews`, `ProductQueryService`의 `searchByKeyword`·`findShopProducts`·`findPopularProducts`·`findShopProductCategories`입니다. 반환 타입이 전부 `port.out` `*Result`라 `commandRecordsShouldBeBoundaryTyped`를 통과합니다. 유스케이스 분리 후에는 각각 `ReviewShopByRatingQueryUseCase`·`ReviewShopStatisticsQueryUseCase`·`ReviewMemberCountQueryUseCase`·`ReviewWrittenProductIdsQueryUseCase`·`ReviewMyListQueryUseCase`, `ProductKeywordSearchQueryUseCase`·`ProductByShopQueryUseCase`·`ProductPopularQueryUseCase`·`ProductCategoryByShopQueryUseCase`에 하나씩 선언됐습니다. 예를 들어 `ReviewMyListQueryUseCase#findMyReviews`는 `MemberMyReviewListQueryService`가 쓰는 협력 메서드입니다(위임 래퍼 `MemberReviewService`는 삭제됨).
- 같은 시그니처 주의: `ReviewMemberListQueryUseCase#findMemberReviews`와 `ReviewMyListQueryUseCase#findMyReviews`는 둘 다 `(Long, int, int) → PageResult<MyReviewListItemResult>`입니다(분리 전에는 `ReviewQueryUseCase` 한 포트의 두 메서드). `findMemberReviews`는 `visibleToCustomer()` 조건이라 고객에게 보이는 리뷰만 나오고 `ownerOnly`가 항상 `false`이며, `findMyReviews`는 `hidden = false` 조건만 걸려 점주에게만 공개한 리뷰도 포함하고 `ownerOnly`에 실제 값이 들어갑니다. 구현은 둘 다 `ReviewQueryAdapter`의 `findReviewsByMemberId`·`findMyReviews`입니다.
- 가드 위반이 나면 `@SuppressWarnings`나 예외 목록을 두지 않고, 호출부가 UseCase 인터페이스를 주입하게 바꿉니다. 필요한 메서드가 도메인 타입을 쓰면 write 포트나 도메인 서비스를 직접 주입합니다.
- 반증으로 확인했습니다. 구체 서비스를 주입하는 probe 클래스를 넣으면 가드가 실패합니다. 가드는 공허 통과를 막기 위해 구현 집합 크기 ≥ 200도 함께 단정합니다(도입 시점 실측 202).
- 이후 유스케이스 분리([ADR-0043](0043-usecase-per-service.md))로 협력 메서드는 per-op 포트(`ReviewMyListQueryUseCase` 등)에 하나씩 선언됐습니다. "한 줄 위임만 하는 협력 빈도 두지 않는다"는 원칙이 덧붙었습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · "application 서비스 CQRS 분리 규칙 (`{도메인}CommandService`/`{도메인}QueryService`)" · 서비스 간 협력도 UseCase 인터페이스로 주입하고, 도메인 타입 위임은 포트·도메인 서비스를 직접 주입하며, 트랜잭션 속성을 호출부로 옮긴다
- `backend/application-core/AGENTS.md` · "`QueryUseCase`에는 컨트롤러 표면만 올린다 — 협력용 public 메서드를 전사하지 않는다" · 제목은 앵커 보존용으로 유지하고, 현재 원칙(구체 주입 금지)을 담는다
- `backend/application-core/AGENTS.md` · "UseCase 구현 구체 주입 금지 — `servicesShouldDependOnUseCasesNotImplementations`" · 가드 검사 내용·공허 통과 방지·반증
- `backend/application-core/AGENTS.md` · "UseCase 필드명 — `useCaseFieldsShouldBeNamedUseCase`" / "CQRS 규칙의 `*QueryUseCase` 확장 — `commandServicesShouldNotDependOnQueryPorts`"
