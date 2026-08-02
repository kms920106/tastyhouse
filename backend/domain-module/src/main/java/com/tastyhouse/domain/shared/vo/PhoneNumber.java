package com.tastyhouse.domain.shared.vo;

public record PhoneNumber(String value) {

    private static final String PHONE_NUMBER_PATTERN = "^01[0-9]{8,9}$";

    public PhoneNumber {
        if (value == null || !value.matches(PHONE_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("올바른 휴대폰번호 형식이 아닙니다. (예: 01012345678)");
        }
    }
}
