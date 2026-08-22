package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 영양성분 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 *
 * <p>수치 14개가 전부 {@code Integer}라 <b>위치 기반 전달에서 순서를 착각하면 컴파일은 통과하고 값만
 * 조용히 뒤바뀐다.</b> 이 파일을 고칠 때는 도메인 getter 순서·엔티티 파라미터 순서·아래 호출 인자 순서를
 * 반드시 하나씩 대조한다.
 */
final class ProductNutritionMapper {

    private ProductNutritionMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductNutrition toDomain(ProductNutritionJpaEntity entity) {
        return ProductNutrition.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getServingSize(),
            entity.getTotalAmount(),
            entity.getFlavor(),
            entity.getSize(),
            entity.getCalorie(),
            entity.getSugars(),
            entity.getProtein(),
            entity.getSaturatedFat(),
            entity.getNatrium(),
            entity.getCarbohydrate(),
            entity.getCholesterol(),
            entity.getFat(),
            entity.getTransFat(),
            entity.getCaffeine(),
            entity.isSetMenu(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductNutritionJpaEntity toEntity(ProductNutrition domain) {
        return ProductNutritionJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getServingSize(),
            domain.getTotalAmount(),
            domain.getFlavor(),
            domain.getSize(),
            domain.getCalorie(),
            domain.getSugars(),
            domain.getProtein(),
            domain.getSaturatedFat(),
            domain.getNatrium(),
            domain.getCarbohydrate(),
            domain.getCholesterol(),
            domain.getFat(),
            domain.getTransFat(),
            domain.getCaffeine(),
            domain.isSetMenu()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductNutritionJpaEntity entity, ProductNutrition domain) {
        entity.applyChanges(
            domain.getServingSize(),
            domain.getTotalAmount(),
            domain.getFlavor(),
            domain.getSize(),
            domain.getCalorie(),
            domain.getSugars(),
            domain.getProtein(),
            domain.getSaturatedFat(),
            domain.getNatrium(),
            domain.getCarbohydrate(),
            domain.getCholesterol(),
            domain.getFat(),
            domain.getTransFat(),
            domain.getCaffeine(),
            domain.isSetMenu()
        );
    }
}
