package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.ShopRiderVisitGuideDeleteCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 부적합 라이더 안내 문구 삭제 조치 요청.
 *
 * <p>사유를 쿼리 파라미터가 아니라 바디로 받는 이유: 한글 사유가 URL에 그대로 로깅되는 것을 피하기 위함이다.
 */
@Schema(description = "라이더 안내 문구 삭제 조치 요청")
public record ShopRiderVisitGuideDeleteRequest(
    @NotBlank(message = "삭제 조치 사유는 필수입니다.")
    @Size(max = 200, message = "삭제 조치 사유는 최대 200자까지 입력할 수 있습니다.")
    @Schema(description = "삭제 조치 사유 (최대 200자)", example = "가게 방문과 관련 없는 문구입니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopRiderVisitGuideDeleteCommand toCommand(Long shopId, Long adminId) {
        return new ShopRiderVisitGuideDeleteCommand(shopId, adminId, reason);
    }
}
