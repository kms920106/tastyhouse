package com.tastyhouse.webapi.faq.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.faq.port.in.FaqQueryUseCase;
import com.tastyhouse.webapi.faq.adapter.in.web.request.FaqSearchRequest;
import com.tastyhouse.webapi.faq.adapter.in.web.response.FaqCategoryListItemResponse;
import com.tastyhouse.webapi.faq.adapter.in.web.response.FaqListItemResponse;

@RestController
@RequestMapping("/api/faqs")
@Tag(name = "FAQ", description = "자주하는 질문 API")
public class FaqApiController {

    private final FaqQueryUseCase faqQueryService;

    public FaqApiController(FaqQueryUseCase faqQueryService) {
        this.faqQueryService = faqQueryService;
    }

    @Operation(summary = "FAQ 카테고리 목록 조회", description = "활성화된 FAQ 카테고리 목록을 정렬 순서대로 조회합니다.")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryListItemResponse>>> getFaqCategories() {
        List<FaqCategoryListItemResponse> categories = faqQueryService.getFaqCategories().stream()
            .map(FaqCategoryListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "FAQ 목록 조회", description = "카테고리 ID로 필터링하거나 전체 FAQ 목록을 조회합니다. categoryId 미입력 시 전체 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<FaqListItemResponse>>> getFaqList(@Valid @ModelAttribute FaqSearchRequest search) {
        List<FaqListItemResponse> faqs = faqQueryService.getFaqList(search.categoryId()).stream()
            .map(FaqListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(faqs));
    }
}
