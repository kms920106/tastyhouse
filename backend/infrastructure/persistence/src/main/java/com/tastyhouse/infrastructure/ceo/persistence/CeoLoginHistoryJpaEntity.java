package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "CEO_LOGIN_HISTORY")
public class CeoLoginHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private CeoLoginResult result;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 20, columnDefinition = "VARCHAR(20)")
    private CeoLoginFailureReason failureReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    protected CeoLoginHistoryJpaEntity() {
    }

    private CeoLoginHistoryJpaEntity(
        Long ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        this.ceoId = ceoId;
        this.result = result;
        this.failureReason = failureReason;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    static CeoLoginHistoryJpaEntity create(
        Long ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        return new CeoLoginHistoryJpaEntity(ceoId, result, failureReason, ipAddress, userAgent);
    }

    public Long getId() {
        return this.id;
    }

    public Long getCeoId() {
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
}
