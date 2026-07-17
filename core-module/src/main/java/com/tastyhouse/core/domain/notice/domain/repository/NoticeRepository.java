package com.tastyhouse.core.domain.notice.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface NoticeRepository {

    PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery);

    PageResult<NoticeListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery);

    Optional<Notice> findById(NoticeId noticeId);

    Notice save(Notice notice);
}
