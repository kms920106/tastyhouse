package com.tastyhouse.core.domain.member.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {

    LOW_USAGE_FREQUENCY("서비스 이용 빈도가 낮아서"),
    INSUFFICIENT_CONTENT("콘텐츠가 부족해서"),
    SWITCH_TO_ANOTHER_SERVICE("다른 서비스로 이동"),
    PRIVACY_CONCERNS("개인정보 보호 우려"),
    OTHER("기타");

    private final String description;
}
