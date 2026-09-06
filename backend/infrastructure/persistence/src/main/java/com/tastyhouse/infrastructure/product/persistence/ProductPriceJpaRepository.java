package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPriceJpaRepository extends JpaRepository<ProductPriceJpaEntity, Long> {
    List<ProductPriceJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);

    @Query("""
        select pp
        from ProductPriceJpaEntity pp, ProductJpaEntity p, ProductShopLinkJpaEntity l
        where pp.productId = p.id
          and l.productId = p.id
          and l.shopId = :shopId
          and p.deleted = false
        order by l.sort asc, pp.sort asc
        """)
    List<ProductPriceJpaEntity> findAllByShopId(@Param("shopId") Long shopId);
}
