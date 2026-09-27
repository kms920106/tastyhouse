package com.tastyhouse.application.point.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.point.model.PointType;
import com.tastyhouse.application.point.port.in.PointManagementQueryUseCase;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointManagementQueryPort;
import com.tastyhouse.application.point.port.out.PointSearchCondition;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

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
        String pointType = type == null ? null : PointType.from(type).name();
        PointSearchCondition condition = PointSearchCondition.of(memberId, pointType);
        PageQuery pageQuery = PageQuery.of(page, size);
        return pointManagementQueryPort.findPointHistoryPage(condition, pageQuery);
    }
}
