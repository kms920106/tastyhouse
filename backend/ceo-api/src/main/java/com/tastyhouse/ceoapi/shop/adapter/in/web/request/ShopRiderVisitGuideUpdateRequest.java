package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderVisitGuideUpdateCommand;

/**
 * 라이더 가게방문 안내 문구 등록·수정 요청.
 *
 * <p>빈 문자열을 허용한다 — 삭제 전용 엔드포인트를 따로 두지 않고 "빈 값 PUT = 삭제"로 통일하기 때문이다.
 * 금칙어·실주소·배차 어휘 같은 도메인 규칙은 여기가 아니라 도메인이 판정한다(관리자 경로에서도 같은
 * 게이트가 적용되어야 하므로).
 */
@Schema(description = "라이더 가게방문 안내 문구 등록 요청")
public record ShopRiderVisitGuideUpdateRequest(
    @NotNull(message = "라이더 가게방문 안내 문구는 필수 필드입니다(빈 문자열은 삭제로 처리됩니다).")
    @Size(max = 200, message = "라이더 가게방문 안내는 최대 200자까지 입력할 수 있습니다.")
    @Schema(description = "라이더 가게방문 안내 문구 (최대 200자, 빈 문자열이면 삭제)",
        example = "OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String visitGuide
) {

    public ShopRiderVisitGuideUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopRiderVisitGuideUpdateCommand(ceoId, shopId, visitGuide());
    }
}
