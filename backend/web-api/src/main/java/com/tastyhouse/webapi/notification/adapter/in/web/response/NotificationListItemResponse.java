package com.tastyhouse.webapi.notification.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

@Schema(description = "알림함 목록 항목 응답")
public record NotificationListItemResponse(
    @Schema(description = "알림 ID", example = "12")
    Long id,

    @Schema(
        description = "알림 유형",
        allowableValues = {"REVIEW_OWNER_REPLY"},
        example = "REVIEW_OWNER_REPLY"
    )
    String type,

    @Schema(description = "알림 제목", example = "사장님 답변이 등록되었어요")
    String title,

    @Schema(description = "알림 본문", example = "BBQ치킨 성내점 사장님이 회원님의 리뷰에 답변을 남겼어요.")
    String body,

    @Schema(
        description = "이동 대상 유형. 이동 대상이 없으면 null입니다.",
        allowableValues = {"REVIEW"},
        example = "REVIEW"
    )
    String targetType,

    @Schema(description = "이동 대상 식별자. 이동 대상이 없으면 null입니다.", example = "482")
    Long targetId,

    @Schema(description = "읽음 여부", example = "false")
    boolean read,

    @Schema(description = "알림 생성 일시", example = "2026-06-20T14:03:00")
    LocalDateTime createdAt
) {
    public static NotificationListItemResponse from(NotificationListItemResult result) {
        NotificationType type = result.type();
        NotificationTargetType targetType = result.targetType();
        return new NotificationListItemResponse(
            result.id(),
            type == null ? null : type.name(),
            result.title(),
            result.body(),
            targetType == null ? null : targetType.name(),
            result.targetId(),
            result.read(),
            result.createdAt()
        );
    }
}
