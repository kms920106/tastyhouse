package com.tastyhouse.application.partnership.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.partnership.port.in.PartnershipManagementCommandUseCase;
import com.tastyhouse.application.partnership.port.in.PartnershipDeleteCommand;
import com.tastyhouse.application.partnership.port.in.PartnershipStatusChangeCommand;
import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.partnership.repository.PartnershipRepository;
import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class PartnershipManagementCommandService implements PartnershipManagementCommandUseCase {

    private final PartnershipRepository partnershipRepository;

    public PartnershipManagementCommandService(PartnershipRepository partnershipRepository) {
        this.partnershipRepository = partnershipRepository;
    }

    @Override
    public void changeStatus(PartnershipStatusChangeCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipStatus partnershipStatus = PartnershipStatus.from(command.status());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.changeStatus(partnershipStatus);
        partnershipRepository.save(partnershipRequest);
    }

    @Override
    public void deletePartnershipRequest(PartnershipDeleteCommand command) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(command.partnershipRequestId());
        PartnershipRequest partnershipRequest = findPartnershipRequestOrThrow(partnershipRequestId);

        partnershipRequest.delete();
        partnershipRepository.save(partnershipRequest);
    }

    private PartnershipRequest findPartnershipRequestOrThrow(PartnershipRequestId partnershipRequestId) {
        return partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
