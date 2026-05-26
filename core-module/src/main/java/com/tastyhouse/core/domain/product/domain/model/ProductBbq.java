package com.tastyhouse.core.domain.product.domain.model;

import com.tastyhouse.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Product와 BBQ 메뉴 ID 매핑 엔티티 — BBQ API 외부 메뉴 ID 임시 저장 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT_BBQ")
public class ProductBbq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "bbq_menu_id", nullable = false)
    private Long bbqMenuId;

    @Column(name = "bbq_category_id")
    private Long bbqCategoryId;

    @Column(name = "is_options_synced", nullable = false)
    private Boolean isOptionsSynced;

    private ProductBbq(Long productId, Long bbqMenuId, Long bbqCategoryId, Boolean isOptionsSynced) {
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.isOptionsSynced = isOptionsSynced != null ? isOptionsSynced : false;
    }

    public static ProductBbq of(Long productId, Long bbqMenuId, Long bbqCategoryId, Boolean isOptionsSynced) {
        return new ProductBbq(productId, bbqMenuId, bbqCategoryId, isOptionsSynced);
    }

    public void markOptionsSynced() {
        this.isOptionsSynced = true;
    }
}
