package com.tastyhouse.application.notice.store;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.application.notice.port.out.write.NoticeState;

final class NoticeStateMapper {
    private NoticeStateMapper() {
    }

    static Notice toDomain(NoticeState state) {
        return Notice.reconstitute(
            state.id(),
            state.title(),
            state.content(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static NoticeState toState(Notice notice) {
        return new NoticeState(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.isDeleted(),
            notice.getCreatedAt(),
            notice.getUpdatedAt()
        );
    }
}
