package com.tastyhouse.core.domain.partnership.domain.repository;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;

public interface PartnershipRepository {

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}
