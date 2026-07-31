package com.tastyhouse.domain.policy.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.policy.domain.vo.PolicyDocumentId;

/**
 * 정책 문서 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PolicyDocumentJpaEntity} + {@code PolicyDocumentMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code PolicyDocumentRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class PolicyDocument {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final PolicyType type;
    private final String version;
    private String title;
    private String content;
    private boolean mandatory;
    private LocalDateTime effectiveDate;
    private boolean current;
    private final String createdBy;
    private String updatedBy;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

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

    /**
     * 신규 정책 문서를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
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
