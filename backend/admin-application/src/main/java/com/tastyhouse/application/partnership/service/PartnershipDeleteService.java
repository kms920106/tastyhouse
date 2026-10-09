package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteCommand;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteUseCase;
import com.tastyhouse.application.partnership.port.out.write.PartnershipLoadPort;
import com.tastyhouse.application.partnership.port.out.write.PartnershipSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class PartnershipDeleteService implements PartnershipDeleteUseCase {

    private final PartnershipLoadPort partnershipLoadPort;
    private final PartnershipSavePort partnershipSavePort;

    public PartnershipDeleteService(PartnershipLoadPort partnershipLoadPort, PartnershipSavePort partnershipSavePort) {
        this.partnershipLoadPort = partnershipLoadPort;
        this.partnershipSavePort = partnershipSavePort;
    }

    @Override
    public void deletePartnershipRequest(PartnershipDeleteCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.delete();
        partnershipSavePort.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipLoadPort.findById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
