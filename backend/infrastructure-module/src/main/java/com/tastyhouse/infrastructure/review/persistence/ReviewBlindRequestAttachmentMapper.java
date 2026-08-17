package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 게시중단 요청 첨부 서류 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환
 * 책임을 infrastructure에 둔다.
 */
final class ReviewBlindRequestAttachmentMapper {

    private ReviewBlindRequestAttachmentMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로). 불변 애그리거트라 감사 시각을 넘기지 않는다.
     */
    static ReviewBlindRequestAttachment toDomain(ReviewBlindRequestAttachmentJpaEntity entity) {
        return ReviewBlindRequestAttachment.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getBlindRequestId(), ReviewBlindRequestId::of),
            IdMapping.vo(entity.getAttachmentFileId(), UploadedFileId::of),
            entity.getSort()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReviewBlindRequestAttachmentJpaEntity toEntity(ReviewBlindRequestAttachment domain) {
        return ReviewBlindRequestAttachmentJpaEntity.create(
            IdMapping.raw(domain.getBlindRequestId(), ReviewBlindRequestId::value),
            IdMapping.raw(domain.getAttachmentFileId(), UploadedFileId::value),
            domain.getSort()
        );
    }
}
