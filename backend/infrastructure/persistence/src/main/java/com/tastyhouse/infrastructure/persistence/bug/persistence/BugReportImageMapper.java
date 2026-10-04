package com.tastyhouse.infrastructure.persistence.bug.persistence;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

final class BugReportImageMapper {

    private BugReportImageMapper() {
    }

    static BugReportImage toDomain(BugReportImageJpaEntity entity) {
        return BugReportImage.reconstitute(
            entity.getId(),
            entity.getBugReportId() == null ? null : BugReportId.of(entity.getBugReportId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSort()
        );
    }

    static BugReportImageJpaEntity toEntity(BugReportImage bugReportImage) {
        return BugReportImageJpaEntity.create(
            bugReportImage.getBugReportId() == null ? null : bugReportImage.getBugReportId().value(),
            bugReportImage.getImageFileId() == null ? null : bugReportImage.getImageFileId().value(),
            bugReportImage.getSort()
        );
    }
}
