package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface PartnershipQueryUseCase {

    PageResult<PartnershipRequestListItemResult> getPartnershipRequests(
        String businessName,
        String contactName,
        String contactPhone,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    );

    PartnershipRequestDetailResult getPartnershipRequest(Long id);
}
