package com.tastyhouse.infrastructure.policy.persistence;

import java.time.LocalDateTime;

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

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 정책 문서 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PolicyDocument}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PolicyDocumentMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "POLICY_DOCUMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyDocumentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private PolicyType type;

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

    private PolicyDocumentJpaEntity(
        PolicyType type,
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PolicyDocumentMapper#toEntity}에서만 호출한다.
     */
    static PolicyDocumentJpaEntity create(
        PolicyType type,
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

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드(type/version/createdBy)는 건드리지 않는다.
     */
    void applyChanges(String title, String content, boolean mandatory, LocalDateTime effectiveDate, String updatedBy, boolean current) {
        this.title = title;
        this.content = content;
        this.mandatory = mandatory;
        this.effectiveDate = effectiveDate;
        this.updatedBy = updatedBy;
        this.current = current;
    }
}
