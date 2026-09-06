package com.tastyhouse.application.point.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;
import com.tastyhouse.application.point.port.in.PointQueryUseCase;
import com.tastyhouse.application.point.port.out.PointHistoryItemViewResult;
import com.tastyhouse.application.point.port.out.PointHistoryViewResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class PointQueryService implements PointQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public PointBalanceResult getMemberPoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .orElseGet(() -> new PointBalanceResult(0, 0));
    }

    @Override
    public PointHistoryViewResult getPointHistory(Long memberId) {
        PointBalanceResult balance = getMemberPoint(memberId);

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

    @Override
    public Integer getUsablePoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .map(PointBalanceResult::availablePoints)
            .orElse(0);
    }

    private PointHistoryItemViewResult toPointHistoryItemViewResult(PointHistoryResult history) {
        String pointType = history.pointType().name();
        Integer pointAmount = "USE".equals(pointType) ? -history.pointAmount() : history.pointAmount();
        return new PointHistoryItemViewResult(
            history.reason(),
            history.createdAt().toLocalDate(),
            pointAmount,
            pointType
        );
    }
}
