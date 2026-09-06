package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "REVIEW_BLIND_REQUEST_ATTACHMENT",
    indexes = {
        @Index(name = "idx_review_blind_request_attachment_request_id", columnList = "blind_request_id")
    }
)
public class ReviewBlindRequestAttachmentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blind_request_id", nullable = false)
    private Long blindRequestId;

    @Column(name = "attachment_file_id", nullable = false)
    private Long attachmentFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ReviewBlindRequestAttachmentJpaEntity() {
    }

    private ReviewBlindRequestAttachmentJpaEntity(Long blindRequestId, Long attachmentFileId, Integer sort) {
        this.blindRequestId = blindRequestId;
        this.attachmentFileId = attachmentFileId;
        this.sort = sort;
    }

    static ReviewBlindRequestAttachmentJpaEntity create(Long blindRequestId, Long attachmentFileId, Integer sort) {
        return new ReviewBlindRequestAttachmentJpaEntity(blindRequestId, attachmentFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public Long getBlindRequestId() {
        return this.blindRequestId;
    }

    public Long getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
