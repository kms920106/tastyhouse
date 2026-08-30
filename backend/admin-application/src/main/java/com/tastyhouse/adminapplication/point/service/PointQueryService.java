package com.tastyhouse.adminapplication.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;
import com.tastyhouse.application.point.port.out.PointSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.point.response.PointBalanceResponse;
import com.tastyhouse.adminapplication.point.response.PointHistoryResponse;
import com.tastyhouse.adminapplication.point.port.in.PointQueryUseCase;

/**
 * 포인트 관리 조회 서비스.
 *
 * <p>읽기 포트({@link PointQueryPort})만 주입해 조회하고 Response를 조립한다. write 포트·도메인
 * 서비스를 주입하지 않으며, 수동 적립·차감은 {@link PointCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class PointQueryService implements PointQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public PointBalanceResponse getPointBalance(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .map(result -> toPointBalanceResponse(memberId, result))
            .orElseGet(() -> PointBalanceResponse.zero(memberId));
    }

    @Override
    public PaginationResponse<PointHistoryResponse> getPointHistories(Long memberId, String type, int page, int size) {
        PointType pointType = type == null ? null : PointType.from(type);
        PointSearchCondition condition = PointSearchCondition.of(memberId, pointType);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<PointHistoryResponse> pageResult = pointQueryPort.findPointHistoryPage(condition, pageQuery)
            .map(this::toPointHistoryResponse);
        return PaginationResponse.from(pageResult);
    }

    private PointBalanceResponse toPointBalanceResponse(Long memberId, PointBalanceResult result) {
        return PointBalanceResponse.from(memberId, result.availablePoints(), result.expiredThisMonth());
    }

    private PointHistoryResponse toPointHistoryResponse(PointHistoryResult result) {
        return PointHistoryResponse.from(result.pointType().name(), result.pointAmount(), result.reason(), result.createdAt());
    }
}
