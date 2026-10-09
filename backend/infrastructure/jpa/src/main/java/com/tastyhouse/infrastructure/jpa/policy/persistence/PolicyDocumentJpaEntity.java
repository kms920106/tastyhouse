package com.tastyhouse.infrastructure.jpa.policy.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "POLICY_DOCUMENT")
class PolicyDocumentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String type;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    protected PolicyDocumentJpaEntity() {
    }

    private PolicyDocumentJpaEntity(
        String type,
        String version,
        String title,
        String content,
        boolean current,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy,
        String updatedBy
    ) {
        this.type = type;
        this.version = version;
        this.title = title;
        this.content = content;
        this.current = current;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    static PolicyDocumentJpaEntity create(
        String type,
        String version,
        String title,
        String content,
        boolean current,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy,
        String updatedBy
    ) {
        return new PolicyDocumentJpaEntity(type, version, title, content, current, mandatory, effectiveDate, createdBy, updatedBy);
    }

    void applyChanges(String title, String content, boolean mandatory, LocalDateTime effectiveDate, String updatedBy, boolean current) {
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.updatedBy = updatedBy;
        this.current = current;
    }

    public Long getId() {
        return this.id;
    }

    public String getType() {
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

    public boolean isCurrent() {
        return this.current;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public LocalDateTime getEffectiveDate() {
        return this.effectiveDate;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }
}
