package com.tastyhouse.application.notice.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.notice.port.in.NoticeQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeQueryPort noticeQueryPort;

    public NoticeQueryService(NoticeQueryPort noticeQueryPort) {
        this.noticeQueryPort = noticeQueryPort;
    }

    @Override
    public PageResult<NoticeListItemResult> getNoticeList(int page, int size) {
        return noticeQueryPort.findVisibleNotices(PageQuery.of(page, size));
    }
}
