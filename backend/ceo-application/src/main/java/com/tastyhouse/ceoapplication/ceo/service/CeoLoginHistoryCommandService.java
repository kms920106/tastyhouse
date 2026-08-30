package com.tastyhouse.ceoapplication.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistoryCommandUseCase;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistoryFailureCommand;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.domain.ceo.vo.CeoId;

/**
 * 점주 로그인 이력 기록 서비스(CQRS command 측).
 *
 * <p>이 클래스가 존재하는 이유는 <b>트랜잭션 경계</b> 하나 때문이다. 호출부인
 * {@code AuthService#login}은 {@code @Transactional}이 아니어야 한다 — 거기에 트랜잭션을 걸면 인증 실패
 * 시 던져지는 Spring Security 예외와 함께 <b>실패 이력까지 롤백되어 영구히 남지 않는다</b>. 호출부가
 * 비트랜잭션이므로 이 서비스의 매 호출이 프록시를 거쳐 <b>독립 트랜잭션으로 즉시 커밋</b>되고,
 * {@code REQUIRES_NEW}가 필요 없다.
 */
@Service
@Transactional
public class CeoLoginHistoryCommandService implements CeoLoginHistoryCommandUseCase {

    private final CeoLoginHistoryRecorder ceoLoginHistoryRecorder;

    public CeoLoginHistoryCommandService(CeoLoginHistoryRecorder ceoLoginHistoryRecorder) {
        this.ceoLoginHistoryRecorder = ceoLoginHistoryRecorder;
    }

    /**
     * 로그인 성공 이력을 기록한다.
     */
    @Override
    public void recordSuccess(CeoLoginHistorySuccessCommand command) {
        ceoLoginHistoryRecorder.recordSuccess(
            CeoId.of(command.ceoId()),
            command.ipAddress(),
            command.userAgent()
        );
    }

    /**
     * 로그인 실패 이력을 기록한다.
     */
    @Override
    public void recordFailure(CeoLoginHistoryFailureCommand command) {
        ceoLoginHistoryRecorder.recordFailure(
            CeoId.of(command.ceoId()),
            CeoLoginFailureReason.valueOf(command.failureReason()),
            command.ipAddress(),
            command.userAgent()
        );
    }
}
