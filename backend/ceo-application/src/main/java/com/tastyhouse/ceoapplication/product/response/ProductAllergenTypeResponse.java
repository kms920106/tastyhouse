package com.tastyhouse.ceoapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 알레르기 유발성분 코드·라벨 한 쌍.
 *
 * <p>점주 화면의 체크박스 목록을 <b>서버가 공급</b>한다 — 화면이 코드↔라벨 매핑표를 들고 있으면 성분이
 * 추가·변경될 때 화면 배포가 필요해진다. 배열 순서는 법령 열거 순서이며 화면은 그 순서대로 그린다.
 */
@Schema(description = "알레르기 유발성분 코드·라벨")
public record ProductAllergenTypeResponse(
    @Schema(description = "성분 코드", example = "MILK")
    String code,

    @Schema(description = "성분 한글 라벨", example = "우유")
    String label
) {

    public static ProductAllergenTypeResponse from(String code, String label) {
        return new ProductAllergenTypeResponse(
            code,
            label
        );
    }
}
