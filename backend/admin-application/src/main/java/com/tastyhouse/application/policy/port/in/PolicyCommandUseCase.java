package com.tastyhouse.application.policy.port.in;

public interface PolicyCommandUseCase {

    Long createPolicy(PolicyCreateCommand command);

    void updatePolicy(PolicyUpdateCommand command);

    void activateCurrentPolicy(PolicyActivateCommand command);
}
