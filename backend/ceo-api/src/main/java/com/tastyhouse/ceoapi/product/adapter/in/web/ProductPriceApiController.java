package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductPriceReplaceRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductPriceResponse;
import com.tastyhouse.application.product.port.in.ProductPriceCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductPriceReplaceCommand;
import com.tastyhouse.application.product.port.in.ProductPriceQueryUseCase;

@Tag(name = "Ceo Product Price", description = "점주 메뉴 가격 관리 API")
@RestController
@RequestMapping("/api/products")
public class ProductPriceApiController {
    private final ProductPriceQueryUseCase productPriceQueryService;
    private final ProductPriceCommandUseCase productPriceCommandUseCase;

    public ProductPriceApiController(
        ProductPriceQueryUseCase productPriceQueryService,
        ProductPriceCommandUseCase productPriceCommandUseCase
    ) {
        this.productPriceQueryService = productPriceQueryService;
        this.productPriceCommandUseCase = productPriceCommandUseCase;
    }

    @Operation(summary = "메뉴 가격 목록 조회",
        description = "표시 순서 오름차순입니다. 가격 행이 없으면 빈 배열입니다. 매장가·픽업가는 "
            + "미인증·미설정이면 null이며, 0원(무료)과 구분됩니다. 응답의 id를 그대로 수정 요청에 실어 "
            + "보내야 기존 행이 갱신됩니다.")
    @GetMapping("/v1/{id}/prices")
    public ResponseEntity<ApiResponse<List<ProductPriceResponse>>> getPrices(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        List<ProductPriceResponse> response = productPriceQueryService.getPrices( userDetails.getCeoId(), request.shopId(), id ).stream()
            .map(ProductPriceResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 가격 등록/수정",
        description = "전체 교체입니다 — 요청에 담기지 않은 기존 가격 행은 삭제됩니다. 가격은 1개 이상이어야 "
            + "하고, 2개 이상이면 가격명이 필수이며 중복될 수 없습니다. 매장가·픽업가는 매장 가격 인증을 "
            + "받은 가게만 설정할 수 있고, 할인이 진행 중인 메뉴는 가격을 변경할 수 없습니다. sort=0 행의 "
            + "배달가가 메뉴 대표가로 동기화되며, 배달가가 매장가보다 높아지면 가게 인증이 즉시 해제됩니다.")
    @PutMapping("/v1/{id}/prices")
    public ResponseEntity<ApiResponse<Void>> replacePrices(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductPriceReplaceRequest request
    ) {
        ProductPriceReplaceCommand command = request.toCommand(userDetails.getCeoId(), id);
        productPriceCommandUseCase.replacePrices(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
