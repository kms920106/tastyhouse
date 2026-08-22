package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPriceJpaRepository extends JpaRepository<ProductPriceJpaEntity, Long> {

    List<ProductPriceJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);

    /**
     * 가게의 삭제되지 않은 메뉴에 속한 가격 행 전체를 조회한다. 가격 행이 {@code shop_id}를 직접 들고
     * 있지 않으므로 {@code PRODUCT}로 조인해 소유 가게와 소프트 삭제를 함께 판정한다.
     */
    @Query("""
        select pp
        from ProductPriceJpaEntity pp, ProductJpaEntity p
        where pp.productId = p.id
          and p.shopId = :shopId
          and p.deleted = false
        order by p.sort asc, pp.sort asc
        """)
    List<ProductPriceJpaEntity> findAllByShopId(@Param("shopId") Long shopId);
}
