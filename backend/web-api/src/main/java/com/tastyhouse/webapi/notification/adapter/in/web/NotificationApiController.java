package com.tastyhouse.webapi.notification.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.notification.adapter.in.web.response.NotificationListItemResponse;
import com.tastyhouse.application.notification.port.in.NotificationCommandUseCase;
import com.tastyhouse.application.notification.port.in.NotificationMarkAllAsReadCommand;
import com.tastyhouse.application.notification.port.in.NotificationMarkAsReadCommand;
import com.tastyhouse.application.notification.port.in.NotificationQueryUseCase;
import com.tastyhouse.webapi.security.CurrentUser;

/**
 * 인앱 알림함 API(web).
 *
 * <p>전 엔드포인트가 로그인 필수다 — 모든 조회·전이가 "내 알림"으로 스코프되며, 대상 회원은 경로/바디가
 * 아니라 <b>토큰에서만</b> 얻는다. 회원 식별자를 요청으로 받으면 그 자체가 IDOR 입구가 된다.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "인앱 알림함 API")
public class NotificationApiController {

    private final NotificationQueryUseCase notificationQueryService;
    private final NotificationCommandUseCase notificationCommandUseCase;

    public NotificationApiController(
        NotificationQueryUseCase notificationQueryService,
        NotificationCommandUseCase notificationCommandUseCase
    ) {
        this.notificationQueryService = notificationQueryService;
        this.notificationCommandUseCase = notificationCommandUseCase;
    }

    @Operation(summary = "내 알림 목록 조회", description = "로그인한 회원의 알림 목록을 최신순으로 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<NotificationListItemResponse>>> getNotifications(
        @Valid @ModelAttribute PageRequest pageRequest,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaginationResponse<NotificationListItemResponse> pageResponse = PaginationResponse.from(
            notificationQueryService.findNotifications(
                userDetails.getMemberId(),
                pageRequest.page(),
                pageRequest.size()
            ).map(NotificationListItemResponse::from)
        );

        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements()
        ));
    }

    @Operation(summary = "미읽음 알림 개수 조회", description = "헤더 배지에 표시할 미읽음 알림 개수를 조회합니다.")
    @GetMapping("/v1/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@CurrentUser MemberUserDetails userDetails) {
        long unreadCount = notificationQueryService.countUnread(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    @Operation(
        summary = "알림 단건 읽음 처리",
        description = "알림을 읽음으로 표시합니다. 이미 읽은 알림에 다시 호출해도 성공합니다(멱등). "
            + "다른 회원의 알림이면 존재를 노출하지 않기 위해 404로 응답합니다."
    )
    @PutMapping("/v1/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
        @Parameter(description = "알림 ID", example = "12") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        NotificationMarkAsReadCommand command = NotificationMarkAsReadCommand.of(id, userDetails.getMemberId());
        notificationCommandUseCase.markAsRead(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "미읽음 알림을 모두 읽음으로 표시합니다(멱등).")
    @PutMapping("/v1/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUser MemberUserDetails userDetails) {
        NotificationMarkAllAsReadCommand command = NotificationMarkAllAsReadCommand.of(userDetails.getMemberId());
        notificationCommandUseCase.markAllAsRead(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
