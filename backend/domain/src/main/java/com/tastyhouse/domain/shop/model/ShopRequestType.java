package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ShopRequestType {
    TRADEMARK_CHANGE("상표 변경 요청", "요청 상표 이미지", false),
    THUMBNAIL_CHANGE("대표이미지 변경 요청", "요청 대표이미지", false),
    DELIVERY_AREA_ADJUSTMENT("배달지역 조정 신청", "정보제공 동의서", true),
    REVIEW_BLIND("리뷰 게시중단 요청", "요청 사유", false),
    STORE_PRICE_VERIFICATION("매장 가격 인증 요청", "매장 가격표 이미지", false);

    private final String description;
    private final String attachmentLabel;
    private final boolean contractAmending;

    ShopRequestType(String description, String attachmentLabel, boolean contractAmending) {
        this.description = description;
        this.attachmentLabel = attachmentLabel;
        this.contractAmending = contractAmending;
    }

    public static ShopRequestType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_TYPE_UNKNOWN,
                ErrorCode.SHOP_REQUEST_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }

    public String getAttachmentLabel() {
        return this.attachmentLabel;
    }

    public boolean isContractAmending() {
        return this.contractAmending;
    }
}
