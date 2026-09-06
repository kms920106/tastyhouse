package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface CeoLoginHistoryCommandUseCase {

    void recordSuccess(CeoLoginHistorySuccessCommand command);

    void recordFailure(CeoLoginHistoryFailureCommand command);
}
