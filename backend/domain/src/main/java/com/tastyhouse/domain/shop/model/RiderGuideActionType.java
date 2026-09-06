package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 안내 조치 유형.
 *
 * <p>라이더 안내는 승인 워크플로가 아니라 <b>등록 즉시 반영 + 관리자 사후 검수</b> 모델이므로,
 * 관리자 조치는 사전 승인/반려가 아니라 수정 요청({@link #REVISION_REQUEST})과
 * 삭제({@link #DELETION})다.
 */
public enum RiderGuideActionType {

    UPDATE("등록·수정"),
    REVISION_REQUEST("수정 요청"),
    DELETION("삭제 조치");

    private final String description;

    RiderGuideActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static RiderGuideActionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN,
                ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
