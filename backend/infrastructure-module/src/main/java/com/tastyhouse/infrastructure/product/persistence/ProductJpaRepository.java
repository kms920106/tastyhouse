package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 Spring Data 리포지토리.
 *
 * <p>대부분의 파생 쿼리에 {@code AndDeletedFalse}가 붙어 있다 — 소프트 삭제된 메뉴가 일반 경로에
 * 다시 나타나지 않게 한다. <b>상속받은 {@code findById}에는 그 필터가 없으므로</b> 일반 로드에는
 * {@link #findByIdAndDeletedFalse}를 쓰고, 삭제·저장 경로만 필터 없는 {@code findById}를 쓴다.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    Optional<ProductJpaEntity> findByIdAndDeletedFalse(Long id);

    List<ProductJpaEntity> findAllByShopIdAndIdInAndDeletedFalse(Long shopId, List<Long> ids);

    long countByShopIdAndVisibleTrueAndDeletedFalse(Long shopId);

    long countByShopIdAndVisibleTrueAndRepresentativeTrueAndDeletedFalse(Long shopId);

    List<ProductJpaEntity> findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqualAndDeletedFalse(
        LocalDateTime baseTime
    );

    boolean existsByShopIdAndNameAndDeletedFalse(Long shopId, String name);

    boolean existsByShopIdAndNameAndIdNotAndDeletedFalse(Long shopId, String name, Long excludedId);

    /**
     * 미분류 메뉴 목록. {@code productCategoryId}에 {@code null}을 넘기는 대신 <b>별도 메서드</b>로
     * 두는 이유는, null 파라미터가 "조건 없음"으로 해석돼 가게의 모든 메뉴가 대상이 되는 것을 막기 위함이다.
     */
    List<ProductJpaEntity> findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc(Long shopId);

    List<ProductJpaEntity> findAllByShopIdAndProductCategoryIdAndDeletedFalseOrderBySortAsc(
        Long shopId,
        Long productCategoryId
    );

    long countByProductCategoryIdAndDeletedFalse(Long productCategoryId);
}
