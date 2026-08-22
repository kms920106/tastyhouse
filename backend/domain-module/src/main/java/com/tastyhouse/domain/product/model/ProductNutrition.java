package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 메뉴 영양성분 순수 도메인 모델(메뉴당 1건, upsert).
 *
 * <p><b>{@code Product} 안에 넣지 않고 분리한 이유</b>는 (1) 수치가 14개나 붙어 이미 큰 애그리거트를
 * 더 키우고, (2) 대부분의 메뉴가 입력하지 않아 전부 NULL로 남으며, (3) 손님이 "영양성분 보기"를 눌러야
 * 조회되는 지연 로딩 대상이기 때문이다. 반면 중량({@code Product#weightText})은 문자열 한 칸이고 메뉴
 * 목록에서도 쓰이므로 {@code Product}에 직접 뒀다.
 *
 * <p><b>필수 5종(열량·당류·단백질·포화지방·나트륨)은 전부 채우거나 전부 비운다.</b> 일부만 채운
 * 영양성분 표시는 법적으로 의미가 없고 오히려 오표시가 되므로, 이 모델이 그 집합 불변식을 소유한다.
 * DB 제약으로는 "5개 모두 NULL 또는 5개 모두 NOT NULL"을 표현할 수 없어 도메인이 판정한다.
 *
 * <p>필수 5종의 필드명·타입은 BBQ 크롤러의 {@code Nutrient} DTO와 일치시켰다 — 나중에 외부 연동으로
 * 채울 때 매핑 없이 그대로 옮기려는 것이다.
 *
 * <p><b>{@code @Embedded} record로 묶지 않은 이유</b>는 14개 수치가 전부 {@code Integer}라, 컴포넌트
 * 선언 순서가 어긋나면 값이 조용히 뒤바뀌기 때문이다({@code EmbeddedRecordComponentOrderTest}가
 * 강제하는 그 사고). 평면 필드로 두어 생성자 파라미터명이 곧 컬럼 매핑이 되게 한다.
 *
 * <p>승인 워크플로가 없다. 이미지·채식과 달리 영양성분은 점주(가맹본사)만이 아는 사실 정보여서
 * 관리자가 검증할 근거가 없고, 정확성 책임도 가게 측에 있다.
 */
public class ProductNutrition {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private String servingSize; // 1회 제공량 (예: 100g)
    private String totalAmount; // 총 제공량
    private String flavor; // 맛
    private String size; // 사이즈
    private Integer calorie; // 열량 kcal (필수 5종)
    private Integer sugars; // 당류 g (필수 5종)
    private Integer protein; // 단백질 g (필수 5종)
    private Integer saturatedFat; // 포화지방 g (필수 5종)
    private Integer natrium; // 나트륨 mg (필수 5종)
    private Integer carbohydrate; // 탄수화물 g (선택)
    private Integer cholesterol; // 콜레스테롤 mg (선택)
    private Integer fat; // 지방 g (선택)
    private Integer transFat; // 트랜스지방 g (선택)
    private Integer caffeine; // 카페인 mg (선택)
    private boolean setMenu; // 세트 메뉴 여부 (손님 화면 안내문구 노출 조건)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ProductNutrition(
        Long id,
        ProductId productId,
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
        boolean setMenu,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 영양성분을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ProductNutrition of(
        ProductId productId,
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
        validate(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);

        return new ProductNutrition(null, productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ProductNutrition reconstitute(
        Long id,
        ProductId productId,
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
        boolean setMenu,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductNutrition(id, productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu, createdAt, updatedAt);
    }

    /**
     * 영양성분을 전체 교체한다(PUT 시맨틱). 부분 수정 개념이 없으므로 넘어오지 않은 항목은 비워진다.
     */
    public void update(
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
        validate(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);

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
     * 필수 5종의 "함께 채우거나 함께 비우기" 제약과 전 수치의 음수 금지를 판정한다.
     *
     * <p>음수 검사를 필수 5종에만 걸지 않고 선택 9종까지 함께 거는 이유는, 선택 항목이라도 <b>표시되는
     * 값</b>이라 음수면 그대로 오표시이기 때문이다.
     */
    private static void validate(
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine
    ) {
        validateRequiredTogether(calorie, sugars, protein, saturatedFat, natrium);
        validateNonNegative(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);
    }

    /**
     * 필수 5종은 전부 채워졌거나 전부 비어 있어야 한다 — 하나라도 채웠으면 나머지 4개도 필수다.
     */
    private static void validateRequiredTogether(Integer... requiredValues) {
        long filled = Arrays.stream(requiredValues).filter(Objects::nonNull).count();
        if (filled != 0 && filled != requiredValues.length) {
            throw new BusinessException(ErrorCode.PRODUCT_NUTRITION_REQUIRED_FIELD_MISSING);
        }
    }

    private static void validateNonNegative(Integer... values) {
        for (Integer value : values) {
            if (value != null && value < 0) {
                throw new BusinessException(ErrorCode.PRODUCT_NUTRITION_VALUE_NEGATIVE);
            }
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
