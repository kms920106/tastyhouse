package com.tastyhouse.application.partnership.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.application.partnership.port.in.PartnershipCommandUseCase;
import com.tastyhouse.application.partnership.port.in.PartnershipRequestCreateCommand;

@Service
@WebApp
@Transactional
public class PartnershipCommandService implements PartnershipCommandUseCase {

    private final PartnershipRepository partnershipRepository;

    public PartnershipCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    @Override
    public Long createPartnershipRequest(PartnershipRequestCreateCommand command) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            command.businessName(), command.address(), command.addressDetail(),
            command.contactName(), command.contactPhone(), command.consultationRequestedAt()
        );
        PartnershipRequest saved = partnershipRepository.save(partnershipRequest);
        return saved.getPartnershipRequestId().value();
    }
}
