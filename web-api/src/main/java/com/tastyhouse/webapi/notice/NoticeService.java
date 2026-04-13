package com.tastyhouse.webapi.notice;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.notice.dto.NoticeListItemDto;
import com.tastyhouse.core.service.NoticeCoreService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.notice.response.NoticeListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeCoreService noticeCoreService;

    @Transactional(readOnly = true)
    public PageResult<NoticeListItem> searchNoticeList(PageRequest pageRequest) {
        return noticeCoreService.findAllWithPagination(
            pageRequest.page(), pageRequest.size()
        ).map(this::convertToNoticeListItem);
    }

    private NoticeListItem convertToNoticeListItem(NoticeListItemDto dto) {
        return NoticeListItem.from(
            dto.id(),
            dto.title(),
            dto.content(),
            dto.createdAt()
        );
    }
}
