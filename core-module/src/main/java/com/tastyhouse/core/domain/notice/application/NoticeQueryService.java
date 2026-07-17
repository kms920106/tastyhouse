package com.tastyhouse.core.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeDetailResult;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    public PageResult<NoticeListItemResult> findVisibleNotices(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeRepository.findVisibleNotices(pageQuery);
    }

    public PageResult<NoticeListItemResult> findAllNotices(NoticeSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeRepository.findAllNotices(condition, pageQuery);
    }

    public NoticeDetailResult findDetailById(NoticeId noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeDetailResult.from(
            notice.getNoticeId(),
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.getCreatedAt(),
            notice.getUpdatedAt()
        );
    }
}
