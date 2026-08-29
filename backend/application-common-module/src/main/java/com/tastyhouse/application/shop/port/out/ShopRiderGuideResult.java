package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 라이더 안내 단건 조회 결과. 가게 실주소·좌표를 함께 담아, 픽업 위치 미설정 시의 폴백 안내와
 * "실주소와 동일하게 설정" 버튼의 참고값으로 쓴다.
 */
public record ShopRiderGuideResult(
    Long shopId,
    String shopName,
    String visitGuide,
    String pickupRoadAddress,
    String pickupLotAddress,
    String pickupDetailAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    String shopRoadAddress,
    String shopLotAddress,
    BigDecimal shopLatitude,
    BigDecimal shopLongitude,
    LocalDateTime updatedAt
) {
}
