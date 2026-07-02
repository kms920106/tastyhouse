package com.tastyhouse.webapi.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.event.application.EventQueryService;
import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventListItemResponse;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventQueryService eventQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<EventListItemResponse> getEventList(EventStatus status, int page, int size) {
        return eventQueryService.findEventListItemsByStatus(status, page, size)
            .map(this::convertToEventListItemResponse);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        EventDetailDto dto = eventQueryService.findEventDetailById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        return EventDetailResponse.from(fileService.getUrlByPath(dto.bannerFilePath()));
    }

    @Transactional(readOnly = true)
    public PageResult<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        return eventQueryService.findAllEventAnnouncements(page, size)
            .map(this::convertToEventAnnouncementListItemResponse);
    }

    private EventListItemResponse convertToEventListItemResponse(EventListItemDto dto) {
        return EventListItemResponse.from(
            dto.eventId(),
            dto.name(),
            fileService.getUrlByPath(dto.thumbnailFilePath()),
            dto.startAt(),
            dto.endAt()
        );
    }

    private EventAnnouncementListItemResponse convertToEventAnnouncementListItemResponse(EventAnnouncement announcement) {
        return EventAnnouncementListItemResponse.from(
            announcement.getId(),
            announcement.getName(),
            announcement.getContent(),
            announcement.getAnnouncedAt()
        );
    }
}
