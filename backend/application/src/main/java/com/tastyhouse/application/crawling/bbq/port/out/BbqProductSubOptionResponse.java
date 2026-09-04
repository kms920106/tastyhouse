package com.tastyhouse.application.crawling.bbq.port.out;

import java.util.List;

/**
 * BBQ 상품 서브 옵션 응답.
 *
 * @param id                                 서브 옵션 ID
 * @param subOptionTitle                     서브 옵션 제목
 * @param requiredSelectCount                필수 선택 개수
 * @param maxSelectCount                     최대 선택 개수
 * @param subOptionItemDetailResponseList    서브 옵션 아이템 상세 목록
 */
public record BbqProductSubOptionResponse(
    Long id,
    String subOptionTitle,
    Integer requiredSelectCount,
    Integer maxSelectCount,
    List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
) {
    public static BbqProductSubOptionResponse from(
        Long id,
        String subOptionTitle,
        Integer requiredSelectCount,
        Integer maxSelectCount,
        List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
    ) {
        return new BbqProductSubOptionResponse(
            id,
            subOptionTitle,
            requiredSelectCount,
            maxSelectCount,
            subOptionItemDetailResponseList
        );
    }
}
