package com.tastyhouse.core.domain.notice.domain.repository;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeRepository {

    Page<NoticeListItemDto> findVisibleNotices(Pageable pageable);

    Page<NoticeListItemDto> findAllNotices(NoticeSearchCondition condition, Pageable pageable);

    Optional<Notice> findById(Long id);

    Notice save(Notice notice);
}
