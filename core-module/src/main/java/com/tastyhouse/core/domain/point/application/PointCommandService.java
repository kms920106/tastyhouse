package com.tastyhouse.core.domain.point.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.point.domain.event.PointEarnedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointRefundedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointUsedEvent;
import com.tastyhouse.core.domain.point.domain.model.Point;
import com.tastyhouse.core.domain.point.domain.model.PointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.PointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.PointRepository;
import com.tastyhouse.core.domain.point.application.dto.command.PointDeductCommand;
import com.tastyhouse.core.domain.point.application.dto.command.PointEarnCommand;
import com.tastyhouse.core.domain.point.application.dto.command.PointReclaimCommand;
import com.tastyhouse.core.domain.point.application.dto.command.PointRefundCommand;
import com.tastyhouse.core.domain.point.application.dto.command.PointUseCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class PointCommandService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void usePoints(PointUseCommand command) {
        Point point = pointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        point.deductPoints(command.pointAmount());
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(command.memberId(), PointType.USE, -command.pointAmount(), "주문 결제 사용")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void earnPoints(PointEarnCommand command) {
        Point point = pointRepository.findByMemberId(command.memberId())
            .orElseGet(() -> pointRepository.save(Point.of(command.memberId())));

        point.addPoints(command.pointAmount());
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(command.memberId(), PointType.EARNED, command.pointAmount(), command.reason())
        );

        eventPublisher.publishEvent(new PointEarnedEvent(command.memberId(), command.pointAmount(), command.reason(), LocalDateTime.now()));
    }

    public void refundPoints(PointRefundCommand command) {
        Point point = pointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        point.addPoints(command.pointAmount());
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(command.memberId(), PointType.REFUND, command.pointAmount(), "결제 취소 환불")
        );

        eventPublisher.publishEvent(new PointRefundedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void reclaimEarnedPoints(PointReclaimCommand command) {
        Point point = pointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        int deductAmount = Math.min(point.getAvailablePoints(), command.pointAmount());
        point.deductPoints(deductAmount);
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(command.memberId(), PointType.USE, -deductAmount, "결제 취소 적립금 회수")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), deductAmount, LocalDateTime.now()));
    }

    public void deductPoints(PointDeductCommand command) {
        Point point = pointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        point.deductPoints(command.pointAmount());
        pointRepository.save(point);

        pointHistoryRepository.save(
            PointHistory.of(command.memberId(), PointType.USE, -command.pointAmount(), command.reason())
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }
}
