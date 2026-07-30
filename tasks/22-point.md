# point 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **이벤트 리스너 보유 — order/payment 클러스터와 결합.**

## 현황
- core: `point/application/` — `PointCommandService`(2 repo: Point+PointHistory, 이벤트 발행, 명시적 save 5지점), `PointEventListener`(주문/결제 이벤트에 반응해 적립·회수 추정), QueryService + `PointHistoryResult`.
- 소비자: web-api(내 포인트 조회 `PointApiController`), admin-api(포인트 관리), 이벤트 경유 간접 소비(order/payment).

## 작업
1. **(C) 하강 — 이 도메인의 핵심**: `usePoints`/`earnPoints`/`refundPoints`/`reclaimEarnedPoints`/`deductPoints`는 전부 "Point 잔액 변경 + PointHistory 기록"의 원자 연산이자 이벤트로 트리거되는 액터 무관 규칙 → `PointLedgerService`(가칭) 도메인 서비스로 통째 하강(패턴 1). `DomainServiceConfig` 등록.
2. **(E)**: `PointEventListener` → infrastructure `point/listener/`, 본문은 `PointLedgerService` 호출로 축소.
3. **(A)**: admin의 수동 적립/차감이 있으면 admin `PointCommandService`(@Transactional)에서 `PointLedgerService` 호출(패턴 2).
4. **(B)**: infra `infrastructure/point/query/PointQueryDao` 신설(패턴 3 — 잔액·이력 페이징), `PointHistoryResult`는 infra query 패키지로 이관. web `PointQueryService`(readOnly)가 DAO 주입. Repository write 순수화(패턴 4).
5. core `point/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. `PointLedgerService`가 40-order/41-payment 작업의 호출 대상이 되므로 시그니처를 이 파일 하단에 기록해 인계.

## 인계 — `PointLedgerService` 시그니처 (40-order / 41-payment 작업용)

위치: `core-module/src/main/java/com/tastyhouse/core/domain/point/domain/service/PointLedgerService.java`

```java
public class PointLedgerService {

    // 주문 결제 사용 (사유 고정 "주문 결제 사용")
    public void usePoints(MemberId memberId, int pointAmount);

    // 사유 지정 차감 (관리자 수동 차감 등)
    public void deductPoints(MemberId memberId, int pointAmount, String reason);

    // 적립 (포인트 계정 없으면 잔액 0으로 생성 후 적립)
    public void earnPoints(MemberId memberId, int pointAmount, String reason);

    // 결제 취소 환불 (사유 고정 "결제 취소 환불")
    public void refundPoints(MemberId memberId, int pointAmount);

    // 결제 취소 적립금 회수 (사유 고정 "결제 취소 적립금 회수", 잔액 부족 시 남은 잔액만큼만 차감)
    public void reclaimEarnedPoints(MemberId memberId, int pointAmount);
}
```

**호출 시 주의사항**

- **트랜잭션 경계는 호출자가 선언한다.** 이 클래스는 `@Service`/`@Transactional` 없는 순수 POJO(패턴 1)이므로, 호출하는 command 서비스 또는 이벤트 리스너가 `@Transactional`을 반드시 갖고 있어야 한다. 현재 호출자 3곳(`OrderCommandService#createOrder`, `PaymentEventListener`(`REQUIRES_NEW`), admin `PointCommandService`) 모두 경계를 선언하고 있다.
- 빈 등록은 infrastructure-module `DomainServiceConfig#pointLedgerService`. 생성자 파라미터는 `(PointRepository, PointHistoryRepository, DomainEventPublisher)`.
- 이벤트 발행은 `DomainEventPublisher` 포트를 통해 내부에서 수행한다(`PointEarnedEvent`/`PointUsedEvent`/`PointRefundedEvent`). 호출자가 별도로 포인트 이벤트를 발행할 필요 없다.
- 잔액 부족 시 `usePoints`/`deductPoints`는 `POINT_INSUFFICIENT`로 실패하고, `reclaimEarnedPoints`는 실패하지 않고 남은 잔액만큼만 회수한다.
- 포인트 계정이 없으면 `earnPoints`는 계정을 생성하지만, 나머지 4개는 `POINT_NOT_FOUND`로 실패한다.

**조회(read)는 이 서비스가 아니라 infra query DAO를 쓴다**: `infrastructure/point/query/PointQueryDao`(`findBalanceByMemberId`/`findPointHistories`/`findPointHistoryPage`). `PointHistoryRepository`는 `save`만 남은 write 포트다.
