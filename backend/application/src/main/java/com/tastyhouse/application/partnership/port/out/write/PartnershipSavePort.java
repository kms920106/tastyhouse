package com.tastyhouse.application.partnership.port.out.write;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;

public interface PartnershipSavePort {

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}
