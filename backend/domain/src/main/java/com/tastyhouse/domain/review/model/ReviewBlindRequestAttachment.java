package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;

public class ReviewBlindRequestAttachment {
    private final Long id;
    private final ReviewBlindRequestId blindRequestId;
    private final UploadedFileId attachmentFileId;
    private final int sort;

    private ReviewBlindRequestAttachment(
        Long id,
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        this.id = id;
        this.blindRequestId = blindRequestId;
        this.attachmentFileId = attachmentFileId;
        this.sort = sort;
    }

    public static ReviewBlindRequestAttachment of(
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        return new ReviewBlindRequestAttachment(null, blindRequestId, attachmentFileId, sort);
    }

    public static ReviewBlindRequestAttachment reconstitute(
        Long id,
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        return new ReviewBlindRequestAttachment(id, blindRequestId, attachmentFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ReviewBlindRequestId getBlindRequestId() {
        return this.blindRequestId;
    }

    public UploadedFileId getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public int getSort() {
        return this.sort;
    }
}
