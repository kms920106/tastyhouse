package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

/**
 * 가게 원산지 표시 정보 read model.
 *
 * <p>{@code sourceType}을 도메인 enum이 아니라 {@code String}으로 담는다 — 점주·손님 응답이 모두 코드
 * 문자열을 그대로 내려주므로 소비 Service에서 {@code name()} 변환을 한 번 더 하지 않게 한다.
 *
 * <p>{@code updatedAt}은 점주 화면만 쓴다(손님 응답에서는 제외). DAO가 한 벌로 투영하고 어느 필드를
 * 노출할지는 각 모듈 Response가 결정한다.
 */
public record ShopOriginInfoResult(
    Long id,
    Long shopId,
    String sourceType,
    String content,
    String url,
    LocalDateTime updatedAt
) {

}
