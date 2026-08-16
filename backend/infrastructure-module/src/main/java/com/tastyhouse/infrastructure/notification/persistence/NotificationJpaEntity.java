package com.tastyhouse.infrastructure.notification.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 인앱 알림 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Notification}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code NotificationMapper}가 수행한다.
 *
 * <p><b>enum 컬럼에 {@code columnDefinition = "VARCHAR(30)"}을 반드시 병기한다.</b> 빠뜨리면
 * {@code MySQLDialect}가 네이티브 {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}가
 * "wrong column type ... but expecting [enum (...)]"로 부팅을 거부한다.
 */
@Entity
@Table(name = "NOTIFICATION")
public class NotificationJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30, columnDefinition = "VARCHAR(30)")
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    protected NotificationJpaEntity() {
    }

    private NotificationJpaEntity(
        Long memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt
    ) {
        this.memberId = memberId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.readAt = readAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code NotificationMapper#toEntity}에서만 호출한다.
     */
    static NotificationJpaEntity create(
        Long memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt
    ) {
        return new NotificationJpaEntity(memberId, type, title, body, targetType, targetId, read, readAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 알림에서 가변인 것은
     * 읽음 상태뿐이므로 그 둘만 복사하며, 감사 필드·식별자·본문은 건드리지 않는다.
     */
    void applyChanges(boolean read, LocalDateTime readAt) {
        this.read = read;
        this.readAt = readAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
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
}
