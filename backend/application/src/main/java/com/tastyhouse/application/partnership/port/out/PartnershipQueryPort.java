package com.tastyhouse.application.partnership.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface PartnershipQueryPort {

    PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, PageQuery pageQuery);

    Optional<PartnershipRequestDetailResult> findDetailById(Long id);
}
