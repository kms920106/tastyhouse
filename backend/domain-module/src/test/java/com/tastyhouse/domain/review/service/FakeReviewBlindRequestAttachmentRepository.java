package com.tastyhouse.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;

/**
 * 게시중단 요청 첨부 write 포트의 인메모리 fake.
 *
 * <p>불변 애그리거트라 적재만 재현하면 충분하다. 적재된 첨부를 그대로 노출해 순번 부여를 검증할 수 있게 한다.
 */
public class FakeReviewBlindRequestAttachmentRepository implements ReviewBlindRequestAttachmentRepository {

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
