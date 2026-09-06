package com.tastyhouse.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.vo.NotificationId;

/**
 * 알림 write 포트.
 *
 * <p>목록·미읽음 개수 등 표현 목적 조회는 infrastructure-module의 {@code NotificationQueryDao}가 맡고,
 * 여기에는 상태 전이에 필요한 조회만 남긴다(write 포트 잔류 판정 기준).
 */
public interface NotificationRepository {

    Optional<Notification> findById(NotificationId notificationId);

    /**
     * 전체 읽음 처리 대상 — 해당 회원의 <b>미읽음</b> 알림만 로드한다.
     *
     * <p>이미 읽은 것까지 로드하면 {@code readAt}을 최초 1회만 기록한다는 규칙 덕에 값은 안 바뀌지만,
     * 불필요한 행을 통째로 읽고 저장하게 되므로 미읽음으로 좁힌다.
     */
    List<Notification> findUnreadByMemberId(MemberId memberId);

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);
}
