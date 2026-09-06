package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.shop.port.in.GeoPointCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveCommand;

@Schema(description = "배달지역 도형 저장 요청")
public record ShopDeliveryAreaPolygonSaveRequest(
    @NotEmpty(message = "도형의 링 목록은 비어 있을 수 없습니다.")
    @Size(max = 20, message = "링은 최대 20개까지 가능합니다.")
    @Schema(description = "도형을 이루는 링 목록(각 링은 좌표 3개 이상)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@NotEmpty(message = "링의 좌표 목록은 비어 있을 수 없습니다.")
        @Size(min = 3, max = 5000, message = "각 링은 좌표가 3개 이상 5000개 이하여야 합니다.")
        List<@Valid GeoPointRequest>> rings
) {
    public ShopDeliveryAreaPolygonSaveCommand toCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaPolygonSaveCommand(ceoId, shopId, toRingCommands());
    }

    public List<List<GeoPointCommand>> toRingCommands() {
        return rings().stream()
            .map(ring -> ring.stream().map(GeoPointRequest::toCommand).toList())
            .toList();
    }
}
