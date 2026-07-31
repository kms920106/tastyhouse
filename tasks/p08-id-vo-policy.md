# P8. ID VO 반쪽 적용 해소 — 정책 결정 후 일관화

## 배경

도메인 모델의 FK 필드가 raw `Long` 78개 vs ID VO 21개로 갈려 있고, VO 21개 중 17개가 `MemberId`다. `ShopId`/`ProductId`/`ReviewId` 등은 **정의만 있고 리포지토리 파라미터 승격용으로만 쓰이며 모델 필드로는 안 쓰인다**. 한 클래스 안에서도 규칙이 갈린다. 일관성 이득도 단순성 이득도 못 얻은 최악의 중간 상태다.

## 문제 상세

```java
// domain-module/.../order/domain/model/Order.java:25-26 — 같은 클래스 안 혼재
private final MemberId memberId;   // VO
private final Long shopId;         // raw — ShopId VO가 존재하는데도
private Long memberCouponId;       // raw — MemberCouponId VO가 존재하는데도
```

- `product/domain/model/Product.java:23` `private final Long shopId;`
- `review/domain/model/Review.java` — `memberId`만 VO, `shopId`/`productId`/`orderId`는 raw
- raw Long 연속 시그니처의 실재 위험: `ReviewLifecycleService.register(shopId, productId, memberId, orderId, ...)` — 인자 순서 착오를 타입이 못 잡음
- 인프라 측 원인: `AttributeConverter`가 `MemberIdConverter`/`OrderIdConverter`/`PaymentIdConverter`/`AmountConverter` 4개뿐. `@Convert` VO 컬럼은 QueryDSL join 시 `Expressions.numberPath` 수동 path 우회가 필요(프로젝트 메모리 `querydsl-convert-vo-join`, 실사례: `OrderQueryDao:300`, `PaymentQueryDao:128,135`, `ReservationQueryDao:184`, `MemberFollowRepositoryImpl:24`) — 이 비용이 확산을 멈춘 실질 원인으로 추정.

## 작업 지시 (정책 결정이 선행 — 사용자 확인 필수)

**1단계: 사용자에게 선택지 질문 (체크리스트 형식)**

> 도메인 모델 FK 필드의 ID 타입 정책을 어떻게 통일할까요?
> - [ ] **A. VO 전면 확산** — 모든 애그리거트 간 FK 필드를 `XxxId` VO로. 타입 안전 최대. 비용: 컨버터 다수 신설 + `@Convert` 컬럼의 QueryDSL join 우회 지점 증가(현재 4곳 → 수십 곳 예상), 도메인 서비스·매퍼 시그니처 광범위 수정.
> - [ ] **B. 경계 축소 — memberId 수준 유지 + 시그니처만 VO** (권장 후보) — 모델 필드는 현행 유지(신규 확산 안 함)하되, **도메인 서비스 public 시그니처의 raw Long 연속 구간만 VO로 승격**(`register(ShopId, ProductId, MemberId, OrderId, ...)`). 인자 뒤바뀜 위험(실질 문제)만 제거하고 인프라 비용은 동결. 서비스 내부에서 `.value()`로 풀어 모델에 전달.
> - [ ] **C. raw 회귀** — `MemberId`류 모델 필드도 raw Long으로 되돌려 단순화. 기존 `@Convert` 4개·우회 코드 제거. 타입 안전 포기.

**2단계: 결정된 정책 실행**

- A 선택 시: 도메인별 순차 전환(컨버터 신설 → 모델 필드 교체 → 매퍼/JpaEntity `@Convert` → QueryDSL 우회 추가). CLAUDE.md ID VO 경계 규칙 표(HTTP=Long, api Service에서 승격, query DAO=Long)는 유지 — 바뀌는 건 "도메인 모델 내부"와 "엔티티 FK" 행의 적용률뿐.
- B 선택 시: raw Long이 2개 이상 연속되는 도메인 서비스 public 시그니처를 전수 grep으로 수집해 VO 승격. 모델 필드·인프라 무변경.
- C 선택 시: `MemberId` 모델 필드 15곳+컨버터 제거의 팬아웃을 조사 후 실행(대규모 — 별도 세부 계획 수립 후).

**3단계: 결정과 근거를 CLAUDE.md의 "ID VO 경계 규칙" 절에 반영** — 어떤 계층까지 VO를 쓰는지 표를 현실과 일치시킨다.

## 수용 기준

- [ ] 사용자 정책 결정이 기록됨 (질문→선택 결과)
- [ ] 결정 정책 기준으로 "같은 클래스 안 혼재"가 사라지거나, 남는 혼재의 근거가 규칙으로 문서화됨
- [ ] raw Long 3연속 이상 도메인 서비스 public 시그니처 0건 (B 이상 선택 시)
- [ ] CLAUDE.md ID VO 경계 규칙 표가 실태와 일치
- [ ] 테스트 통과 (verify-without-gradle)

## 주의사항

- **이 태스크는 코드보다 결정이 먼저다.** 결정 없이 임의로 전환하지 말 것.
- A 선택 시 `@Convert` 컬럼 QueryDSL 우회는 프로젝트 메모리 `querydsl-convert-vo-join`의 `Expressions.numberPath` 패턴을 따른다.
- P1/P4가 `Order.java`를 만진다 — 모델 필드 타입을 바꾸는 A/C안은 순차 필수.
