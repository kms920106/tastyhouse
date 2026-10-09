package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaSavePort {

    ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea);

    List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas);

    void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source);

    void deleteById(Long deliveryAreaId);
}
