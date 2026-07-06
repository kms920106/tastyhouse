package com.tastyhouse.adminapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.response.NoticeListItemResponse;
import com.tastyhouse.adminapi.notice.response.NoticePageResponse;
import com.tastyhouse.core.domain.notice.application.NoticeCommandService;
import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.NoticeDetailDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.command.CreateNoticeCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeUpdateCommand;
import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    public NoticePageResponse getNotices(String title, String content, Boolean visible, int page, int size) {
        NoticeSearchCondition condition = NoticeSearchCondition.of(title, content, visible);
        PageResult<NoticeListItemResponse> pageResult = noticeQueryService.findAllNotices(condition, page, size)
            .map(NoticeListItemResponse::from);
        return NoticePageResponse.from(pageResult);
    }

    public Long createNotice(String title, String content, boolean visible) {
        NoticeId noticeId = noticeCommandService.createNotice(CreateNoticeCommand.of(title, content, visible));
        return noticeId.value();
    }

    public NoticeDetailResponse getNotice(Long id) {
        NoticeDetailDto noticeDetail = noticeQueryService.findDetailById(NoticeId.of(id));
        return NoticeDetailResponse.from(noticeDetail);
    }

    public void updateNotice(Long id, String title, String content, boolean visible) {
        noticeCommandService.updateNotice(NoticeId.of(id), NoticeUpdateCommand.of(title, content, visible));
    }

    public void deleteNotice(Long id) {
        noticeCommandService.deleteNotice(NoticeId.of(id));
    }
}
