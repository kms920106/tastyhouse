package com.tastyhouse.application.partnership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.partnership.port.in.PartnershipManagementDetailQueryUseCase;
import com.tastyhouse.application.partnership.port.out.PartnershipQueryPort;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class PartnershipManagementDetailQueryService implements PartnershipManagementDetailQueryUseCase {

    private final PartnershipQueryPort partnershipQueryPort;

    public PartnershipManagementDetailQueryService(PartnershipQueryPort partnershipQueryPort) {
        this.partnershipQueryPort = partnershipQueryPort;
    }

    @Override
    public PartnershipRequestDetailResult getPartnershipRequest(Long id) {
        return partnershipQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
