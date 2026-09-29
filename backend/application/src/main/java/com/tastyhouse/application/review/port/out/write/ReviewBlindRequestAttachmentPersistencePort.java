package com.tastyhouse.application.review.port.out.write;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;

public interface ReviewBlindRequestAttachmentPersistencePort {
    List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments);
}
