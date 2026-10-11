package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;

public interface ShopAmenitySavePort {

    ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory);

    ShopAmenity saveAmenity(ShopAmenity amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);
}
