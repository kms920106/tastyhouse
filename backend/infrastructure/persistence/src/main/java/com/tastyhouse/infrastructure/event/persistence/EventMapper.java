package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.file.vo.UploadedFileId;

final class EventMapper {
    private EventMapper() {
    }

    static Event toDomain(EventJpaEntity entity) {
        return Event.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSubtitle(),
            entity.getThumbnailImageFileId() == null ? null : UploadedFileId.of(entity.getThumbnailImageFileId()),
            entity.getBannerImageFileId() == null ? null : UploadedFileId.of(entity.getBannerImageFileId()),
            entity.getContentHtml(),
            entity.getStatus() == null ? null : EventStatus.valueOf(entity.getStatus()),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static EventJpaEntity toEntity(Event event) {
        return EventJpaEntity.create(
            event.getName(),
            event.getDescription(),
            event.getSubtitle(),
            event.getThumbnailImageFileId() == null ? null : event.getThumbnailImageFileId().value(),
            event.getBannerImageFileId() == null ? null : event.getBannerImageFileId().value(),
            event.getContentHtml(),
            event.getStatus() == null ? null : event.getStatus().name(),
            event.getStartAt(),
            event.getEndAt(),
            event.isDeleted()
        );
    }

    static void applyChanges(EventJpaEntity entity, Event event) {
        entity.applyChanges(
            event.getName(),
            event.getDescription(),
            event.getSubtitle(),
            event.getThumbnailImageFileId() == null ? null : event.getThumbnailImageFileId().value(),
            event.getBannerImageFileId() == null ? null : event.getBannerImageFileId().value(),
            event.getContentHtml(),
            event.getStatus() == null ? null : event.getStatus().name(),
            event.getStartAt(),
            event.getEndAt(),
            event.isDeleted()
        );
    }
}
