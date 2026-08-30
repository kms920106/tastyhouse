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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupMergeExclusionCreateRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupMergePreviewSearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupMergeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductOptionGroupMergeSuggestionSearchRequest;
import com.tastyhouse.ceoapplication.product.response.ProductOptionGroupMergePreviewResponse;
import com.tastyhouse.ceoapplication.product.response.ProductOptionGroupMergeSuggestionResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeExclusionCreateCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeQueryUseCase;

/**
 * 점주 옵션그룹 <b>합치기</b> API.
 *
 * <p>{@link ProductOptionGroupApiController}에 넣지 않고 컨트롤러를 분리한 이유는, 합치기가
 * 추천·제외·미리보기·실행 4개 워크플로를 갖는 독립 기능이어서 그 클래스가 두 배가 되기 때문이다.
 *
 * <p><b>분리(unmerge) 엔드포인트는 없다</b> — 합치기는 비가역이며, 그 사실을 라우트의 부재로
 * 표현한다({@code ProductOptionGroup}이 un-hide 메서드를 의도적으로 두지 않은 것과 같은 형태).
 */
@Tag(name = "Ceo Product Option Group Merge", description = "점주 옵션그룹 합치기 API")
@RestController
@RequestMapping("/api/products")
public class ProductOptionGroupMergeApiController {

    private final ProductOptionGroupMergeQueryUseCase productOptionGroupMergeQueryService;
    private final ProductOptionGroupMergeCommandUseCase productOptionGroupMergeCommandUseCase;

    public ProductOptionGroupMergeApiController(
        ProductOptionGroupMergeQueryUseCase productOptionGroupMergeQueryService,
        ProductOptionGroupMergeCommandUseCase productOptionGroupMergeCommandUseCase
    ) {
        this.productOptionGroupMergeQueryService = productOptionGroupMergeQueryService;
        this.productOptionGroupMergeCommandUseCase = productOptionGroupMergeCommandUseCase;
    }

    @Operation(summary = "옵션그룹 합치기 추천 목록",
        description = "옵션그룹명·최소/최대 선택 개수·옵션명·가격이 모두 같은 옵션그룹을 묶어 반환합니다. "
            + "점주가 [X]로 제외한 묶음은 제외됩니다. signature는 제외 요청에 그대로 실어 보내는 "
            + "불투명 토큰입니다.")
    @GetMapping("/v1/option-groups/merge-suggestions")
    public ResponseEntity<ApiResponse<List<ProductOptionGroupMergeSuggestionResponse>>> getMergeSuggestions(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute ProductOptionGroupMergeSuggestionSearchRequest request
    ) {
        List<ProductOptionGroupMergeSuggestionResponse> response =
            productOptionGroupMergeQueryService.getMergeSuggestions(userDetails.getCeoId(), request.shopId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션그룹 합치기 추천 제외",
        description = "추천 목록에서 해당 묶음을 영구 제외합니다. 서버가 optionGroupIds로 서명을 재계산해 "
            + "낡거나 위조된 토큰을 거부합니다(PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH). "
            + "재클릭은 멱등이며 기존 제외 ID를 반환합니다. 옵션명·가격이 수정되면 서명이 달라져 다시 "
            + "추천되는 것이 의도된 동작입니다.")
    @PostMapping("/v1/option-groups/merge-suggestions/exclusions")
    public ResponseEntity<ApiResponse<Long>> excludeMergeSuggestion(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductOptionGroupMergeExclusionCreateRequest request
    ) {
        ProductOptionGroupMergeExclusionCreateCommand command = request.toCommand(userDetails.getCeoId());
        Long exclusionId = productOptionGroupMergeCommandUseCase.excludeMergeSuggestion(command);
        return ResponseEntity.ok(ApiResponse.success(exclusionId));
    }

    @Operation(summary = "옵션그룹 합치기 미리보기",
        description = "기준 그룹과 후보들의 차이(diff)를 반환합니다. mergeable=false면 합치기 버튼을 "
            + "비활성화하고 blockedReason을 안내합니다. 후보에만 있는 옵션(ONLY_IN_CANDIDATE)은 "
            + "합치면 사라집니다.")
    @GetMapping("/v1/option-groups/merge-preview")
    public ResponseEntity<ApiResponse<ProductOptionGroupMergePreviewResponse>> getMergePreview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute ProductOptionGroupMergePreviewSearchRequest request
    ) {
        ProductOptionGroupMergePreviewResponse response = productOptionGroupMergeQueryService.getMergePreview(
            userDetails.getCeoId(),
            request.shopId(),
            request.baseOptionGroupId(),
            request.optionGroupIds()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "옵션그룹 합치기 실행",
        description = "경로의 {id}는 기준(살아남는) 옵션그룹입니다. 흡수된 그룹은 감춰지고 그 연결은 "
            + "기준 그룹으로 옮겨집니다. 흡수 그룹의 옵션은 기준으로 옮겨지지 않고 함께 감춰집니다. "
            + "되돌릴 수 없습니다. 살아남은 기준 그룹 ID를 반환하므로 목록을 재조회하세요.")
    @PostMapping("/v1/option-groups/{id}/merge")
    public ResponseEntity<ApiResponse<Long>> mergeProductOptionGroups(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionGroupMergeRequest request
    ) {
        ProductOptionGroupMergeCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long baseOptionGroupId = productOptionGroupMergeCommandUseCase.mergeProductOptionGroups(command);
        return ResponseEntity.ok(ApiResponse.success(baseOptionGroupId));
    }
}
