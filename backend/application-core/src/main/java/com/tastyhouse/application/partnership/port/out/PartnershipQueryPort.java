package com.tastyhouse.application.partnership.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PartnershipQueryPort {

    PageResult<PartnershipRequestListItemResult> findPartnershipRequests(PartnershipSearchCondition condition, PageQuery pageQuery);

    Optional<PartnershipRequestDetailResult> findDetailById(Long id);
}
