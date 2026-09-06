package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface PartnershipManagementCommandUseCase {

    void changeStatus(PartnershipStatusChangeCommand command);

    void deletePartnershipRequest(PartnershipDeleteCommand command);
}
