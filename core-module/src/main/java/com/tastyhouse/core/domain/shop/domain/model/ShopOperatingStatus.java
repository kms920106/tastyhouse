package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 앱에 노출하는 가게 실시간 영업 상태.
 *
 * <p>영업시간·휴게시간·정기휴무·임시휴무·임시중지·공휴일휴무를 종합해 계산한다.
 * HTTP 경계로는 {@code name()} 문자열로 노출한다(도메인 enum 경계 규칙).
 */
@Getter
@RequiredArgsConstructor
public enum ShopOperatingStatus {

    OPEN("영업중"),
    PREPARING("준비중");

    private final String description;
}
