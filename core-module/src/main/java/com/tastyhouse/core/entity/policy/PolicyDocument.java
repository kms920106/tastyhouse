package com.tastyhouse.core.entity.policy;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "POLICY_DOCUMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private PolicyType type; // 약관 유형 (예: TERMS_OF_SERVICE, PRIVACY_POLICY, MARKETING)

    @Column(name = "version", nullable = false, length = 20)
    private String version; // 약관 버전 (예: 1.0, 2.1)

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 약관 제목

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content; // 약관 본문 내용

    @Column(name = "is_current", nullable = false)
    private Boolean current = false; // 현재 적용 중인 약관 여부 (true: 현재 버전)

    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory = true; // 필수 동의 여부 (true: 필수)

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate; // 약관 시행 일시

    @Column(name = "created_by", length = 100)
    private String createdBy; // 약관 등록자

    @Column(name = "updated_by", length = 100)
    private String updatedBy; // 약관 최종 수정자

    private PolicyDocument(
        PolicyType type,
        String version,
        String title,
        String content,
        Boolean current,
        Boolean mandatory,
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

    public static PolicyDocument of(
        PolicyType type,
        String version,
        String title,
        String content,
        Boolean current,
        Boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        return new PolicyDocument(
            type,
            version,
            title,
            content,
            current,
            mandatory,
            effectiveDate,
            createdBy,
            null)
            ;
    }

    public void updateCurrent(Boolean current) {
        this.current = current;
    }

    public void update(
        String title,
        String content,
        Boolean mandatory,
        LocalDateTime effectiveDate,
        String updatedBy
    ) {
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.updatedBy = updatedBy;
    }
}
