package com.tastyhouse.webapi.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.notification.query.NotificationListItemResult;
import com.tastyhouse.infrastructure.notification.query.NotificationQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.notification.response.NotificationListItemResponse;

/**
 * 알림함 조회 서비스(CQRS query 측).
 *
 * <p>infra read 어댑터({@link NotificationQueryDao})만 주입해 조회하고 Response를 조립한다(private 매퍼).
 * 도메인 enum은 HTTP 경계로 내보내지 않으므로 이 계층에서 상수명 문자열로 낮춘다.
 */
@Service
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationQueryDao notificationQueryDao;

    public NotificationQueryService(NotificationQueryDao notificationQueryDao) {
        this.notificationQueryDao = notificationQueryDao;
    }

    /**
     * 내 알림 목록 — 최신순.
     */
    public PaginationResponse<NotificationListItemResponse> findNotifications(Long memberId, int page, int size) {
        PageResult<NotificationListItemResult> pageResult =
            notificationQueryDao.findNotificationsByMemberId(memberId, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toNotificationListItemResponse));
    }

    /**
     * 내 미읽음 알림 개수 — 헤더 배지용.
     */
    public long countUnread(Long memberId) {
        return notificationQueryDao.countUnreadByMemberId(memberId);
    }

    private NotificationListItemResponse toNotificationListItemResponse(NotificationListItemResult result) {
        NotificationType type = result.type();
        NotificationTargetType targetType = result.targetType();
        return NotificationListItemResponse.from(
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
