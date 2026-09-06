package com.tastyhouse.infrastructure.notice.persistence;

import com.tastyhouse.domain.notice.model.Notice;

final class NoticeMapper {
    private NoticeMapper() {
    }

    static Notice toDomain(NoticeJpaEntity entity) {
        return Notice.reconstitute(
            entity.getId(),
            entity.getTitle(),
            entity.getContent(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static NoticeJpaEntity toEntity(Notice domain) {
        return NoticeJpaEntity.create(
            domain.getTitle(),
            domain.getContent(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    static void applyChanges(NoticeJpaEntity entity, Notice domain) {
        entity.applyChanges(
            domain.getTitle(),
            domain.getContent(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
