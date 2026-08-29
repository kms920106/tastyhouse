package com.tastyhouse.webapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.product.adapter.in.web.response.ProductNutritionResponse;
import com.tastyhouse.webapi.product.application.port.in.ProductNutritionQueryUseCase;

/**
 * 손님용 메뉴 영양성분·알레르기 조회 API.
 *
 * <p>대형 {@code ProductApiController}에 얹지 않고 별도 컨트롤러로 둔다 — 영양성분은 "영양성분 보기"를
 * 눌렀을 때만 조회되는 지연 로딩 대상이라 메뉴 상세와 수명이 다르고, 그 화면 하나만이 이 응답을 쓴다
 * ({@code ShopOrderNoticeApiController}가 같은 판단을 따른다).
 *
 * <p><b>인증이 필요하지 않다.</b> {@code /api/products/**}가 이미 {@code PublicPaths}에 등록돼 있어 이
 * 경로도 비로그인으로 열린다 — 알레르기 표시는 로그인 여부와 무관하게 보여야 하는 안전 정보다.
 */
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
        ProductNutritionResponse response = productNutritionQueryService.getNutrition(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
