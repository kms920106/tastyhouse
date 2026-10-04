package com.tastyhouse.application.ceo.port.in;

public interface CeoLoginHistoryCommandUseCase {

    void recordSuccess(CeoLoginHistorySuccessCommand command);

    void recordFailure(CeoLoginHistoryFailureCommand command);
}
