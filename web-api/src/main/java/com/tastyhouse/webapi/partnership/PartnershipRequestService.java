package com.tastyhouse.webapi.partnership;

import com.tastyhouse.core.entity.partnership.PartnershipRequest;
import com.tastyhouse.core.service.PartnershipCoreService;
import com.tastyhouse.webapi.partnership.request.PartnershipRequestCreateRequest;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnershipRequestService {

    private final PartnershipCoreService partnershipCoreService;

    @Transactional
    public PartnershipRequestResponse createPartnershipRequest(PartnershipRequestCreateRequest request) {
        PartnershipRequest partnershipRequest = PartnershipRequest.of(
            request.businessName(),
            request.address(),
            request.addressDetail(),
            request.contactName(),
            request.contactPhone(),
            request.consultationRequestedAt()
        );

        PartnershipRequest savedRequest = partnershipCoreService.save(partnershipRequest);

        return PartnershipRequestResponse.from(
            savedRequest.getId(),
            savedRequest.getBusinessName(),
            savedRequest.getAddress(),
            savedRequest.getAddressDetail(),
            savedRequest.getContactName(),
            savedRequest.getContactPhone(),
            savedRequest.getConsultationRequestedAt(),
            savedRequest.getCreatedAt()
        );
    }
}
