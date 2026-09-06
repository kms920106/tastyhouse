package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface PolicyCommandUseCase {

    Long createPolicy(PolicyCreateCommand command);

    void updatePolicy(PolicyUpdateCommand command);

    void activateCurrentPolicy(PolicyActivateCommand command);
}
