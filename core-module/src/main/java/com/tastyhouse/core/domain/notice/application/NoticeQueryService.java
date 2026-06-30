package com.tastyhouse.core.domain.notice.application;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeListItemDto> findVisibleNotices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return noticeRepository.findVisibleNotices(pageable);
    }

    public Page<NoticeListItemDto> findAllNotices(NoticeSearchCondition condition, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return noticeRepository.findAllNotices(condition, pageable);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
