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

## 전환 결과 기록 (잔여물·사양 편차 사유)

- **작업 2번(`web SearchCommandService`로 검색어 로그 적재 흡수) 미수행** — 전제가 성립하지 않았다. 전환 시점에 `SearchKeywordLogRepository.save(...)`의 **호출부가 코드베이스 전체에 0건**이었다(컨트롤러·서비스 어디에서도 검색어를 로깅하지 않고, `SEARCH_KEYWORD_LOG` 테이블은 batch 집계 쿼리가 읽기만 함). 흡수할 command 경로가 없어 죽은 `SearchCommandService`를 신설하지 않았다. write 포트의 `save`는 향후 로깅 기능이 붙을 자리로 그대로 남겼다(집계용 `findTop10KeywordsSince`·`deleteOlderThan`은 갱신 불변식에 필요해 잔류가 확정).
- **작업 3번 후반(가게/메뉴/리뷰 검색 위임) 잠정 유지** — `SearchQueryService`가 `ProductQueryService`·`ReviewRepository`·`ShopRepository`(core)를 그대로 주입한다. 해당 도메인의 infra query DAO가 그룹 3에서 생기므로, **shop/product/review 전환 작업자는 이 세 의존을 각 도메인의 infra query DAO 주입으로 교체**해야 한다(파일 상단 Javadoc에도 명시).
- **`RecommendedKeyword` 쓰기 체인 삭제** — 유일한 조회(`findActiveOrderBySortOrder`)가 query DAO로 이관되어 write 포트가 완전히 미사용이 됐다. README "소비 모듈이 실제 쓰는 메서드만 이관 — 안 쓰는 것은 삭제" 규칙에 따라 도메인 모델·포트·RepositoryImpl·Mapper·JpaRepository·단위 테스트를 제거했다(JpaEntity는 DAO가 투영 대상으로 사용하므로 유지). 읽기 전용 애그리거트라 write 경로가 애초에 없었다.
- **트랜잭션 경계** — `PopularKeywordRefreshService`는 순수 POJO라 자체 트랜잭션이 없고, `batch-module`의 `SearchKeywordSchedulerService`(`@Transactional`)가 경계를 제공한다. `PopularKeywordRepositoryImpl#deleteAll`이 `entityManager.flush()`를 호출하므로 이 래퍼는 선택이 아니라 **필수**다(없으면 런타임 실패).
