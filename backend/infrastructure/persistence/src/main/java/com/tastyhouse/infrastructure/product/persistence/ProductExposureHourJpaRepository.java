package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductExposureHourJpaRepository extends JpaRepository<ProductExposureHourJpaEntity, Long> {

    List<ProductExposureHourJpaEntity> findAllByProductId(Long productId);

    /**
     * replace-all 교체의 선행 삭제. bulk delete로 즉시 DB에 반영해야 뒤이은 insert가
     * {@code uk_product_exposure_hour}(product_id + day_type)를 위반하지 않는다.
     *
     * <p>derived {@code deleteAllByProductId}는 영속성 컨텍스트에 delete action만 큐잉하는데,
     * Hibernate의 기본 flush 순서는 action을 타입별로 묶어 <b>insert를 delete보다 먼저</b> 실행하므로
     * 같은 {@code day_type}을 재사용하는 교체에서 항상 중복 키로 실패한다.
     * {@code clearAutomatically}는 삭제된 행이 1차 캐시에 남아 뒤이은 조회를 오염시키지 않도록 한다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductExposureHourJpaEntity h where h.productId = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
