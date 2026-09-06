package com.tastyhouse.application.point.service;

import com.tastyhouse.application.shared.marker.AdminApp;
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
import com.tastyhouse.application.point.port.in.PointManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class PointManagementQueryService implements PointManagementQueryUseCase {

    private final PointManagementQueryPort pointManagementQueryPort;

    public PointManagementQueryService(PointManagementQueryPort pointManagementQueryPort) {
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
