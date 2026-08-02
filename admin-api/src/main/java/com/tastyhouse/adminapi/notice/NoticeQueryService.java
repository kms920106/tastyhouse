package com.tastyhouse.adminapi.notice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.notice.query.NoticeDetailResult;
import com.tastyhouse.infrastructure.notice.query.NoticeManagementListItemResult;
import com.tastyhouse.infrastructure.notice.query.NoticeQueryDao;
import com.tastyhouse.infrastructure.notice.query.NoticeSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.response.NoticeListItemResponse;

/**
 * 공지사항 관리 조회 서비스.
 *
 * <p>infra read 어댑터({@link NoticeQueryDao})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link NoticeCommandService}가 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class NoticeQueryService {

    private final NoticeQueryDao noticeQueryDao;

    public NoticeQueryService(NoticeQueryDao noticeQueryDao) {
        this.noticeQueryDao = noticeQueryDao;
    }

    public PaginationResponse<NoticeListItemResponse> getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<NoticeListItemResponse> pageResult = noticeQueryDao.findAllNotices(condition, pageQuery)
            .map(this::toNoticeListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public NoticeDetailResponse getNotice(Long id) {
        NoticeDetailResult noticeDetail = noticeQueryDao.findDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
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
