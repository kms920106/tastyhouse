package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notice.port.in.NoticeManagementListQueryUseCase;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class NoticeManagementListQueryService implements NoticeManagementListQueryUseCase {

    private final NoticeManagementQueryPort noticeManagementQueryPort;

    public NoticeManagementListQueryService(NoticeManagementQueryPort noticeManagementQueryPort) {
        this.noticeManagementQueryPort = noticeManagementQueryPort;
    }

    @Override
    public PageResult<NoticeManagementListItemResult> getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return noticeManagementQueryPort.findAllNotices(condition, pageQuery);
    }
}
