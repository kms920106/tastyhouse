package com.tastyhouse.domain.product.model;

public enum ProductHiddenReason {
    MANUALLY_HIDDEN("점주가 숨김 처리한 메뉴입니다."),

    BEFORE_EXPOSURE_PERIOD("노출 시작일 이전입니다."),

    AFTER_EXPOSURE_PERIOD("노출 종료일이 지났습니다."),

    OUT_OF_EXPOSURE_HOURS("노출 시간대가 아닙니다.");

    private final String description;

    ProductHiddenReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
