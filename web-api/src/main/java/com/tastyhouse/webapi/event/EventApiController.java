package com.tastyhouse.webapi.event;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.event.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.response.EventDetailResponse;
import com.tastyhouse.webapi.event.response.EventListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventApiController {

    private final EventService eventService;

    @Operation(summary = "이벤트 목록 조회", description = "상태별 이벤트 목록을 조회합니다. (진행중, 종료)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/list")
    public ResponseEntity<CommonResponse<List<EventListItemResponse>>> getEventList(
        @Parameter(description = "이벤트 상태 (ACTIVE: 진행중, ENDED: 종료)", example = "ACTIVE")
        @RequestParam EventStatus status,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<EventListItemResponse> pageResult = eventService.getEventList(status, pageRequest.page(), pageRequest.size());
        CommonResponse<List<EventListItemResponse>> response = CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 상세 조회", description = "이벤트의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/{eventId}")
    public ResponseEntity<CommonResponse<EventDetailResponse>> getEventDetail(
        @Parameter(description = "이벤트 ID", example = "1")
        @PathVariable Long eventId
    ) {
        EventDetailResponse event = eventService.getEventDetail(eventId);
        return ResponseEntity.ok(CommonResponse.success(event));
    }

    @Operation(summary = "당첨자 발표 목록 조회", description = "모든 이벤트의 당첨자 발표 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/v1/announcements")
    public ResponseEntity<CommonResponse<List<EventAnnouncementListItemResponse>>> getEventAnnouncementList(
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<EventAnnouncementListItemResponse> pageResult = eventService.getEventAnnouncementList(pageRequest.page(), pageRequest.size());
        CommonResponse<List<EventAnnouncementListItemResponse>> response = CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }
}
