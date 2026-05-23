package com.tastyhouse.core.entity.product;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Product와 BBQ 메뉴 ID 매핑 엔티티
 * BBQ API의 외부 메뉴 ID를 임시 저장
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT_BBQ")
public class ProductBbq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "bbq_menu_id", nullable = false)
    private Long bbqMenuId; // BBQ API 외부 메뉴 ID

    @Column(name = "bbq_category_id")
    private Long bbqCategoryId; // BBQ API 외부 카테고리 ID

    @Column(name = "is_options_synced", nullable = false)
    private Boolean isOptionsSynced; // 옵션 동기화 완료 여부 (true: 동기화 완료)

    private ProductBbq(
        Long productId,
        Long bbqMenuId,
        Long bbqCategoryId,
        Boolean isOptionsSynced
    ) {
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.isOptionsSynced = isOptionsSynced != null ? isOptionsSynced : false;
    }

    public static ProductBbq of(
        Long productId,
        Long bbqMenuId,
        Long bbqCategoryId,
        Boolean isOptionsSynced
    ) {
        return new ProductBbq(
            productId,
            bbqMenuId,
            bbqCategoryId,
            isOptionsSynced
        );
    }

    /**
     * 옵션 동기화 완료 표시
     */
    public void markOptionsSynced() {
        this.isOptionsSynced = true;
    }
}
