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
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapplication.notice.response.NoticeListItemResponse;
import com.tastyhouse.adminapplication.notice.port.in.NoticeQueryUseCase;

/**
 * 공지사항 관리 조회 서비스.
 *
 * <p>읽기 포트({@link NoticeManagementQueryPort})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link NoticeCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeManagementQueryPort noticeManagementQueryPort;

    public NoticeQueryService(NoticeManagementQueryPort noticeManagementQueryPort) {
        this.noticeManagementQueryPort = noticeManagementQueryPort;
    }

    @Override
    public PaginationResponse<NoticeListItemResponse> getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<NoticeListItemResponse> pageResult = noticeManagementQueryPort.findAllNotices(condition, pageQuery)
            .map(this::toNoticeListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public NoticeDetailResponse getNotice(Long id) {
        NoticeDetailResult noticeDetail = noticeManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
        return toNoticeDetailResponse(noticeDetail);
    }

    private NoticeListItemResponse toNoticeListItemResponse(NoticeManagementListItemResult dto) {
        return NoticeListItemResponse.from(dto.id(), dto.title(), dto.content(), dto.visible(), dto.createdAt());
    }

    private NoticeDetailResponse toNoticeDetailResponse(NoticeDetailResult dto) {
        return NoticeDetailResponse.from(
            dto.id(),
            dto.title(),
            dto.content(),
            dto.visible(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
