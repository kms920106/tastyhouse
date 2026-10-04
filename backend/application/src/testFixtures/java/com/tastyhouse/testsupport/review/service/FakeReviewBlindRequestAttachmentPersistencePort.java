package com.tastyhouse.testsupport.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentPersistencePort;

public class FakeReviewBlindRequestAttachmentPersistencePort implements ReviewBlindRequestAttachmentPersistencePort {

    private final List<ReviewBlindRequestAttachment> attachments = new ArrayList<>();
    private long sequence = 0L;

    @Override
    public List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> newAttachments) {
        List<ReviewBlindRequestAttachment> persisted = newAttachments.stream()
            .map(attachment -> ReviewBlindRequestAttachment.reconstitute(
                ++sequence,
                attachment.getBlindRequestId(),
                attachment.getAttachmentFileId(),
                attachment.getSort()
            ))
            .toList();
        attachments.addAll(persisted);
        return persisted;
    }

    public List<ReviewBlindRequestAttachment> saved() {
        return List.copyOf(attachments);
    }
}
