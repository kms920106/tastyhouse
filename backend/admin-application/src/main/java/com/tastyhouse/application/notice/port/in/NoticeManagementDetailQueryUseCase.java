package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;

public interface NoticeManagementDetailQueryUseCase {

    NoticeDetailResult getNotice(Long id);
}
