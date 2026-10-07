package com.tastyhouse.application.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.in.PointBalanceQueryUseCase;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;

@Service
@Transactional(readOnly = true)
class PointBalanceQueryService implements PointBalanceQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointBalanceQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public PointBalanceResult getMemberPoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .orElseGet(() -> new PointBalanceResult(0, 0));
    }
}
