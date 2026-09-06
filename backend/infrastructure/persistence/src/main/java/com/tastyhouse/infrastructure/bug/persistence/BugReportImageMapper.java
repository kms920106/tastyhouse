package com.tastyhouse.infrastructure.bug.persistence;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class BugReportImageMapper {
    private BugReportImageMapper() {
    }

    static BugReportImage toDomain(BugReportImageJpaEntity entity) {
        return BugReportImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getBugReportId(), BugReportId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort()
        );
    }

    static BugReportImageJpaEntity toEntity(BugReportImage domain) {
        return BugReportImageJpaEntity.create(
            IdMapping.raw(domain.getBugReportId(), BugReportId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}
