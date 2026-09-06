package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    Optional<ProductJpaEntity> findByIdAndDeletedFalse(Long id);

    List<ProductJpaEntity> findAllByShopIdAndIdInAndDeletedFalse(Long shopId, List<Long> ids);

    @Query("""
        select count(distinct p.id)
        from ProductJpaEntity p, ProductShopLinkJpaEntity l
        where l.productId = p.id
          and l.shopId = :shopId
          and p.visible = true
          and p.deleted = false
        """)
    long countVisibleByShopLink(@Param("shopId") Long shopId);

    long countByShopIdAndVisibleTrueAndRepresentativeTrueAndDeletedFalse(Long shopId);

    long countByShopIdAndRepresentativeTrueAndDeletedFalse(Long shopId);

    List<ProductJpaEntity> findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqualAndDeletedFalse(
        LocalDateTime baseTime
    );

    boolean existsByShopIdAndNameAndDeletedFalse(Long shopId, String name);

    boolean existsByShopIdAndNameAndIdNotAndDeletedFalse(Long shopId, String name, Long excludedId);

    List<ProductJpaEntity> findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc(Long shopId);

    List<ProductJpaEntity> findAllByShopIdAndProductCategoryIdAndDeletedFalseOrderBySortAsc(
        Long shopId,
        Long productCategoryId
    );

    long countByProductCategoryIdAndDeletedFalse(Long productCategoryId);
}
