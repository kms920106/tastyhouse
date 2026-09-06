package com.tastyhouse.infrastructure.event.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "EVENT_WINNER",
    indexes = {
        @Index(name = "idx_event_winner_event_id", columnList = "event_id, is_deleted"),
        @Index(name = "idx_event_winner_announced_at", columnList = "announced_at")
    }
)
public class EventWinnerJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "winner_name", nullable = false, length = 50)
    private String winnerName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, length = 11))
    private PhoneNumber phoneNumber;

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected EventWinnerJpaEntity() {
    }

    private EventWinnerJpaEntity(
        Long eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        this.eventId = eventId;
        this.rankNo = rankNo;
        this.winnerName = winnerName;
        this.phoneNumber = phoneNumber;
        this.announcedAt = announcedAt;
        this.deleted = deleted;
    }

    static EventWinnerJpaEntity create(
        Long eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        return new EventWinnerJpaEntity(eventId, rankNo, winnerName, phoneNumber, announcedAt, deleted);
    }

    void applyChanges(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public Long getEventId() {
        return this.eventId;
    }

    public Integer getRankNo() {
        return this.rankNo;
    }

    public String getWinnerName() {
        return this.winnerName;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public LocalDateTime getAnnouncedAt() {
        return this.announcedAt;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
