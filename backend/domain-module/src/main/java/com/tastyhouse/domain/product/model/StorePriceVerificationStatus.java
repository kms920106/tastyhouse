package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 매장 가격 인증 요청의 상태 5종.
 *
 * <p><b>공용 {@code ApprovalStatus}(4종)를 쓰지 않는 이유는 {@link #IN_PROGRESS}가 필요하기 때문이다.</b>
 * 매장가격 인증은 가격표 이미지와 실제 매장을 대조하는 검수라 "접수됨"과 "검수 착수"가 실제로 다른
 * 단계이며, 점주 화면이 그 둘을 구분해 보여준다. 반면 이미지 변경 승인은 단일 단계라
 * {@code ApprovalStatus}로 충분하다({@code ShopRequestStatus}의 같은 판단).
 *
 * <p>통합 인덱스({@code ShopRequestIndex})의 {@code ShopRequestStatus}로의 매핑은
 * {@code ShopRequestIndexRecorder}가 소유한다 — 공용 enum이 특정 컨텍스트를 알게 되는 역방향 의존을
 * 만들지 않는다.
 */
public enum StorePriceVerificationStatus {

    PENDING("대기"),
    IN_PROGRESS("검수 중"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELED("취소");

    private final String description;

    StorePriceVerificationStatus(String description) {
        this.description = description;
    }

    public static StorePriceVerificationStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_STATUS_UNKNOWN,
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    /** 아직 처리 중인 상태인지(대기·검수 중) — 재요청 차단 판정에 쓴다. */
    public boolean isOpen() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public String getDescription() {
        return this.description;
    }
}
