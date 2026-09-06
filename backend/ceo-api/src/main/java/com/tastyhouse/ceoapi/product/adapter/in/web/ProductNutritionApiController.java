package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductNutritionUpdateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAllergenTypeResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductNutritionResponse;
import com.tastyhouse.application.product.port.in.ProductNutritionCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductNutritionDeleteCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductNutritionOwnerQueryUseCase;

@Tag(name = "Ceo Product Nutrition", description = "점주 메뉴 영양성분·알레르기 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductNutritionApiController {
    private final ProductNutritionOwnerQueryUseCase productNutritionQueryService;
    private final ProductNutritionCommandUseCase productNutritionCommandUseCase;

    public ProductNutritionApiController(ProductNutritionOwnerQueryUseCase productNutritionQueryService, ProductNutritionCommandUseCase productNutritionCommandUseCase) {
        this.productNutritionQueryService = productNutritionQueryService;
        this.productNutritionCommandUseCase = productNutritionCommandUseCase;
    }

    @Operation(summary = "알레르기 유발성분 코드 목록",
        description = "점주 화면의 체크박스 목록을 서버가 공급합니다. 배열 순서는 법령 열거 순서이며, "
            + "목록이 바뀌어도 화면 배포가 필요하지 않습니다.")
    @GetMapping("/v1/allergens")
    public ResponseEntity<ApiResponse<List<ProductAllergenTypeResponse>>> getAllergenTypes() {
        List<ProductAllergenTypeResponse> response = productNutritionQueryService.getAllergenTypes().stream()
            .map(ProductAllergenTypeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 영양성분·알레르기 조회",
        description = "미입력 메뉴는 data가 null입니다. allergens는 화면이 체크박스 상태를 복원할 수 있도록 "
            + "한글 라벨이 아니라 코드 배열로 내려갑니다.")
    @GetMapping("/v1/{id}/nutrition")
    public ResponseEntity<ApiResponse<ProductNutritionResponse>> getNutrition(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductNutritionResponse response = ProductNutritionResponse.from(productNutritionQueryService.getNutrition( userDetails.getCeoId(), request.shopId(), id ));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 영양성분·알레르기 등록/수정",
        description = "전체 교체입니다. 열량·당류·단백질·포화지방·나트륨은 전부 채우거나 전부 비워야 하며, "
            + "알레르기 목록은 통째로 교체됩니다(빈 배열이면 표시가 비워집니다). 관리자 승인을 거치지 않습니다.")
    @PutMapping("/v1/{id}/nutrition")
    public ResponseEntity<ApiResponse<Void>> updateNutrition(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductNutritionUpdateRequest request
    ) {
        ProductNutritionUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        productNutritionCommandUseCase.updateNutrition(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 영양성분·알레르기 삭제",
        description = "행을 지웁니다(소프트 삭제 아님 — 과거 주문이 참조하지 않는 부가 정보입니다). "
            + "알레르기 목록도 함께 지워지며, 없는 정보를 지우려 하면 404입니다.")
    @DeleteMapping("/v1/{id}/nutrition")
    public ResponseEntity<ApiResponse<Void>> deleteNutrition(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductNutritionDeleteCommand command = request.toNutritionDeleteCommand(userDetails.getCeoId(), id);
        productNutritionCommandUseCase.deleteNutrition(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
