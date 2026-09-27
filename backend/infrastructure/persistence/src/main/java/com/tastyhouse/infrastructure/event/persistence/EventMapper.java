package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.application.event.port.out.write.EventState;

final class EventMapper {
    private EventMapper() {
    }

    static EventState toState(EventJpaEntity entity) {
        return new EventState(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSubtitle(),
            entity.getThumbnailImageFileId(),
            entity.getBannerImageFileId(),
            entity.getContentHtml(),
            entity.getStatus(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static EventJpaEntity toEntity(EventState state) {
        return EventJpaEntity.create(
            state.name(),
            state.description(),
            state.subtitle(),
            state.thumbnailImageFileId(),
            state.bannerImageFileId(),
            state.contentHtml(),
            state.status(),
            state.startAt(),
            state.endAt(),
            state.deleted()
        );
    }

    static void applyChanges(EventJpaEntity entity, EventState state) {
        entity.applyChanges(
            state.name(),
            state.description(),
            state.subtitle(),
            state.thumbnailImageFileId(),
            state.bannerImageFileId(),
            state.contentHtml(),
            state.status(),
            state.startAt(),
            state.endAt(),
            state.deleted()
        );
    }
}
