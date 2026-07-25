package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;

/**
 * 금칙어 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProhibitedWordJpaEntity} + {@code ProhibitedWordMapper}가 담당한다.
 * Java 애플리케이션 계층에 생성/변경 경로가 없는 읽기 전용 애그리거트(SQL/수동 시드)이므로
 * 신규 생성 팩토리({@code of})는 두지 않는다.
 */
@Getter
public class ProhibitedWord {

    private final Long id;
    private final String word; // 금칙어
    private final String reason; // 등록 불가 사유 분류 (nullable)

    private ProhibitedWord(Long id, String word, String reason) {
        this.id = id;
        this.word = word;
        this.reason = reason;
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static ProhibitedWord reconstitute(Long id, String word, String reason) {
        return new ProhibitedWord(id, word, reason);
    }
}
