package com.tastyhouse.core.domain.point.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.point.domain.event.PointEarnedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointRefundedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointUsedEvent;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;
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

    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void usePoints(PointUseCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        memberPoint.deductPoints(command.pointAmount());
        memberPointRepository.save(memberPoint);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.USE, -command.pointAmount(), "주문 결제 사용")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void earnPoints(PointEarnCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseGet(() -> memberPointRepository.save(MemberPoint.of(command.memberId())));

        memberPoint.addPoints(command.pointAmount());
        memberPointRepository.save(memberPoint);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.EARNED, command.pointAmount(), command.reason())
        );

        eventPublisher.publishEvent(new PointEarnedEvent(command.memberId(), command.pointAmount(), command.reason(), LocalDateTime.now()));
    }

    public void refundPoints(PointRefundCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        memberPoint.addPoints(command.pointAmount());
        memberPointRepository.save(memberPoint);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.REFUND, command.pointAmount(), "결제 취소 환불")
        );

        eventPublisher.publishEvent(new PointRefundedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void reclaimEarnedPoints(PointReclaimCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        int deductAmount = Math.min(memberPoint.getAvailablePoints(), command.pointAmount());
        memberPoint.deductPoints(deductAmount);
        memberPointRepository.save(memberPoint);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.USE, -deductAmount, "결제 취소 적립금 회수")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), deductAmount, LocalDateTime.now()));
    }

    public void deductPoints(PointDeductCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        memberPoint.deductPoints(command.pointAmount());
        memberPointRepository.save(memberPoint);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.USE, -command.pointAmount(), command.reason())
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }
}
