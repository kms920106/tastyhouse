package com.tastyhouse.core.domain.notice.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface NoticeRepository {

    PageResult<NoticeListItemDto> findVisibleNotices(PageQuery pageQuery);

    PageResult<NoticeListItemDto> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery);

    Optional<Notice> findById(Long id);

    Notice save(Notice notice);
}
