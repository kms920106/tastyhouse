package com.tastyhouse.adminapplication.point.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointManagementQueryPort;
import com.tastyhouse.application.point.port.out.PointSearchCondition;
import com.tastyhouse.adminapplication.point.port.in.PointQueryUseCase;

/**
 * 포인트 관리 조회 서비스.
 *
 * <p>읽기 포트({@link PointManagementQueryPort})만 주입해 조회한다. write 포트·도메인 서비스를 주입하지
 * 않으며, 수동 적립·차감은 {@link PointCommandService}가 담당한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class PointQueryService implements PointQueryUseCase {

    private final PointManagementQueryPort pointManagementQueryPort;

    public PointQueryService(PointManagementQueryPort pointManagementQueryPort) {
        this.pointManagementQueryPort = pointManagementQueryPort;
    }

    @Override
    public Optional<PointBalanceResult> getPointBalance(Long memberId) {
        return pointManagementQueryPort.findBalanceByMemberId(memberId);
    }

    @Override
    public PageResult<PointHistoryResult> getPointHistories(Long memberId, String type, int page, int size) {
        PointType pointType = type == null ? null : PointType.from(type);
        PointSearchCondition condition = PointSearchCondition.of(memberId, pointType);
        PageQuery pageQuery = PageQuery.of(page, size);
        return pointManagementQueryPort.findPointHistoryPage(condition, pageQuery);
    }
}
