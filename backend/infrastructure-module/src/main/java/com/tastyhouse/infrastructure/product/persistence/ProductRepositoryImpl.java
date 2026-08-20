package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 상품 write 어댑터. 표현 목적 조회는 {@code infrastructure/product/query/ProductQueryDao}가 담당한다.
 *
 * <p><b>{@code deleted} 필터를 거는 곳과 걸지 않는 곳이 갈린다.</b> 일반 로드({@link #findById})에
 * 필터를 걸어 두면 <b>신규 주문·신규 메뉴평가 차단이 자동으로 성립</b>한다. 반면 삭제 자신은
 * 필터 없는 순수 PK 조회({@link #findByIdIncludingDeleted})로 대상을 읽어야 한다 —
 * {@code findById}를 재사용하면 삭제가 영원히 실패한다({@code RankPeriodRepositoryImpl#delete} 선례).
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findByIdAndDeletedFalse(id.value()).map(ProductMapper::toDomain);
    }

    /**
     * 삭제 대상을 <b>필터 없이</b> 로드한다. {@link #findById}를 재사용하면 이미 삭제된 행을 다시
     * 읽지 못해 멱등 처리와 상태 확인이 불가능해진다.
     */
    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return productJpaRepository.findById(id.value()).map(ProductMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ProductJpaEntity saved = productJpaRepository.save(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        }

        // 필터 없는 순수 PK 조회여야 한다 — 삭제 전이를 저장하는 경로가 바로 이곳이기 때문이다.
        ProductJpaEntity entity = productJpaRepository.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품입니다: " + product.getId()));
        ProductMapper.applyChanges(entity, product);
        return ProductMapper.toDomain(entity);
    }

    @Override
    public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = ids.stream().map(ProductId::value).toList();
        return productJpaRepository.findAllByShopIdAndIdInAndDeletedFalse(shopId.value(), rawIds).stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        return productJpaRepository.countByShopIdAndVisibleTrueAndDeletedFalse(shopId.value());
    }

    @Override
    public long countVisibleRepresentativeByShopId(ShopId shopId) {
        return productJpaRepository
            .countByShopIdAndVisibleTrueAndRepresentativeTrueAndDeletedFalse(shopId.value());
    }

    @Override
    public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqualAndDeletedFalse(baseTime)
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByShopIdAndName(ShopId shopId, String name) {
        return productJpaRepository.existsByShopIdAndNameAndDeletedFalse(shopId.value(), name);
    }

    @Override
    public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        return productJpaRepository
            .existsByShopIdAndNameAndIdNotAndDeletedFalse(shopId.value(), name, excludedId.value());
    }

    /**
     * {@code productCategoryId}가 {@code null}이면 <b>미분류 메뉴</b>가 대상이다. 파생 쿼리의
     * {@code ...CategoryIdIsNull...}과 {@code ...CategoryId...}를 갈라 쓰는 이유는, 하나로 합치면
     * null이 "조건 없음"으로 해석돼 가게의 모든 메뉴가 대상이 되기 때문이다.
     */
    @Override
    public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
        List<ProductJpaEntity> entities = productCategoryId == null
            ? productJpaRepository
                .findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc(shopId.value())
            : productJpaRepository.findAllByShopIdAndProductCategoryIdAndDeletedFalseOrderBySortAsc(
                shopId.value(), productCategoryId.value());
        return entities.stream().map(ProductMapper::toDomain).toList();
    }

    @Override
    public long countByCategoryId(ProductCategoryId productCategoryId) {
        return productJpaRepository.countByProductCategoryIdAndDeletedFalse(productCategoryId.value());
    }
}
