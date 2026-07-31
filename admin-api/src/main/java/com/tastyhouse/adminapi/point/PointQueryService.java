package com.tastyhouse.adminapi.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.point.domain.model.PointType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.point.query.PointBalanceResult;
import com.tastyhouse.infrastructure.point.query.PointHistoryResult;
import com.tastyhouse.infrastructure.point.query.PointQueryDao;
import com.tastyhouse.infrastructure.point.query.PointSearchCondition;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.point.response.PointBalanceResponse;
import com.tastyhouse.adminapi.point.response.PointHistoryResponse;

/**
 * 포인트 관리 조회 서비스.
 *
 * <p>infra read 어댑터({@link PointQueryDao})만 주입해 조회하고 Response를 조립한다. write 포트·도메인
 * 서비스를 주입하지 않으며, 수동 적립·차감은 {@link PointCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointQueryService {

    private final PointQueryDao pointQueryDao;

    public PointBalanceResponse getPointBalance(Long memberId) {
        return pointQueryDao.findBalanceByMemberId(MemberId.of(memberId))
            .map(result -> toPointBalanceResponse(memberId, result))
            .orElseGet(() -> PointBalanceResponse.zero(memberId));
    }

    public PaginationResponse<PointHistoryResponse> getPointHistories(Long memberId, String type, int page, int size) {
        PointType pointType = type == null ? null : PointType.from(type);
        PointSearchCondition condition = PointSearchCondition.of(MemberId.of(memberId), pointType);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<PointHistoryResponse> pageResult = pointQueryDao.findPointHistoryPage(condition, pageQuery)
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
