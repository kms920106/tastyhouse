package com.tastyhouse.webapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.product.port.in.ProductNutritionQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductNutritionView;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductNutritionResponse;

@Tag(name = "Product Nutrition", description = "메뉴 영양성분·알레르기 API")
@RestController
@RequestMapping("/api/products")
public class ProductNutritionApiController {
    private final ProductNutritionQueryUseCase productNutritionQueryService;

    public ProductNutritionApiController(ProductNutritionQueryUseCase productNutritionQueryService) {
        this.productNutritionQueryService = productNutritionQueryService;
    }

    @Operation(summary = "메뉴 영양성분·알레르기 조회",
        description = "미입력이면 data가 null입니다. allergens는 코드가 아니라 한글 라벨 배열로 내려가므로 "
            + "화면이 코드→라벨 매핑표를 들 필요가 없습니다.")
    @GetMapping("/v1/{id}/nutrition")
    public ResponseEntity<ApiResponse<ProductNutritionResponse>> getNutrition(@PathVariable Long id) {
        ProductNutritionView view = productNutritionQueryService.getNutrition(id);
        ProductNutritionResponse response = view == null ? null : ProductNutritionResponse.from(view);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
