package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;

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
