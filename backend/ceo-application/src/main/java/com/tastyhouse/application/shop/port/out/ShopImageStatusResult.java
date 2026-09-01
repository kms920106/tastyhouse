package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 가게 이미지(상표/대표이미지) 현황 — 현재 적용 중인 URL과 변경요청 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 현재 이미지 URL과 변경요청 목록이 <b>서로 다른 읽기 포트</b>
 * ({@code ShopBasicInfoQueryPort}·{@code ShopOwnerQueryPort})에서 오므로 유스케이스가 두 번 조회해
 * 합친다. 표현 계약이 {@code from(Result)} 한 번으로 끝낼 수 있도록 둘을 묶는다.
 */
public record ShopImageStatusResult(
    String currentImageUrl,
    List<ShopImageChangeRequestResult> requests
) {
}
