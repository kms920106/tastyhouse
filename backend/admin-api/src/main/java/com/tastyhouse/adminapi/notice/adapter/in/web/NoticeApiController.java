package com.tastyhouse.adminapi.notice.adapter.in.web;

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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.notice.adapter.in.web.request.NoticeCreateRequest;
import com.tastyhouse.adminapi.notice.adapter.in.web.request.NoticeSearchRequest;
import com.tastyhouse.adminapi.notice.adapter.in.web.request.NoticeUpdateRequest;
import com.tastyhouse.adminapi.notice.adapter.in.web.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.adapter.in.web.response.NoticeListItemResponse;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.adminapplication.notice.port.in.NoticeCommandUseCase;
import com.tastyhouse.adminapplication.notice.port.in.NoticeCreateCommand;
import com.tastyhouse.adminapplication.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.adminapplication.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.adminapplication.notice.port.in.NoticeQueryUseCase;

@Tag(name = "Notice Admin", description = "공지사항 관리자 API")
@RestController
@RequestMapping("/api/notices")
public class NoticeApiController {

    private final NoticeCommandUseCase noticeCommandUseCase;
    private final NoticeQueryUseCase noticeQueryUseCase;

    public NoticeApiController(NoticeCommandUseCase noticeCommandUseCase, NoticeQueryUseCase noticeQueryUseCase) {
        this.noticeCommandUseCase = noticeCommandUseCase;
        this.noticeQueryUseCase = noticeQueryUseCase;
    }

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징 조회합니다. (비노출 공지 포함) title/content는 부분 일치 검색, visible은 null=전체/true=노출/false=비노출")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<NoticeListItemResponse>>> getNotices(
        @Valid @ModelAttribute NoticeSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<NoticeManagementListItemResult> pageResult = noticeQueryUseCase.getNotices(search.title(), search.content(), search.visible(), pageRequest.page(), pageRequest.size());
        PaginationResponse<NoticeListItemResponse> pageResponse = PaginationResponse.from(pageResult.map(NoticeListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createNotice(@Valid @RequestBody NoticeCreateRequest request) {
        NoticeCreateCommand command = request.toCommand();
        Long id = noticeCommandUseCase.createNotice(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세을 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(@PathVariable Long id) {
        NoticeDetailResponse response = NoticeDetailResponse.from(noticeQueryUseCase.getNotice(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
        @PathVariable Long id,
        @Valid @RequestBody NoticeUpdateRequest request
    ) {
        NoticeUpdateCommand command = request.toCommand(id);
        noticeCommandUseCase.updateNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        NoticeDeleteCommand command = NoticeDeleteCommand.of(id);
        noticeCommandUseCase.deleteNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
