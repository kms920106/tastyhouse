package com.tastyhouse.adminapi.notice;

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
import com.tastyhouse.adminapi.notice.request.NoticeCreateRequest;
import com.tastyhouse.adminapi.notice.request.NoticeSearchRequest;
import com.tastyhouse.adminapi.notice.request.NoticeUpdateRequest;
import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.adminapi.notice.response.NoticeListItemResponse;
import com.tastyhouse.adminapi.notice.response.NoticePageResponse;

@Tag(name = "Notice Admin", description = "공지사항 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeApiController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징 조회합니다. (비노출 공지 포함) title/content는 부분 일치 검색, visible은 null=전체/true=노출/false=비노출")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<NoticeListItemResponse>>> getNotices(
        @Valid @ModelAttribute NoticeSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        NoticePageResponse pageResponse = noticeService.getNotices(search.title(), search.content(), search.visible(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createNotice(@Valid @RequestBody NoticeCreateRequest request) {
        Long id = noticeService.createNotice(request.title(), request.content(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세을 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(@PathVariable Long id) {
        NoticeDetailResponse response = noticeService.getNotice(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
        @PathVariable Long id,
        @Valid @RequestBody NoticeUpdateRequest request
    ) {
        noticeService.updateNotice(id, request.title(), request.content(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
