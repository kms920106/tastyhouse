package com.tastyhouse.application.event.store;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.event.port.out.write.EventState;

final class EventStateMapper {
    private EventStateMapper() {
    }

    static Event toDomain(EventState state) {
        return Event.reconstitute(
            state.id(),
            state.name(),
            state.description(),
            state.subtitle(),
            state.thumbnailImageFileId() == null ? null : UploadedFileId.of(state.thumbnailImageFileId()),
            state.bannerImageFileId() == null ? null : UploadedFileId.of(state.bannerImageFileId()),
            state.contentHtml(),
            state.status() == null ? null : EventStatus.valueOf(state.status()),
            state.startAt(),
            state.endAt(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static EventState toState(Event event) {
        return new EventState(
            event.getId(),
            event.getName(),
            event.getDescription(),
            event.getSubtitle(),
            event.getThumbnailImageFileId() == null ? null : event.getThumbnailImageFileId().value(),
            event.getBannerImageFileId() == null ? null : event.getBannerImageFileId().value(),
            event.getContentHtml(),
            event.getStatus() == null ? null : event.getStatus().name(),
            event.getStartAt(),
            event.getEndAt(),
            event.isDeleted(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
