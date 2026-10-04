package com.tastyhouse.application.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventSearchCondition;
import com.tastyhouse.application.event.port.out.EventWinnerResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class EventManagementQueryService implements EventManagementQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size) {
        String eventStatus = status == null ? null : EventStatus.from(status).name();
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageQuery pageQuery = PageQuery.of(page, size);
        return eventManagementQueryPort.findAllEvents(condition, pageQuery);
    }

    @Override
    public EventManagementDetailResult getEvent(Long id) {
        EventManagementDetailResult detail = eventManagementQueryPort.findEventDetailById(EventId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_NOT_FOUND));
        requireResolvedFileUrl(detail.thumbnailImageFileId(), detail.thumbnailUrl());
        requireResolvedFileUrl(detail.bannerImageFileId(), detail.bannerUrl());
        return detail;
    }

    @Override
    public EventAnnouncementResult getAnnouncement(Long id) {
        return eventManagementQueryPort.findAnnouncementByEventId(EventId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
    }

    @Override
    public List<EventWinnerResult> getWinners(Long id) {
        return eventManagementQueryPort.findWinnersByEventId(EventId.of(id).value());
    }

    private void requireResolvedFileUrl(Long fileId, String fileUrl) {
        if (fileId != null && fileUrl == null) {
            throw new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
