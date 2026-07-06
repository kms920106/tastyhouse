package com.tastyhouse.webapi.partnership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.partnership.application.PartnershipCommandService;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.webapi.partnership.request.PartnershipRequestCreateRequest;
import com.tastyhouse.webapi.partnership.response.PartnershipRequestResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnershipRequestService {

    private final PartnershipCommandService partnershipCommandService;

    @Transactional
    public PartnershipRequestResponse createPartnershipRequest(PartnershipRequestCreateRequest request) {
        PartnershipRequestResult result = partnershipCommandService.create(
            request.businessName(),
            request.address(),
            request.addressDetail(),
            request.contactName(),
            request.contactPhone(),
            request.consultationRequestedAt()
        );

        return PartnershipRequestResponse.from(
            result.id().value(),
            result.businessName(),
            result.address(),
            result.addressDetail(),
            result.contactName(),
            result.contactPhone(),
            result.consultationRequestedAt(),
            result.createdAt()
        );
    }
}
