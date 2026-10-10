package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.time.LocalDateTime;

import com.tastyhouse.domain.notice.model.Notice;

final class NoticeRowMapper {

    private NoticeRowMapper() {
    }

    static Notice toDomain(NoticeRow row) {
        return Notice.reconstitute(
            row.id(),
            row.title(),
            row.content(),
            row.visible(),
            row.deleted(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    static Notice toDomain(NoticeWriteRow row) {
        return Notice.reconstitute(
            row.getId(),
            row.getTitle(),
            row.getContent(),
            row.isVisible(),
            row.isDeleted(),
            row.getCreatedAt(),
            row.getUpdatedAt()
        );
    }

    static NoticeWriteRow toWriteRow(Notice notice, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new NoticeWriteRow(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.isDeleted(),
            createdAt,
            updatedAt
        );
    }
}
