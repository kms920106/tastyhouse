package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tastyhouse.domain.shop.model.DeliveryAreaSource;

public interface ShopDeliveryAreaJpaRepository extends JpaRepository<ShopDeliveryAreaJpaEntity, Long> {

    List<ShopDeliveryAreaJpaEntity> findByShopIdOrderByIdAsc(Long shopId);

    List<ShopDeliveryAreaJpaEntity> findByShopIdAndSource(Long shopId, DeliveryAreaSource source);

    boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    long countByShopId(Long shopId);

    /**
     * 특정 출처의 행을 일괄 삭제한다.
     *
     * <p>파생 삭제(derived {@code deleteBy...})는 대상을 <b>먼저 조회한 뒤 건별로 삭제</b>하므로 수백 건에서
     * 쿼리가 그만큼 늘어난다. 폴리곤 재저장은 매번 이 삭제로 시작하므로 벌크 DELETE 한 방으로 처리한다.
     *
     * <p>{@code clearAutomatically}로 영속성 컨텍스트를 비우는 이유: 벌크 연산은 1차 캐시를 우회하므로,
     * 같은 트랜잭션에서 이어지는 재삽입이 이미 삭제된 엔티티를 캐시에서 보고 유니크 제약을 오판할 수 있다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ShopDeliveryAreaJpaEntity e WHERE e.shopId = :shopId AND e.source = :source")
    void deleteByShopIdAndSource(@Param("shopId") Long shopId, @Param("source") DeliveryAreaSource source);

    /** 가게에 등록된 행정동 식별자만 투영한다 — 중복 판정에 행 전체가 필요하지 않다. */
    @Query("SELECT e.adminDongId FROM ShopDeliveryAreaJpaEntity e WHERE e.shopId = :shopId ORDER BY e.id ASC")
    List<Long> findAdminDongIdsByShopId(@Param("shopId") Long shopId);
}
