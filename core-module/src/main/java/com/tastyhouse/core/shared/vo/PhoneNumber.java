package com.tastyhouse.core.shared.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PhoneNumber {

    private static final String PHONE_NUMBER_PATTERN = "^01[0-9]{8,9}$";

    private final String value;

    public PhoneNumber(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || !value.matches(PHONE_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("올바른 휴대폰번호 형식이 아닙니다. (예: 01012345678)");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
