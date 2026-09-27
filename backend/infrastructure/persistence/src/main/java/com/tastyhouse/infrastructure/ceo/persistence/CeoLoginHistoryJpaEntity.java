package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "CEO_LOGIN_HISTORY")
public class CeoLoginHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "result", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String result;

    @Column(name = "failure_reason", length = 20, columnDefinition = "VARCHAR(20)")
    private String failureReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    protected CeoLoginHistoryJpaEntity() {
    }

    private CeoLoginHistoryJpaEntity(
        Long ceoId,
        String result,
        String failureReason,
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
        String result,
        String failureReason,
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

    public String getResult() {
        return this.result;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }
}
