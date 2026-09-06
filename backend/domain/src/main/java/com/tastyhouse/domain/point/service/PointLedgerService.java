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

    public void usePoints(MemberId memberId, int pointAmount) {
        deduct(memberId, pointAmount, USE_ON_ORDER_REASON);
    }

    public void deductPoints(MemberId memberId, int pointAmount, String reason) {
        deduct(memberId, pointAmount, reason);
    }

    public void earnPoints(MemberId memberId, int pointAmount, String reason) {
        Point point = pointRepository.findByMemberId(memberId)
            .orElseGet(() -> pointRepository.save(Point.of(memberId)));

        point.addPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(PointHistory.of(memberId, PointType.EARNED, pointAmount, reason));

        domainEventPublisher.publish(new PointEarnedEvent(memberId, pointAmount, reason, LocalDateTime.now()));
    }

    public void refundPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        point.addPoints(pointAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(memberId, PointType.REFUND, pointAmount, REFUND_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointRefundedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

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
