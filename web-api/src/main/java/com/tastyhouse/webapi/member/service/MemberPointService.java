package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.service.PointCoreService;
import com.tastyhouse.webapi.member.response.PointHistoryItemResponse;
import com.tastyhouse.webapi.member.response.PointHistoryResponse;
import com.tastyhouse.webapi.member.response.PointResponse;
import com.tastyhouse.webapi.member.response.UsablePointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberPointService {

    private final PointCoreService pointCoreService;

    // 회원의 보유 포인트 및 이번 달 소멸 예정 포인트를 조회
    @Transactional(readOnly = true)
    public PointResponse getMemberPoint(Long memberId) {
        return pointCoreService.findMemberPoint(memberId)
            .map(PointResponse::from)
            .orElseGet(() -> new PointResponse(0, 0));
    }

    // 회원의 포인트 적립·사용 내역을 최신순으로 조회
    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = pointCoreService.findPointHistory(memberId)
            .stream()
            .map(PointHistoryItemResponse::from)
            .collect(Collectors.toList());

        return new PointHistoryResponse(pointResponse.availablePoints(), pointResponse.expiredThisMonth(), histories);
    }

    // 회원이 즉시 사용 가능한 포인트를 조회
    @Transactional(readOnly = true)
    public UsablePointResponse getUsablePoint(Long memberId) {
        return pointCoreService.findMemberPoint(memberId)
            .map(UsablePointResponse::from)
            .orElseGet(() -> new UsablePointResponse(0));
    }
}
