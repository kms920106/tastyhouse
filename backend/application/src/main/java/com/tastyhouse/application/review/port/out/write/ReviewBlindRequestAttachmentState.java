package com.tastyhouse.application.review.port.out.write;

public record ReviewBlindRequestAttachmentState(
    Long id,
    Long blindRequestId,
    Long attachmentFileId,
    int sort
) {
}
