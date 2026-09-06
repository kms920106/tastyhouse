package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.FoodType;

public interface ShopSearchQueryPort {

    List<ShopMapMarkerResult> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    PageResult<BestShopItemResult> findBestShops(Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, Long memberId, Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(Long memberId, PageQuery pageQuery);
}
