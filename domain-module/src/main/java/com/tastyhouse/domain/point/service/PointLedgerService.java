package com.tastyhouse.domain.point.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.event.PointRefundedEvent;
import com.tastyhouse.domain.point.event.PointUsedEvent;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 포인트 원장(도메인 서비스).
 *
 * <p>모든 연산이 "{@code Point} 잔액 변경 + {@code PointHistory} 이력 기록 + 변동 이벤트 발행"을 한
 * 트랜잭션에서 함께 수행하는 원자 연산이다. 애그리거트 타입 2개를 함께 load &amp; save 하는 불변식
 * 오케스트레이션(분류 C)이므로 도메인 계층에 두어, 트리거 경로(주문 결제·결제 취소·추천 보상·관리자
 * 수동 조정)가 여러 개여도 "잔액과 이력은 항상 함께 움직인다"는 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스 또는 infrastructure의 이벤트 리스너가 선언한다.
 *
 * <p>{@code Point}는 순수 POJO라 더티 체킹이 없으므로 잔액 변경 후 명시적으로
 * {@code pointRepository.save(point)}를 호출한다.
 */
public class PointLedgerService {

    private static final String USE_ON_ORDER_REASON = "주문 결제 사용";
    private static final String REFUND_ON_CANCEL_REASON = "결제 취소 환불";
    private static final String RECLAIM_ON_CANCEL_REASON = "결제 취소 적립금 회수";

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 주문 결제에 포인트를 사용한다. 보유 잔액이 부족하면 {@code POINT_INSUFFICIENT}로 실패한다.
     */
    public void usePoints(MemberId memberId, int pointAmount) {
        deduct(memberId, pointAmount, USE_ON_ORDER_REASON);
    }

    /**
     * 사유를 지정해 포인트를 차감한다(관리자 수동 차감 등). 보유 잔액이 부족하면 실패한다.
     */
    public void deductPoints(MemberId memberId, int pointAmount, String reason) {
        deduct(memberId, pointAmount, reason);
    }

    /**
     * 포인트를 적립한다. 포인트 계정이 없는 회원이면 잔액 0인 계정을 먼저 만든다.
     */
    public void earnPoints(MemberId memberId, int pointAmount, String reason) {
        Point point = pointRepository.findByMemberId(memberId)
            .orElseGet(() -> pointRepository.save(Point.of(memberId)));

        point.addPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(PointHistory.of(memberId, PointType.EARNED, pointAmount, reason));

        domainEventPublisher.publish(new PointEarnedEvent(memberId, pointAmount, reason, LocalDateTime.now()));
    }

    /**
     * 결제 취소로 사용했던 포인트를 되돌려준다.
     */
    public void refundPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        point.addPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(memberId, PointType.REFUND, pointAmount, REFUND_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointRefundedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    /**
     * 결제 취소로 이미 적립된 포인트를 회수한다. 회수 시점에 잔액이 적립액보다 적을 수 있으므로
     * 남은 잔액만큼만 차감한다(잔액 부족으로 실패시키지 않는다).
     *
     * <p><b>{@code deduct}와 함께 {@link PointUsedEvent}를 발행하지만 두 경로는 배타적이다</b>
     * (P9에서 호출 경로 역추적으로 확정). 이 메서드의 유일한 호출부는
     * {@code PaymentEventListener#onPaymentCancelled}(결제 <b>취소</b> 후처리)이고,
     * {@code deduct}의 호출부는 {@code OrderPlacementService#place}(주문 <b>접수</b>)와 관리자 수동
     * 차감 API뿐이다. 접수와 취소는 서로 다른 요청·다른 트랜잭션이며 어느 쪽도 상대를 호출하지 않으므로,
     * 한 트랜잭션에서 {@code PointUsedEvent}가 두 번 발행되는 경로는 존재하지 않는다.
     * (취소 후처리 트랜잭션은 {@code refundPoints}와 이 메서드를 함께 호출하지만, 전자가 발행하는 것은
     * {@link PointRefundedEvent}라 중복이 아니다.)
     */
    public void reclaimEarnedPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        int deductAmount = Math.min(point.getAvailablePoints(), pointAmount);
        point.deductPoints(deductAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(memberId, PointType.USE, -deductAmount, RECLAIM_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointUsedEvent(memberId, deductAmount, LocalDateTime.now()));
    }

    /**
     * 잔액 차감 공통 경로 — {@link #usePoints}(주문 결제)와 {@link #deductPoints}(관리자 수동 차감)의
     * 공통 구현이다. 여기서 발행하는 {@link PointUsedEvent}가 {@link #reclaimEarnedPoints}의 발행과
     * 중복되지 않는 근거는 그 메서드 Javadoc 참고.
     */
    private void deduct(MemberId memberId, int pointAmount, String reason) {
        Point point = findPointOrThrow(memberId);

        point.deductPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(PointHistory.of(memberId, PointType.USE, -pointAmount, reason));

        domainEventPublisher.publish(new PointUsedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    private Point findPointOrThrow(MemberId memberId) {
        return pointRepository.findByMemberId(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId.value()));
    }
}
