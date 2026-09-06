package com.tastyhouse.domain.bug.model;

import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

public class BugReportImage {
    private final Long id;
    private final BugReportId bugReportId;
    private final UploadedFileId imageFileId;
    private final Integer sort;

    private BugReportImage(Long id, BugReportId bugReportId, UploadedFileId imageFileId, Integer sort) {
        this.id = id;
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static BugReportImage of(BugReportId bugReportId, UploadedFileId imageFileId, Integer sort) {
        return new BugReportImage(null, bugReportId, imageFileId, sort);
    }

    public static BugReportImage reconstitute(Long id, BugReportId bugReportId, UploadedFileId imageFileId, Integer sort) {
        return new BugReportImage(id, bugReportId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public BugReportId getBugReportId() {
        return this.bugReportId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
