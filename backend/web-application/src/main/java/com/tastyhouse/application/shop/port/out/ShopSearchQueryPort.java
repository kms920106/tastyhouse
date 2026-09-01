package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.FoodType;

/**
 * 가게 탐색 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>지도·베스트·최신순·키워드검색·북마크처럼 회원이 가게를 찾는 조회를 담당한다. 전부 배달권역과
 * 북마크 여부를 함께 투영한다는 점에서 관리 목록과 성격이 다르다. 관리 목록 조회는
 * {@link ShopSearchManagementQueryPort}가 소유한다 — <b>공유 메서드는 0개다.</b>
 */
public interface ShopSearchQueryPort {

    List<ShopMapMarkerResult> findNearbyShops(BigDecimal latitude, BigDecimal longitude);

    PageResult<BestShopItemResult> findBestShops(Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<LatestShopItemResult> findLatestShops(Long stationId, List<FoodType> foodTypes, List<Amenity> amenities, Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> searchByKeywordWithBookmark(String keyword, Long memberId, Long deliveryAdminDongId, PageQuery pageQuery);

    PageResult<ShopBookmarkedItemResult> findMyBookmarkedShops(Long memberId, PageQuery pageQuery);
}
