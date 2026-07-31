# P4. 애그리거트 `of()` 팩토리 불변식 검증 추가

## 배경

전 도메인(77개 non-enum 모델)에 `of()`(신규 생성) + `reconstitute()`(DB 재구성, "불변식 우회 방지" Javadoc 명시) 2종 팩토리가 일관 적용돼 있으나, **정작 `of()`에 검증이 거의 없어 우회를 막을 불변식 자체가 존재하지 않는다**. 검증 보유 모델은 ~20개뿐이고 그마저 ID VO record의 compact constructor와 enum `from()`에 편중돼 있다.

## 문제 상세 (대표 사례)

- `domain-module/.../order/domain/model/Order.java:95-137` — `of()`가 21개 파라미터를 받아 null→0 치환만. `finalAmount` 음수, `totalDiscountAmount != product+coupon+point` 통과.
- `domain-module/.../coupon/domain/model/Coupon.java:78-110` — `discountType=RATE`인데 `discountAmount=200`(200%) 통과, `issueStartAt > issueEndAt` 통과.
- `domain-module/.../coupon/domain/model/MemberCoupon.java:84` — `useEndAt`이 null이면 `isExpired()`에서 `LocalDateTime.now().isAfter(null)` **NPE**.
- `domain-module/.../product/domain/model/Product.java:75-109` — `originalPrice` 음수 통과, `discountPrice > originalPrice` 통과.
- `domain-module/.../reservation/domain/model/Reservation.java:61-71` — `partySize` 0/음수 통과 (과거 날짜 검증은 `ReservationBookingService:94`에 있음 — 이건 서비스 잔류가 맞는지 재검토).
- `domain-module/.../shop/domain/model/Shop.java:80-109` — `latitude`/`longitude` 범위, `name` 공백 검증 없음.
- `domain-module/.../shop/domain/service/ShopBusinessHourService.java:106-121` `validateBusinessHour` — "5분 단위, 최소 1시간~최대 23시간 55분"은 **`ShopBusinessHour` 값 자체의 불변식**인데 서비스에 있어 `ShopBusinessHour.of()` 직접 호출 경로(배치/마이그레이션)가 우회 가능.

## 작업 지시

1. 우선 대상 4개: `Order`(금액 정합·음수 금지), `Coupon`(RATE 할인율 0<r<=100, 기간 순서, `useEndAt` null 정책 확정), `Product`(가격 음수 금지, `discountPrice <= originalPrice`), `Reservation`(partySize >= 1). 각 `of()`에 검증을 추가하고 위반 시 `BusinessException` + 적절한 `ErrorCode`(신설 필요 시 `ErrorCode.java`에 추가).
2. `MemberCoupon.isExpired()`의 NPE를 수정한다 — `useEndAt` null의 의미(무기한?)를 `Coupon.of()` 검증 정책과 함께 확정하고 null-safe 처리.
3. `ShopBusinessHourService.validateBusinessHour`(:106-121)의 순수 값 검증을 `ShopBusinessHour.of()`로 이동한다. 서비스에는 크로스 애그리거트 검증(`validateBreakTimeWithinBusinessHours`, :130-152)만 남긴다 — 이건 영업시간 애그리거트를 읽으므로 서비스 잔류가 정당.
4. **`reconstitute()`에는 검증을 넣지 않는다** — 기존 DB 데이터가 새 불변식을 위반해도 로드는 가능해야 한다. 이 비대칭을 각 모델 Javadoc에 한 줄로 명시.
5. 검증 추가 전 **기존 데이터·호출부 영향 조사 필수**: 각 검증에 대해 현재 호출부가 위반 값을 실제로 넘기는 경로가 있는지 grep으로 확인하고, 있다면 그 호출부를 먼저 고친다(예외를 완화하는 게 아니라).
6. domain-module 단위 테스트: 각 검증의 경계값(통과 최소/최대, 위반 케이스)을 추가. 기존 모델 테스트(`OrderTest`/`CouponTest`/`ProductTest`/`ReservationTest`/`ShopBusinessHourTest`)에 편입.

## 수용 기준

- [ ] 우선 4개 모델의 `of()`가 위 불변식을 검증하고, 위반 시 `BusinessException` (테스트 증명)
- [ ] `MemberCoupon.isExpired()` NPE 재현 불가 (null useEndAt 테스트)
- [ ] 영업시간 규격 검증이 `ShopBusinessHour.of()`에 있고 서비스 경유 없이도 강제됨
- [ ] `reconstitute()`는 무검증 유지
- [ ] 기존 정상 생성 플로 무회귀 — 호출부 전수 grep 결과를 보고에 포함
- [ ] domain-module 테스트 전량 통과 (verify-without-gradle)

## 주의사항

- **P1(Order 상태 머신)과 같은 파일(`Order.java`)을 만진다** — P1의 `updateAmounts` 검증과 이 태스크의 `of()` 검증은 같은 정합 규칙을 공유하므로, private 검증 메서드 하나로 합쳐 양쪽에서 호출하는 형태를 권장. P1 담당과 조율.
- **P7(shop 로직 복구)과 `ShopBusinessHour.java` 충돌** — P7이 `isOpenAt()`을 추가한다. 순차 수행 또는 담당 조율.
- 검증 추가는 동작 변경(잘못된 생성이 예외로)이다. 추천 커밋 메시지 본문에 "동작 변경 있음"을 명시할 것.
