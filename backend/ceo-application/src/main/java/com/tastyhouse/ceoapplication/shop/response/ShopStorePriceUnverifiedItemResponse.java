package com.tastyhouse.ceoapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장가격 인증을 충족하지 못한 메뉴 한 건.
 *
 * <p>메뉴명을 함께 내려주는 이유는 점주가 어느 메뉴를 고쳐야 하는지 즉시 알아야 하기 때문이다 —
 * 화면이 메뉴 id로 이름을 다시 조회하게 하면 목록 길이만큼 추가 요청이 발생한다.
 *
 * <p>{@code reason}은 한글 문구가 아니라 <b>enum 상수명</b>이다. 사유별로 점주가 할 조치가 다르므로
 * (미등록은 매장가 입력, 배달가 초과는 배달가 인하) 화면이 코드로 분기해 각기 다른 안내·버튼을
 * 띄운다 — 문구를 내려주면 서버 문구 변경이 곧 화면 분기 파손이 된다.
 */
@Schema(description = "매장가격 미인증 메뉴")
public record ShopStorePriceUnverifiedItemResponse(
    @Schema(description = "메뉴 ID", example = "1")
    Long productId,

    @Schema(description = "메뉴명", example = "후라이드 치킨")
    String productName,

    @Schema(description = "미인증 사유 코드", example = "STORE_PRICE_NOT_REGISTERED",
        allowableValues = {"DELIVERY_PRICE_HIGHER_THAN_STORE", "STORE_PRICE_NOT_REGISTERED"})
    String reason
) {

    public static ShopStorePriceUnverifiedItemResponse from(Long productId, String productName, String reason) {
        return new ShopStorePriceUnverifiedItemResponse(productId, productName, reason);
    }
}
