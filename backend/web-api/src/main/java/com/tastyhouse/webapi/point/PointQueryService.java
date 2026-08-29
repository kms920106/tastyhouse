package com.tastyhouse.webapi.point;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.point.port.out.PointQueryPort;
import com.tastyhouse.webapi.point.application.port.in.PointQueryUseCase;
import com.tastyhouse.webapi.point.response.PointHistoryItemResponse;
import com.tastyhouse.webapi.point.response.PointHistoryResponse;
import com.tastyhouse.webapi.point.response.PointResponse;
import com.tastyhouse.webapi.point.response.PointUsableResponse;

/**
 * 내 포인트 조회 서비스.
 *
 * <p>읽기 포트({@link PointQueryPort})만 주입해 조회하고 Response를 조립한다. web-api에는 포인트
 * 쓰기 경로가 없으므로(주문 결제 사용은 order 도메인 트랜잭션 안에서 도메인 서비스가 처리) CommandService를
 * 두지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class PointQueryService implements PointQueryUseCase {

    private final PointQueryPort pointQueryPort;

    public PointQueryService(PointQueryPort pointQueryPort) {
        this.pointQueryPort = pointQueryPort;
    }

    @Override
    public PointResponse getMemberPoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .map(this::toPointResponse)
            .orElseGet(() -> PointResponse.of(0, 0));
    }

    @Override
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = pointQueryPort.findPointHistories(memberId)
            .stream()
            .map(this::toPointHistoryItemResponse)
            .toList();

        return PointHistoryResponse.from(
            pointResponse.availablePoints(),
            pointResponse.expiredThisMonth(),
            histories
        );
    }

    @Override
    public PointUsableResponse getUsablePoint(Long memberId) {
        return pointQueryPort.findBalanceByMemberId(memberId)
            .map(result -> PointUsableResponse.of(result.availablePoints()))
            .orElseGet(() -> PointUsableResponse.of(0));
    }

    private PointResponse toPointResponse(PointBalanceResult result) {
        return PointResponse.of(result.availablePoints(), result.expiredThisMonth());
    }

    private PointHistoryItemResponse toPointHistoryItemResponse(PointHistoryResult history) {
        String pointType = history.pointType().name();
        Integer pointAmount = "USE".equals(pointType) ? -history.pointAmount() : history.pointAmount();
        return PointHistoryItemResponse.from(
            history.reason(),
            history.createdAt().toLocalDate(),
            pointAmount,
            pointType
        );
    }
}
