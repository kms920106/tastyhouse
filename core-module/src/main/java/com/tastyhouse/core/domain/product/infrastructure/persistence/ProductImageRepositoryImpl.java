package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductImage;
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
            .where(productImage.productId.eq(productId), productImage.isActive.eq(true))
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
    public ProductImage save(ProductImage entity) {
        return productImageJpaRepository.save(entity);
    }
}
