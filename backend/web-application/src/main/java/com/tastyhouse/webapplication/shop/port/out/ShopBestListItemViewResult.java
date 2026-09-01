package com.tastyhouse.webapplication.shop.port.out;

import java.util.List;

/**
 * 베스트 가게 목록 항목(손님 화면) — 실시간 영업 상태를 함께 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code BestShopItemResult}는 목록 투영 하나의 산출물이라
 * 실시간 영업 상태를 갖지 않는다 — 그 값은 여섯 애그리거트를 함께 읽는 도메인 서비스
 * ({@code ShopOperatingStatusService})가 <b>페이지 전체를 한 번에</b> 판정해 만드는 것이고, 판정 시각도
 * 서비스가 읽는다. 그 결합을 컨트롤러로 올리면 인바운드 어댑터가 도메인 서비스와 시계를 알게 되므로
 * 서비스에 남기고 결과만 담아 넘긴다.
 *
 * <p>{@code operatingStatus}·{@code foodTypes}는 도메인 enum을 {@code name()}으로 강등한 String이다 —
 * 인바운드 포트가 도메인 enum을 노출하지 않게 하려면 강등이 서비스에서 끝나야 한다. 판정 대상이 아닌
 * 가게(상태 맵에 없는 경우)는 {@code operatingStatus}가 {@code null}이며, 이는 기존 응답과 같다.
 */
public record ShopBestListItemViewResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<String> foodTypes,
    String operatingStatus,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
