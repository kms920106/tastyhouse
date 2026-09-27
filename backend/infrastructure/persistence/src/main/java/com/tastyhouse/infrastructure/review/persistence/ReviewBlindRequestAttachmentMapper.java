package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentState;

final class ReviewBlindRequestAttachmentMapper {
    private ReviewBlindRequestAttachmentMapper() {
    }

    static ReviewBlindRequestAttachmentState toState(ReviewBlindRequestAttachmentJpaEntity entity) {
        return new ReviewBlindRequestAttachmentState(
            entity.getId(),
            entity.getBlindRequestId(),
            entity.getAttachmentFileId(),
            entity.getSort()
        );
    }

    static ReviewBlindRequestAttachmentJpaEntity toEntity(ReviewBlindRequestAttachmentState state) {
        return ReviewBlindRequestAttachmentJpaEntity.create(
            state.blindRequestId(),
            state.attachmentFileId(),
            state.sort()
        );
    }
}
