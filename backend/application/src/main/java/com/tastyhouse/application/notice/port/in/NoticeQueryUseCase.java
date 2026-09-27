package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@WebApp
public interface NoticeQueryUseCase {

    PageResult<NoticeListItemResult> getNoticeList(int page, int size);
}
