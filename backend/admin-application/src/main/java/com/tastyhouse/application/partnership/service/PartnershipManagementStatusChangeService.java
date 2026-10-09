package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.application.partnership.port.in.PartnershipManagementStatusChangeUseCase;
import com.tastyhouse.application.partnership.port.in.PartnershipStatusChangeCommand;
import com.tastyhouse.application.partnership.port.out.write.PartnershipLoadPort;
import com.tastyhouse.application.partnership.port.out.write.PartnershipSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class PartnershipManagementStatusChangeService implements PartnershipManagementStatusChangeUseCase {

    private final PartnershipLoadPort partnershipLoadPort;
    private final PartnershipSavePort partnershipSavePort;

    public PartnershipManagementStatusChangeService(PartnershipLoadPort partnershipLoadPort, PartnershipSavePort partnershipSavePort) {
        this.partnershipLoadPort = partnershipLoadPort;
        this.partnershipSavePort = partnershipSavePort;
    }

    @Override
    public void changeStatus(PartnershipStatusChangeCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipStatus partnershipStatus = PartnershipStatus.from(command.status());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.changeStatus(partnershipStatus);
        partnershipSavePort.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipLoadPort.findActiveById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
