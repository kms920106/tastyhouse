package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemDto;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ShopRepository {

    List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    Page<BestShopItemDto> findBestShops(Pageable pageable);

    Page<LatestShopItemDto> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Pageable pageable);

    Page<ShopBookmarkedItemDto> findMyBookmarkedShops(Long memberId, Pageable pageable);

    Page<ShopBookmarkedItemDto> searchByKeywordWithBookmark(String keyword, Long memberId, Pageable pageable);
}
