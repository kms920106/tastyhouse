package com.tastyhouse.adminapi.event.adapter.in.web;

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

import com.tastyhouse.application.event.port.in.EventCommandUseCase;
import com.tastyhouse.application.event.port.in.EventCreateCommand;
import com.tastyhouse.application.event.port.in.EventDeleteCommand;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;
import com.tastyhouse.application.event.port.in.EventUpdateCommand;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventCreateRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventSearchRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventUpdateRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventDetailResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventListItemResponse;

@Tag(name = "Event Admin", description = "이벤트 관리자 API")
@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventManagementQueryUseCase eventQueryUseCase;

    public EventApiController(EventCommandUseCase eventCommandUseCase, EventManagementQueryUseCase eventQueryUseCase) {
        this.eventCommandUseCase = eventCommandUseCase;
        this.eventQueryUseCase = eventQueryUseCase;
    }

    @Operation(summary = "이벤트 목록 조회", description = "이벤트 목록을 페이징 조회합니다. (삭제된 이벤트 제외) name은 부분 일치 검색, status 미지정 시 전체 상태 조회")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<EventListItemResponse>>> getEvents(
        @Valid @ModelAttribute EventSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<EventManagementListItemResult> pageResult = eventQueryUseCase.getEvents(search.name(), search.status(), pageRequest.page(), pageRequest.size());
        PaginationResponse<EventListItemResponse> pageResponse = PaginationResponse.from(pageResult.map(EventListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "이벤트 등록", description = "새로운 이벤트를 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventCreateCommand command = request.toCommand();
        Long id = eventCommandUseCase.createEvent(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "이벤트 상세 조회", description = "이벤트 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEvent(@PathVariable Long id) {
        EventDetailResponse response = EventDetailResponse.from(eventQueryUseCase.getEvent(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "이벤트 수정", description = "기존 이벤트를 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateEvent(
        @PathVariable Long id,
        @Valid @RequestBody EventUpdateRequest request
    ) {
        EventUpdateCommand command = request.toCommand(id);
        eventCommandUseCase.updateEvent(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "이벤트 삭제", description = "기존 이벤트를 삭제합니다. (Soft Delete)")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        EventDeleteCommand command = EventDeleteCommand.of(id);
        eventCommandUseCase.deleteEvent(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
