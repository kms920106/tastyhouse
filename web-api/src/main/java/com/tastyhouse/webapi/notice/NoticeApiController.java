package com.tastyhouse.webapi.notice;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.domain.notice.application.NoticeQueryService;
import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.notice.response.NoticeListItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지사항 관리 API")
public class NoticeApiController {

    private final NoticeQueryService noticeQueryService;

    @Operation(summary = "공지사항 목록 조회", description = "페이징된 공지사항 목록을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공",
        content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1")
    public ResponseEntity<CommonResponse<List<NoticeListItem>>> getNoticeList(
        @Valid @ModelAttribute PageRequest pageRequest) {

        PageResult<NoticeListItem> pageResult = PageResult
            .from(noticeQueryService.findAllWithPagination(pageRequest.page(), pageRequest.size()))
            .map(this::toNoticeListItem);

        return ResponseEntity.ok(CommonResponse.success(
            pageResult.getContent(),
            pageRequest.page(),
            pageRequest.size(),
            pageResult.getTotalElements()
        ));
    }

    private NoticeListItem toNoticeListItem(NoticeListItemDto dto) {
        return NoticeListItem.from(dto.id(), dto.title(), dto.content(), dto.createdAt());
    }
}
