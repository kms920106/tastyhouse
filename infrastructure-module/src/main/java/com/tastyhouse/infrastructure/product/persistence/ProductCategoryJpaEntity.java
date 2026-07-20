package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 상품 카테고리 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductCategory}와 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductCategoryMapper}가 수행한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    private ProductCategoryJpaEntity(Long shopId, String name, Integer sort, boolean visible) {
        this.shopId = shopId;
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductCategoryMapper#toEntity}에서만 호출한다.
     */
    static ProductCategoryJpaEntity create(Long shopId, String name, Integer sort, boolean visible) {
        return new ProductCategoryJpaEntity(shopId, name, sort, visible);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). shopId는 건드리지 않는다.
     */
    void applyChanges(String name, Integer sort, boolean visible) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }
}
