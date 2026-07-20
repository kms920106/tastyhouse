package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepresentativeImage;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public List<ProductImage> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productImageJpaEntity)
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductImageMapper::toDomain)
            .toList();
    }

    @Override
    public String findFilePathByImageFileId(Long imageFileId) {
        return queryFactory
            .select(uploadedFile.filePath)
            .from(uploadedFile)
            .where(uploadedFile.id.eq(imageFileId))
            .fetchOne();
    }

    @Override
    public List<ProductRepresentativeImage> findRepresentativeImagePathsByProductIds(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");
        return queryFactory
            .select(Projections.constructor(
                ProductRepresentativeImage.class,
                productImageJpaEntity.productId,
                uploadedFile.filePath
            ))
            .from(productImageJpaEntity)
            .innerJoin(uploadedFile).on(productImageJpaEntity.imageFileId.eq(uploadedFile.id))
            .where(
                productImageJpaEntity.productId.in(productIds),
                productImageJpaEntity.visible.eq(true),
                productImageJpaEntity.sort.eq(
                    JPAExpressions
                        .select(subProductImage.sort.min())
                        .from(subProductImage)
                        .where(subProductImage.productId.eq(productImageJpaEntity.productId)
                            .and(subProductImage.visible.eq(true)))
                )
            )
            .fetch();
    }

    @Override
    public ProductImage save(ProductImage entity) {
        ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(entity));
        return ProductImageMapper.toDomain(saved);
    }
}
