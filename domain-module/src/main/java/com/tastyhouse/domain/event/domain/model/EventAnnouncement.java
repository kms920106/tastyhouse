package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.domain.vo.EventId;

/**
 * 이벤트 당첨자 발표 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code EventAnnouncementJpaEntity} + {@code EventAnnouncementMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code EventAnnouncementRepository#save}를 호출해야 한다.
 */
public class EventAnnouncement {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final EventId eventId; // 이벤트 ID (EVENT.id 참조)
    private String name; // 당첨자 발표 제목
    private String content; // 당첨자 발표 내용
    private LocalDateTime announcedAt; // 당첨자 발표 일시

    private EventAnnouncement(
        Long id,
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.content = content;
        this.announcedAt = announcedAt;
    }

    /**
     * 신규 발표를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static EventAnnouncement of(
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncement(null, eventId, name, content, announcedAt);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static EventAnnouncement reconstitute(
        Long id,
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncement(id, eventId, name, content, announcedAt);
    }

    public void update(
        String name,
        String contentHtml,
        LocalDateTime announcedAt
    ) {
        this.name = name;
        this.content = contentHtml;
        this.announcedAt = announcedAt;
    }

    public Long getId() {
        return this.id;
    }

    public EventId getEventId() {
        return this.eventId;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getAnnouncedAt() {
        return this.announcedAt;
    }
}
