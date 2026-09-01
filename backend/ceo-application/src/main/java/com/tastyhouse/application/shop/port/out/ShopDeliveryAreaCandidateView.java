package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

/**
 * 배달지역 환산 후보 행정동 한 건 — 이미 등록됐는지 여부를 함께 담는다.
 *
 * <p><b>챕터 09</b>에서 신설. {@code AdminDongCandidateResult}에 "이 가게에 이미 등록된 동인가"를 더한
 * 형태다. 그 판정은 등록 목록과의 대조이므로 application의 일이고, 표현 계약은 결과만 옮긴다.
 */
public record ShopDeliveryAreaCandidateView(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    boolean alreadyRegistered
) {
}
