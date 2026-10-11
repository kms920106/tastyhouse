package com.tastyhouse.application.ceo.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface CeoLoginHistoryQueryPort {

    PageResult<CeoLoginHistoryResult> findLoginHistoryPage(CeoLoginHistorySearchCondition condition, PageQuery pageQuery);
}
