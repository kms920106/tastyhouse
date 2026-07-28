# product 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 2 완료(30-review와 병렬 가능하나 리스너 인계 확인). 그룹 3. **batch-module 소비자·이벤트 리스너 보유.**

## 현황
- core: `product/application/` — `ProductCommandService`(6 repo: Product+옵션그룹/옵션/이미지/카테고리 등, 이벤트 발행), `ProductReviewEventListener`(리뷰 이벤트 → 상품 리뷰 통계 갱신), QueryService + result(`OptionInfo` 등). `ProductRepositoryImpl`은 file/shop Q타입 조인.
- 소비자: web-api(상품/옵션 조회·오늘의할인), admin-api(상품 CRUD), **batch-module**(`ProductCommandService`/`ProductQueryService`, `ProductScheduler#markBbqOptionsSynced`, BBQ 옵션 동기화 command 다수).

## 작업
1. **(C) 하강**: 상품 생성/수정(Product+옵션그룹+옵션+이미지 원자 일괄 save)을 `ProductCompositionService`(가칭)로, 리뷰 통계 갱신(`updateProductReviewStats`)을 `ProductReviewStatsService`(가칭)로 하강.
2. **(E)**: `ProductReviewEventListener` → infrastructure `product/listener/`, 본문은 `ProductReviewStatsService` 호출로 축소(30-review가 발행측을 `DomainEventPublisher`로 교체했는지 확인 — 미완이면 함께 처리).
3. batch-module: BBQ 동기화 use case용 자기 application 서비스 신설(`@Transactional`), `SaveProductBbqCommand` 등 batch 전용 command는 batch로 이동하거나 도메인 서비스 입력 record로 격하. core `ProductCommandService`/`ProductQueryService` import 전부 제거.
4. **(A)**: admin `ProductCommandService`(@Transactional)로 카테고리/공통옵션 등 단일 애그리거트 CRUD 흡수(패턴 2 — 기존 facade `ProductService`는 Command/Query로 분해).
5. **(B)**: infra `infrastructure/product/query/ProductQueryDao` 신설(패턴 3 — web용 옵션 포함 상세·오늘의할인 목록, admin용 관리 목록, batch용 동기화 대상 조회까지 소비자별 메서드 분리 — batch도 이 infra DAO를 소비). file/shop 조인은 같은 모듈 내 참조. Result는 infra query 소유(충돌 시 `Management` 한정어). 각 모듈 `ProductQueryService`(readOnly)가 DAO 주입. Repository 9개 write 순수화(패턴 4).
6. core `product/application/` 삭제.

## 완료 기준
- batch 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.
