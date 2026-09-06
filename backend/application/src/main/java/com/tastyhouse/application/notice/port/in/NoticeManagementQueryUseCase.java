package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface NoticeManagementQueryUseCase {

    PageResult<NoticeManagementListItemResult> getNotices(String title, String content, Boolean visible, int page, int size);

    NoticeDetailResult getNotice(Long id);
}
