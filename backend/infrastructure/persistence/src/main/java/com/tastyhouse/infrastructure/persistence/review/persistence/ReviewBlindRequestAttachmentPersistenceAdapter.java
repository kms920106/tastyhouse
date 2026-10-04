package com.tastyhouse.infrastructure.persistence.review.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentPersistencePort;

@Repository
public class ReviewBlindRequestAttachmentPersistenceAdapter implements ReviewBlindRequestAttachmentPersistencePort {

    private final ReviewBlindRequestAttachmentJpaRepository reviewBlindRequestAttachmentJpaRepository;

    public ReviewBlindRequestAttachmentPersistenceAdapter(
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
