package com.tastyhouse.application.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.in.PointUsableQueryUseCase;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;

@Service
@Transactional(readOnly = true)
class PointUsableQueryService implements PointUsableQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointUsableQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public Integer getUsablePoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .map(PointBalanceResult::availablePoints)
            .orElse(0);
    }
}
