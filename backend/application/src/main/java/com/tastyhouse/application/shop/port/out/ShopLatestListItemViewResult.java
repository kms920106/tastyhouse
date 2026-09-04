package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 최신 가게 목록 항목(손님 화면) — 실시간 영업 상태를 함께 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. 거처와 근거는 {@link ShopBestListItemViewResult}와 같다 — 공유 읽기 계약
 * {@code LatestShopItemResult}에 없는 실시간 영업 상태를 도메인 서비스가 페이지 단위로 판정하므로,
 * 그 결합을 서비스에 남기고 결과를 담아 넘긴다. enum 강등도 서비스가 끝낸다.
 */
public record ShopLatestListItemViewResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    LocalDateTime createdAt,
    Long reviewCount,
    Long bookmarkCount,
    List<String> foodTypes,
    String operatingStatus,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
