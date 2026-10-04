package com.tastyhouse.infrastructure.persistence.review.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;

final class ReviewBlindRequestAttachmentMapper {

    private ReviewBlindRequestAttachmentMapper() {
    }

    static ReviewBlindRequestAttachment toDomain(ReviewBlindRequestAttachmentJpaEntity entity) {
        return ReviewBlindRequestAttachment.reconstitute(
            entity.getId(),
            entity.getBlindRequestId() == null ? null : ReviewBlindRequestId.of(entity.getBlindRequestId()),
            entity.getAttachmentFileId() == null ? null : UploadedFileId.of(entity.getAttachmentFileId()),
            entity.getSort()
        );
    }

    static ReviewBlindRequestAttachmentJpaEntity toEntity(ReviewBlindRequestAttachment attachment) {
        return ReviewBlindRequestAttachmentJpaEntity.create(
            attachment.getBlindRequestId() == null ? null : attachment.getBlindRequestId().value(),
            attachment.getAttachmentFileId() == null ? null : attachment.getAttachmentFileId().value(),
            attachment.getSort()
        );
    }
}
