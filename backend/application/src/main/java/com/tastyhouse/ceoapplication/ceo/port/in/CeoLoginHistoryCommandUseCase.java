package com.tastyhouse.ceoapplication.ceo.port.in;

/**
 * 점주 로그인 이력 기록 쓰기 인바운드 포트.
 */
public interface CeoLoginHistoryCommandUseCase {

    void recordSuccess(CeoLoginHistorySuccessCommand command);

    void recordFailure(CeoLoginHistoryFailureCommand command);
}
