package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NoticeManagementListQueryUseCase {

    PageResult<NoticeManagementListItemResult> getNotices(String title, String content, Boolean visible, int page, int size);
}
