package com.tastyhouse.application.partnership.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

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
