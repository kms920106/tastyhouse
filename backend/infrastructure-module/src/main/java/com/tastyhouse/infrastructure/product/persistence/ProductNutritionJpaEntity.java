package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 영양성분 JPA 영속 모델. 순수 도메인 모델 {@code ProductNutrition}과 분리된 영속 전용 엔티티다.
 *
 * <p><b>14개 수치를 {@code @Embedded} record로 묶지 않고 평면 필드로 둔다.</b> 전부 {@code Integer}라
 * record 컴포넌트 선언 순서가 어긋나면 값이 조용히 뒤바뀌는데({@code EmbeddedRecordComponentOrderTest}가
 * 잡는 그 사고), 평면 필드는 {@code @Column}이 이름으로 매핑하므로 그 위험이 구조적으로 없다.
 */
@Entity
@Table(name = "PRODUCT_NUTRITION")
public class ProductNutritionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "serving_size", length = 50)
    private String servingSize; // 1회 제공량

    @Column(name = "total_amount", length = 50)
    private String totalAmount; // 총 제공량

    @Column(name = "flavor", length = 50)
    private String flavor; // 맛

    @Column(name = "size", length = 50)
    private String size; // 사이즈

    @Column(name = "calorie")
    private Integer calorie; // 열량 kcal (필수 5종)

    @Column(name = "sugars")
    private Integer sugars; // 당류 g (필수 5종)

    @Column(name = "protein")
    private Integer protein; // 단백질 g (필수 5종)

    @Column(name = "saturated_fat")
    private Integer saturatedFat; // 포화지방 g (필수 5종)

    @Column(name = "natrium")
    private Integer natrium; // 나트륨 mg (필수 5종)

    @Column(name = "carbohydrate")
    private Integer carbohydrate; // 탄수화물 g (선택)

    @Column(name = "cholesterol")
    private Integer cholesterol; // 콜레스테롤 mg (선택)

    @Column(name = "fat")
    private Integer fat; // 지방 g (선택)

    @Column(name = "trans_fat")
    private Integer transFat; // 트랜스지방 g (선택)

    @Column(name = "caffeine")
    private Integer caffeine; // 카페인 mg (선택)

    @Column(name = "is_set_menu", nullable = false)
    private boolean setMenu; // 세트 메뉴 여부 (안내문구 노출 조건)

    protected ProductNutritionJpaEntity() {
    }

    private ProductNutritionJpaEntity(
        Long productId,
        String servingSize,
        String totalAmount,
        String flavor,
        String size,
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine,
        boolean setMenu
    ) {
        this.productId = productId;
        this.servingSize = servingSize;
        this.totalAmount = totalAmount;
        this.flavor = flavor;
        this.size = size;
        this.calorie = calorie;
        this.sugars = sugars;
        this.protein = protein;
        this.saturatedFat = saturatedFat;
        this.natrium = natrium;
        this.carbohydrate = carbohydrate;
        this.cholesterol = cholesterol;
        this.fat = fat;
        this.transFat = transFat;
        this.caffeine = caffeine;
        this.setMenu = setMenu;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductNutritionMapper#toEntity}에서만 호출한다.
     */
    static ProductNutritionJpaEntity create(
        Long productId,
        String servingSize,
        String totalAmount,
        String flavor,
        String size,
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine,
        boolean setMenu
    ) {
        return new ProductNutritionJpaEntity(productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·productId는 건드리지 않는다.
     */
    void applyChanges(
        String servingSize,
        String totalAmount,
        String flavor,
        String size,
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine,
        boolean setMenu
    ) {
        this.servingSize = servingSize;
        this.totalAmount = totalAmount;
        this.flavor = flavor;
        this.size = size;
        this.calorie = calorie;
        this.sugars = sugars;
        this.protein = protein;
        this.saturatedFat = saturatedFat;
        this.natrium = natrium;
        this.carbohydrate = carbohydrate;
        this.cholesterol = cholesterol;
        this.fat = fat;
        this.transFat = transFat;
        this.caffeine = caffeine;
        this.setMenu = setMenu;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getServingSize() {
        return this.servingSize;
    }

    public String getTotalAmount() {
        return this.totalAmount;
    }

    public String getFlavor() {
        return this.flavor;
    }

    public String getSize() {
        return this.size;
    }

    public Integer getCalorie() {
        return this.calorie;
    }

    public Integer getSugars() {
        return this.sugars;
    }

    public Integer getProtein() {
        return this.protein;
    }

    public Integer getSaturatedFat() {
        return this.saturatedFat;
    }

    public Integer getNatrium() {
        return this.natrium;
    }

    public Integer getCarbohydrate() {
        return this.carbohydrate;
    }

    public Integer getCholesterol() {
        return this.cholesterol;
    }

    public Integer getFat() {
        return this.fat;
    }

    public Integer getTransFat() {
        return this.transFat;
    }

    public Integer getCaffeine() {
        return this.caffeine;
    }

    public boolean isSetMenu() {
        return this.setMenu;
    }
}
