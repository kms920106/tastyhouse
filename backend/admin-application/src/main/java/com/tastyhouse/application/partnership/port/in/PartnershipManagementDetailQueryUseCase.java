package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;

public interface PartnershipManagementDetailQueryUseCase {

    PartnershipRequestDetailResult getPartnershipRequest(Long id);
}
