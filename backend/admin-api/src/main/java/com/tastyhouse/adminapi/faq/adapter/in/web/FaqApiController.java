package com.tastyhouse.adminapi.faq.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import com.tastyhouse.application.faq.port.in.FaqCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCreateUseCase;
import com.tastyhouse.application.faq.port.in.FaqDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqDeleteUseCase;
import com.tastyhouse.application.faq.port.in.FaqManagementDetailQueryUseCase;
import com.tastyhouse.application.faq.port.in.FaqManagementListQueryUseCase;
import com.tastyhouse.application.faq.port.in.FaqUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqUpdateUseCase;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.faq.adapter.in.web.request.FaqCreateRequest;
import com.tastyhouse.adminapi.faq.adapter.in.web.request.FaqSearchRequest;
import com.tastyhouse.adminapi.faq.adapter.in.web.request.FaqUpdateRequest;
import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqDetailResponse;
import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqListItemResponse;

@Tag(name = "FAQ Admin", description = "FAQ 관리자 API")
@RestController
@RequestMapping("/api/faqs")
class FaqApiController {

    private final FaqCreateUseCase faqCreateUseCase;
    private final FaqUpdateUseCase faqUpdateUseCase;
    private final FaqDeleteUseCase faqDeleteUseCase;
    private final FaqManagementListQueryUseCase faqManagementListQueryUseCase;
    private final FaqManagementDetailQueryUseCase faqManagementDetailQueryUseCase;

    public FaqApiController(
        FaqCreateUseCase faqCreateUseCase,
        FaqUpdateUseCase faqUpdateUseCase,
        FaqDeleteUseCase faqDeleteUseCase,
        FaqManagementListQueryUseCase faqManagementListQueryUseCase,
        FaqManagementDetailQueryUseCase faqManagementDetailQueryUseCase
    ) {
        this.faqCreateUseCase = faqCreateUseCase;
        this.faqUpdateUseCase = faqUpdateUseCase;
        this.faqDeleteUseCase = faqDeleteUseCase;
        this.faqManagementListQueryUseCase = faqManagementListQueryUseCase;
        this.faqManagementDetailQueryUseCase = faqManagementDetailQueryUseCase;
    }

    @Operation(summary = "FAQ 항목 등록", description = "새로운 FAQ 항목을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createFaq(@Valid @RequestBody FaqCreateRequest request) {
        FaqCreateCommand command = request.toCommand();
        Long id = faqCreateUseCase.createFaq(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "FAQ 항목 목록 조회", description = "FAQ 항목 목록을 페이징 조회합니다. (비노출 포함) categoryId/visible 필터, question은 부분 일치 검색")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<FaqListItemResponse>>> getFaqs(
        @Valid @ModelAttribute FaqSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<FaqManagementListItemResult> pageResult = faqManagementListQueryUseCase.getFaqs(search.categoryId(), search.question(), search.visible(), pageRequest.page(), pageRequest.size());
        PaginationResponse<FaqListItemResponse> pageResponse = PaginationResponse.from(pageResult.map(FaqListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "FAQ 항목 상세 조회", description = "FAQ 항목 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<FaqDetailResponse>> getFaq(@PathVariable Long id) {
        FaqDetailResponse response = FaqDetailResponse.from(faqManagementDetailQueryUseCase.getFaq(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "FAQ 항목 수정", description = "기존 FAQ 항목을 수정합니다. 카테고리 이동도 가능합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFaq(
        @PathVariable Long id,
        @Valid @RequestBody FaqUpdateRequest request
    ) {
        FaqUpdateCommand command = request.toCommand(id);
        faqUpdateUseCase.updateFaq(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "FAQ 항목 삭제", description = "기존 FAQ 항목을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable Long id) {
        FaqDeleteCommand command = FaqDeleteCommand.of(id);
        faqDeleteUseCase.deleteFaq(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
