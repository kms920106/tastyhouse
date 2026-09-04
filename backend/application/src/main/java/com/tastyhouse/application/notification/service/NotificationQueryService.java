package com.tastyhouse.application.notification.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.application.notification.port.out.NotificationQueryPort;
import com.tastyhouse.application.notification.port.in.NotificationQueryUseCase;

/**
 * 알림함 조회 서비스(CQRS query 측).
 *
 * <p>읽기 포트({@link NotificationQueryPort})만 주입해 조회하고 그 결과를 그대로 내보낸다 — 응답 조립과
 * 도메인 enum의 문자열 강등은 web-api의 Response가 맡는다.
 */
@Service
@WebApp
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationQueryPort notificationQueryPort;

    public NotificationQueryService(NotificationQueryPort notificationQueryPort) {
        this.notificationQueryPort = notificationQueryPort;
    }

    /**
     * 내 알림 목록 — 최신순.
     */
    @Override
    public PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size) {
        return notificationQueryPort.findNotificationsByMemberId(memberId, PageQuery.of(page, size));
    }

    /**
     * 내 미읽음 알림 개수 — 헤더 배지용.
     */
    @Override
    public long countUnread(Long memberId) {
        return notificationQueryPort.countUnreadByMemberId(memberId);
    }
}
