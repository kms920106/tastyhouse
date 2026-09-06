package com.tastyhouse.domain.notification.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 인앱 알림함의 알림 1건(순수 도메인 모델).
 *
 * <p>읽음 상태({@code read}·{@code readAt})만 가변이고 나머지는 전부 {@code final}이다 — 알림은 발생
 * 시점의 사실을 기록한 것이라 내용이 나중에 바뀌면 "내가 받은 알림"과 어긋난다. 원본(리뷰·답변)이
 * 수정되어도 알림 문구는 그대로 두는 것이 의도된 동작이다.
 *
 * <p><b>{@code MEMBER.push_notification_enabled}를 여기에 적용하지 않는다.</b> 그 플래그는 푸시
 * <b>수신 동의</b>이고 알림함은 사용자가 앱 안에서 직접 열어 보는 것이라 성격이 다르다. 추후 FCM 발송을
 * 붙일 때 그 경로에만 적용한다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code NotificationJpaEntity} + {@code NotificationMapper}가 담당하며, 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 명시적 {@code save} 호출이다.
 */
public class Notification {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final NotificationTargetType targetType; // 이동 대상이 없으면 null
    private final Long targetId; // 이동 대상이 없으면 null
    private boolean read;
    private LocalDateTime readAt; // 아직 읽지 않았으면 null
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Notification(
        Long id,
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 알림을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없고, 항상 미읽음 상태로 시작한다.
     *
     * <p>{@code targetType}/{@code targetId}는 함께 null일 수 있다(이동 대상 없는 알림).
     */
    public static Notification of(
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId
    ) {
        return new Notification(null, memberId, type, title, body, targetType, targetId, false, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며, 불변식을 우회한
     * 임의 생성을 막기 위해 이 팩토리로만 식별자·읽음 상태·감사 시각을 주입한다.
     */
    public static Notification reconstitute(
        Long id,
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Notification(id, memberId, type, title, body, targetType, targetId, read, readAt, createdAt, updatedAt);
    }

    /**
     * 읽음 처리한다.
     *
     * <p><b>멱등이다</b> — 이미 읽은 알림에 다시 호출해도 예외 없이 성공하고 {@code readAt}은 최초 1회만
     * 기록한다. 알림함은 화면을 열 때마다 읽음을 보내는 성격이라 재호출을 실패로 다루면 정상 흐름이 깨지고,
     * {@code readAt}을 갱신하면 "언제 처음 봤는가"라는 값의 의미가 사라진다.
     */
    public void markAsRead(LocalDateTime readAt) {
        if (this.read) {
            return;
        }
        this.read = true;
        this.readAt = readAt;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public NotificationType getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public NotificationTargetType getTargetType() {
        return this.targetType;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public boolean isRead() {
        return this.read;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
