package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 프랜차이즈 배달지역 조정 신청 처리 상태.
 *
 * <p>공용 {@code ApprovalStatus}(PENDING/APPROVED/REJECTED)를 쓰지 않고 도메인 전용 enum을 둔다 —
 * 이 워크플로에는 "가맹본부에 자료를 전달했고 조정이 진행 중"(IN_PROGRESS)이라는 중간 상태가 있어
 * 3단계로는 표현할 수 없기 때문이다({@code BugReportStatus} 선례).
 */
public enum DeliveryAreaAdjustmentStatus {

    PENDING,      // 접수 대기 (점주가 신청했고 관리자가 아직 확인하지 않음)
    IN_PROGRESS,  // 조정 중 (가맹본부에 자료를 전달했고 조정이 진행 중)
    COMPLETED,    // 조정 완료 (조정 성립. 배달지역 반영은 별도 수행)
    REJECTED;     // 반려 (형식 미비·조정 불성립)

    public static DeliveryAreaAdjustmentStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
