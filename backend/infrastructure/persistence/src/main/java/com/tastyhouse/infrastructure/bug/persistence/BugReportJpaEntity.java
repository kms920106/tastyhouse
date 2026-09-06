package com.tastyhouse.infrastructure.bug.persistence;

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

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "BUG_REPORT",
    indexes = {
        @Index(name = "idx_bug_report_member_id", columnList = "member_id"),
        @Index(name = "idx_bug_report_status", columnList = "status")
    }
)
public class BugReportJpaEntity extends BaseEntity {
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

    protected BugReportJpaEntity() {
    }

    private BugReportJpaEntity(
        Long memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        this.memberId = memberId;
        this.device = device;
        this.title = title;
        this.content = content;
        this.status = status;
        this.category = category;
        this.priority = priority;
        this.assigneeAdminId = assigneeAdminId;
        this.adminAnswer = adminAnswer;
        this.resolvedAt = resolvedAt;
        this.appVersion = appVersion;
        this.platform = platform;
        this.osVersion = osVersion;
    }

    static BugReportJpaEntity create(
        Long memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        return new BugReportJpaEntity(
            memberId, device, title, content,
            status, category, priority, assigneeAdminId, adminAnswer, resolvedAt,
            appVersion, platform, osVersion
        );
    }

    void applyChanges(
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt
    ) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.category = category;
        this.priority = priority;
        this.assigneeAdminId = assigneeAdminId;
        this.adminAnswer = adminAnswer;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public String getDevice() {
        return this.device;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public BugReportStatus getStatus() {
        return this.status;
    }

    public BugReportCategory getCategory() {
        return this.category;
    }

    public BugReportPriority getPriority() {
        return this.priority;
    }

    public Long getAssigneeAdminId() {
        return this.assigneeAdminId;
    }

    public String getAdminAnswer() {
        return this.adminAnswer;
    }

    public LocalDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public BugReportPlatform getPlatform() {
        return this.platform;
    }

    public String getOsVersion() {
        return this.osVersion;
    }
}
