package com.tastyhouse.webapi.event;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.entity.event.dto.EventDetailDto;
import com.tastyhouse.core.entity.event.dto.EventListItemDto;
import com.tastyhouse.core.entity.event.dto.PrizeItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.EventCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventDurationResponse;
import com.tastyhouse.webapi.event.response.EventListItemResponse;
import com.tastyhouse.webapi.event.response.PrizeItem;
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
                List<PrizeItemDto> prizes = eventCoreService.findPrizeItemsByEventId(event.getId());
                return prizes.stream()
                    .map(this::convertToPrizeItem)
                    .toList();
            })
            .orElse(Collections.emptyList());
    }

    private PrizeItem convertToPrizeItem(PrizeItemDto dto) {
        return PrizeItem.from(
            dto.id(),
            dto.prizeRank(),
            dto.name(),
            dto.brand(),
            fileService.getUrlByPath(dto.imageFilePath())
        );
    }

    @Transactional(readOnly = true)
    public PageResult<EventListItemResponse> getEventList(EventStatus status, int page, int size) {
        return PageResult.from(eventCoreService.findEventListItemsByStatus(status, page, size))
            .map(this::convertToEventListItemResponse);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId) {
        EventDetailDto dto = eventCoreService.findEventDetailById(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        return EventDetailResponse.from(fileService.getUrlByPath(dto.bannerFilePath()));
    }

    @Transactional(readOnly = true)
    public PageResult<EventAnnouncementListItemResponse> getEventAnnouncementList(int page, int size) {
        return PageResult.from(eventCoreService.findAllEventAnnouncements(page, size))
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
