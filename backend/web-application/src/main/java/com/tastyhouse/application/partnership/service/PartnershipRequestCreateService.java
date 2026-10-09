package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.application.partnership.port.in.PartnershipRequestCreateCommand;
import com.tastyhouse.application.partnership.port.in.PartnershipRequestCreateUseCase;
import com.tastyhouse.application.partnership.port.out.write.PartnershipSavePort;

@Service
@Transactional
class PartnershipRequestCreateService implements PartnershipRequestCreateUseCase {

    private final PartnershipSavePort partnershipSavePort;

    public PartnershipRequestCreateService(PartnershipSavePort partnershipSavePort) {
        this.partnershipSavePort = partnershipSavePort;
    }

    @Override
    public Long createPartnershipRequest(PartnershipRequestCreateCommand command) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            command.businessName(), command.address(), command.addressDetail(),
            command.contactName(), command.contactPhone(), command.consultationRequestedAt()
        );
        PartnershipRequest saved = partnershipSavePort.save(partnershipRequest);
        return saved.getPartnershipRequestId().value();
    }
}
