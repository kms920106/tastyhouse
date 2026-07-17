package com.tastyhouse.adminapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.domain.notice.application.NoticeCommandService;
import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeCreateCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeUpdateCommand;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeDetailResult;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.response.NoticeListItemResponse;
import com.tastyhouse.adminapi.notice.response.NoticePageResponse;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    public NoticePageResponse getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageResult<NoticeListItemResponse> pageResult = noticeQueryService.findAllNotices(condition, page, size)
            .map(this::toNoticeListItemResponse);
        return NoticePageResponse.from(pageResult);
    }

    public Long createNotice(String title, String content, boolean visible) {
        NoticeCreateCommand command = NoticeCreateCommand.of(title, content, visible);
        NoticeId noticeId = noticeCommandService.createNotice(command);
        return noticeId.value();
    }

    public NoticeDetailResponse getNotice(Long id) {
        NoticeDetailResult noticeDetail = noticeQueryService.findDetailById(NoticeId.of(id));
        return NoticeDetailResponse.from(
            noticeDetail.noticeId().value(),
            noticeDetail.title(),
            noticeDetail.content(),
            noticeDetail.visible(),
            noticeDetail.createdAt(),
            noticeDetail.updatedAt()
        );
    }

    private NoticeListItemResponse toNoticeListItemResponse(NoticeListItemResult dto) {
        return NoticeListItemResponse.from(dto.id(), dto.title(), dto.content(), dto.visible(), dto.createdAt());
    }

    public void updateNotice(Long id, String title, String content, boolean visible) {
        NoticeId noticeId = NoticeId.of(id);
        NoticeUpdateCommand command = NoticeUpdateCommand.of(title, content, visible);
        noticeCommandService.updateNotice(noticeId, command);
    }

    public void deleteNotice(Long id) {
        NoticeId noticeId = NoticeId.of(id);
        noticeCommandService.deleteNotice(noticeId);
    }
}
