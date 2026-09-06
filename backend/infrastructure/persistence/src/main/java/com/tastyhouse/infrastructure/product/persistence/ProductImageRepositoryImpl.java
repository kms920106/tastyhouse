package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

@Repository
public class ProductImageRepositoryImpl implements ProductImageRepository {
    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    public ProductImageRepositoryImpl(JPAQueryFactory queryFactory, ProductImageJpaRepository productImageJpaRepository) {
        this.queryFactory = queryFactory;
        this.productImageJpaRepository = productImageJpaRepository;
    }

    @Override
    public UploadedFileId findRepresentativeImageFileId(ProductId productId) {
        Long imageFileId = queryFactory
            .select(productImageJpaEntity.imageFileId)
            .from(productImageJpaEntity)
            .where(productImageJpaEntity.productId.eq(productId.value()), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetchFirst();

        return IdMapping.vo(imageFileId, UploadedFileId::of);
    }

    @Override
    public ProductImage save(ProductImage entity) {
        if (entity.getId() == null) {
            ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(entity));
            return ProductImageMapper.toDomain(saved);
        }

        ProductImageJpaEntity managed = productImageJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 이미지입니다: " + entity.getId()));
        ProductImageMapper.applyChanges(managed, entity);
        return ProductImageMapper.toDomain(managed);
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return productImageJpaRepository.findById(id).map(ProductImageMapper::toDomain);
    }

    @Override
    public List<ProductImage> findAllByProductId(ProductId productId) {
        return productImageJpaRepository.findAllByProductIdOrderBySortAsc(productId.value()).stream()
            .map(ProductImageMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ProductImage productImage) {
        productImageJpaRepository.deleteById(productImage.getId());
    }
}
