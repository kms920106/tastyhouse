package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteCommand;
import com.tastyhouse.application.partnership.port.in.PartnershipManagementCommandUseCase;
import com.tastyhouse.application.partnership.port.in.PartnershipStatusChangeCommand;
import com.tastyhouse.application.partnership.port.out.write.PartnershipPersistencePort;

@Service
@Transactional
public class PartnershipManagementCommandService implements PartnershipManagementCommandUseCase {

    private final PartnershipPersistencePort partnershipPersistencePort;

    public PartnershipManagementCommandService(PartnershipPersistencePort partnershipPersistencePort) {
        this.partnershipPersistencePort = partnershipPersistencePort;
    }

    @Override
    public void changeStatus(PartnershipStatusChangeCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipStatus partnershipStatus = PartnershipStatus.from(command.status());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.changeStatus(partnershipStatus);
        partnershipPersistencePort.save(partnershipRequest);
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
