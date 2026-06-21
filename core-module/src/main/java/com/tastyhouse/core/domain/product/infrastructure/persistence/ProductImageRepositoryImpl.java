package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import com.tastyhouse.core.domain.product.domain.model.QProductImage;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public List<ProductImage> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productImage)
            .where(productImage.productId.eq(productId), productImage.isVisible.eq(true))
            .orderBy(productImage.sort.asc())
            .fetch();
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
        QProductImage subProductImage = new QProductImage("subProductImage");
        return queryFactory
            .select(Projections.constructor(
                ProductRepresentativeImage.class,
                productImage.productId,
                uploadedFile.filePath
            ))
            .from(productImage)
            .innerJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(
                productImage.productId.in(productIds),
                productImage.isVisible.eq(true),
                productImage.sort.eq(
                    JPAExpressions
                        .select(subProductImage.sort.min())
                        .from(subProductImage)
                        .where(subProductImage.productId.eq(productImage.productId)
                            .and(subProductImage.isVisible.eq(true)))
                )
            )
            .fetch();
    }

    @Override
    public ProductImage save(ProductImage entity) {
        return productImageJpaRepository.save(entity);
    }
}
