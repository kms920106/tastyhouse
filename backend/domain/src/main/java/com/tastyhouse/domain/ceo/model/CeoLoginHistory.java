package com.tastyhouse.domain.ceo.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;

public class CeoLoginHistory {
    private final Long id;
    private final CeoId ceoId;
    private final CeoLoginResult result;
    private final CeoLoginFailureReason failureReason;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime createdAt;

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

    public static CeoLoginHistory of(
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        return new CeoLoginHistory(null, ceoId, result, failureReason, ipAddress, userAgent, null);
    }

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
