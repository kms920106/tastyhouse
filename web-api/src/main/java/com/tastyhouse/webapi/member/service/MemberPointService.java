package com.tastyhouse.webapi.member.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.application.PointQueryService;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;
import com.tastyhouse.webapi.member.response.MemberPointHistoryItemResponse;
import com.tastyhouse.webapi.member.response.MemberPointHistoryResponse;
import com.tastyhouse.webapi.member.response.MemberPointResponse;
import com.tastyhouse.webapi.member.response.MemberUsablePointResponse;

@Service
@RequiredArgsConstructor
public class MemberPointService {

    private final PointQueryService pointQueryService;

    @Transactional(readOnly = true)
    public MemberPointResponse getMemberPoint(Long memberId) {
        return pointQueryService.findMemberPoint(MemberId.of(memberId))
            .map(this::toPointResponse)
            .orElseGet(() -> MemberPointResponse.of(0, 0));
    }

    @Transactional(readOnly = true)
    public MemberPointHistoryResponse getPointHistory(Long memberId) {
        MemberPointResponse pointResponse = getMemberPoint(memberId);

        List<MemberPointHistoryItemResponse> histories = pointQueryService.findPointHistory(MemberId.of(memberId))
            .stream()
            .map(this::toPointHistoryItemResponse)
            .collect(Collectors.toList());

        return MemberPointHistoryResponse.from(
            pointResponse.availablePoints(),
            pointResponse.expiredThisMonth(),
            histories
        );
    }

    @Transactional(readOnly = true)
    public MemberUsablePointResponse getUsablePoint(Long memberId) {
        return pointQueryService.findMemberPoint(MemberId.of(memberId))
            .map(result -> MemberUsablePointResponse.of(result.availablePoints()))
            .orElseGet(() -> MemberUsablePointResponse.of(0));
    }

    private MemberPointResponse toPointResponse(MemberPointResult result) {
        return MemberPointResponse.of(result.availablePoints(), result.expiredThisMonth());
    }

    private MemberPointHistoryItemResponse toPointHistoryItemResponse(MemberPointHistoryResult history) {
        String pointType = history.pointType().name();
        Integer pointAmount = "USE".equals(pointType) ? -history.pointAmount() : history.pointAmount();
        return MemberPointHistoryItemResponse.from(
            history.reason(),
            history.createdAt().toLocalDate(),
            pointAmount,
            pointType
        );
    }
}
