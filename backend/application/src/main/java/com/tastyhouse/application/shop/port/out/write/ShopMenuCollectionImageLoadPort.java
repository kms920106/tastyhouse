package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

public interface ShopMenuCollectionImageLoadPort {

    Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id);

    List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId);
}
