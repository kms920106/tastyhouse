package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.application.bug.port.out.write.BugReportImageState;

final class BugReportImageMapper {
    private BugReportImageMapper() {
    }

    static BugReportImageState toState(BugReportImageJpaEntity entity) {
        return new BugReportImageState(
            entity.getId(),
            entity.getBugReportId(),
            entity.getImageFileId(),
            entity.getSort()
        );
    }

    static BugReportImageJpaEntity toEntity(BugReportImageState state) {
        return BugReportImageJpaEntity.create(
            state.bugReportId(),
            state.imageFileId(),
            state.sort()
        );
    }
}
