package com.tastyhouse.adminapi.faq;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.faq.request.FaqCategoryCreateRequest;
import com.tastyhouse.adminapi.faq.request.FaqCategoryUpdateRequest;
import com.tastyhouse.adminapi.faq.request.FaqCreateRequest;
import com.tastyhouse.adminapi.faq.request.FaqSearchRequest;
import com.tastyhouse.adminapi.faq.request.FaqUpdateRequest;
import com.tastyhouse.adminapi.faq.response.FaqCategoryResponse;
import com.tastyhouse.adminapi.faq.response.FaqDetailResponse;
import com.tastyhouse.adminapi.faq.response.FaqListItemResponse;
import com.tastyhouse.adminapi.faq.response.FaqPageResponse;

@Tag(name = "FAQ Admin", description = "FAQ 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faqs")
public class FaqApiController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 카테고리 등록", description = "새로운 FAQ 카테고리를 등록합니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createCategory(@Valid @RequestBody FaqCategoryCreateRequest request) {
        Long id = faqService.createCategory(request.name(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "FAQ 카테고리 목록 조회", description = "FAQ 카테고리 목록을 정렬 순서대로 조회합니다. (비노출 포함)")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> getCategories() {
        List<FaqCategoryResponse> categories = faqService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "FAQ 카테고리 상세 조회", description = "FAQ 카테고리 상세를 조회합니다.")
    @GetMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> getCategory(@PathVariable Long categoryId) {
        FaqCategoryResponse response = faqService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "FAQ 카테고리 수정", description = "기존 FAQ 카테고리를 수정합니다.")
    @PutMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody FaqCategoryUpdateRequest request
    ) {
        faqService.updateCategory(categoryId, request.name(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "FAQ 카테고리 삭제", description = "기존 FAQ 카테고리를 삭제합니다. 소속된 FAQ 항목이 있으면 삭제할 수 없습니다.")
    @DeleteMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        faqService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "FAQ 항목 등록", description = "새로운 FAQ 항목을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createFaq(@Valid @RequestBody FaqCreateRequest request) {
        Long id = faqService.createFaq(request.faqCategoryId(), request.question(), request.answer(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "FAQ 항목 목록 조회", description = "FAQ 항목 목록을 페이징 조회합니다. (비노출 포함) categoryId/visible 필터, question은 부분 일치 검색")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<FaqListItemResponse>>> getFaqs(
        @Valid @ModelAttribute FaqSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        FaqPageResponse pageResponse = faqService.getFaqs(search.categoryId(), search.question(), search.visible(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "FAQ 항목 상세 조회", description = "FAQ 항목 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<FaqDetailResponse>> getFaq(@PathVariable Long id) {
        FaqDetailResponse response = faqService.getFaq(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "FAQ 항목 수정", description = "기존 FAQ 항목을 수정합니다. 카테고리 이동도 가능합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFaq(
        @PathVariable Long id,
        @Valid @RequestBody FaqUpdateRequest request
    ) {
        faqService.updateFaq(id, request.faqCategoryId(), request.question(), request.answer(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "FAQ 항목 삭제", description = "기존 FAQ 항목을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
