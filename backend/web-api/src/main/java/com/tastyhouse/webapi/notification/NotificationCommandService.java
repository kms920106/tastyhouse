package com.tastyhouse.webapi.notification;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.service.NotificationService;
import com.tastyhouse.domain.notification.vo.NotificationId;

/**
 * 알림함 명령 서비스(CQRS command 측).
 *
 * <p>수신자 소유권 검증(IDOR 방어)과 읽음 멱등성은 도메인 서비스({@link NotificationService})가 소유하고,
 * 이 서비스는 식별자 승격·읽은 시각 해석·트랜잭션 경계만 책임진다.
 *
 * <p>{@code ..query..}를 주입하지 않는다(CQRS 교차 주입 금지).
 */
@Service
@Transactional
public class NotificationCommandService {

    private final NotificationService notificationService;

    public NotificationCommandService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 단건 읽음 처리(멱등). 남의 알림이면 {@code NOTIFICATION_NOT_FOUND}(404)다.
     */
    public void markAsRead(Long id, Long memberId) {
        NotificationId notificationId = NotificationId.of(id);
        MemberId targetMemberId = MemberId.of(memberId);
        notificationService.markAsRead(notificationId, targetMemberId, LocalDateTime.now());
    }

    /**
     * 내 미읽음 알림을 모두 읽음 처리한다(멱등).
     */
    public void markAllAsRead(Long memberId) {
        MemberId targetMemberId = MemberId.of(memberId);
        notificationService.markAllAsRead(targetMemberId, LocalDateTime.now());
    }
}
