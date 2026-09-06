package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 메뉴 알레르기 유발성분 순수 도메인 모델(메뉴당 N건).
 *
 * <p>성분 하나가 한 행이며, 같은 메뉴에 같은 성분이 두 번 들어가지 않도록 DB가
 * {@code UNIQUE(product_id, allergen_type)}로 막는다.
 *
 * <p><b>전이 메서드가 없다.</b> 알레르기 성분 목록은 점주 화면에서 체크박스 묶음을 통째로 저장하는
 * replace-all이라, 행 하나를 수정하는 경로가 존재하지 않는다({@code ProductNutritionService}가 기존
 * 행을 지우고 새로 넣는다). 전이 메서드가 없는 것이 "현재는 변경 경로가 없다"의 구조적 표현이라는
 * 이 저장소의 원칙과 일관된다.
 */
public class ProductAllergen {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final AllergenType allergenType;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ProductAllergen(
        Long id,
        ProductId productId,
        AllergenType allergenType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.allergenType = allergenType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 알레르기 성분을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ProductAllergen of(ProductId productId, AllergenType allergenType) {
        return new ProductAllergen(null, productId, allergenType, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ProductAllergen reconstitute(
        Long id,
        ProductId productId,
        AllergenType allergenType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductAllergen(id, productId, allergenType, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public AllergenType getAllergenType() {
        return this.allergenType;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
