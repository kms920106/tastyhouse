package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 게시중단 요청 첨부 서류 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewBlindRequestAttachment}와 분리된 영속 전용 엔티티다. 변환은
 * {@code ReviewBlindRequestAttachmentMapper}가 수행한다.
 *
 * <p>{@code ReviewImageJpaEntity}와 달리 {@code BaseEntity}를 상속하지 않는다 — 불변 애그리거트라 감사
 * 시각을 소비하지 않고, 테이블에도 {@code created_at}/{@code updated_at} 컬럼이 없다
 * ({@code verification} 도메인이 {@code BaseEntity}를 상속하지 않는 것과 같은 형태).
 */
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음).
     * {@code ReviewBlindRequestAttachmentMapper#toEntity}에서만 호출한다.
     */
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
