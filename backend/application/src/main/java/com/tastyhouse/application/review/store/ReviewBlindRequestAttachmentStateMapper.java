package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;

final class ReviewBlindRequestAttachmentStateMapper {
    private ReviewBlindRequestAttachmentStateMapper() {
    }

    static ReviewBlindRequestAttachment toDomain(ReviewBlindRequestAttachmentState state) {
        return ReviewBlindRequestAttachment.reconstitute(
            state.id(),
            state.blindRequestId() == null ? null : ReviewBlindRequestId.of(state.blindRequestId()),
            state.attachmentFileId() == null ? null : UploadedFileId.of(state.attachmentFileId()),
            state.sort()
        );
    }

    static ReviewBlindRequestAttachmentState toState(ReviewBlindRequestAttachment attachment) {
        return new ReviewBlindRequestAttachmentState(
            attachment.getId(),
            attachment.getBlindRequestId() == null ? null : attachment.getBlindRequestId().value(),
            attachment.getAttachmentFileId() == null ? null : attachment.getAttachmentFileId().value(),
            attachment.getSort()
        );
    }
}
