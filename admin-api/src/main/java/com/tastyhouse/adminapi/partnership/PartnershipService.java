package com.tastyhouse.adminapi.partnership;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.core.domain.partnership.application.PartnershipCommandService;
import com.tastyhouse.core.domain.partnership.application.PartnershipQueryService;
import com.tastyhouse.core.domain.partnership.application.dto.PartnershipSearchCondition;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestListItemResult;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestDetailResponse;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestListItemResponse;
import com.tastyhouse.adminapi.partnership.response.PartnershipRequestPageResponse;

@Service
@RequiredArgsConstructor
public class PartnershipService {

    private final PartnershipCommandService partnershipCommandService;
    private final PartnershipQueryService partnershipQueryService;

    public PartnershipRequestPageResponse getPartnershipRequests(String businessName, String contactName, String contactPhone,
                                                                 String status, LocalDateTime startDate, LocalDateTime endDate,
                                                                 int page, int size) {
        PartnershipStatus partnershipStatus = status == null ? null : PartnershipStatus.from(status);
        PartnershipSearchCondition condition = PartnershipSearchCondition.of(businessName, contactName, contactPhone, partnershipStatus, startDate, endDate);
        PageResult<PartnershipRequestListItemResponse> pageResult = partnershipQueryService.findPartnershipRequests(condition, page, size)
            .map(this::toPartnershipRequestListItemResponse);
        return PartnershipRequestPageResponse.from(pageResult);
    }

    public PartnershipRequestDetailResponse getPartnershipRequest(Long id) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(id);
        PartnershipRequestResult result = partnershipQueryService.findPartnershipRequestById(partnershipRequestId);
        return PartnershipRequestDetailResponse.from(
            result.id().value(),
            result.businessName(),
            result.address(),
            result.addressDetail(),
            result.contactName(),
            result.contactPhone(),
            result.status() != null ? result.status().name() : null,
            result.consultationRequestedAt(),
            result.createdAt(),
            result.updatedAt()
        );
    }

    public void changeStatus(Long id, String status) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(id);
        PartnershipStatus partnershipStatus = PartnershipStatus.from(status);
        partnershipCommandService.changeStatus(partnershipRequestId, partnershipStatus);
    }

    public void deletePartnershipRequest(Long id) {
        PartnershipRequestId partnershipRequestId = PartnershipRequestId.of(id);
        partnershipCommandService.delete(partnershipRequestId);
    }

    private PartnershipRequestListItemResponse toPartnershipRequestListItemResponse(PartnershipRequestListItemResult result) {
        return PartnershipRequestListItemResponse.from(
            result.id(),
            result.businessName(),
            result.contactName(),
            result.contactPhone(),
            result.status() != null ? result.status().name() : null,
            result.consultationRequestedAt(),
            result.createdAt()
        );
    }
}
