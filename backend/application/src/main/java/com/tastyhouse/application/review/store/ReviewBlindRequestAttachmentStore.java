package com.tastyhouse.application.review.store;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentState;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentStatePort;

public class ReviewBlindRequestAttachmentStore implements ReviewBlindRequestAttachmentRepository {
    private final ReviewBlindRequestAttachmentStatePort reviewBlindRequestAttachmentStatePort;

    public ReviewBlindRequestAttachmentStore(ReviewBlindRequestAttachmentStatePort reviewBlindRequestAttachmentStatePort) {
        this.reviewBlindRequestAttachmentStatePort = reviewBlindRequestAttachmentStatePort;
    }

    @Override
    public List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments) {
        List<ReviewBlindRequestAttachmentState> states = attachments.stream()
            .map(ReviewBlindRequestAttachmentStateMapper::toState)
            .toList();
        return reviewBlindRequestAttachmentStatePort.saveAll(states).stream()
            .map(ReviewBlindRequestAttachmentStateMapper::toDomain)
            .toList();
    }
}
