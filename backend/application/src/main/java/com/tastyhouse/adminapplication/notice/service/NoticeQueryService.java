package com.tastyhouse.adminapplication.notice.service;

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
import com.tastyhouse.adminapplication.notice.port.in.NoticeQueryUseCase;

/**
 * 공지사항 관리 조회 서비스.
 *
 * <p>읽기 포트({@link NoticeManagementQueryPort})만 주입해 조회한다. write 포트를 주입하지 않으며,
 * 쓰기는 {@link NoticeCommandService}가 담당한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeManagementQueryPort noticeManagementQueryPort;

    public NoticeQueryService(NoticeManagementQueryPort noticeManagementQueryPort) {
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
