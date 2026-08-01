# P5. domain write 포트의 표현 목적 조회 유출 이관

## 배경

루트 CLAUDE.md "write 포트 잔류 판정 기준": 조회는 "이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가"로 가른다 — 아니면(화면용이면) infra query DAO로 내린다. 이 기준을 위반해 표현 전용 조회가 domain repository에 남아 있고, 이것이 **api QueryService가 write 포트를 주입하는 위반(P2의 11건)의 진원지**다.

## 문제 상세

### 5-1. `ShopDetailRepository` — 표현 전용 목록 조회 (가장 심각)

`domain-module/src/main/java/com/tastyhouse/domain/shop/domain/repository/ShopDetailRepository.java`

| 메서드 | 도메인 소비자 | api QueryService 소비 (유출) |
|---|---|---|
| `findOrderMethodsByShopId` (:74) | **없음** | web `ShopQueryService:538`, admin `ShopQueryService:250` |
| `findPhotoCategoriesByShopId` (:87,96) | **없음** | web `ShopQueryService:387`, admin `ShopQueryService:274` |
| `findLatestOwnerMessageByShopId` (:113) | **없음** | web `ShopQueryService:317`, ceo `ShopIntroductionQueryService:32` |
| `findBusinessHoursByShopId` (:52) | `ShopBusinessHourService:130`, `ShopOperatingStatusService:86` | web `ShopQueryService:294`, ceo `ShopBusinessHourQueryService:32`, admin `ShopQueryService:128` |
| `findClosedDaysByShopId` (:63) | `ShopBusinessHourService:92`, `ShopOperatingStatusService:88` | web `ShopQueryService:296`, ceo `ShopClosedDayQueryService:36`, admin `ShopQueryService:162` |

위 3건은 Javadoc 스스로 "회원 상세의 주문방식 노출"(:85), "카테고리별 사진 묶음 조립"(:94), "가게소개 조회·수정 화면"(:111)이라고 표현 목적을 자백한다.

### 5-2. 기타 유출

- `domain-module/.../search/domain/repository/SearchKeywordLogRepository.java:12` — `List<Object[]> findTop10KeywordsSince(...)`: 타입 없는 JPQL 튜플이 도메인 계약에 노출. 소비자는 도메인 내부(`PopularKeywordRefreshService:59`)지만 반환 타입이 인프라 투영 형식.
- `domain-module/.../member/domain/repository/MemberRepository.java` — `Map<Long, String> findNicknamesByIds(...)`: Javadoc이 "리뷰 댓글·답글 작성자 표시명"이라 명시 — 표시명 조회 = 표현 목적.
- 참고(수용 가능·이번 범위 제외): `PopularKeywordRepository.findActiveOrderByRank()`(도메인 소비 있음), `ProhibitedWordRepository.findAll()`(참조 데이터, 성능 문제는 P11).

## 작업 지시

1. **표현 전용 3건 이관**: `findOrderMethodsByShopId`/`findPhotoCategoriesByShopId`/`findLatestOwnerMessageByShopId`를 `ShopDetailRepository`(포트)와 `infrastructure-module/.../shop/persistence/ShopDetailRepositoryImpl.java`에서 제거하고, `infrastructure-module/.../shop/query/ShopQueryDao`에 Result record 반환 조회로 신설한다. Result record는 CLAUDE.md 규칙대로 `<ctx>/query/`에 독립 파일·`Result` 접미어·`@QueryProjection`, **소비자가 실제 쓰는 필드만** 담는다. 소비 QueryService 6곳의 주입·매핑을 교체.
2. **겸용 2건 분리**: `findBusinessHoursByShopId`/`findClosedDaysByShopId`는 도메인 소비(불변식 검증)가 있으므로 write 포트에 남긴다. 대신 api QueryService 6곳의 소비를 `ShopQueryDao`의 신규 투영 조회로 교체한다 — "양쪽에 같은 데이터를 읽는 메서드가 생기는 것을 허용한다"(CLAUDE.md, 목적·반환 타입이 다르므로 중복 아님).
3. **`findTop10KeywordsSince` 타입화**: 도메인에 `KeywordCount(String keyword, long count)` record를 두고 포트 반환 타입을 교체하거나, 기존 선례인 `rank`의 `port/MemberReviewCount` + `MemberReviewCountPort` 패턴을 따라 port로 옮긴다. `infrastructure/.../search/persistence/SearchKeywordLogJpaRepository.java:21`의 JPQL 매핑도 함께 수정.
4. **`findNicknamesByIds` 이관**: `MemberQueryDao`로 옮기고, 소비자(리뷰 댓글/답글 조립 — `ReviewQueryService` 경로)를 교체. 단 소비자가 CommandService라면 P3과 조율.
5. 이관 후 각 QueryService의 write 포트 주입이 실제로 사라지는지 확인 — P2의 위반 목록 11건 중 shop 관련 건들이 해소돼야 한다.

## 수용 기준

- [x] `ShopDetailRepository`에 도메인 소비자 0인 메서드가 없음
- [x] web/admin/ceo의 shop 관련 QueryService에서 `ShopDetailRepository` 주입 제거
- [x] 도메인 포트에 `List<Object[]>`·`Map<Long,String>` 반환 0건
- [x] 신설 Result record가 CLAUDE.md 네이밍(Result 접미어, admin 충돌 시 Management 한정어) 준수
- [x] HTTP 응답 JSON 무변경 (기존 화면 계약 유지)
- [x] 관련 테스트 통과 (verify-without-gradle)

## 주의사항

- **P7(shop 로직 복구)과 같은 영역** — `ShopDetailRepository`·shop QueryService를 둘 다 만진다. **P5를 먼저** 수행 권장.
- **P2(ArchUnit)와 연동** — 이 태스크 완료 후 P2 예외 목록에서 해당 QueryService들을 제거하라고 전달.
- ceo-api QueryService들의 Javadoc에 적힌 "write 포트 잔류 단건 조회를 쓴다" 개별 정당화 주석은 이관 후 삭제/갱신할 것.
