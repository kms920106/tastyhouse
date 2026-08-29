package com.tastyhouse.application.shop.port.out;

/**
 * 지하철역 목록 항목 결과.
 */
public record StationResult(
    Long id,
    String stationName
) {
}
