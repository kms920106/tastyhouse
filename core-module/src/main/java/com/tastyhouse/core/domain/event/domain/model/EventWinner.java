package com.tastyhouse.core.domain.event.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;
import com.tastyhouse.core.shared.vo.PhoneNumber;

@Getter
@Entity
@Table(
    name = "EVENT_WINNER",
    indexes = {
        @Index(name = "idx_event_winner_event_id", columnList = "event_id, is_deleted"),
        @Index(name = "idx_event_winner_announced_at", columnList = "announced_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventWinner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "event_id", nullable = false)
    private Long eventId; // 이벤트 ID (EVENT.id 참조)

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo; // 당첨 순위

    @Column(name = "winner_name", nullable = false, length = 50)
    private String winnerName; // 당첨자 이름

    @Embedded
    private PhoneNumber phoneNumber; // 휴대폰 번호 (값 객체)

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt; // 당첨 발표 일시

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false; // 삭제 여부 (Soft Delete)

    private EventWinner(
        Long eventId,
        Integer rankNo,
        String winnerName,
        String phoneNumber,
        LocalDateTime announcedAt
    ) {
        this.eventId = eventId;
        this.rankNo = rankNo;
        this.winnerName = winnerName;
        this.phoneNumber = new PhoneNumber(phoneNumber);
        this.announcedAt = announcedAt;
    }

    public static EventWinner of(
        Long eventId,
        Integer rankNo,
        String winnerName,
        String phoneNumber,
        LocalDateTime announcedAt
    ) {
        return new EventWinner(
            eventId,
            rankNo,
            winnerName,
            phoneNumber,
            announcedAt
        );
    }

    public void delete() {
        this.deleted = true;
    }
}
