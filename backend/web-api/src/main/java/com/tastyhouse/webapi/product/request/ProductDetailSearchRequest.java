package com.tastyhouse.webapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 상세 조회 파라미터 — 가격을 해석할 주문유형 하나를 받는다.
 *
 * <p>파라미터가 하나여도 개별 {@code @RequestParam}을 나열하지 않고 이 record로 묶는다(GET 조회
 * 파라미터 규칙). 도메인 enum이 아니라 {@code String}으로 받고 승격은 {@code ProductQueryService}가
 * {@code OrderMethod.from(...)}으로 수행한다 — HTTP 경계에 도메인 enum을 노출하면 상수 추가가 곧
 * 공개 스키마 변경이 된다.
 *
 * <p><b>기본값 {@code DELIVERY}를 compact constructor에서 정규화한다.</b> 파라미터 없이 호출하는 기존
 * 클라이언트가 있으므로(이 엔드포인트는 원래 쿼리 파라미터가 없었다) 미지정을 오류로 만들 수 없고,
 * 서비스마다 기본값을 채우면 판단이 흩어져 한쪽만 바뀔 수 있다.
 */
@Schema(description = "메뉴 상세 조회 요청")
public record ProductDetailSearchRequest(
    @Schema(description = "가격을 해석할 주문유형. 미지정이면 DELIVERY로 조회합니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "TAKEOUT")
    String orderMethod
) {
    /** 주문유형 미지정 시 기본값 — 손님 화면의 기본 진입 경로가 배달이다. */
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    public ProductDetailSearchRequest {
        if (orderMethod == null || orderMethod.isBlank()) {
            orderMethod = DEFAULT_ORDER_METHOD;
        }
    }
}
