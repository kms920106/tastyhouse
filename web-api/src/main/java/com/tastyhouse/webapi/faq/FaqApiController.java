package com.tastyhouse.webapi.faq;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.faq.response.FaqCategoryListItemResponse;
import com.tastyhouse.webapi.faq.response.FaqListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQ", description = "자주하는 질문 API")
public class FaqApiController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 카테고리 목록 조회", description = "활성화된 FAQ 카테고리 목록을 정렬 순서대로 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/categories")
    public ResponseEntity<CommonResponse<List<FaqCategoryListItemResponse>>> getFaqCategories() {
        List<FaqCategoryListItemResponse> categories = faqService.searchCategories();
        return ResponseEntity.ok(CommonResponse.success(categories));
    }

    @Operation(summary = "FAQ 목록 조회", description = "카테고리 ID로 필터링하거나 전체 FAQ 목록을 조회합니다. categoryId 미입력 시 전체 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1")
    public ResponseEntity<CommonResponse<List<FaqListItemResponse>>> getFaqList(
            @RequestParam(required = false) Long categoryId) {
        List<FaqListItemResponse> faqs = faqService.searchFaqListItemResponses(categoryId);
        return ResponseEntity.ok(CommonResponse.success(faqs));
    }
}
