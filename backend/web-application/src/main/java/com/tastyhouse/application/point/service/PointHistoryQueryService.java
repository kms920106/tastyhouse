package com.tastyhouse.application.point.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.in.PointHistoryQueryUseCase;
import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryItemViewResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointHistoryViewResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;

@Service
@Transactional(readOnly = true)
class PointHistoryQueryService implements PointHistoryQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointHistoryQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public PointHistoryViewResult getPointHistory(Long memberId) {
        PointBalanceResult balance = pointQueryPort.findBalanceByMemberId(memberId)
            .orElseGet(() -> new PointBalanceResult(0, 0));

        List<PointHistoryItemViewResult> histories = pointQueryPort.findPointHistories(memberId)
            .stream()
            .map(this::toPointHistoryItemViewResult)
            .toList();

        return new PointHistoryViewResult(
            balance.availablePoints(),
            balance.expiredThisMonth(),
            histories
        );
    }

    private PointHistoryItemViewResult toPointHistoryItemViewResult(PointHistoryResult history) {
        String pointType = history.pointType();
        Integer pointAmount = "USE".equals(pointType) ? -history.pointAmount() : history.pointAmount();
        return new PointHistoryItemViewResult(
            history.reason(),
            history.createdAt().toLocalDate(),
            pointAmount,
            pointType
        );
    }
}
