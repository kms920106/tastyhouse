package com.tastyhouse.application.point.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.event.PointRefundedEvent;
import com.tastyhouse.domain.point.event.PointUsedEvent;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.application.point.port.out.write.PointHistorySavePort;
import com.tastyhouse.application.point.port.out.write.PointLoadPort;
import com.tastyhouse.application.point.port.out.write.PointSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class PointLedgerService {

    private static final String USE_ON_ORDER_REASON = "주문 결제 사용";
    private static final String REFUND_ON_CANCEL_REASON = "결제 취소 환불";
    private static final String RECLAIM_ON_CANCEL_REASON = "결제 취소 적립금 회수";

    private final PointLoadPort pointLoadPort;
    private final PointSavePort pointSavePort;
    private final PointHistorySavePort pointHistorySavePort;
    private final DomainEventPublisher domainEventPublisher;

    public PointLedgerService(
        PointLoadPort pointLoadPort,
        PointSavePort pointSavePort,
        PointHistorySavePort pointHistorySavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.pointLoadPort = pointLoadPort;
        this.pointSavePort = pointSavePort;
        this.pointHistorySavePort = pointHistorySavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void usePoints(MemberId memberId, int pointAmount) {
        deduct(memberId, pointAmount, USE_ON_ORDER_REASON);
    }

    public void deductPoints(MemberId memberId, int pointAmount, String reason) {
        deduct(memberId, pointAmount, reason);
    }

    public void earnPoints(MemberId memberId, int pointAmount, String reason) {
        Point point = pointLoadPort.findByMemberId(memberId)
            .orElseGet(() -> pointSavePort.save(Point.of(memberId)));

        point.addPoints(pointAmount);
        pointSavePort.save(point);

        pointHistorySavePort.save(PointHistory.of(memberId, PointType.EARNED, pointAmount, reason));

        domainEventPublisher.publish(new PointEarnedEvent(memberId, pointAmount, reason, LocalDateTime.now()));
    }

    public void refundPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        point.addPoints(pointAmount);
        pointSavePort.save(point);

        pointHistorySavePort.save(
            PointHistory.of(memberId, PointType.REFUND, pointAmount, REFUND_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointRefundedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    public void reclaimEarnedPoints(MemberId memberId, int pointAmount) {
        Point point = findPointOrThrow(memberId);

        int deductAmount = Math.min(point.getAvailablePoints(), pointAmount);
        point.deductPoints(deductAmount);
        pointSavePort.save(point);

        pointHistorySavePort.save(
            PointHistory.of(memberId, PointType.USE, -deductAmount, RECLAIM_ON_CANCEL_REASON)
        );

        domainEventPublisher.publish(new PointUsedEvent(memberId, deductAmount, LocalDateTime.now()));
    }

    private void deduct(MemberId memberId, int pointAmount, String reason) {
        Point point = findPointOrThrow(memberId);

        point.deductPoints(pointAmount);
        pointSavePort.save(point);

        pointHistorySavePort.save(PointHistory.of(memberId, PointType.USE, -pointAmount, reason));

        domainEventPublisher.publish(new PointUsedEvent(memberId, pointAmount, LocalDateTime.now()));
    }

    private Point findPointOrThrow(MemberId memberId) {
        return pointLoadPort.findByMemberId(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId.value()));
    }
}
