package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentState;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentStatePort;

@Repository
public class ReviewBlindRequestAttachmentStatePortImpl implements ReviewBlindRequestAttachmentStatePort {
    private final ReviewBlindRequestAttachmentJpaRepository reviewBlindRequestAttachmentJpaRepository;

    public ReviewBlindRequestAttachmentStatePortImpl(
        ReviewBlindRequestAttachmentJpaRepository reviewBlindRequestAttachmentJpaRepository
    ) {
        this.reviewBlindRequestAttachmentJpaRepository = reviewBlindRequestAttachmentJpaRepository;
    }

    @Override
    public List<ReviewBlindRequestAttachmentState> saveAll(List<ReviewBlindRequestAttachmentState> states) {
        List<ReviewBlindRequestAttachmentJpaEntity> entities = states.stream()
            .map(ReviewBlindRequestAttachmentMapper::toEntity)
            .toList();

        return reviewBlindRequestAttachmentJpaRepository.saveAll(entities).stream()
            .map(ReviewBlindRequestAttachmentMapper::toState)
            .toList();
    }
}
