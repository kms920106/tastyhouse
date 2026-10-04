package com.tastyhouse.adminapi.event.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.event.port.in.EventAnnouncementCreateCommand;
import com.tastyhouse.application.event.port.in.EventAnnouncementUpdateCommand;
import com.tastyhouse.application.event.port.in.EventCommandUseCase;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventAnnouncementCreateRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventAnnouncementUpdateRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventAnnouncementResponse;

@Tag(name = "Event Announcement Admin", description = "이벤트 당첨자 발표 공지 관리자 API")
@RestController
@RequestMapping("/api/events")
class EventAnnouncementAdminApiController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventManagementQueryUseCase eventQueryUseCase;

    public EventAnnouncementAdminApiController(EventCommandUseCase eventCommandUseCase, EventManagementQueryUseCase eventQueryUseCase) {
        this.eventCommandUseCase = eventCommandUseCase;
        this.eventQueryUseCase = eventQueryUseCase;
    }

    @Operation(summary = "당첨자 발표 공지 등록", description = "이벤트의 당첨자 발표 공지를 등록합니다. (이벤트당 1개)")
    @PostMapping("/v1/{id}/announcement")
    public ResponseEntity<ApiResponse<Long>> createAnnouncement(
        @PathVariable Long id,
        @Valid @RequestBody EventAnnouncementCreateRequest request
    ) {
        EventAnnouncementCreateCommand command = request.toCommand(id);
        Long announcementId = eventCommandUseCase.createAnnouncement(command);
        return ResponseEntity.ok(ApiResponse.success(announcementId));
    }

    @Operation(summary = "당첨자 발표 공지 수정", description = "이벤트의 당첨자 발표 공지를 수정합니다.")
    @PutMapping("/v1/{id}/announcement")
    public ResponseEntity<ApiResponse<Void>> updateAnnouncement(
        @PathVariable Long id,
        @Valid @RequestBody EventAnnouncementUpdateRequest request
    ) {
        EventAnnouncementUpdateCommand command = request.toCommand(id);
        eventCommandUseCase.updateAnnouncement(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "당첨자 발표 공지 조회", description = "이벤트의 당첨자 발표 공지를 조회합니다.")
    @GetMapping("/v1/{id}/announcement")
    public ResponseEntity<ApiResponse<EventAnnouncementResponse>> getAnnouncement(@PathVariable Long id) {
        EventAnnouncementResponse response = EventAnnouncementResponse.from(eventQueryUseCase.getAnnouncement(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
