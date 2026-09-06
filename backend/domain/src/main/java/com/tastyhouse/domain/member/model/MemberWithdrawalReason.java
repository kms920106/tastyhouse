package com.tastyhouse.domain.member.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum MemberWithdrawalReason {

    LOW_USAGE_FREQUENCY("서비스 이용 빈도가 낮아서"),
    INSUFFICIENT_CONTENT("콘텐츠가 부족해서"),
    SWITCH_TO_ANOTHER_SERVICE("다른 서비스로 이동"),
    PRIVACY_CONCERNS("개인정보 보호 우려"),
    OTHER("기타");

    private final String description;

    MemberWithdrawalReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static MemberWithdrawalReason from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.WITHDRAWAL_REASON_TYPE_UNKNOWN,
                ErrorCode.WITHDRAWAL_REASON_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
