package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteCommand;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteUseCase;
import com.tastyhouse.application.partnership.port.out.write.PartnershipPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class PartnershipDeleteService implements PartnershipDeleteUseCase {

    private final PartnershipPersistencePort partnershipPersistencePort;

    public PartnershipDeleteService(PartnershipPersistencePort partnershipPersistencePort) {
        this.partnershipPersistencePort = partnershipPersistencePort;
    }

    @Override
    public void deletePartnershipRequest(PartnershipDeleteCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.delete();
        partnershipPersistencePort.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipPersistencePort.findById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
