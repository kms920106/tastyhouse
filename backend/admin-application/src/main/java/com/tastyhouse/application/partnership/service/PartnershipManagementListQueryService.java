package com.tastyhouse.application.partnership.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.application.partnership.port.in.PartnershipManagementListQueryUseCase;
import com.tastyhouse.application.partnership.port.out.PartnershipQueryPort;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.partnership.port.out.PartnershipSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class PartnershipManagementListQueryService implements PartnershipManagementListQueryUseCase {

    private final PartnershipQueryPort partnershipQueryPort;

    public PartnershipManagementListQueryService(PartnershipQueryPort partnershipQueryPort) {
        this.partnershipQueryPort = partnershipQueryPort;
    }

    @Override
    public PageResult<PartnershipRequestListItemResult> getPartnershipRequests(
        String businessName,
        String contactName,
        String contactPhone,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    ) {
        String partnershipStatus = status == null ? null : PartnershipStatus.from(status).name();
        PartnershipSearchCondition condition = PartnershipSearchCondition.of(businessName, contactName, contactPhone, partnershipStatus, startDate, endDate);
        PageQuery pageQuery = PageQuery.of(page, size);
        return partnershipQueryPort.findPartnershipRequests(condition, pageQuery);
    }
}
