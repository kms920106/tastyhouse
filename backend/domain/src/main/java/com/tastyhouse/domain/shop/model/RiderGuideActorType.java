package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 안내 변경 주체. 점주 본인 변경과 관리자 사후 검수 조치를 구분한다.
 */
public enum RiderGuideActorType {

    CEO("점주"),
    ADMIN("관리자");

    private final String description;

    RiderGuideActorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * 조치 유형과 주체를 한 ErrorCode로 묶어 쓰되, 메시지에는 어느 쪽이 잘못됐는지 남긴다 —
     * 별도 코드를 늘리는 것보다 프론트가 다뤄야 할 코드 수를 줄이는 편이 낫다.
     */
    public static RiderGuideActorType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN,
                "알 수 없는 라이더 안내 변경 주체입니다: " + code);
        }
    }
}
