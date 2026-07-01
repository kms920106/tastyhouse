package com.tastyhouse.webapi.member.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.point.application.PointQueryService;
import com.tastyhouse.webapi.member.response.PointHistoryItemResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;

@Service
@RequiredArgsConstructor
public class MemberPointService {

    private final PointQueryService pointQueryService;

    @Transactional(readOnly = true)
    public PointResponse getMemberPoint(Long memberId) {
        return pointQueryService.findMemberPoint(memberId)
            .map(PointResponse::from)
            .orElseGet(() -> PointResponse.of(0, 0));
    }

    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = pointQueryService.findPointHistory(memberId)
            .stream()
            .map(PointHistoryItemResponse::from)
            .collect(Collectors.toList());

        return PointHistoryResponse.from(
            pointResponse.availablePoints(),
            pointResponse.expiredThisMonth(),
            histories
        );
    }

    @Transactional(readOnly = true)
    public UsablePointResponse getUsablePoint(Long memberId) {
        return pointQueryService.findMemberPoint(memberId)
            .map(UsablePointResponse::from)
            .orElseGet(() -> UsablePointResponse.of(0));
    }
}
