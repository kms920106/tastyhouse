package com.tastyhouse.webapplication.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.application.product.port.out.ProductNutritionResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.webapplication.product.response.ProductNutritionResponse;
import com.tastyhouse.webapplication.product.port.in.ProductNutritionQueryUseCase;

/**
 * 손님용 메뉴 영양성분·알레르기 조회 서비스(CQRS query 측).
 *
 * <p>미입력이면 {@code null}을 돌려준다({@code data: null}) — 화면은 그때 "영양성분 보기"를 노출하지
 * 않는다.
 *
 * <p><b>알레르기 코드를 한글 라벨로 바꾸는 것이 이 서비스의 몫이다.</b> DAO는 코드를 투영하고
 * ({@code ProductQueryPort#findAllergenTypes}) 점주 응답은 그 코드를 그대로 쓰므로, 라벨 변환을 DAO에
 * 넣으면 점주 경로가 라벨을 다시 코드로 되돌려야 한다. 변환은 {@link AllergenType}의 한글 설명을
 * 그대로 쓴다 — 라벨 목록을 이 모듈에 복제하면 성분이 추가될 때 두 곳이 갈린다.
 */
@Service
@Transactional(readOnly = true)
public class ProductNutritionQueryService implements ProductNutritionQueryUseCase {

    private final ProductQueryPort productQueryPort;

    public ProductNutritionQueryService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public ProductNutritionResponse getNutrition(Long productId) {
        return productQueryPort.findNutrition(productId)
            .map(dto -> toProductNutritionResponse(dto, toAllergenLabels(productQueryPort.findAllergenTypes(productId))))
            .orElse(null);
    }

    private List<String> toAllergenLabels(List<String> allergenCodes) {
        return allergenCodes.stream()
            .map(code -> AllergenType.from(code).getDescription())
            .toList();
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
