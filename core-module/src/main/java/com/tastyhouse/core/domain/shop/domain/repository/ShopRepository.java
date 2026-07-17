package com.tastyhouse.core.domain.shop.domain.repository;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopRepository {

    List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    PageResult<BestShopItemResult> findBestShops(PageQuery pageQuery);

    PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(MemberId memberId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, MemberId memberId, PageQuery pageQuery);
}
