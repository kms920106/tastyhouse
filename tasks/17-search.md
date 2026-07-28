# search 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능). **batch-module 소비자 있음 주의.**

## 현황
- core: `search/application/` — `SearchKeywordCommandService`(2 repo: PopularKeyword deleteAll+saveAll, SearchKeywordLog), `SearchResultQueryService`(타 도메인 위임 조회), QueryService/result. 애그리거트 3개(`PopularKeyword`/`RecommendedKeyword`/`SearchKeywordLog`).
- 소비자: web-api(검색·인기/추천 키워드 조회, 검색어 로깅), **batch-module**(`SearchKeywordCommandService` — 인기 키워드 집계 갱신).

## 작업
1. **(C) 판정**: 인기 키워드 갱신(deleteAll+saveAll 원자 교체)은 batch가 트리거하는 액터 무관 연산 → 도메인 서비스 `PopularKeywordRefreshService`(가칭)로 하강. batch-module의 스케줄러는 자기 application 서비스(`@Transactional`)에서 이 도메인 서비스를 호출.
2. **(A)**: 검색어 로그 적재는 web `SearchCommandService`(@Transactional)로 흡수(패턴 2).
3. **(B)**: infra `infrastructure/search/query/SearchQueryDao` 신설(패턴 3 — 인기/추천 키워드 조회), Result는 infra query 소유. web `SearchQueryService`(readOnly)가 DAO 주입. `SearchResultQueryService`(가게/메뉴 검색 위임)는 web `SearchQueryService`가 **infra의 shop/product QueryDao를 직접 조합**하는 형태로 이동하되, 해당 DAO들은 그룹 3에서 생기므로 **shop/product 전환 후 재확인** 필요 — 그 전에는 임시로 기존 core 경로 유지 후 그룹 3 작업자에게 인계 메모.
4. core `search/application/` 삭제(3번 잔여물 제외 가능 — 잔여 시 사유를 이 파일 하단에 기록).

## 완료 기준
- batch-module 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.
