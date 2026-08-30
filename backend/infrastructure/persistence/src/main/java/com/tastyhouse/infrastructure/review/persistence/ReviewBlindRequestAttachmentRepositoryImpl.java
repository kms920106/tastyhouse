package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;

/**
 * 게시중단 요청 첨부 서류 write 어댑터.
 *
 * <p>불변 애그리거트라 update 경로가 없어 {@code save}가 insert 전용이다(load-copy-save 불필요 —
 * {@code ReviewImageRepositoryImpl}과 동형).
 */
@Repository
public class ReviewBlindRequestAttachmentRepositoryImpl implements ReviewBlindRequestAttachmentRepository {

    private final ReviewBlindRequestAttachmentJpaRepository reviewBlindRequestAttachmentJpaRepository;

    public ReviewBlindRequestAttachmentRepositoryImpl(
        ReviewBlindRequestAttachmentJpaRepository reviewBlindRequestAttachmentJpaRepository
    ) {
        this.reviewBlindRequestAttachmentJpaRepository = reviewBlindRequestAttachmentJpaRepository;
    }

    @Override
    public List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments) {
        List<ReviewBlindRequestAttachmentJpaEntity> entities = attachments.stream()
            .map(ReviewBlindRequestAttachmentMapper::toEntity)
            .toList();

        return reviewBlindRequestAttachmentJpaRepository.saveAll(entities).stream()
            .map(ReviewBlindRequestAttachmentMapper::toDomain)
            .toList();
    }
}
