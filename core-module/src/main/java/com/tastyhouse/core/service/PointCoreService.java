package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.point.MemberPoint;
import com.tastyhouse.core.entity.point.MemberPointHistory;
import com.tastyhouse.core.entity.point.PointType;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.point.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointCoreService {

    private final MemberPointRepository memberPointRepository;

    @Transactional(readOnly = true)
    public Optional<MemberPoint> findMemberPoint(Long memberId) {
        return memberPointRepository.findByMemberId(memberId);
    }

    @Transactional
    public void usePoints(Long memberId, int pointAmount) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        if (memberPoint.getAvailablePoints() < pointAmount) {
            throw new BusinessException(ErrorCode.POINT_INSUFFICIENT);
        }

        memberPoint.deductPoints(pointAmount);

        memberPointRepository.saveHistory(
            MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.USE)
                .pointAmount(-pointAmount)
                .reason("주문 결제 사용")
                .build()
        );
    }

    @Transactional
    public void earnPoints(Long memberId, int pointAmount, String reason) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND));

        memberPoint.addPoints(pointAmount);

        memberPointRepository.saveHistory(
            MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.EARNED)
                .pointAmount(pointAmount)
                .reason(reason)
                .build()
        );
    }

    @Transactional
    public MemberPoint getOrCreateMemberPoint(Long memberId) {
        MemberPoint existing = memberPointRepository.findByMemberId(memberId).orElse(null);
        if (existing != null) {
            return existing;
        }
        return memberPointRepository.save(
            MemberPoint.builder()
                .memberId(memberId)
                .availablePoints(0)
                .build()
        );
    }

    @Transactional
    public void refundPoints(Long memberId, int pointAmount) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId));

        memberPoint.addPoints(pointAmount);

        memberPointRepository.saveHistory(
            MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.REFUND)
                .pointAmount(pointAmount)
                .reason("결제 취소 환불")
                .build()
        );
    }

    @Transactional
    public void reclaimEarnedPoints(Long memberId, int pointAmount) {
        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
                "포인트 정보를 찾을 수 없습니다. memberId=" + memberId));

        int deductAmount = Math.min(memberPoint.getAvailablePoints(), pointAmount);
        memberPoint.deductPoints(deductAmount);

        memberPointRepository.saveHistory(
            MemberPointHistory.builder()
                .memberId(memberId)
                .pointType(PointType.USE)
                .pointAmount(-deductAmount)
                .reason("결제 취소 적립금 회수")
                .build()
        );
    }
}
