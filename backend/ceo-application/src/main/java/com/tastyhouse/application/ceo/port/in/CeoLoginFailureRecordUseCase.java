package com.tastyhouse.application.ceo.port.in;

public interface CeoLoginFailureRecordUseCase {

    void recordFailure(CeoLoginHistoryFailureCommand command);
}
