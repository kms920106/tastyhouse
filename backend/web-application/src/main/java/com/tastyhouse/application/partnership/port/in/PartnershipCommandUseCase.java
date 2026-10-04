package com.tastyhouse.application.partnership.port.in;

public interface PartnershipCommandUseCase {

    Long createPartnershipRequest(PartnershipRequestCreateCommand command);
}
