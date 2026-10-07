package com.tastyhouse.application.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.point.port.in.PointDeductCommand;
import com.tastyhouse.application.point.port.in.PointDeductUseCase;

@Service
@Transactional
class PointDeductService implements PointDeductUseCase {

    private final PointLedgerService pointLedgerService;

    public PointDeductService(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    @Override
    public void deductPoint(PointDeductCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        pointLedgerService.deductPoints(targetMemberId, command.amount(), command.reason());
    }
}
