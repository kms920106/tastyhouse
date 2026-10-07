package com.tastyhouse.application.partnership.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PartnershipManagementListQueryUseCase {

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
}
