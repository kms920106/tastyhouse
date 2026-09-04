package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

@Schema(description = "일괄 처리 실패 항목")
public record ProductAvailabilityFailureResponse(
    @Schema(description = "실패한 대상 ID", example = "101")
    Long id,

    @Schema(description = "실패한 대상의 이름(메뉴명 또는 옵션명). 화면에 그대로 노출한다.", example = "치즈추가")
    String name,

    @Schema(description = "실패 사유 코드", example = "PRODUCT_OPTION_MIN_SELECT_VIOLATION")
    String errorCode,

    @Schema(description = "사용자 노출 문구", example = "최소 선택 개수만큼의 옵션은 판매 중이어야 합니다.")
    String message
) {

    public static ProductAvailabilityFailureResponse from(ProductAvailabilityChangeView.Failure failure) {
        ErrorCode errorCode = failure.errorCode();
        return new ProductAvailabilityFailureResponse(
            failure.id(),
            failure.name(),
            errorCode.getCode(),
            errorCode.getDefaultMessage()
        );
    }
}
