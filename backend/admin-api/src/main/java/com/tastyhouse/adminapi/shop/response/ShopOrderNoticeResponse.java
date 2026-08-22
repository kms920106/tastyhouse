package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자 주문안내 조회 응답.
 *
 * <p>점주용({@code ceoapi.shop.response.ShopOrderNoticeResponse})과 필드 구성이 같지만, 소비자가
 * 다른 응답이라 각 모듈이 자기 사본을 소유한다(admin/web Result 충돌 시 통합 금지 원칙과 동일한
 * 취지 — 화면 계약이 갈릴 여지를 남긴다).
 */
@Schema(description = "관리자 주문안내 응답")
public record ShopOrderNoticeResponse(
    @Schema(description = "주문안내 본문 (미설정이면 null)", example = "포장 주문은 매장에서 10분 정도 소요됩니다.")
    String content,

    @Schema(description = "관리자 게시중단 여부", example = "false")
    boolean hidden,

    @Schema(description = "게시중단 사유 (게시중이면 null)", example = "외부 결제 유도 문구가 포함되어 있습니다.")
    String hiddenReason
) {
    public static ShopOrderNoticeResponse of(String content, boolean hidden, String hiddenReason) {
        return new ShopOrderNoticeResponse(content, hidden, hiddenReason);
    }

    /**
     * 아직 주문안내를 등록하지 않은 가게의 응답. 게시중단은 등록된 문구에만 걸리므로 항상 게시중이다.
     */
    public static ShopOrderNoticeResponse empty() {
        return new ShopOrderNoticeResponse(null, false, null);
    }
}
