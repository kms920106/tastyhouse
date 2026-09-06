package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.application.product.port.out.ProductVegetarianStatusResult;

@Schema(description = "메뉴 채식 설정 현황")
public record ProductVegetarianStatusResponse(
    @Schema(description = "현재 반영된 채식 단계. 채식 메뉴가 아니면 null", example = "VEGAN",
        allowableValues = {"VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"})
    String vegetarianType,

    @Schema(description = "채식 설정 요청 목록(최근 순)")
    List<ProductVegetarianRequestResponse> requests,

    @Schema(description = "채식 설정 변경(신청)이 가능한지 여부. 가게 카테고리가 채식 금지 목록에 해당하면 false",
        example = "true")
    boolean changeable
) {
    public static ProductVegetarianStatusResponse from(ProductVegetarianStatusResult result) {
        VegetarianType vegetarianType = result.vegetarianType();
        return new ProductVegetarianStatusResponse(
            vegetarianType == null ? null : vegetarianType.name(),
            result.requests().stream().map(ProductVegetarianRequestResponse::from).toList(),
            result.changeable()
        );
    }
}
