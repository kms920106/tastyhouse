package com.tastyhouse.application.notice.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface NoticeQueryPort {

    PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery);
}
