package com.tastyhouse.webapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.notice.response.NoticeListItemResponse;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeQueryService noticeQueryService;

    public PaginationResponse<NoticeListItemResponse> getNoticeList(int page, int size) {
        var pageResult = noticeQueryService.findVisibleNotices(page, size)
            .map(this::toNoticeListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private NoticeListItemResponse toNoticeListItemResponse(NoticeListItemResult dto) {
        return NoticeListItemResponse.from(dto.id(), dto.title(), dto.content(), dto.createdAt());
    }
}
