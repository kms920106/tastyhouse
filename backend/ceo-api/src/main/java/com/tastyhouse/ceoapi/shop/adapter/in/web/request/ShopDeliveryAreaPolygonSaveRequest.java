package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.shop.application.port.in.GeoPointCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaPolygonSaveCommand;

/**
 * 배달지역 도형 저장 요청(전체 교체).
 *
 * <p>{@code rings}는 <b>링의 배열</b>이다 — 링이 여럿이면 떨어진 두 구역이나 구멍(배달 제외 구역)을
 * 표현한다. 어느 쪽인지는 클라이언트가 표시하지 않고 서버가 위상(even-odd)으로 판정한다.
 *
 * <p>링 개수·정점 개수 상한은 여기서 형식으로 한 번, 도메인 정책({@code ShopDeliveryAreaPolicy})에서 한 번
 * 검증한다. 중복 검증이 아니라 <b>층이 다르다</b> — Bean Validation은 요청 형식을, 도메인은 저장 가능한
 * 도형인지를 본다(도메인 서비스는 HTTP 경계 밖에서도 호출될 수 있다).
 */
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
        List<List<GeoPointCommand>> ringCommands = rings().stream()
            .map(ring -> ring.stream().map(GeoPointRequest::toCommand).toList())
            .toList();
        return new ShopDeliveryAreaPolygonSaveCommand(ceoId, shopId, ringCommands);
    }
}
