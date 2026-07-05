package com.tastyhouse.webapi.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.webapi.notice.response.NoticeListItemResponse;
import com.tastyhouse.webapi.notice.response.NoticeListPageResult;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeQueryService noticeQueryService;

    public NoticeListPageResult getNoticeList(int page, int size) {
        var pageResult = noticeQueryService.findVisibleNotices(page, size)
            .map(NoticeListItemResponse::from);
        return NoticeListPageResult.of(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
