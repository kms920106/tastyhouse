package com.tastyhouse.application.partnership.port.in;

public interface PartnershipManagementCommandUseCase {

    void changeStatus(PartnershipStatusChangeCommand command);

    void deletePartnershipRequest(PartnershipDeleteCommand command);
}
