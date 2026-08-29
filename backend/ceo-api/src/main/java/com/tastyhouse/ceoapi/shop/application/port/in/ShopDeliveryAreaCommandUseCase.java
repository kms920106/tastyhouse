package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkDeleteResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkResponse;

/**
 * 점주 가게 배달가능지역(행정동 직접 등록·반경·도형) 쓰기 인바운드 포트.
 *
 * <p>일괄 처리 반환 타입은 기존 계약 그대로 Response record를 유지한다(챕터 02는 wire 계약을 바꾸지
 * 않는다).
 */
public interface ShopDeliveryAreaCommandUseCase {

    Long addDeliveryArea(ShopDeliveryAreaCreateCommand command);

    void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command);

    ShopDeliveryAreaBulkResponse addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command);

    ShopDeliveryAreaBulkDeleteResponse removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command);

    ShopDeliveryAreaBulkResponse applyRadius(ShopDeliveryAreaRadiusApplyCommand command);

    void savePolygon(ShopDeliveryAreaPolygonSaveCommand command);

    void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command);
}
