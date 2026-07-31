package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.shared.vo.PhoneNumber;

/**
 * 이벤트 당첨자 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code EventWinnerJpaEntity} + {@code EventWinnerMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code EventWinnerRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class EventWinner {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long eventId; // 이벤트 ID (EVENT.id 참조)
    private final Integer rankNo; // 당첨 순위
    private final String winnerName; // 당첨자 이름
    private final PhoneNumber phoneNumber; // 휴대폰 번호 (값 객체)
    private final LocalDateTime announcedAt; // 당첨 발표 일시
    private boolean deleted; // 삭제 여부 (Soft Delete)

    private EventWinner(
        Long id,
        Long eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        this.id = id;
        this.eventId = eventId;
        this.rankNo = rankNo;
        this.winnerName = winnerName;
        this.phoneNumber = phoneNumber;
        this.announcedAt = announcedAt;
        this.deleted = deleted;
    }

    /**
     * 신규 당첨자를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static EventWinner of(
        Long eventId,
        Integer rankNo,
        String winnerName,
        String phoneNumber,
        LocalDateTime announcedAt
    ) {
        return new EventWinner(
            null,
            eventId,
            rankNo,
            winnerName,
            new PhoneNumber(phoneNumber),
            announcedAt,
            false
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static EventWinner reconstitute(
        Long id,
        Long eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        return new EventWinner(id, eventId, rankNo, winnerName, phoneNumber, announcedAt, deleted);
    }

    public void delete() {
        this.deleted = true;
    }
}
