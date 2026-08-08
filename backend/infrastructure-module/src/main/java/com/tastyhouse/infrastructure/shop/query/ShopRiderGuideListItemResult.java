package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

/**
 * 라이더 안내 등록 가게 목록 조회 결과(관리자 검수 화면용).
 */
public record ShopRiderGuideListItemResult(
    Long shopId,
    String shopName,
    String visitGuide,
    boolean hasPickupLocation,
    LocalDateTime updatedAt
) {

}
