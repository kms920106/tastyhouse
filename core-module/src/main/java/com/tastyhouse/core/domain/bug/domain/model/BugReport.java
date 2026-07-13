package com.tastyhouse.core.domain.bug.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(
    name = "BUG_REPORT",
    indexes = {
        @Index(name = "idx_bug_report_member_id", columnList = "member_id"),
        @Index(name = "idx_bug_report_status", columnList = "status")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BugReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "device", nullable = false, length = 100)
    private String device;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private BugReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, columnDefinition = "VARCHAR(20)")
    private BugReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, columnDefinition = "VARCHAR(20)")
    private BugReportPriority priority;

    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    @Column(name = "admin_answer", columnDefinition = "TEXT")
    private String adminAnswer;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "app_version", length = 30)
    private String appVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, columnDefinition = "VARCHAR(20)")
    private BugReportPlatform platform;

    @Column(name = "os_version", length = 30)
    private String osVersion;

    private BugReport(
        Long memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        this.memberId = memberId;
        this.device = device;
        this.title = title;
        this.content = content;
        this.appVersion = appVersion;
        this.platform = platform;
        this.osVersion = osVersion;
        this.status = BugReportStatus.RECEIVED;
    }

    public static BugReport create(
        Long memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        return new BugReport(memberId, device, title, content, appVersion, platform, osVersion);
    }

    public BugReportId getBugReportId() {
        return BugReportId.of(id);
    }

    /**
     * 트리아지: 분류/우선순위 지정. 어떤 상태에서도 재분류 가능.
     */
    public void classify(BugReportCategory category, BugReportPriority priority) {
        this.category = category;
        this.priority = priority;
    }

    /**
     * 담당자 배정. 어떤 상태에서도 재배정 가능.
     */
    public void assignTo(Long adminId) {
        this.assigneeAdminId = adminId;
    }

    /**
     * 처리 시작: RECEIVED|ON_HOLD -> IN_PROGRESS
     */
    public void startProgress() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.ON_HOLD) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.IN_PROGRESS;
    }

    /**
     * 처리 완료: RECEIVED|IN_PROGRESS|ON_HOLD -> RESOLVED (처리 결과 기록, 완료 시각 세팅)
     */
    public void resolve(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.RESOLVED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * 반려: RECEIVED|IN_PROGRESS|ON_HOLD -> REJECTED (반려 사유 기록, 종결 시각 세팅)
     */
    public void reject(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.REJECTED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * 보류: RECEIVED|IN_PROGRESS -> ON_HOLD
     */
    public void hold() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.ON_HOLD;
    }
}
