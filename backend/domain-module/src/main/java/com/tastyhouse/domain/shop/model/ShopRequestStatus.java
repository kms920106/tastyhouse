package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 요청처리 현황의 통합 상태 5종.
 *
 * <p>유형별 원본 enum({@code ApprovalStatus}, {@code DeliveryAreaAdjustmentStatus})을 하나로 모은 표현이며,
 * 매핑은 {@code ShopRequestIndexRecorder}가 소유한다(공용 enum이 특정 컨텍스트를 알게 되는 역방향 의존을
 * 만들지 않는다).
 *
 * <p>이미지 변경에는 {@link #IN_PROGRESS}가 없다 — 검수가 단일 단계라 실재하지 않는 상태를 원본에 넣지
 * 않고 "그 유형은 IN_PROGRESS를 결코 갖지 않는다"로 표현한다. 덕분에 상태 필터가 유형별로 갈리지 않는다.
 */
public enum ShopRequestStatus {

    PENDING("대기중"),
    IN_PROGRESS("진행"),
    REJECTED("반려"),
    CANCELED("취소"),
    APPROVED("승인");

    private final String description;

    ShopRequestStatus(String description) {
        this.description = description;
    }

    public static ShopRequestStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_STATUS_UNKNOWN,
                ErrorCode.SHOP_REQUEST_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    /** 아직 처리 중인 상태인지(대기중·진행). */
    public boolean isOpen() {
        return this == PENDING || this == IN_PROGRESS;
    }

    /** 종결된 상태인지(반려·취소·승인). */
    public boolean isClosed() {
        return !isOpen();
    }

    public String getDescription() {
        return this.description;
    }
}
