package com.tastyhouse.webapi.point.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.application.PointQueryService;
import com.tastyhouse.core.domain.point.application.dto.result.PointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.PointResult;
import com.tastyhouse.webapi.point.response.PointHistoryItemResponse;
import com.tastyhouse.webapi.point.response.PointHistoryResponse;
import com.tastyhouse.webapi.point.response.PointResponse;
import com.tastyhouse.webapi.point.response.PointUsableResponse;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointQueryService pointQueryService;

    @Transactional(readOnly = true)
    public PointResponse getMemberPoint(Long memberId) {
        return pointQueryService.findMemberPoint(MemberId.of(memberId))
            .map(this::toPointResponse)
            .orElseGet(() -> PointResponse.of(0, 0));
    }

    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = pointQueryService.findPointHistory(MemberId.of(memberId))
            .stream()
            .map(this::toPointHistoryItemResponse)
            .collect(Collectors.toList());

        return PointHistoryResponse.from(
            pointResponse.availablePoints(),
            pointResponse.expiredThisMonth(),
            histories
        );
    }

    @Transactional(readOnly = true)
    public PointUsableResponse getUsablePoint(Long memberId) {
        return pointQueryService.findMemberPoint(MemberId.of(memberId))
            .map(result -> PointUsableResponse.of(result.availablePoints()))
            .orElseGet(() -> PointUsableResponse.of(0));
    }

    private PointResponse toPointResponse(PointResult result) {
        return PointResponse.of(result.availablePoints(), result.expiredThisMonth());
    }

    private PointHistoryItemResponse toPointHistoryItemResponse(PointHistoryResult history) {
        String pointType = history.pointType().name();
        Integer pointAmount = "USE".equals(pointType) ? -history.pointAmount() : history.pointAmount();
        return PointHistoryItemResponse.from(
            history.reason(),
            history.createdAt().toLocalDate(),
            pointAmount,
            pointType
        );
    }
}
