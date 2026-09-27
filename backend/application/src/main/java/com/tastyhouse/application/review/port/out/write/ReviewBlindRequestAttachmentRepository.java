package com.tastyhouse.application.review.port.out.write;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;

public interface ReviewBlindRequestAttachmentRepository {
    List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments);
}
