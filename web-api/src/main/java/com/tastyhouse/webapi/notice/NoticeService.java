package com.tastyhouse.webapi.notice;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.webapi.notice.response.NoticeListItemResponse;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeQueryService noticeQueryService;

    public NoticeListPageResult getNoticeList(int page, int size) {
        var pageResult = noticeQueryService.findVisibleNotices(page, size)
            .map(NoticeListItemResponse::from);
        return NoticeListPageResult.of(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }

    public record NoticeListPageResult(List<NoticeListItemResponse> content, int page, int size, long totalElements) {
        public static NoticeListPageResult of(List<NoticeListItemResponse> content, int page, int size, long totalElements) {
            return new NoticeListPageResult(content, page, size, totalElements);
        }
    }
}
