package com.tastyhouse.core.domain.event.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.vo.EventId;

public record EventManagementDetailResult(
    EventId eventId,
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    Long bannerImageFileId,
    String contentHtml,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static EventManagementDetailResult from(Event event) {
        return new EventManagementDetailResult(
            event.getEventId(),
            event.getName(),
            event.getDescription(),
            event.getSubtitle(),
            event.getThumbnailImageFileId(),
            event.getBannerImageFileId(),
            event.getContentHtml(),
            event.getStatus(),
            event.getStartAt(),
            event.getEndAt(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
