package com.tastyhouse.core.domain.partnership.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;
import com.tastyhouse.core.domain.partnership.application.dto.PartnershipSearchCondition;
import com.tastyhouse.core.domain.partnership.application.dto.result.PartnershipRequestListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface PartnershipRepository {

    Optional<PartnershipRequest> findById(PartnershipRequestId partnershipRequestId);

    PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, PageQuery pageQuery);

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}
