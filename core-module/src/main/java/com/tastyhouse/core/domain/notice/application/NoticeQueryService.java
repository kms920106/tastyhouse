package com.tastyhouse.core.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    public PageResult<NoticeListItemDto> findVisibleNotices(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeRepository.findVisibleNotices(pageQuery);
    }

    public PageResult<NoticeListItemDto> findAllNotices(NoticeSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeRepository.findAllNotices(condition, pageQuery);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
