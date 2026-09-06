package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class EventMapper {
    private EventMapper() {
    }

    static Event toDomain(EventJpaEntity entity) {
        return Event.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSubtitle(),
            IdMapping.vo(entity.getThumbnailImageFileId(), UploadedFileId::of),
            IdMapping.vo(entity.getBannerImageFileId(), UploadedFileId::of),
            entity.getContentHtml(),
            entity.getStatus(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static EventJpaEntity toEntity(Event domain) {
        return EventJpaEntity.create(
            domain.getName(),
            domain.getDescription(),
            domain.getSubtitle(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getBannerImageFileId(), UploadedFileId::value),
            domain.getContentHtml(),
            domain.getStatus(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isDeleted()
        );
    }

    static void applyChanges(EventJpaEntity entity, Event domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.getSubtitle(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getBannerImageFileId(), UploadedFileId::value),
            domain.getContentHtml(),
            domain.getStatus(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isDeleted()
        );
    }
}
