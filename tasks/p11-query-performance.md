# P11. 조회 성능 결함 수정 — 전체 로딩 페이징·N+1·전량 로드

## 배경

구조 문제(비대 DAO·단건 조회 유틸 남용)의 결과로 read 경로에 성능 결함이 누적돼 있다. 페이지 1건 요청에 전체 행을 메모리에 올리는 count, 루프 내 단건 조회 N+1, 매 호출 전량 로드가 대표다. 동작(응답 JSON)은 바꾸지 않고 쿼리 형태만 고치는 작업이다.

## 문제 상세

### 11-1. 전체 로딩 후 count — `query.fetch().size()` 7곳

페이징 total을 count 쿼리가 아니라 전 행 fetch 후 `size()`로 구한다:

- `infrastructure-module/.../review/query/ReviewQueryDao.java:103, 146, 200, 294, 388` (5곳)
- `infrastructure-module/.../product/query/ProductQueryDao.java:88`
- `infrastructure-module/.../review/query/ReviewManagementQueryDao.java:81`

### 11-2. N+1

- `web-api/.../order/OrderQueryService.java:130` — 주문 상세의 주문상품 매핑 루프 안에서 상품마다 `reviewQueryService.isReviewedByOrderAndProduct(...)` → `reviewQueryDao.existsByOrderIdAndProductIdAndMemberId` 호출. 상품 N개 = 쿼리 N번.
- `web-api/.../file/FileService.java:64` `getUrlByFileId(Long)` — 단건 파일 경로 조회가 14곳에서 호출되고 QueryService 20여 개에 주입. 목록 응답 조립 루프에서 호출되는 지점은 fileId N개 = 쿼리 N번.

### 11-3. 전량 로드

- `domain-module/.../shop/domain/service/ProhibitedWordValidator.java` — 매 검증 호출마다 `ProhibitedWordRepository.findAll()`로 금칙어 전량 로드. 서비스 Javadoc(:19)이 "추후 캐싱 고려"라고 스스로 인정.

## 작업 지시

1. **count 쿼리화 (7곳)**: 각 지점을 QueryDSL count 쿼리(`select(entity.count())` 등 같은 where 재사용)로 교체한다. 코드베이스에 이미 올바른 count 패턴을 쓰는 DAO(예: `NoticeQueryDao` 등)를 grep으로 찾아 그 관용구를 그대로 따른다. `fetch().size()`를 쓴 이유가 있는 지점(중복 제거 후 카운트 등 count 쿼리로 등가 표현이 어려운 경우)이 있는지 각각 확인하고, 등가가 아니면 근거를 주석으로 남기고 스킵.
2. **주문상세 리뷰여부 N+1**: `reviewQueryDao`에 `Set<Long> findReviewedProductIds(orderId, memberId, Collection<Long> productIds)`류의 IN 배치 조회를 신설하고, `OrderQueryService`가 루프 전에 1회 조회 후 메모리 판정하도록 교체.
3. **FileService URL N+1**: `getUrlByFileId` 호출 14곳 중 **루프 안에서 호출되는 지점만** 골라(전수 grep 후 분류), `List<Long> → Map<Long, String>` 배치 변환 메서드(`getUrlsByFileIds`)를 신설해 교체한다. 단건 문맥(상세 1건)은 그대로 둔다.
4. **금칙어 캐싱**: `ProhibitedWordValidator`는 domain-module 순수 POJO이므로 Spring `@Cacheable`을 붙일 수 없다. 선택지를 조사해 가장 침습이 적은 안을 적용:
   - (a) 빈 등록 지점(`infrastructure/DomainServiceConfig`)에서 캐싱 데코레이터로 감싼 `ProhibitedWordRepository`를 주입
   - (b) Validator 내부에 TTL 캐시 필드(순수 자바) — 도메인에 캐시 로직이 들어오는 단점
   - (c) 현행 유지 + 호출 빈도 실측 후 판단
   결정 근거를 보고에 남긴다. 금칙어는 SQL 시드 read-only 데이터라 정합성 리스크가 낮다는 점 참고.
5. 교체 전후 쿼리 로그(p6spy)를 대조해 **결과 동일 + 쿼리 수 감소**를 증명한다.

## 수용 기준

- [ ] `fetch().size()` count가 7곳에서 제거(또는 등가 불가 근거 주석)
- [ ] 주문 상세 조회 쿼리 수가 상품 수와 무관해짐
- [ ] 루프 내 `getUrlByFileId` 호출 0건
- [ ] 응답 JSON 무변경 (대표 케이스 전후 대조)
- [ ] 테스트 통과 (verify-without-gradle)

## 주의사항

- count 쿼리 교체 시 join·groupBy가 있는 쿼리는 count 결과가 달라질 수 있다(행 뻥튀기) — `countDistinct` 필요 여부를 쿼리별로 판단.
- 이 태스크는 P2~P7과 파일 겹침이 적어 병렬 수행에 적합하나, `ReviewQueryDao`는 P6(정렬 enum화)과 같은 파일 — 조율 필요.
