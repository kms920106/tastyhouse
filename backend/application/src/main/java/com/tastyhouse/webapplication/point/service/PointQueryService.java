package com.tastyhouse.webapplication.point.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;
import com.tastyhouse.webapplication.point.port.in.PointQueryUseCase;
import com.tastyhouse.webapplication.point.port.out.PointHistoryItemViewResult;
import com.tastyhouse.webapplication.point.port.out.PointHistoryViewResult;

/**
 * 내 포인트 조회 서비스.
 *
 * <p>읽기 포트({@link PointQueryPort})만 주입해 조회하고 조회 결과를 조립한다. Response 조립은
 * 챕터 10에서 컨트롤러(web-api)로 올라갔다. web-api에는 포인트 쓰기 경로가 없으므로(주문 결제 사용은
 * order 도메인 트랜잭션 안에서 도메인 서비스가 처리) CommandService를 두지 않는다.
 */
@Service
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
