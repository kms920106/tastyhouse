package com.tastyhouse.application.ceo.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface CeoLoginHistoryQueryPort {

    PageResult<CeoLoginHistoryResult> findLoginHistoryPage(CeoLoginHistorySearchCondition condition, PageQuery pageQuery);
}
