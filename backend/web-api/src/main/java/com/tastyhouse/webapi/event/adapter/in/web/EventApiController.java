package com.tastyhouse.webapi.event.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.event.port.in.EventQueryUseCase;
import com.tastyhouse.webapi.event.adapter.in.web.request.EventSearchRequest;
import com.tastyhouse.webapi.event.adapter.in.web.response.EventAnnouncementListItemResponse;
import com.tastyhouse.webapi.event.adapter.in.web.response.EventDetailResponse;
import com.tastyhouse.webapi.event.adapter.in.web.response.EventListItemResponse;

@RestController
@RequestMapping("/api/event")
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventApiController {

    private final EventQueryUseCase eventQueryService;

    public EventApiController(EventQueryUseCase eventQueryService) {
        this.eventQueryService = eventQueryService;
    }

    @Operation(summary = "이벤트 목록 조회", description = "상태별 이벤트 목록을 조회합니다. (진행중, 종료)")
    @GetMapping("/v1/list")
    public ResponseEntity<ApiResponse<List<EventListItemResponse>>> getEventList(
        @Valid @ModelAttribute EventSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var pageResult = PaginationResponse.from(
            eventQueryService.getEventList(search.status(), pageRequest.page(), pageRequest.size())
                .map(EventListItemResponse::from)
        );
        ApiResponse<List<EventListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 상세 조회", description = "이벤트의 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEventDetail(
        @Parameter(description = "이벤트 ID", example = "1")
        @PathVariable Long id
    ) {
        EventDetailResponse event = EventDetailResponse.from(eventQueryService.getEventDetail(id));
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @Operation(summary = "당첨자 발표 목록 조회", description = "모든 이벤트의 당첨자 발표 목록을 조회합니다.")
    @GetMapping("/v1/announcements")
    public ResponseEntity<ApiResponse<List<EventAnnouncementListItemResponse>>> getEventAnnouncementList(
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var pageResult = PaginationResponse.from(
            eventQueryService.getEventAnnouncementList(pageRequest.page(), pageRequest.size())
                .map(EventAnnouncementListItemResponse::from)
        );
        ApiResponse<List<EventAnnouncementListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }
}
