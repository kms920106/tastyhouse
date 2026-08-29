package com.tastyhouse.webapi.notice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.webapi.notice.application.port.in.NoticeQueryUseCase;
import com.tastyhouse.webapi.notice.response.NoticeListItemResponse;

/**
 * 공지사항 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. infra read
 * 읽기 포트({@link NoticeQueryPort})를 주입해 노출(visible=true) 공지만 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeQueryPort noticeQueryPort;

    public NoticeQueryService(NoticeQueryPort noticeQueryPort) {
        this.noticeQueryPort = noticeQueryPort;
    }

    @Override
    public PaginationResponse<NoticeListItemResponse> getNoticeList(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<NoticeListItemResponse> pageResult = noticeQueryPort.findVisibleNotices(pageQuery)
            .map(this::toNoticeListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private NoticeListItemResponse toNoticeListItemResponse(NoticeListItemResult dto) {
        return NoticeListItemResponse.from(dto.id(), dto.title(), dto.content(), dto.createdAt());
    }
}
