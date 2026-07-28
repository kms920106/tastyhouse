# shop 도메인 전환 (최대 도메인)

> 선행: `tasks/README.md` + `00-phase0` + 그룹 2 완료. 그룹 3(병렬 가능). **application 서비스 23개 — 이 전환의 최대 단일 작업.**

## 현황
- core: `shop/application/` — `ShopCommandService`(6 repo), 점주 기능 세분 서비스 20여 개(`ShopDeliveryTip`/`ShopPhoneNumber`/`ShopImageChange`/`ShopContentBoard`/`ShopSuspension`/`ShopTemporaryClosure`/`ShopHygieneBadge`/`ShopConvenienceInfo`/`ShopOperatingStatus`/`ShopOrderAvailability`/`ShopScheduledOrder` 각 Command/Query), **(D) 정책류**: `ShopOperatingStatusCalculator`, `ProhibitedWordValidator`, `ScheduledOrderSlotCalculator`.
- 소비자: **ceo-api**(점주 설정 전반 — 최다 소비자, `ShopOwnershipValidator` 선검증 규칙 유지), admin-api(가게 CRUD·검수), web-api(노출 조회).

## 작업
1. **(D) 이동**: `ShopOperatingStatusCalculator`·`ProhibitedWordValidator`·`ScheduledOrderSlotCalculator` → `shop/domain/service/`. 기존 core 테스트(`ShopOperatingStatusCalculatorTest`) 패키지 동반 이동.
2. **(C) 하강**:
   - `ShopCommandService`의 createShop/updateShop(Shop + 자식 16종 일괄 save) → `ShopAggregateService`(가칭).
   - 이미지 승인 워크플로(`approveImageChange` = ShopImageChangeRequest 승인 + Shop 이미지 반영 원자 연산, PENDING 중복·노출정지 차단 규칙) → `ShopImageApprovalService`(가칭). admin(검수)과 ceo(요청)가 공유하는 액터 무관 규칙.
   - 금칙어 검수 통과 시 즉시 반영(소개/찾아오는길)은 `ProhibitedWordValidator`(domain) 호출을 각 모듈 서비스에 유지.
3. **(A)**: 점주 세분 기능(배달팁·전화번호·휴무·임시중지·콘텐츠보드·편의정보 등 단일 애그리거트 연산)은 ceo-api의 기존 `Shop*Service`들에 흡수하되 각각 Command/Query 성격으로 분해(패턴 2, `@Transactional`). `ShopOwnershipValidator` 선호출 규칙 불변. admin의 가게 CRUD는 admin `ShopCommandService`로.
4. **(B)**: infra `infrastructure/shop/query/`에 query DAO 신설(패턴 3 — 대형 도메인이므로 용도별 분리 허용: 예 `ShopQueryDao`(관리·설정 조회) + `ShopSearchQueryDao`(`findLatestShops`/`searchByKeywordWithBookmark`/베스트 대형 조인)). web/ceo/admin 소비 메서드 분리. `BestShopItemResult` 등 result 36개 중 shop 소속 다수가 infra query 패키지로 이동 — **이관 전 이름 충돌 검사 필수, admin 전용 충돌분은 `Management` 한정어 부여**. web/ceo/admin 각 `ShopQueryService`(readOnly)가 DAO 주입. `ShopRepository` 등 write 순수화(패턴 4).
5. core `shop/application/` 삭제.

## 완료 기준
- ceo-api 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. 하강한 도메인 서비스 시그니처를 하단에 기록(17-search의 위임 조회, 40-order의 가게 검증에서 참조).
