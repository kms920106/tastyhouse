package com.tastyhouse.core.domain.shop.domain.repository;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemDto;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopRepository {

    List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    PageResult<BestShopItemDto> findBestShops(PageQuery pageQuery);

    PageResult<LatestShopItemDto> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemDto> findMyBookmarkedShops(Long memberId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, PageQuery pageQuery);
}
