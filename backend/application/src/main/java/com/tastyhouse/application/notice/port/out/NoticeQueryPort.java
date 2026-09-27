package com.tastyhouse.application.notice.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NoticeQueryPort {

    PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery);
}
