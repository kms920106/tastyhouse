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

/**
 * 점주 로그인 이력 JPA 영속 모델(append-only). 순수 도메인 모델 {@code CeoLoginHistory}와 분리된 영속
 * 전용 엔티티다.
 *
 * <p>enum 필드는 {@code @Enumerated(EnumType.STRING)}과 {@code columnDefinition = "VARCHAR(n)"}을
 * 병기한다 — {@code columnDefinition}을 빠뜨리면 Hibernate 6의 {@code MySQLDialect}가 네이티브
 * {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}에서 부팅이 실패한다. {@code n}은 {@code schema.sql}과
 * 일치해야 한다(result/failureReason 모두 20).
 */
@Entity
@Table(name = "CEO_LOGIN_HISTORY")
public class CeoLoginHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId; // 점주 ID (CEO.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private CeoLoginResult result; // 결과 (SUCCESS, FAILURE)

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 20, columnDefinition = "VARCHAR(20)")
    private CeoLoginFailureReason failureReason; // 실패 사유 (성공 시 NULL)

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // 접속 IP (IPv6 최대 45자, 판별 불가 시 NULL)

    @Column(name = "user_agent", length = 500)
    private String userAgent; // 접속 기기 정보 (500자 초과분은 Recorder가 절단)

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
