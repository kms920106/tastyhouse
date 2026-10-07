package com.tastyhouse.application.ceo.port.in;

public interface CeoLoginSuccessRecordUseCase {

    void recordSuccess(CeoLoginHistorySuccessCommand command);
}
