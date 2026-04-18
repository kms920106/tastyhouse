package com.tastyhouse.webapi.event;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.event.Event;
import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventPrize;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.EventCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.event.response.EventDurationResponse;
import com.tastyhouse.webapi.event.response.PrizeItem;
import com.tastyhouse.webapi.event.response.EventListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventCoreService eventCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Optional<EventDurationResponse> getRankingEventDuration() {
        return eventCoreService.findActiveRankingEvent()
                .map(event -> EventDurationResponse.from(
                    event.getStartAt(),
                    event.getEndAt()
                ));
    }

    @Transactional(readOnly = true)
    public List<PrizeItem> getActivePrizes() {
        return eventCoreService.findActiveRankingEvent()
            .map(event -> {
                List<EventPrize> eventPrizes = eventCoreService.findEventPrizes(event.getId());
                return eventPrizes.stream()
                    .map(this::convertToPrizeItem)
                    .toList();
            })
            .orElse(Collections.emptyList());
    }

    private PrizeItem convertToPrizeItem(EventPrize eventPrize) {
        return PrizeItem.from(
            eventPrize.getId(),
            eventPrize.getPrizeRank(),
            eventPrize.getName(),
            eventPrize.getBrand(),
            fileService.getFileUrl(eventPrize.getImageFileId())
        );
    }

    @Transactional(readOnly = true)
    public PageResult<EventListItemResponse> getEventList(EventStatus status, int page, int size) {
        return eventCoreService.searchEventsByStatus(status, page, size)
            .map(this::convertToEventListItemResponse);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        Event event = eventCoreService.findEventById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        return convertToEventDetailResponse(event);
    }

    @Transactional(readOnly = true)
    public PageResult<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        return eventCoreService.findAllEventAnnouncements(page, size)
            .map(this::convertToEventAnnouncementListItemResponse);
    }

    private EventListItemResponse convertToEventListItemResponse(Event event) {
        return EventListItemResponse.from(
            event.getId(),
            event.getName(),
            fileService.getFileUrl(event.getThumbnailImageFileId()),
            event.getStartAt(),
            event.getEndAt()
        );
    }

    private EventDetailResponse convertToEventDetailResponse(Event event) {
        return EventDetailResponse.from(fileService.getFileUrl(event.getBannerImageFileId()));
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
