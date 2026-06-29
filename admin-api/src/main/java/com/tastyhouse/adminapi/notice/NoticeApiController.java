package com.tastyhouse.adminapi.notice;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.notice.request.NoticeCreateRequest;
import com.tastyhouse.adminapi.notice.request.NoticeUpdateRequest;
import com.tastyhouse.adminapi.notice.response.NoticeDetailResponse;
import com.tastyhouse.core.domain.notice.application.NoticeCommandService;
import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.command.CreateNoticeCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.UpdateNoticeCommand;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notice Admin", description = "공지사항 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeApiController {

    private final NoticeCommandService noticeCommandService;
    private final NoticeQueryService noticeQueryService;

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징 조회합니다. (비노출 공지 포함)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<NoticeListItemDto>>> getNotices(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<NoticeListItemDto> notices = noticeQueryService.findAllForAdmin(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            notices.getContent(),
            notices.getNumber(),
            notices.getSize(),
            notices.getTotalElements()
        ));
    }

    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createNotice(@Valid @RequestBody NoticeCreateRequest request) {
        Long id = noticeCommandService.createNotice(new CreateNoticeCommand(
            request.title(),
            request.content(),
            request.visible()
        ));
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(@PathVariable Long id) {
        Notice notice = noticeQueryService.findById(id);
        NoticeDetailResponse response = new NoticeDetailResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.isVisible(),
            notice.getCreatedAt(),
            notice.getUpdatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
        @PathVariable Long id,
        @Valid @RequestBody NoticeUpdateRequest request
    ) {
        noticeCommandService.updateNotice(id, new UpdateNoticeCommand(
            request.title(),
            request.content(),
            request.visible()
        ));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeCommandService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
