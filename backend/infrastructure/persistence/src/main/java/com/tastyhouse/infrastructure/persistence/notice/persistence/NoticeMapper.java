package com.tastyhouse.infrastructure.persistence.notice.persistence;

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

    static NoticeJpaEntity toEntity(Notice notice) {
        return NoticeJpaEntity.create(
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.isDeleted()
        );
    }

    static void applyChanges(NoticeJpaEntity entity, Notice notice) {
        entity.applyChanges(
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.isDeleted()
        );
    }
}
