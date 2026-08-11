package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopDeliveryTipHolidayJpaRepository extends JpaRepository<ShopDeliveryTipHolidayJpaEntity, Long> {

    Optional<ShopDeliveryTipHolidayJpaEntity> findByShopId(Long shopId);

    /**
     * 삭제 후 재등록 교체의 선행 삭제. {@code uk_shop_delivery_tip_holiday_shop_id}가
     * 가게당 1건을 강제하므로 insert보다 먼저 DB에 반영돼야 한다 — 상세 근거는
     * {@link ShopDeliveryTipTierJpaRepository#deleteByShopId(Long)} 참고.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ShopDeliveryTipHolidayJpaEntity h where h.shopId = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}
