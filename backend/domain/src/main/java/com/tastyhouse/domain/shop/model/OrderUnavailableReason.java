package com.tastyhouse.domain.shop.model;

public enum OrderUnavailableReason {
    PERMANENTLY_CLOSED("폐업한 가게입니다"),
    HIDDEN("노출정지 상태입니다"),
    SUSPENDED("영업 임시중지 중입니다"),
    PUBLIC_HOLIDAY_CLOSED("공휴일 휴무입니다"),
    TEMPORARILY_CLOSED("임시휴무 기간입니다"),
    REGULAR_CLOSED_DAY("정기휴무일입니다"),
    OUT_OF_BUSINESS_HOURS("영업시간이 아닙니다"),
    BREAK_TIME("휴게시간입니다"),
    ORDER_METHOD_NOT_SUPPORTED("이 가게가 지원하지 않는 주문유형입니다");

    private final String displayName;

    OrderUnavailableReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
