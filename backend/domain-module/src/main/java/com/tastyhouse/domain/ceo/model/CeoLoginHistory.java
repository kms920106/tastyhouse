package com.tastyhouse.domain.ceo.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;

/**
 * 점주 로그인 이력 순수 도메인 모델(append-only).
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code CeoLoginHistoryJpaEntity} + {@code CeoLoginHistoryMapper}가 담당한다. 기록 후에는 바뀌지
 * 않으므로 전 필드가 final이고 상태전이 메서드가 없다.
 *
 * <p>1행의 단위는 "로그인 시도 1회"다. 점주가 로그인하면 주문자 이름·연락처·주소 같은 회원 개인정보를
 * 열람할 수 있으므로, 로그인 시점이 곧 개인정보처리시스템 접속 시점이다.
 *
 * <p>{@code ipAddress}/{@code userAgent}는 nullable이다 — IP는 프록시 구성에 따라 판별하지 못할 수 있고,
 * User-Agent는 클라이언트가 보내지 않을 수 있다. 기록 자체를 막을 사유가 아니므로 null로 남긴다.
 */
public class CeoLoginHistory {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final CeoId ceoId;
    private final CeoLoginResult result;
    private final CeoLoginFailureReason failureReason; // 성공 시 null
    private final String ipAddress; // 판별 불가 시 null
    private final String userAgent; // 미전송 시 null
    private final LocalDateTime createdAt; // = 로그인 시각. 재구성 전 신규 상태는 null

    private CeoLoginHistory(
        Long id,
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.result = result;
        this.failureReason = failureReason;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    /**
     * 신규 이력을 만든다. {@code failureReason}은 실패({@link CeoLoginResult#FAILURE})일 때만 채운다.
     */
    public static CeoLoginHistory of(
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        return new CeoLoginHistory(null, ceoId, result, failureReason, ipAddress, userAgent, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static CeoLoginHistory reconstitute(
        Long id,
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
    ) {
        return new CeoLoginHistory(id, ceoId, result, failureReason, ipAddress, userAgent, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public CeoLoginResult getResult() {
        return this.result;
    }

    public CeoLoginFailureReason getFailureReason() {
        return this.failureReason;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
