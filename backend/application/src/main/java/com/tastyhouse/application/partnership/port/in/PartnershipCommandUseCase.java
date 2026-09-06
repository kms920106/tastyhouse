package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface PartnershipCommandUseCase {

    Long createPartnershipRequest(PartnershipRequestCreateCommand command);
}
