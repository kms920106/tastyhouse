package com.tastyhouse.infrastructure.product.persistence;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.domain.model.ProductImage;
import com.tastyhouse.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.domain.product.domain.vo.ProductId;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;

/**
 * 상품 이미지 write 어댑터. 화면 갤러리용 이미지 목록 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductImageJpaRepository productImageJpaRepository;

    public ProductImageRepositoryImpl(JPAQueryFactory queryFactory, ProductImageJpaRepository productImageJpaRepository) {
        this.queryFactory = queryFactory;
        this.productImageJpaRepository = productImageJpaRepository;
    }

    @Override
    public String findRepresentativeImageFilePath(ProductId productId) {
        return queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(productImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(imageFileId().eq(uploadedFileJpaEntity.id))
            .where(productImageJpaEntity.productId.eq(productId), productImageJpaEntity.visible.eq(true))
            .orderBy(productImageJpaEntity.sort.asc())
            .fetchFirst();
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code PRODUCT_IMAGE.image_file_id}를 raw {@code Long}으로
     * 비교하기 위한 path.
     */
    private NumberPath<Long> imageFileId() {
        return Expressions.numberPath(Long.class, productImageJpaEntity, "imageFileId");
    }

    @Override
    public ProductImage save(ProductImage entity) {
        ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(entity));
        return ProductImageMapper.toDomain(saved);
    }
}
