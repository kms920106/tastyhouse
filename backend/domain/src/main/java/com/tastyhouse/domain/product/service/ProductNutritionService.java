package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.repository.ProductAllergenRepository;
import com.tastyhouse.domain.product.repository.ProductNutritionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 메뉴 영양성분·알레르기 유발성분의 조회·upsert·삭제(도메인 서비스).
 *
 * <p><b>영양성분과 알레르기를 한 서비스가 소유하는 이유</b>는 두 값이 한 화면에서 함께 저장되고 함께
 * 지워지는 한 벌이기 때문이다. 나누면 "영양성분만 저장되고 알레르기는 이전 값이 남은" 중간 상태가
 * 생기는데, 그 상태는 손님 화면에 <b>잘못된 알레르기 표시</b>로 노출된다 — 알레르기 오표시의 대가는
 * 다른 필드의 드리프트와 비교할 수 없다.
 *
 * <p><b>알레르기 목록은 replace-all로 교체한다.</b> 점주 화면이 체크박스 묶음을 통째로 저장하므로 행
 * 단위 추가·삭제 경로를 열지 않고, 기존 행을 전부 지운 뒤 새 목록을 넣는다. 빈 목록을 보내면 알레르기
 * 표시가 비워진다.
 *
 * <p><b>승인 워크플로가 없다.</b> 이미지·채식과 달리 영양성분은 점주(가맹본사)만이 아는 사실 정보여서
 * 관리자가 검증할 근거가 없고, 정확성 책임도 가게 측에 있다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 소비 모듈의 command
 * 서비스가 선언한다.
 */
public class ProductNutritionService {

    private final ProductNutritionRepository productNutritionRepository;
    private final ProductAllergenRepository productAllergenRepository;
    private final ProductRepository productRepository;

    public ProductNutritionService(
        ProductNutritionRepository productNutritionRepository,
        ProductAllergenRepository productAllergenRepository,
        ProductRepository productRepository
    ) {
        this.productNutritionRepository = productNutritionRepository;
        this.productAllergenRepository = productAllergenRepository;
        this.productRepository = productRepository;
    }

    /**
     * 영양성분과 알레르기 목록을 함께 upsert 한다(PUT 전체 교체).
     *
     * <p>필수 5종의 "함께 채우거나 함께 비우기"·음수 금지 불변식은 애그리거트
     * ({@link ProductNutrition})가 소유하므로 여기서 다시 검증하지 않는다.
     */
    public void upsertNutrition(
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
        List<AllergenType> allergenTypes
    ) {
        validateProductExists(productId);

        ProductNutrition existing = productNutritionRepository.findByProductId(productId).orElse(null);
        ProductNutrition productNutrition;
        if (existing == null) {
            productNutrition = ProductNutrition.of(productId, servingSize, totalAmount, flavor, size,
                calorie, sugars, protein, saturatedFat, natrium,
                carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
        } else {
            existing.update(servingSize, totalAmount, flavor, size,
                calorie, sugars, protein, saturatedFat, natrium,
                carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
            productNutrition = existing;
        }

        productNutritionRepository.save(productNutrition);
        replaceAllergens(productId, allergenTypes);
    }

    /**
     * 영양성분과 알레르기 목록을 함께 지운다.
     *
     * <p>소프트 삭제가 아니라 행을 지운다 — 과거 주문이 참조하지 않는 부가 정보이고, 주문 시점의
     * 영양성분을 보존해야 할 요구가 없다(주문 스냅샷에 담기지 않는다).
     *
     * <p>없는 정보를 지우려 하면 {@code PRODUCT_NUTRITION_NOT_FOUND}(404)로 거부한다. 알레르기 목록은
     * 영양성분 행에 종속되므로 <b>영양성분이 존재할 때만</b> 함께 지워진다.
     */
    public void deleteNutrition(ProductId productId) {
        ProductNutrition productNutrition = productNutritionRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NUTRITION_NOT_FOUND));

        productAllergenRepository.deleteAllByProductId(productId);
        productNutritionRepository.delete(productNutrition);
    }

    /**
     * 알레르기 목록을 통째로 교체한다. 기존 행을 먼저 지우므로
     * {@code UNIQUE(product_id, allergen_type)} 충돌이 발생하지 않는다.
     *
     * <p>중복 입력은 거절하지 않고 <b>distinct로 정규화</b>한다 — 같은 성분을 두 번 체크한 것은 점주의
     * 실수라기보다 화면 상태의 잡음이고, 저장 결과("우유 1건")는 어느 쪽이든 같기 때문이다.
     */
    private void replaceAllergens(ProductId productId, List<AllergenType> allergenTypes) {
        productAllergenRepository.deleteAllByProductId(productId);

        if (allergenTypes == null || allergenTypes.isEmpty()) {
            return;
        }

        List<ProductAllergen> allergens = allergenTypes.stream()
            .distinct()
            .map(allergenType -> ProductAllergen.of(productId, allergenType))
            .toList();
        productAllergenRepository.saveAll(allergens);
    }

    private void validateProductExists(ProductId productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
