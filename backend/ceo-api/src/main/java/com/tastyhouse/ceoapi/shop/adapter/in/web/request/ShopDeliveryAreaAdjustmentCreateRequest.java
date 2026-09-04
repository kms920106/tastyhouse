package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentCreateCommand;

/**
 * 배달지역 조정 신청 접수 요청.
 *
 * <p>동의서 파일({@code file})은 이 record가 아니라 컨트롤러가 {@code MultipartFile} 파라미터로 별도
 * 수신한다 — {@code multipart/form-data}의 텍스트 파트만 여기서 검증·문서화한다.
 */
@Schema(description = "배달지역 조정 신청 요청")
public record ShopDeliveryAreaAdjustmentCreateRequest(
    @NotBlank(message = "상대 가맹점 상호명은 필수입니다.")
    @Size(max = 255, message = "상대 가맹점 상호명은 255자를 초과할 수 없습니다.")
    @Schema(description = "상대 가맹점 상호명", example = "맛있는집 강남점", requiredMode = Schema.RequiredMode.REQUIRED)
    String counterpartShopName,

    @NotBlank(message = "상대 가맹점 사업자등록번호는 필수입니다.")
    @Pattern(regexp = "\\d{10}", message = "사업자등록번호는 하이픈을 제외한 숫자 10자리여야 합니다.")
    @Schema(description = "상대 가맹점 사업자등록번호(하이픈 제외 10자리)", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    String counterpartBusinessNumber,

    @NotBlank(message = "가맹본부명은 필수입니다.")
    @Size(max = 255, message = "가맹본부명은 255자를 초과할 수 없습니다.")
    @Schema(description = "가맹본부명", example = "맛있는집 본사", requiredMode = Schema.RequiredMode.REQUIRED)
    String franchiseName,

    @NotBlank(message = "배달지역 중첩 사유는 필수입니다.")
    @Size(max = 1000, message = "배달지역 중첩 사유는 1000자를 초과할 수 없습니다.")
    @Schema(description = "배달지역 중첩 사유", example = "역삼1동 전역이 중첩되어 주문이 분산됩니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public ShopDeliveryAreaAdjustmentCreateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaAdjustmentCreateCommand(
            ceoId,
            shopId,
            counterpartShopName(),
            counterpartBusinessNumber(),
            franchiseName(),
            reason()
        );
    }
}
