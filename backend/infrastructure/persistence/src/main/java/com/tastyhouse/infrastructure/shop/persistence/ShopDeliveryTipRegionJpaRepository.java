package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopDeliveryTipRegionJpaRepository extends JpaRepository<ShopDeliveryTipRegionJpaEntity, Long> {

    List<ShopDeliveryTipRegionJpaEntity> findByShopId(Long shopId);

    long countByShopId(Long shopId);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    /**
     * replace-all 교체의 선행 삭제. 같은 행정동을 그대로 유지하며 팁만 바꾸는 교체에서
     * {@code uk_shop_delivery_tip_region}(shop_id + admin_dong_id) 중복을 피하려면
     * insert보다 먼저 DB에 반영돼야 한다 — 상세 근거는
     * {@link ShopDeliveryTipTierJpaRepository#deleteByShopId(Long)} 참고.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShopDeliveryTipRegionJpaEntity r where r.shopId = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}
