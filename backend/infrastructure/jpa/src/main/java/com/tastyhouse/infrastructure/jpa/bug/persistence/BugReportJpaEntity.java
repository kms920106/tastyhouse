package com.tastyhouse.infrastructure.jpa.bug.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "BUG_REPORT",
    indexes = {
        @Index(name = "idx_bug_report_member_id", columnList = "member_id"),
        @Index(name = "idx_bug_report_status", columnList = "status")
    }
)
class BugReportJpaEntity extends BaseEntity {

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

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "category", length = 20, columnDefinition = "VARCHAR(20)")
    private String category;

    @Column(name = "priority", length = 20, columnDefinition = "VARCHAR(20)")
    private String priority;

    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    @Column(name = "admin_answer", columnDefinition = "TEXT")
    private String adminAnswer;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "app_version", length = 30)
    private String appVersion;

    @Column(name = "platform", length = 20, columnDefinition = "VARCHAR(20)")
    private String platform;

    @Column(name = "os_version", length = 30)
    private String osVersion;

    protected BugReportJpaEntity() {
    }

    private BugReportJpaEntity(
        Long memberId,
        String device,
        String title,
        String content,
        String status,
        String category,
        String priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        String platform,
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
        String status,
        String category,
        String priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        String platform,
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
        String status,
        String category,
        String priority,
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

    public String getStatus() {
        return this.status;
    }

    public String getCategory() {
        return this.category;
    }

    public String getPriority() {
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

    public String getPlatform() {
        return this.platform;
    }

    public String getOsVersion() {
        return this.osVersion;
    }
}
