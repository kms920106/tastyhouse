package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 이 가게 <b>메뉴판에 노출되는</b> 메뉴 수 — {@code PRODUCT_SHOP_LINK}를 통해 센다.
     *
     * <p>"메뉴판에는 최소 1개 메뉴가 노출되어야 한다"는 규칙의 근거다. 메뉴-가게 연결(N:M) 도입으로
     * 이 판정은 <b>가게 메뉴판 단위</b>여야 한다 — {@code PRODUCT.shop_id}로 세면 다른 가게에서
     * 불러온 메뉴가 빠지고, 반대로 이 가게 메뉴판에 없는 원본 메뉴가 잘못 포함된다.
     *
     * <p>{@code distinct}가 필요한 이유는 링크가 메뉴당 가게마다 1건이라 같은 가게 안에서는 중복이
     * 생기지 않지만, 조인 형태가 바뀌어도 개수가 부풀지 않도록 방어하기 위해서다.
     */
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
