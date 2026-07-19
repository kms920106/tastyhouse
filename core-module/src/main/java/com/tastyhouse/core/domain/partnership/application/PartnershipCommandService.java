package com.tastyhouse.core.domain.partnership.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class PartnershipCommandService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipRequestResult create(String businessName, String address, String addressDetail,
                                           String contactName, String contactPhone,
                                           LocalDateTime consultationRequestedAt) {
        PartnershipRequest saved = partnershipRepository.save(
            PartnershipRequest.of(businessName, address, addressDetail, contactName, contactPhone, consultationRequestedAt)
        );
        return PartnershipRequestResult.from(saved);
    }

    public void changeStatus(PartnershipRequestId partnershipRequestId, PartnershipStatus status) {
        PartnershipRequest partnershipRequest = partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
        partnershipRequest.changeStatus(status);
        partnershipRepository.save(partnershipRequest);
    }

    public void delete(PartnershipRequestId partnershipRequestId) {
        PartnershipRequest partnershipRequest = partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
        partnershipRequest.delete();
        partnershipRepository.save(partnershipRequest);
    }
}
