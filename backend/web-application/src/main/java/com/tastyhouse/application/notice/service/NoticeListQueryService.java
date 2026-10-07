package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notice.port.in.NoticeListQueryUseCase;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class NoticeListQueryService implements NoticeListQueryUseCase {

    private final NoticeQueryPort noticeQueryPort;

    public NoticeListQueryService(NoticeQueryPort noticeQueryPort) {
        this.noticeQueryPort = noticeQueryPort;
    }

    @Override
    public PageResult<NoticeListItemResult> getNoticeList(int page, int size) {
        return noticeQueryPort.findVisibleNotices(PageQuery.of(page, size));
    }
}
