package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달지역 도형 저장 command(전체 교체).
 *
 * <p>링 목록의 좌표는 {@link GeoPointCommand}로 담아 위경도 뒤바뀜을 구조적으로 막는다(챕터 02 §5).
 * 도형 형식 위반({@code SHOP_DELIVERY_AREA_POLYGON_INVALID})의 판정은 기존대로 서비스 내부의
 * 기하 매퍼가 담당하므로, 여기서는 구조적 null만 막는다.
 */
public record ShopDeliveryAreaPolygonSaveCommand(
    Long ceoId,
    Long shopId,
    List<List<GeoPointCommand>> rings
) {
    public ShopDeliveryAreaPolygonSaveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
