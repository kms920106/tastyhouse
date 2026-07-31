package com.tastyhouse.infrastructure.product.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.domain.model.ProductImage;
import com.tastyhouse.domain.product.domain.repository.ProductImageRepository;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

/**
 * 상품 이미지 write 어댑터. 화면 갤러리용 이미지 목록 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public String findRepresentativeImageFilePath(Long productId) {
        return queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetchFirst();
    }

    @Override
    public ProductImage save(ProductImage entity) {
        ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(entity));
        return ProductImageMapper.toDomain(saved);
    }
}
