package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

/**
 * 가게 변경이력 목록 조회 조건.
 *
 * <p>{@code shopId}와 {@code changedDate}는 필수다 — 소유권이 검증된 특정 가게의 특정 하루만 조회하며,
 * 이 두 값이 인덱스 레인지 스캔의 진입점이다. {@code category}/{@code changeType}은 선택(null이면 전체)이다.
 *
 * @param retentionFrom 조회 하한(오늘 기준 6개월 전). 서비스가 이미 400으로 거른 뒤에도 쿼리에 항상 실어
 *     보내는 정책 이중 안전망이며, 동시에 인덱스 레인지 스캔을 보장한다.
 */
public record ShopChangeHistorySearchCondition(
    Long shopId,
    ShopChangeCategory category,
    ShopChangeType changeType,
    LocalDate changedDate,
    LocalDate retentionFrom
) {
}
