package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 일괄 품절·숨김 처리에서 개별 대상이 실패한 사유.
 *
 * <p>{@code name}을 함께 담는 이유는 화면이 "닭가슴살샐러드는 마지막 추천 메뉴라 숨길 수 없습니다"처럼
 * 어느 대상이 왜 실패했는지 그대로 보여주기 때문이다 — id만 담으면 화면이 이름을 다시 조회해야 한다.
 *
 * @param id        실패한 대상 id (메뉴 또는 옵션)
 * @param name      실패한 대상의 이름 (화면에 그대로 노출)
 * @param errorCode 실패 사유
 */
public record ProductAvailabilityFailure(
    Long id,
    String name,
    ErrorCode errorCode
) {

    public static ProductAvailabilityFailure of(Long id, String name, ErrorCode errorCode) {
        return new ProductAvailabilityFailure(id, name, errorCode);
    }
}
