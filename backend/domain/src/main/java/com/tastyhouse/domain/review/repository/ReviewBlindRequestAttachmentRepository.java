package com.tastyhouse.domain.review.repository;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;

public interface ReviewBlindRequestAttachmentRepository {
    List<ReviewBlindRequestAttachment> saveAll(List<ReviewBlindRequestAttachment> attachments);
}
