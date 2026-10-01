package com.tastyhouse.application.point.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.event.PointRefundedEvent;
import com.tastyhouse.domain.point.event.PointUsedEvent;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.application.point.port.out.write.PointHistoryPersistencePort;
import com.tastyhouse.application.point.port.out.write.PointPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@SharedApp
public class PointLedgerService {

    private static final String USE_ON_ORDER_REASON = "주문 결제 사용";
    private static final String REFUND_ON_CANCEL_REASON = "결제 취소 환불";
    private static final String RECLAIM_ON_CANCEL_REASON = "결제 취소 적립금 회수";

    private final PointPersistencePort pointPersistencePort;
    private final PointHistoryPersistencePort pointHistoryPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public PointLedgerService(
        PointPersistencePort pointPersistencePort,
        PointHistoryPersistencePort pointHistoryPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.pointPersistencePort = pointPersistencePort;
        this.pointHistoryPersistencePort = pointHistoryPersistencePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void usePoints(MemberId memberId, int pointAmount) {
        deduct(memberId, pointAmount, USE_ON_ORDER_REASON);
    }

    public void deductPoints(MemberId memberId, int pointAmount, String reason) {
        deduct(memberId, pointAmount, reason);
    }

    public void earnPoints(MemberId memberId, int pointAmount, String reason) {
        Point point = pointPersistencePort.findByMemberId(memberId)
            .orElseGet(() -> pointPersistencePort.save(Point.of(memberId)));

        point.addPoints(pointAmount);
        pointPersistencePort.save(point);

        pointHistoryPersistencePort.save(PointHistory.of(memberId, PointType.EARNED, pointAmount, reason));

        domainEventPublisher.publish(new PointEarnedEvent(memberId, pointAmount, reason, LocalDateTime.now()));
    }

    public void refundPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        point.addPoints(pointAmount);
        pointPersistencePort.save(point);

        pointHistoryPersistencePort.save(
            PointHistory.of(memberId, PointType.REFUND, pointAmount, REFUND_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointRefundedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    public void reclaimEarnedPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        int deductAmount = Math.min(point.getAvailablePoints(), pointAmount);
        point.deductPoints(deductAmount);
        pointPersistencePort.save(point);

        pointHistoryPersistencePort.save(
            PointHistory.of(memberId, PointType.USE, -deductAmount, RECLAIM_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointUsedEvent(memberId, deductAmount, LocalDateTime.now()));
    }

    private void deduct(MemberId memberId, int pointAmount, String reason) {
        Point point = findPointOrThrow(memberId);

        point.deductPoints(pointAmount);
        pointPersistencePort.save(point);

        pointHistoryPersistencePort.save(PointHistory.of(memberId, PointType.USE, -pointAmount, reason));

        domainEventPublisher.publish(new PointUsedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    private Point findPointOrThrow(MemberId memberId) {
        return pointPersistencePort.findByMemberId(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId.value()));
    }
}
