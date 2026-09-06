package com.tastyhouse.application.point.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.in.PointCommandUseCase;
import com.tastyhouse.application.point.port.in.PointDeductCommand;
import com.tastyhouse.application.point.port.in.PointEarnCommand;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.service.PointLedgerService;

@Service
@AdminApp
@Transactional
public class PointCommandService implements PointCommandUseCase {

    private final PointLedgerService pointLedgerService;

    public PointCommandService(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    @Override
    public void earnPoint(PointEarnCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        pointLedgerService.earnPoints(targetMemberId, command.amount(), command.reason());
    }

    @Override
    public void deductPoint(PointDeductCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        pointLedgerService.deductPoints(targetMemberId, command.amount(), command.reason());
    }
}
