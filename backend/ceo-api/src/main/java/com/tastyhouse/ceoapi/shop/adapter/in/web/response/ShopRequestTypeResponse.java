package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestTypeView;

/**
 * 요청 유형 카탈로그 항목. 필터 드롭다운을 채우는 데 쓴다.
 *
 * <p>{@code contractAmending}을 함께 내려주면 프론트가 "계약서가 수정되는 요청" 안내를 유형 목록만으로
 * 구성할 수 있다.
 */
@Schema(description = "요청 유형 카탈로그 항목")
public record ShopRequestTypeResponse(

    @Schema(description = "요청 유형 코드", example = "TRADEMARK_CHANGE")
    String code,

    @Schema(description = "요청 유형 한글 라벨", example = "상표 변경 요청")
    String description,

    @Schema(description = "승인 시 전자계약서가 수정되는 요청인지", example = "false")
    boolean contractAmending
) {

    public static ShopRequestTypeResponse from(ShopRequestTypeView view) {
        return new ShopRequestTypeResponse(
            view.requestType().name(),
            view.requestType().getDescription(),
            view.contractAmending()
        );
    }
}
