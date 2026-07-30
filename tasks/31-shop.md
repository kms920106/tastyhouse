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

---

## 전환 결과 (완료)

### 현황 정정
작업 문서가 언급한 `ShopDeliveryTip*`/`ShopOrderAvailability*`/`ShopScheduledOrder*`/`ScheduledOrderSlotCalculator`는 **코드베이스에 존재하지 않았다**(application 서비스 23개가 아니라 실제 19개). 존재하는 것만 전환했다.

### 하강한 도메인 서비스 (`core/domain/shop/domain/service/`, 전부 순수 POJO — 빈 등록은 `DomainServiceConfig`)

| 서비스 | 분류 | 공개 시그니처 |
|---|---|---|
| `ProhibitedWordValidator` | D | `List<String> findViolations(String)` · `void validate(String)` |
| `ShopOperatingStatusCalculator` | D | `ShopOperatingStatus calculate(Shop, List<ShopBusinessHour>, List<ShopBreakTime>, List<ShopClosedDay>, List<ShopTemporaryClosure>, List<ShopSuspension>, boolean publicHoliday, LocalDateTime)` |
| `ShopOperatingStatusService` | C | `ShopOperatingStatus findOperatingStatus(Long shopId, LocalDateTime)` · `Map<Long, ShopOperatingStatus> findOperatingStatuses(List<Long> shopIds, LocalDateTime)` |
| `ShopLifecycleService` | C | `Shop createShop(Long ceoId, Long stationId, String name, BigDecimal lat, BigDecimal lon, String roadAddress, String lotAddress, String phoneNumber, Long thumbnailImageFileId)` · `void updateShop(ShopId, …동일 8개)` · `void closeShop(ShopId)` · `void updateHolidayClosure(ShopId, boolean)` · `void changeVisibility(ShopId, boolean hidden)` · `void createOwnerMessage(Long shopId, String message)` · `boolean toggleBookmark(Long shopId, MemberId)` |
| `ShopImageApprovalService` | C | `Long requestImageChange(Long shopId, ShopImageType, Long imageFileId)` · `void approveImageChange(Long id)` · `void rejectImageChange(Long id, String reason)` · `boolean existsPendingByShopId(Long shopId)` |
| `ShopPhoneNumberRegistryService` | C | `Long addPhoneNumber(Long shopId, String phoneNumber, boolean virtual)` · `void deletePhoneNumber(Long id)` · `void designatePrimary(Long id)` |
| `ShopBusinessHourService` | C/D | `ShopBusinessHour createBusinessHour(Long shopId, DayType, LocalTime open, LocalTime close, Boolean isClosed, Boolean is24Hours)` · `void updateBusinessHour(Long id, …동일 5개)` · `void deleteBusinessHour(Long id)` · `ShopBreakTime createBreakTime(Long shopId, DayType, LocalTime start, LocalTime end)` · `void updateBreakTime(Long id, …동일 3개)` · `void deleteBreakTime(Long id)` · `ShopClosedDay createClosedDay(Long shopId, ClosedDayType)` · `void deleteClosedDay(Long id)` |
| `ShopConvenienceInfoService` | C | `void upsertConvenienceInfo(Long shopId, Boolean parkingAvailable, Boolean parkingPaid, Boolean valetAvailable, Boolean valetPaid, String directionsGuide, BigDecimal displayLatitude, BigDecimal displayLongitude)` |

**40-order 참조용**: 가게 존재 검증은 도메인 서비스가 아니라 write 포트 `ShopRepository#findById(ShopId)`(+`findVisibleById`)를 직접 쓴다 — `order`의 `OrderCommandService`/`OrderQueryService`가 구 `ShopQueryService#findShopById`를 쓰던 지점을 이 포트로 교체해 두었다(컴파일 복구 목적의 최소 수정).

**17-search 참조용**: 가게 키워드 검색 위임은 `ShopSearchQueryDao#searchByKeywordWithBookmark(String keyword, MemberId, PageQuery)`로 이관됐다. `web-api`의 `SearchQueryService`가 이미 이 DAO를 주입하도록 교체해 두었다(잠정 `ShopRepository` 의존 해소).

### infra query DAO (`infrastructure/shop/query/`)
`ShopQueryDao`(설정·관리 조회) / `ShopSearchQueryDao`(목록·검색 대형 조인) / `ShopChoiceQueryDao`(에디터 추천·태그·역) 3개로 용도별 분리. Result 19개 + `ShopSearchCondition` 이관, 신규 `ShopMapMarkerResult`·`ShopChoiceDetailResult`·`TagResult`·`StationResult`·`ShopPhotoCategoryImageManagementResult` 추가.

### 소비 모듈 CQRS 분리
- **ceo-api**: 11개 facade → Command/Query 17개(`ShopQueryService`, `Shop{BusinessHour,Status,Introduction,PhoneNumber,Trademark,ClosedDay,ContentBoard,ConvenienceInfo,Suspension}{Command,Query}Service`, `ShopHygieneBadgeQueryService`). `ShopOwnershipValidator`는 `ShopRepository` 직접 주입으로 전환, 선검증 규칙 불변.
- **admin-api**: `ShopService`(518줄) → `ShopCommandService`/`ShopQueryService`, 그리고 `Shop{ContentBoard,HygieneBadge,ImageChange}{Command,Query}Service`(구 `*AdminService` 3개 대체).
- **web-api**: `ShopService`(555줄) → `ShopCommandService`(즐겨찾기 토글)/`ShopQueryService`. `MemberShopService`·`SearchQueryService`도 DAO 주입으로 전환.

### 검증
전 7모듈 javac 컴파일 오류 0 / core 단위테스트 324개 통과(이동한 `ShopOperatingStatusCalculatorTest` 포함) / ArchUnit 15개 통과 + `ClassFileImporter`로 Shop 클래스 152개가 실제 임포트된 상태에서 `com.querydsl..`·`..persistence..` 의존 0건 확인(공허 통과 아님) / 엔드포인트 매핑 수 HEAD와 동일(web 142·admin 163·ceo 46).
