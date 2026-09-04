package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductNutritionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductNutritionService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 메뉴 영양성분·알레르기 변경 서비스(CQRS command 측).
 *
 * <p>필수 5종의 집합 제약·음수 금지와 알레르기 replace-all은 도메인
 * ({@link ProductNutritionService})이 소유하고, 여기서는 소유권 검증과 트랜잭션 경계, 알레르기 코드
 * 문자열의 enum 승격만 책임진다.
 *
 * <p><b>승인 워크플로를 거치지 않는다.</b> 이미지·채식과 달리 영양성분은 점주(가맹본사)만이 아는 사실
 * 정보여서 관리자가 검증할 근거가 없고, 정확성 책임도 가게 측에 있다.
 */
@Service
@CeoApp
@Transactional
public class ProductNutritionCommandService implements ProductNutritionCommandUseCase {

    private final ProductNutritionService productNutritionService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionCommandService(
        ProductNutritionService productNutritionService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productNutritionService = productNutritionService;
        this.productRepository = productRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateNutrition(ProductNutritionUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();
        String servingSize = command.servingSize();
        String totalAmount = command.totalAmount();
        String flavor = command.flavor();
        String size = command.size();
        Integer calorie = command.calorie();
        Integer sugars = command.sugars();
        Integer protein = command.protein();
        Integer saturatedFat = command.saturatedFat();
        Integer natrium = command.natrium();
        Integer carbohydrate = command.carbohydrate();
        Integer cholesterol = command.cholesterol();
        Integer fat = command.fat();
        Integer transFat = command.transFat();
        Integer caffeine = command.caffeine();
        Boolean setMenu = command.setMenu();
        List<String> allergens = command.allergens();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        productNutritionService.upsertNutrition(
            ProductId.of(productId),
            servingSize,
            totalAmount,
            flavor,
            size,
            calorie,
            sugars,
            protein,
            saturatedFat,
            natrium,
            carbohydrate,
            cholesterol,
            fat,
            transFat,
            caffeine,
            Boolean.TRUE.equals(setMenu),
            toAllergenTypes(allergens)
        );
    }

    @Override
    public void deleteNutrition(ProductNutritionDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long productId = command.productId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        productNutritionService.deleteNutrition(ProductId.of(productId));
    }

    /**
     * 대상 메뉴를 로드하면서 소유 가게까지 대조한다 — 가게 소유권만 검증하면 남의 가게 메뉴 id를 실어
     * 보내는 경로가 열린다. 미존재와 타 가게 소유는 같은 코드로 묶는다.
     */
    private void validateProductOwnedByShop(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    /**
     * 알레르기 코드 문자열을 enum으로 승격한다. enum 밖의 코드는
     * {@code PRODUCT_ALLERGEN_TYPE_UNKNOWN}(400)으로 거절된다({@link AllergenType#from}).
     */
    private List<AllergenType> toAllergenTypes(List<String> allergens) {
        if (allergens == null) {
            return List.of();
        }
        return allergens.stream().map(AllergenType::from).toList();
    }
}
