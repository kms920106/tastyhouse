package com.tastyhouse.application.partnership.store;

import java.util.Optional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

public interface PartnershipRepository {
    Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId);

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}
