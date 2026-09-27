package com.tastyhouse.application.partnership.store;

import java.util.Optional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestStatePort;

public class PartnershipStore implements PartnershipRepository {
    private final PartnershipRequestStatePort partnershipRequestStatePort;

    public PartnershipStore(PartnershipRequestStatePort partnershipRequestStatePort) {
        this.partnershipRequestStatePort = partnershipRequestStatePort;
    }

    @Override
    public Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId) {
        if (partnershipRequestId == null) {
            return Optional.empty();
        }
        return partnershipRequestStatePort.findById(partnershipRequestId.value())
            .map(PartnershipRequestStateMapper::toDomain);
    }

    @Override
    public PartnershipRequest save(PartnershipRequest partnershipRequest) {
        return PartnershipRequestStateMapper.toDomain(
            partnershipRequestStatePort.save(PartnershipRequestStateMapper.toState(partnershipRequest)));
    }
}
