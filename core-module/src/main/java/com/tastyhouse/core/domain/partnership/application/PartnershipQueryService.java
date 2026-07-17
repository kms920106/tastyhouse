package com.tastyhouse.core.domain.partnership.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.repository.PartnershipRepository;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.core.domain.partnership.application.dto.PartnershipSearchCondition;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestListItemResult;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnershipQueryService {

    private final PartnershipRepository partnershipRepository;

    public PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return partnershipRepository.findPartnershipRequests(condition, pageQuery);
    }

    public PartnershipRequestResult findPartnershipRequestById(PartnershipRequestId partnershipRequestId) {
        PartnershipRequest partnershipRequest = partnershipRepository.findById(partnershipRequestId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARTNERSHIP_REQUEST_NOT_FOUND));
        return PartnershipRequestResult.from(partnershipRequest);
    }
}
