package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.application.partnership.port.in.PartnershipCommandUseCase;
import com.tastyhouse.application.partnership.port.in.PartnershipRequestCreateCommand;
import com.tastyhouse.application.partnership.port.out.write.PartnershipPersistencePort;

@Service
@Transactional
class PartnershipCommandService implements PartnershipCommandUseCase {

    private final PartnershipPersistencePort partnershipPersistencePort;

    public PartnershipCommandService(PartnershipPersistencePort partnershipPersistencePort) {
        this.partnershipPersistencePort = partnershipPersistencePort;
    }

    @Override
    public Long createPartnershipRequest(PartnershipRequestCreateCommand command) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            command.businessName(), command.address(), command.addressDetail(),
            command.contactName(), command.contactPhone(), command.consultationRequestedAt()
        );
        PartnershipRequest saved = partnershipPersistencePort.save(partnershipRequest);
        return saved.getPartnershipRequestId().value();
    }
}
