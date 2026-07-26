package com.tastyhouse.core.domain.shop.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.ShopSearchCondition;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBookmarkedItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopRepository {

    List<Shop> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    PageResult<BestShopItemResult> findBestShops(PageQuery pageQuery);

    PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(MemberId memberId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, MemberId memberId, PageQuery pageQuery);

    PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery);

    Optional<Shop> findById(ShopId id);

    /**
     * 회원 노출용 단건 조회. 폐업(permanentlyClosed)·노출정지(hidden) 가게는 조회되지 않아,
     * 딥링크로 비노출 가게 상세에 진입하는 것을 차단한다. admin/ceo는 {@link #findById(ShopId)}를 쓴다.
     */
    Optional<Shop> findVisibleById(ShopId id);

    Shop save(Shop shop);
}
