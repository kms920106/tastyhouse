package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;

/**
 * 게시중단 요청에 첨부된 증빙 서류(신분증·위임장·사업자등록증 등) 순수 도메인 모델.
 *
 * <p>{@link ReviewImage}와 동형의 불변 애그리거트다 — 상태전이가 없어 감사 시각을 소비하지 않고 전 필드가
 * {@code final}이며 {@code of}/{@code reconstitute} 두 팩토리만 공개한다.
 *
 * <p>개수 제한(최대 3개)은 이 모델이 아니라 <b>요청 DTO의 Bean Validation</b>이 판정한다 — 개수는 스키마가
 * 아니라 정책이기 때문이다.
 */
public class ReviewBlindRequestAttachment {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewBlindRequestId blindRequestId;
    private final UploadedFileId attachmentFileId;
    private final int sort;

    private ReviewBlindRequestAttachment(
        Long id,
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        this.id = id;
        this.blindRequestId = blindRequestId;
        this.attachmentFileId = attachmentFileId;
        this.sort = sort;
    }

    /**
     * 신규 첨부를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ReviewBlindRequestAttachment of(
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        return new ReviewBlindRequestAttachment(null, blindRequestId, attachmentFileId, sort);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ReviewBlindRequestAttachment reconstitute(
        Long id,
        ReviewBlindRequestId blindRequestId,
        UploadedFileId attachmentFileId,
        int sort
    ) {
        return new ReviewBlindRequestAttachment(id, blindRequestId, attachmentFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ReviewBlindRequestId getBlindRequestId() {
        return this.blindRequestId;
    }

    public UploadedFileId getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public int getSort() {
        return this.sort;
    }
}
