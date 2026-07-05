package com.tastyhouse.adminapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.adminapi.notice.request.NoticeCreateRequest;
import com.tastyhouse.adminapi.notice.request.NoticeUpdateRequest;
import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.response.NoticeListItemResponse;
import com.tastyhouse.adminapi.notice.response.NoticePageResponse;
import com.tastyhouse.core.domain.notice.application.NoticeCommandService;
import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.NoticeDetailDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.command.CreateNoticeCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeUpdateCommand;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    public NoticePageResponse getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = new NoticeSearchCondition(title, content, visible);
        PageResult<NoticeListItemResponse> pageResult = noticeQueryService.findAllNotices(condition, page, size)
            .map(NoticeListItemResponse::from);
        return new NoticePageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }

    public Long createNotice(NoticeCreateRequest request) {
        CreateNoticeCommand command = new CreateNoticeCommand(request.title(), request.content(), request.visible());
        return noticeCommandService.createNotice(command);
    }

    public NoticeDetailResponse getNotice(Long id) {
        NoticeDetailDto noticeDetail = noticeQueryService.findDetailById(id);
        return NoticeDetailResponse.from(noticeDetail);
    }

    public void updateNotice(Long id, NoticeUpdateRequest request) {
        NoticeUpdateCommand command = new NoticeUpdateCommand(request.title(), request.content(), request.visible());
        noticeCommandService.updateNotice(id, command);
    }

    public void deleteNotice(Long id) {
        noticeCommandService.deleteNotice(id);
    }
}
