package com.tastyhouse.infrastructure.product.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

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
        ProductImageJpaEntity saved = productImageJpaRepository.save(ProductImageMapper.toEntity(entity));
        return ProductImageMapper.toDomain(saved);
    }
}
