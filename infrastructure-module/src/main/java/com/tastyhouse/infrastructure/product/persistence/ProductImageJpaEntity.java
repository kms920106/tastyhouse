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

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상품 이미지 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductImage}와 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductImageMapper}가 수행한다. 도메인 쪽에 update 행위가 없어 {@code applyChanges}는
 * 두지 않는다(insert 전용).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT_IMAGE")
public class ProductImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    private ProductImageJpaEntity(Long productId, Long imageFileId, Integer sort, boolean visible) {
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductImageMapper#toEntity}에서만 호출한다.
     */
    static ProductImageJpaEntity create(Long productId, Long imageFileId, Integer sort, boolean visible) {
        return new ProductImageJpaEntity(productId, imageFileId, sort, visible);
    }
}
