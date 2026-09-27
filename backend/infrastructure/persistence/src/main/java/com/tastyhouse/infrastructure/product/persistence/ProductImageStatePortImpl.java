package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductImageState;
import com.tastyhouse.application.product.port.out.write.ProductImageStatePort;

import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

@Repository
public class ProductImageStatePortImpl implements ProductImageStatePort {
    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    public ProductImageStatePortImpl(JPAQueryFactory queryFactory, ProductImageJpaRepository productImageJpaRepository) {
        this.queryFactory = queryFactory;
        this.productImageJpaRepository = productImageJpaRepository;
    }

    @Override
    public Long findRepresentativeImageFileId(Long productId) {
        return queryFactory
            .select(productImageJpaEntity.imageFileId)
            .from(productImageJpaEntity)
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetchFirst();
    }

    @Override
    public ProductImageState save(ProductImageState state) {
        if (state.id() == null) {
            ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(state));
            return ProductImageMapper.toState(saved);
        }

        ProductImageJpaEntity managed = productImageJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 이미지입니다: " + state.id()));
        ProductImageMapper.applyChanges(managed, state);
        return ProductImageMapper.toState(managed);
    }

    @Override
    public Optional<ProductImageState> findById(Long id) {
        return productImageJpaRepository.findById(id).map(ProductImageMapper::toState);
    }

    @Override
    public List<ProductImageState> findAllByProductId(Long productId) {
        return productImageJpaRepository.findAllByProductIdOrderBySortAsc(productId).stream()
            .map(ProductImageMapper::toState)
            .toList();
    }

    @Override
    public void deleteById(Long id) {
        productImageJpaRepository.deleteById(id);
    }
}
