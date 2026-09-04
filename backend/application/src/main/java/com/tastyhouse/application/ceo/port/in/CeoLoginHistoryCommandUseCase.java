package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 로그인 이력 기록 쓰기 인바운드 포트.
 */
@CeoApp
public interface CeoLoginHistoryCommandUseCase {

    void recordSuccess(CeoLoginHistorySuccessCommand command);

    void recordFailure(CeoLoginHistoryFailureCommand command);
}
