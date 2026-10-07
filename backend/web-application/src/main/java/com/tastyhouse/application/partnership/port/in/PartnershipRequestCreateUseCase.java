package com.tastyhouse.application.partnership.port.in;

public interface PartnershipRequestCreateUseCase {

    Long createPartnershipRequest(PartnershipRequestCreateCommand command);
}
