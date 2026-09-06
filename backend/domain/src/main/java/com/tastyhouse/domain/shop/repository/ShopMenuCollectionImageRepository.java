package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

public interface ShopMenuCollectionImageRepository {
    ShopMenuCollectionImage save(ShopMenuCollectionImage image);

    Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id);

    List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId);

    void delete(ShopMenuCollectionImage image);
}
