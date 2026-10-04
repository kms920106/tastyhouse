package com.tastyhouse.adminapi.event.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.event.port.in.EventCommandUseCase;
import com.tastyhouse.application.event.port.in.EventManagementQueryUseCase;
import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.event.adapter.in.web.request.EventWinnerCreateRequest;
import com.tastyhouse.adminapi.event.adapter.in.web.response.EventWinnerResponse;

@Tag(name = "Event Winner Admin", description = "이벤트 당첨자 관리자 API")
@RestController
@RequestMapping("/api/events")
public class EventWinnerAdminApiController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventManagementQueryUseCase eventQueryUseCase;

    public EventWinnerAdminApiController(EventCommandUseCase eventCommandUseCase, EventManagementQueryUseCase eventQueryUseCase) {
        this.eventCommandUseCase = eventCommandUseCase;
        this.eventQueryUseCase = eventQueryUseCase;
    }

    @Operation(summary = "당첨자 등록", description = "이벤트에 당첨자를 등록합니다.")
    @PostMapping("/v1/{id}/winners")
    public ResponseEntity<ApiResponse<Long>> createWinner(
        @PathVariable Long id,
        @Valid @RequestBody EventWinnerCreateRequest request
    ) {
        EventWinnerCreateCommand command = request.toCommand(id);
        Long winnerId = eventCommandUseCase.createWinner(command);
        return ResponseEntity.ok(ApiResponse.success(winnerId));
    }

    @Operation(summary = "당첨자 목록 조회", description = "이벤트의 당첨자 목록을 순위순으로 조회합니다.")
    @GetMapping("/v1/{id}/winners")
    public ResponseEntity<ApiResponse<List<EventWinnerResponse>>> getWinners(@PathVariable Long id) {
        List<EventWinnerResponse> winners = eventQueryUseCase.getWinners(id).stream()
            .map(EventWinnerResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(winners));
    }

    @Operation(summary = "당첨자 삭제", description = "이벤트의 당첨자를 삭제합니다.")
    @DeleteMapping("/v1/winners/{winnerId}")
    public ResponseEntity<ApiResponse<Void>> deleteWinner(@PathVariable Long winnerId) {
        EventWinnerDeleteCommand command = EventWinnerDeleteCommand.of(winnerId);
        eventCommandUseCase.deleteWinner(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
