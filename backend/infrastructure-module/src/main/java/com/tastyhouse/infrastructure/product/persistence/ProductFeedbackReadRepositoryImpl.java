package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.product.repository.ProductFeedbackReadRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주 의견 확인 시각 write 어댑터.
 */
@Repository
public class ProductFeedbackReadRepositoryImpl implements ProductFeedbackReadRepository {

    private final ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository;

    public ProductFeedbackReadRepositoryImpl(
        ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository
    ) {
        this.productFeedbackReadJpaRepository = productFeedbackReadJpaRepository;
    }

    @Override
    public ProductFeedbackRead save(ProductFeedbackRead feedbackRead) {
        if (feedbackRead.getId() == null) {
            ProductFeedbackReadJpaEntity saved = productFeedbackReadJpaRepository
                .save(ProductFeedbackReadMapper.toEntity(feedbackRead));
            return ProductFeedbackReadMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush.
        // detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ProductFeedbackReadJpaEntity entity = productFeedbackReadJpaRepository
            .findById(feedbackRead.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 고객 의견 확인 이력입니다: " + feedbackRead.getId()));
        ProductFeedbackReadMapper.applyChanges(entity, feedbackRead);
        return ProductFeedbackReadMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductFeedbackRead> findByShopId(ShopId shopId) {
        return productFeedbackReadJpaRepository.findByShopId(shopId.value())
            .map(ProductFeedbackReadMapper::toDomain);
    }
}
