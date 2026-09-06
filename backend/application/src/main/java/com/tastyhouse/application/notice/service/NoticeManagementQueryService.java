package com.tastyhouse.application.notice.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeSearchCondition;
import com.tastyhouse.application.notice.port.in.NoticeManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class NoticeManagementQueryService implements NoticeManagementQueryUseCase {

    private final NoticeManagementQueryPort noticeManagementQueryPort;

    public NoticeManagementQueryService(NoticeManagementQueryPort noticeManagementQueryPort) {
        this.noticeManagementQueryPort = noticeManagementQueryPort;
    }

    @Override
    public PageResult<NoticeManagementListItemResult> getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeManagementQueryPort.findAllNotices(condition, pageQuery);
    }

    @Override
    public NoticeDetailResult getNotice(Long id) {
        return noticeManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
