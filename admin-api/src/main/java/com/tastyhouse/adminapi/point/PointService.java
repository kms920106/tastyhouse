package com.tastyhouse.adminapi.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.PointType;
import com.tastyhouse.core.domain.point.application.PointCommandService;
import com.tastyhouse.core.domain.point.application.PointQueryService;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.domain.point.application.dto.command.PointDeductCommand;
import com.tastyhouse.core.domain.point.application.dto.command.PointEarnCommand;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.point.response.PointBalanceResponse;
import com.tastyhouse.adminapi.point.response.PointHistoryPageResponse;
import com.tastyhouse.adminapi.point.response.PointHistoryResponse;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointCommandService pointCommandService;
    private final PointQueryService pointQueryService;

    public PointBalanceResponse getPointBalance(Long memberId) {
        return pointQueryService.findMemberPoint(MemberId.of(memberId))
            .map(this::toPointBalanceResponse)
            .orElseGet(() -> PointBalanceResponse.zero(memberId));
    }

    public PointHistoryPageResponse getPointHistories(Long memberId, String type, int page, int size) {
        PointType pointType = type == null ? null : PointType.from(type);
        PointSearchCondition condition = PointSearchCondition.of(MemberId.of(memberId), pointType);
        PageResult<PointHistoryResponse> pageResult = pointQueryService.findPointHistory(condition, page, size)
            .map(this::toPointHistoryResponse);
        return PointHistoryPageResponse.from(pageResult);
    }

    public void earnPoint(Long memberId, int amount, String reason) {
        MemberId targetMemberId = MemberId.of(memberId);
        PointEarnCommand command = PointEarnCommand.of(targetMemberId, amount, reason);
        pointCommandService.earnPoints(command);
    }

    public void deductPoint(Long memberId, int amount, String reason) {
        MemberId targetMemberId = MemberId.of(memberId);
        PointDeductCommand command = PointDeductCommand.of(targetMemberId, amount, reason);
        pointCommandService.deductPoints(command);
    }

    private PointBalanceResponse toPointBalanceResponse(MemberPointResult result) {
        return PointBalanceResponse.from(result.memberId().value(), result.availablePoints(), result.expiredThisMonth());
    }

    private PointHistoryResponse toPointHistoryResponse(MemberPointHistoryResult result) {
        return PointHistoryResponse.from(result.pointType().name(), result.pointAmount(), result.reason(), result.createdAt());
    }
}
