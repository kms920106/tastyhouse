package com.tastyhouse.application.partnership.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

public interface PartnershipLoadPort {

    Optional<PartnershipRequest> findActiveById(PartnershipRequestId partnershipRequestId);
}
