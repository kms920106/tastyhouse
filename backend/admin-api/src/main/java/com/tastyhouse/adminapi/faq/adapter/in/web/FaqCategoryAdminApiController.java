package com.tastyhouse.adminapi.faq.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.faq.port.in.FaqCategoryCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryCreateUseCase;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteUseCase;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateUseCase;
import com.tastyhouse.application.faq.port.in.FaqManagementCategoryDetailQueryUseCase;
import com.tastyhouse.application.faq.port.in.FaqManagementCategoryListQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.faq.adapter.in.web.request.FaqCategoryCreateRequest;
import com.tastyhouse.adminapi.faq.adapter.in.web.request.FaqCategoryUpdateRequest;
import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqCategoryResponse;

@Tag(name = "FAQ Category Admin", description = "FAQ 카테고리 관리자 API")
@RestController
@RequestMapping("/api/faqs")
class FaqCategoryAdminApiController {

    private final FaqCategoryCreateUseCase faqCategoryCreateUseCase;
    private final FaqCategoryUpdateUseCase faqCategoryUpdateUseCase;
    private final FaqCategoryDeleteUseCase faqCategoryDeleteUseCase;
    private final FaqManagementCategoryListQueryUseCase faqManagementCategoryListQueryUseCase;
    private final FaqManagementCategoryDetailQueryUseCase faqManagementCategoryDetailQueryUseCase;

    public FaqCategoryAdminApiController(
        FaqCategoryCreateUseCase faqCategoryCreateUseCase,
        FaqCategoryUpdateUseCase faqCategoryUpdateUseCase,
        FaqCategoryDeleteUseCase faqCategoryDeleteUseCase,
        FaqManagementCategoryListQueryUseCase faqManagementCategoryListQueryUseCase,
        FaqManagementCategoryDetailQueryUseCase faqManagementCategoryDetailQueryUseCase
    ) {
        this.faqCategoryCreateUseCase = faqCategoryCreateUseCase;
        this.faqCategoryUpdateUseCase = faqCategoryUpdateUseCase;
        this.faqCategoryDeleteUseCase = faqCategoryDeleteUseCase;
        this.faqManagementCategoryListQueryUseCase = faqManagementCategoryListQueryUseCase;
        this.faqManagementCategoryDetailQueryUseCase = faqManagementCategoryDetailQueryUseCase;
    }

    @Operation(summary = "FAQ 카테고리 등록", description = "새로운 FAQ 카테고리를 등록합니다.")
    @PostMapping("/v1/categories")
    public ResponseEntity<ApiResponse<Long>> createCategory(@Valid @RequestBody FaqCategoryCreateRequest request) {
        FaqCategoryCreateCommand command = request.toCommand();
        Long id = faqCategoryCreateUseCase.createCategory(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "FAQ 카테고리 목록 조회", description = "FAQ 카테고리 목록을 정렬 순서대로 조회합니다. (비노출 포함)")
    @GetMapping("/v1/categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> getCategories() {
        List<FaqCategoryResponse> categories = faqManagementCategoryListQueryUseCase.getCategories().stream()
            .map(FaqCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "FAQ 카테고리 상세 조회", description = "FAQ 카테고리 상세를 조회합니다.")
    @GetMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> getCategory(@PathVariable Long categoryId) {
        FaqCategoryResponse response = FaqCategoryResponse.from(faqManagementCategoryDetailQueryUseCase.getCategory(categoryId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "FAQ 카테고리 수정", description = "기존 FAQ 카테고리를 수정합니다.")
    @PutMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody FaqCategoryUpdateRequest request
    ) {
        FaqCategoryUpdateCommand command = request.toCommand(categoryId);
        faqCategoryUpdateUseCase.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "FAQ 카테고리 삭제", description = "기존 FAQ 카테고리를 삭제합니다. 소속된 FAQ 항목이 있으면 삭제할 수 없습니다.")
    @DeleteMapping("/v1/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        FaqCategoryDeleteCommand command = FaqCategoryDeleteCommand.of(categoryId);
        faqCategoryDeleteUseCase.deleteCategory(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
