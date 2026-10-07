package com.tastyhouse.application.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.point.port.in.PointEarnCommand;
import com.tastyhouse.application.point.port.in.PointEarnUseCase;

@Service
@Transactional
class PointEarnService implements PointEarnUseCase {

    private final PointLedgerService pointLedgerService;

    public PointEarnService(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    @Override
    public void earnPoint(PointEarnCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        pointLedgerService.earnPoints(targetMemberId, command.amount(), command.reason());
    }
}
