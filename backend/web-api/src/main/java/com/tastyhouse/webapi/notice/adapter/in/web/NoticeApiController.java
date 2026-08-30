package com.tastyhouse.webapi.notice.adapter.in.web;

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
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.notice.port.in.NoticeQueryUseCase;
import com.tastyhouse.webapplication.notice.response.NoticeListItemResponse;

@RestController
@RequestMapping("/api/notices")
@Tag(name = "Notice", description = "공지사항 관리 API")
public class NoticeApiController {

    private final NoticeQueryUseCase noticeQueryService;

    public NoticeApiController(NoticeQueryUseCase noticeQueryService) {
        this.noticeQueryService = noticeQueryService;
    }

    @Operation(summary = "공지사항 목록 조회", description = "페이징된 공지사항 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<NoticeListItemResponse>>> getNoticeList(@Valid @ModelAttribute PageRequest pageRequest) {

        PaginationResponse<NoticeListItemResponse> pageResult = noticeQueryService.getNoticeList(pageRequest.page(), pageRequest.size());

        return ResponseEntity.ok(ApiResponse.success(
            pageResult.content(),
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements()
        ));
    }
}
