package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAllergenJpaRepository extends JpaRepository<ProductAllergenJpaEntity, Long> {
    List<ProductAllergenJpaEntity> findAllByProductId(Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductAllergenJpaEntity a where a.productId = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
