package com.tastyhouse.domain.bug.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class BugReport {
    private final Long id;
    private final MemberId memberId;
    private final String device;
    private final String title;
    private final String content;
    private BugReportStatus status;
    private BugReportCategory category;
    private BugReportPriority priority;
    private AdminId assigneeAdminId;
    private String adminAnswer;
    private LocalDateTime resolvedAt;
    private final String appVersion;
    private final BugReportPlatform platform;
    private final String osVersion;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private BugReport(
        Long id,
        MemberId memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        AdminId assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BugReport of(
        MemberId memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion
    ) {
        return new BugReport(
            null, memberId, device, title, content,
            BugReportStatus.RECEIVED, null, null, null, null, null,
            appVersion, platform, osVersion, null, null
        );
    }

    public static BugReport reconstitute(
        Long id,
        MemberId memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        AdminId assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new BugReport(
            id, memberId, device, title, content,
            status, category, priority, assigneeAdminId, adminAnswer, resolvedAt,
            appVersion, platform, osVersion, createdAt, updatedAt
        );
    }

    public BugReportId getBugReportId() {
        return BugReportId.of(id);
    }

    public void classify(BugReportCategory category, BugReportPriority priority) {
        this.category = category;
        this.priority = priority;
    }

    public void assignTo(AdminId adminId) {
        this.assigneeAdminId = adminId;
    }

    public void startProgress() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.ON_HOLD) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.IN_PROGRESS;
    }

    public void resolve(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.RESOLVED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject(String answer) {
        if (this.status == BugReportStatus.RESOLVED || this.status == BugReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.REJECTED;
        this.adminAnswer = answer;
        this.resolvedAt = LocalDateTime.now();
    }

    public void hold() {
        if (this.status != BugReportStatus.RECEIVED && this.status != BugReportStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
        this.status = BugReportStatus.ON_HOLD;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
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

    public AdminId getAssigneeAdminId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
