package com.tastyhouse.ceoapi.shop.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 주문안내 등록·수정 요청.
 *
 * <p>등록과 수정이 하나의 record인 이유는 엔드포인트가 하나이기 때문이다 — 주문안내는 가게당 1건이라
 * {@code PUT}이 전체교체(upsert) 의미론을 갖는다.
 *
 * <p>Bean Validation은 프론트에 필드 단위 오류를 빠르게 돌려주기 위한 1차 방어일 뿐이고, 같은 규칙을
 * {@code ShopOrderNoticeService}가 도메인 예외({@code SHOP_ORDER_NOTICE_CONTENT_*})로 다시 지킨다 —
 * presentation 계약은 다른 진입 경로가 추가되면 우회되므로 유일한 방어선일 수 없다.
 */
@Schema(description = "주문안내 등록·수정 요청")
public record ShopOrderNoticeUpsertRequest(
    @NotBlank(message = "주문안내 내용을 입력해 주세요.")
    @Size(max = 500, message = "주문안내는 500자 이내로 입력해 주세요.")
    @Schema(description = "주문안내 본문 (1~500자)", example = "포장 주문은 매장에서 10분 정도 소요됩니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String content
) {
}
