package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.repository.ProductNutritionRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

@Repository
public class ProductNutritionRepositoryImpl implements ProductNutritionRepository {

    private final ProductNutritionJpaRepository productNutritionJpaRepository;

    public ProductNutritionRepositoryImpl(ProductNutritionJpaRepository productNutritionJpaRepository) {
        this.productNutritionJpaRepository = productNutritionJpaRepository;
    }

    @Override
    public Optional<ProductNutrition> findByProductId(ProductId productId) {
        return productNutritionJpaRepository.findByProductId(IdMapping.raw(productId, ProductId::value))
            .map(ProductNutritionMapper::toDomain);
    }

    @Override
    public ProductNutrition save(ProductNutrition productNutrition) {
        if (productNutrition.getId() == null) {
            ProductNutritionJpaEntity saved =
                productNutritionJpaRepository.save(ProductNutritionMapper.toEntity(productNutrition));
            return ProductNutritionMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ProductNutritionJpaEntity entity = productNutritionJpaRepository.findById(productNutrition.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 영양성분입니다: " + productNutrition.getId()));
        ProductNutritionMapper.applyChanges(entity, productNutrition);
        return ProductNutritionMapper.toDomain(entity);
    }

    @Override
    public void delete(ProductNutrition productNutrition) {
        productNutritionJpaRepository.deleteById(productNutrition.getId());
    }
}
