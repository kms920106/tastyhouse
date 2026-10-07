package com.tastyhouse.application.partnership.port.in;

public interface PartnershipManagementStatusChangeUseCase {

    void changeStatus(PartnershipStatusChangeCommand command);
}
