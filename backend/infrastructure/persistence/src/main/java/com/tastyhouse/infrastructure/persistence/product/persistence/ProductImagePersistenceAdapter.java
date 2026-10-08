package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

@Repository
class ProductImagePersistenceAdapter implements ProductImagePersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    public ProductImagePersistenceAdapter(JPAQueryFactory queryFactory, ProductImageJpaRepository productImageJpaRepository) {
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
        return imageFileId == null ? null : UploadedFileId.of(imageFileId);
    }

    @Override
    public ProductImage save(ProductImage productImage) {
        if (productImage.getId() == null) {
            ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(productImage));
            return ProductImageMapper.toDomain(saved);
        }

        ProductImageJpaEntity managed = productImageJpaRepository.findById(productImage.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 이미지입니다: " + productImage.getId()));
        ProductImageMapper.applyChanges(managed, productImage);
        return ProductImageMapper.toDomain(managed);
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return productImageJpaRepository.findById(id).map(ProductImageMapper::toDomain);
    }

    @Override
    public List<ProductImage> findAllByProductId(ProductId productId) {
        return queryFactory
            .selectFrom(productImageJpaEntity)
            .where(productImageJpaEntity.productId.eq(productId.value()))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductImageMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ProductImage productImage) {
        productImageJpaRepository.deleteById(productImage.getId());
    }
}
