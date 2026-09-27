package com.tastyhouse.infrastructure.notice.persistence;

import com.tastyhouse.application.notice.port.out.write.NoticeState;

final class NoticeMapper {
    private NoticeMapper() {
    }

    static NoticeState toState(NoticeJpaEntity entity) {
        return new NoticeState(
            entity.getId(),
            entity.getTitle(),
            entity.getContent(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static NoticeJpaEntity toEntity(NoticeState state) {
        return NoticeJpaEntity.create(
            state.title(),
            state.content(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(NoticeJpaEntity entity, NoticeState state) {
        entity.applyChanges(
            state.title(),
            state.content(),
            state.visible(),
            state.deleted()
        );
    }
}
