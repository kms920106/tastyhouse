package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductRepresentativeCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.application.product.port.in.ProductRepresentativeClearCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRequestCommand;

@Tag(name = "Ceo Product Representative", description = "점주 사장님 추천(대표 메뉴) API")
@RestController
@RequestMapping("/api/products")
public class ProductRepresentativeApiController {
    private final ProductRepresentativeCommandUseCase productRepresentativeCommandUseCase;

    public ProductRepresentativeApiController(
        ProductRepresentativeCommandUseCase productRepresentativeCommandUseCase
    ) {
        this.productRepresentativeCommandUseCase = productRepresentativeCommandUseCase;
    }

    @Operation(summary = "사장님 추천 메뉴 지정 요청",
        description = "관리자 승인 후 반영됩니다. 가게당 최대 6개이며 이미지가 등록된 메뉴만 지정할 수 있습니다. "
            + "이미 추천이거나 검수 대기 중인 메뉴는 건너뛰므로 반환 목록이 요청보다 짧을 수 있습니다.")
    @PostMapping("/v1/representative-requests")
    public ResponseEntity<ApiResponse<List<Long>>> requestRepresentative(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductRepresentativeCreateRequest request
    ) {
        ProductRepresentativeRequestCommand command = request.toCommand(userDetails.getCeoId());
        List<Long> requestIds = productRepresentativeCommandUseCase.requestRepresentative(command);
        return ResponseEntity.ok(ApiResponse.success(requestIds));
    }

    @Operation(summary = "사장님 추천 메뉴 해제",
        description = "승인을 거치지 않고 즉시 해제됩니다. 단 가게마다 최소 1개는 남아야 하며, 마지막 1개를 "
            + "해제하려 하면 거부됩니다. 이미 해제 상태여도 실패가 아닙니다(멱등).")
    @DeleteMapping("/v1/{id}/representative")
    public ResponseEntity<ApiResponse<Void>> clearRepresentative(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductRepresentativeClearCommand command = request.toRepresentativeClearCommand(userDetails.getCeoId(), id);
        productRepresentativeCommandUseCase.clearRepresentative(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
