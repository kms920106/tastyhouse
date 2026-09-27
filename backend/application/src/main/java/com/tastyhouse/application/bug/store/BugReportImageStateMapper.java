package com.tastyhouse.application.bug.store;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.bug.port.out.write.BugReportImageState;

final class BugReportImageStateMapper {
    private BugReportImageStateMapper() {
    }

    static BugReportImage toDomain(BugReportImageState state) {
        return BugReportImage.reconstitute(
            state.id(),
            state.bugReportId() == null ? null : BugReportId.of(state.bugReportId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort()
        );
    }

    static BugReportImageState toState(BugReportImage bugReportImage) {
        return new BugReportImageState(
            bugReportImage.getId(),
            bugReportImage.getBugReportId() == null ? null : bugReportImage.getBugReportId().value(),
            bugReportImage.getImageFileId() == null ? null : bugReportImage.getImageFileId().value(),
            bugReportImage.getSort()
        );
    }
}
