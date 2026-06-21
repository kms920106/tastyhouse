package com.tastyhouse.core.domain.notice.domain.repository;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeRepository {

    Page<NoticeListItemDto> findAllWithFilter(Pageable pageable);

    Page<NoticeListItemDto> findAllForAdmin(Pageable pageable);

    Optional<Notice> findById(Long id);

    Notice save(Notice notice);

    void deleteById(Long id);
}
