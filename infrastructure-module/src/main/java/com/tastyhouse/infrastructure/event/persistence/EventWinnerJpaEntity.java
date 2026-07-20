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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 이벤트 당첨자 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code EventWinner}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code EventWinnerMapper}가 수행한다.
 */
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
public class EventWinnerJpaEntity extends BaseEntity {

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
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, length = 11))
    private PhoneNumber phoneNumber; // 휴대폰 번호 (값 객체)

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt; // 당첨 발표 일시

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted; // 삭제 여부 (Soft Delete)

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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code EventWinnerMapper#toEntity}에서만 호출한다.
     */
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

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(boolean deleted) {
        this.deleted = deleted;
    }
}
