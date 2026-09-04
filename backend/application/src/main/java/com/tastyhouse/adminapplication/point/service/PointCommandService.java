package com.tastyhouse.adminapplication.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.point.port.in.PointCommandUseCase;
import com.tastyhouse.adminapplication.point.port.in.PointDeductCommand;
import com.tastyhouse.adminapplication.point.port.in.PointEarnCommand;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.service.PointLedgerService;

/**
 * 포인트 관리 command 서비스.
 *
 * <p>관리자의 수동 적립·차감을 처리한다. 잔액 변경과 이력 기록은 애그리거트 2개를 함께 다루는
 * 불변식이므로 도메인 서비스 {@link PointLedgerService}에 위임하고, 이 서비스는 트랜잭션 경계와
 * {@code Long → MemberId} 승격만 담당한다. 조회는 {@code PointManagementQueryService}가 담당하며 이 서비스는
 * infra query DAO를 주입하지 않는다.
 */
@Service
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
