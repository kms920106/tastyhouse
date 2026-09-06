package com.tastyhouse.domain.policy.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

public class PolicyDocument {
    private final Long id;
    private final PolicyType type;
    private final String version;
    private String title;
    private String content;
    private boolean mandatory;
    private LocalDateTime effectiveDate;
    private boolean current;
    private final String createdBy;
    private String updatedBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private PolicyDocument(
        Long id,
        PolicyType type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        boolean current,
        String createdBy,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.current = current;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PolicyDocument of(
        PolicyType type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        return new PolicyDocument(null, type, version, title, content, mandatory, effectiveDate, false, createdBy, null, null, null);
    }

    public static PolicyDocument reconstitute(
        Long id,
        PolicyType type,
        String version,
        String title,
        String content,
        boolean current,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new PolicyDocument(id, type, version, title, content, mandatory, effectiveDate, current, createdBy, updatedBy, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public PolicyType getType() {
        return this.type;
    }

    public String getVersion() {
        return this.version;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public LocalDateTime getEffectiveDate() {
        return this.effectiveDate;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public PolicyDocumentId getPolicyDocumentId() {
        return PolicyDocumentId.of(this.id);
    }

    public void activate() {
        this.current = true;
    }

    public void deactivate() {
        this.current = false;
    }

    public void update(String title, String content, boolean mandatory, LocalDateTime effectiveDate, String updatedBy) {
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.updatedBy = updatedBy;
    }
}
