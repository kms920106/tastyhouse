package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.ceoapplication.shop.port.out.ShopDeliveryAreaBulkDeleteResult;
import com.tastyhouse.ceoapplication.shop.port.out.ShopDeliveryAreaBulkResult;

/**
 * 점주 가게 배달가능지역(행정동 직접 등록·반경·도형) 쓰기 인바운드 포트.
 *
 * <p>일괄 처리 반환 타입은 챕터 09로 Response에서 프레임워크-프리 Result로 바뀌었다. 응답 JSON은 불변이며
 * 조립만 컨트롤러로 올라갔다(챕터 02는 wire 계약을 바꾸지
 * 않는다).
 */
public interface ShopDeliveryAreaCommandUseCase {

    Long addDeliveryArea(ShopDeliveryAreaCreateCommand command);

    void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command);

    ShopDeliveryAreaBulkResult addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command);

    ShopDeliveryAreaBulkDeleteResult removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command);

    ShopDeliveryAreaBulkResult applyRadius(ShopDeliveryAreaRadiusApplyCommand command);

    void savePolygon(ShopDeliveryAreaPolygonSaveCommand command);

    void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command);
}
