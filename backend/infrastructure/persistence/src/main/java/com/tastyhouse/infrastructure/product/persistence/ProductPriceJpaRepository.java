package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPriceJpaRepository extends JpaRepository<ProductPriceJpaEntity, Long> {

    List<ProductPriceJpaEntity> findAllByProductIdOrderBySortAsc(Long productId);

    /**
     * 가게의 삭제되지 않은 메뉴에 속한 가격 행 전체를 조회한다. 가격 행이 {@code shop_id}를 직접 들고
     * 있지 않으므로 {@code PRODUCT_SHOP_LINK}로 조인해 노출 가게와 소프트 삭제를 함께 판정한다.
     * 메뉴-가게 연결(N:M) 도입으로 {@code PRODUCT.shop_id}가 아니라 링크를 쓰며, 이 조건은
     * {@code ProductQueryDao#findShopProductPrices}와 반드시 같아야 한다(갈리면 뱃지가 두 화면에서 달라진다).
     */
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
