package com.tastyhouse.adminapi.point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.point.domain.service.PointLedgerService;

/**
 * 포인트 관리 command 서비스.
 *
 * <p>관리자의 수동 적립·차감을 처리한다. 잔액 변경과 이력 기록은 애그리거트 2개를 함께 다루는
 * 불변식이므로 도메인 서비스 {@link PointLedgerService}에 위임하고, 이 서비스는 트랜잭션 경계와
 * {@code Long → MemberId} 승격만 담당한다. 조회는 {@link PointQueryService}가 담당하며 이 서비스는
 * infra query DAO를 주입하지 않는다.
 */
@Service
@Transactional
public class PointCommandService {

    private final PointLedgerService pointLedgerService;

    public PointCommandService(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    public void earnPoint(Long memberId, int amount, String reason) {
        MemberId targetMemberId = MemberId.of(memberId);
        pointLedgerService.earnPoints(targetMemberId, amount, reason);
    }

    public void deductPoint(Long memberId, int amount, String reason) {
        MemberId targetMemberId = MemberId.of(memberId);
        pointLedgerService.deductPoints(targetMemberId, amount, reason);
    }
}
