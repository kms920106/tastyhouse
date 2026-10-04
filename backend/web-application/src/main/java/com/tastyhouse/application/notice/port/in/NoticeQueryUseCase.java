package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NoticeQueryUseCase {

    PageResult<NoticeListItemResult> getNoticeList(int page, int size);
}
