package com.tastyhouse.application.point.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.in.PointManagementBalanceQueryUseCase;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointManagementQueryPort;

@Service
@Transactional(readOnly = true)
class PointManagementBalanceQueryService implements PointManagementBalanceQueryUseCase {

    private final PointManagementQueryPort pointManagementQueryPort;

    public PointManagementBalanceQueryService(PointManagementQueryPort pointManagementQueryPort) {
        this.pointManagementQueryPort = pointManagementQueryPort;
    }

    @Override
    public Optional<PointBalanceResult> getPointBalance(Long memberId) {
        return pointManagementQueryPort.findBalanceByMemberId(memberId);
    }
}
