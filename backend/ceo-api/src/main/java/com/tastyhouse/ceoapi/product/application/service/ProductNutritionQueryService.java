package com.tastyhouse.ceoapi.product.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAllergenTypeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductNutritionResponse;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.infrastructure.product.query.ProductNutritionResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;

/**
 * 점주용 메뉴 영양성분·알레르기 조회 서비스(CQRS query 측).
 *
 * <p>미입력 메뉴는 {@code null}을 돌려준다(응답 {@code data: null}) — 원산지와 달리 빈 기본값을 만들지
 * 않는 이유는, 영양성분은 필드가 14개나 되어 "빈 폼용 기본값"이 곧 전부 null인 객체이고, 화면이
 * {@code data == null}로 미입력을 판정하는 편이 단순하기 때문이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductNutritionQueryService {

    private final ProductQueryDao productQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductNutritionQueryService(ProductQueryDao productQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.productQueryDao = productQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ProductNutritionResponse getNutrition(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateProductOwnedByShop(shopId, productId);

        return productQueryDao.findNutrition(productId)
            .map(dto -> toProductNutritionResponse(dto, productQueryDao.findAllergenTypes(productId)))
            .orElse(null);
    }

    /**
     * 알레르기 유발성분 코드 목록을 법령 열거 순서(enum 선언 순서)대로 돌려준다.
     */
    public List<ProductAllergenTypeResponse> getAllergenTypes() {
        return Arrays.stream(AllergenType.values())
            .map(allergenType -> ProductAllergenTypeResponse.from(allergenType.name(), allergenType.getDescription()))
            .toList();
    }

    /**
     * 대상 메뉴가 그 가게 메뉴판에 걸려 있는지 대조한다 — 가게 소유권만 검증하면 남의 가게 메뉴 id를
     * 실어 보내는 경로가 열린다. 미존재와 타 가게 메뉴는 같은 코드로 묶는다.
     *
     * <p>메뉴-가게 연결(N:M) 도입으로 <b>동등 비교가 아니라 포함 관계</b>로 판정한다 — 한 메뉴가 여러
     * 가게에 걸리므로, 원본 가게만 인정하면 연결된 가게의 점주가 자기 메뉴판의 메뉴를 열지 못한다.
     */
    private void validateProductOwnedByShop(Long shopId, Long productId) {
        boolean owned = productQueryDao.existsProductInShop(productId, shopId);
        if (!owned) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private ProductNutritionResponse toProductNutritionResponse(ProductNutritionResult dto, List<String> allergens) {
        return ProductNutritionResponse.from(
            dto.servingSize(),
            dto.totalAmount(),
            dto.flavor(),
            dto.size(),
            dto.calorie(),
            dto.sugars(),
            dto.protein(),
            dto.saturatedFat(),
            dto.natrium(),
            dto.carbohydrate(),
            dto.cholesterol(),
            dto.fat(),
            dto.transFat(),
            dto.caffeine(),
            dto.setMenu(),
            allergens
        );
    }
}
