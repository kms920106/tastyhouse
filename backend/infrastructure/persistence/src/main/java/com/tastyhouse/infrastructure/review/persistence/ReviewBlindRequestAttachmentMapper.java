package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewBlindRequestAttachmentMapper {
    private ReviewBlindRequestAttachmentMapper() {
    }

    static ReviewBlindRequestAttachment toDomain(ReviewBlindRequestAttachmentJpaEntity entity) {
        return ReviewBlindRequestAttachment.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getBlindRequestId(), ReviewBlindRequestId::of),
            IdMapping.vo(entity.getAttachmentFileId(), UploadedFileId::of),
            entity.getSort()
        );
    }

    static ReviewBlindRequestAttachmentJpaEntity toEntity(ReviewBlindRequestAttachment domain) {
        return ReviewBlindRequestAttachmentJpaEntity.create(
            IdMapping.raw(domain.getBlindRequestId(), ReviewBlindRequestId::value),
            IdMapping.raw(domain.getAttachmentFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}
