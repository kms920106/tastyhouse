package com.tastyhouse.application.notice.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NoticeManagementQueryPort {

    PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery);

    Optional<NoticeDetailResult> findDetailById(Long id);
}
