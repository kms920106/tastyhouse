package com.tastyhouse.application.partnership.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.partnership.port.out.PartnershipQueryPort;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestDetailResult;
import com.tastyhouse.application.partnership.port.out.PartnershipRequestListItemResult;
import com.tastyhouse.application.partnership.port.out.PartnershipSearchCondition;
import com.tastyhouse.application.partnership.port.in.PartnershipQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class PartnershipQueryService implements PartnershipQueryUseCase {

    private final PartnershipQueryPort partnershipQueryPort;

    public PartnershipQueryService(PartnershipQueryPort partnershipQueryPort) {
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
        PartnershipStatus partnershipStatus = status == null ? null : PartnershipStatus.from(status);
        PartnershipSearchCondition condition = PartnershipSearchCondition.of(businessName, contactName, contactPhone, partnershipStatus, startDate, endDate);
        PageQuery pageQuery = PageQuery.of(page, size);
        return partnershipQueryPort.findPartnershipRequests(condition, pageQuery);
    }

    @Override
    public PartnershipRequestDetailResult getPartnershipRequest(Long id) {
        return partnershipQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
    }
}
