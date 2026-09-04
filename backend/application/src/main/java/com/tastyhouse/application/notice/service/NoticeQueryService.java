package com.tastyhouse.application.notice.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.notice.port.in.NoticeQueryUseCase;

/**
 * 공지사항 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. infra read
 * 읽기 포트({@link NoticeQueryPort})를 주입해 노출(visible=true) 공지만 조회한다.
 */
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
