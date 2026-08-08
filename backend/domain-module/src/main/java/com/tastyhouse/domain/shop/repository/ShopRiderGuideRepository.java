package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 라이더 안내 write 포트.
 *
 * <p>관리자 목록 조회·이력 목록 조회는 표현 목적이므로 이 포트가 아니라 infrastructure query DAO
 * ({@code ShopRiderGuideQueryDao})가 소유한다.
 */
public interface ShopRiderGuideRepository {

    Optional<ShopRiderGuide> findByShopId(ShopId shopId);

    ShopRiderGuide save(ShopRiderGuide riderGuide);

    ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history);
}
