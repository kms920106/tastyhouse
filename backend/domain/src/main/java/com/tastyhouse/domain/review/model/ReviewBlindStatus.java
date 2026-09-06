package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 리뷰 게시중단 요청의 처리 상태.
 *
 * <p><b>공용 {@link ApprovalStatus}에 상수를 추가하지 않고 별도 enum을 둔다.</b> 그 enum은
 * {@code ShopImageChangeRequest}·{@code ShopDeliveryAreaAdjustmentRequest} 등이 공유하므로, 리뷰에만
 * 의미가 있는 {@link #EXPIRED}/{@link #DELETED}를 넣으면 이미지 검수 코드가 도달 불가능한 분기를 갖게
 * 된다. {@code ApprovalStatus} Javadoc의 <i>"도메인 특화 승인상태가 필요하면 그 도메인 enum에서 이 enum을
 * 감싸거나 별도로 정의한다"</i> 가 이 경우다.
 *
 * <p>앞의 네 상수는 {@code ApprovalStatus}와 이름·의미가 그대로 대응하고, 뒤의 둘은 승인 이후의
 * 생애주기(30일 경과 재노출 / 고객 동의 삭제)를 나타낸다.
 */
public enum ReviewBlindStatus {

    PENDING("대기"),
    APPROVED("게시중단"),
    REJECTED("반려"),
    CANCELED("취소"),

    /** 게시중단 30일이 지나 배치가 리뷰를 자동 재노출했다. */
    EXPIRED("재노출"),

    /** 고객이 삭제에 동의해 리뷰가 삭제됐다. */
    DELETED("삭제");

    private final String description;

    ReviewBlindStatus(String description) {
        this.description = description;
    }

    public static ReviewBlindStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_STATUS_UNKNOWN,
                ErrorCode.REVIEW_BLIND_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
