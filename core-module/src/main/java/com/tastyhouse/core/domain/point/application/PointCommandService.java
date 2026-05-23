package com.tastyhouse.core.domain.point.application;

import com.tastyhouse.core.domain.point.application.dto.command.EarnPointCommand;
import com.tastyhouse.core.domain.point.application.dto.command.ReclaimPointCommand;
import com.tastyhouse.core.domain.point.application.dto.command.RefundPointCommand;
import com.tastyhouse.core.domain.point.application.dto.command.UsePointCommand;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;
import com.tastyhouse.core.domain.point.domain.event.PointEarnedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointRefundedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointUsedEvent;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PointCommandService {

    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void usePoints(UsePointCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        memberPoint.deductPoints(command.pointAmount());

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.USE, -command.pointAmount(), "주문 결제 사용")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void earnPoints(EarnPointCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        memberPoint.addPoints(command.pointAmount());

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.EARNED, command.pointAmount(), command.reason())
        );

        eventPublisher.publishEvent(new PointEarnedEvent(command.memberId(), command.pointAmount(), command.reason(), LocalDateTime.now()));
    }

    public void refundPoints(RefundPointCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        memberPoint.addPoints(command.pointAmount());

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.REFUND, command.pointAmount(), "결제 취소 환불")
        );

        eventPublisher.publishEvent(new PointRefundedEvent(command.memberId(), command.pointAmount(), LocalDateTime.now()));
    }

    public void reclaimEarnedPoints(ReclaimPointCommand command) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + command.memberId()));

        int deductAmount = Math.min(memberPoint.getAvailablePoints(), command.pointAmount());
        memberPoint.deductPoints(deductAmount);

        memberPointHistoryRepository.save(
            MemberPointHistory.of(command.memberId(), PointType.USE, -deductAmount, "결제 취소 적립금 회수")
        );

        eventPublisher.publishEvent(new PointUsedEvent(command.memberId(), deductAmount, LocalDateTime.now()));
    }

    public MemberPointResult getOrCreateMemberPoint(Long memberId) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
            .orElseGet(() -> memberPointRepository.save(MemberPoint.of(memberId)));
        return MemberPointResult.from(memberPoint);
    }
}
