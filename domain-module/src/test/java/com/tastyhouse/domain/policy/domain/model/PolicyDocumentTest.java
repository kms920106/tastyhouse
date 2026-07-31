package com.tastyhouse.domain.policy.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.policy.domain.vo.PolicyDocumentId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class PolicyDocumentTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 비활성 상태다")
    void of_createsTransientPolicyDocument() {
        LocalDateTime effectiveDate = LocalDateTime.of(2026, 1, 1, 0, 0);

        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.TERMS_OF_SERVICE, "1.0", "제목", "내용", true, effectiveDate, "admin"
        );

        assertThat(policyDocument.getId()).isNull();
        assertThat(policyDocument.getType()).isEqualTo(PolicyType.TERMS_OF_SERVICE);
        assertThat(policyDocument.getVersion()).isEqualTo("1.0");
        assertThat(policyDocument.getTitle()).isEqualTo("제목");
        assertThat(policyDocument.getContent()).isEqualTo("내용");
        assertThat(policyDocument.isMandatory()).isTrue();
        assertThat(policyDocument.getEffectiveDate()).isEqualTo(effectiveDate);
        assertThat(policyDocument.isCurrent()).isFalse();
        assertThat(policyDocument.getCreatedBy()).isEqualTo("admin");
        assertThat(policyDocument.getUpdatedBy()).isNull();
        assertThat(policyDocument.getCreatedAt()).isNull();
        assertThat(policyDocument.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("activate는 current를 true로, deactivate는 false로 만든다")
    void activate_and_deactivate_toggleCurrent() {
        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.PRIVACY_POLICY, "1.0", "제목", "내용", false, LocalDateTime.now(), "admin"
        );

        policyDocument.activate();
        assertThat(policyDocument.isCurrent()).isTrue();

        policyDocument.deactivate();
        assertThat(policyDocument.isCurrent()).isFalse();
    }

    @Test
    @DisplayName("update는 title·content·mandatory·effectiveDate·updatedBy를 변경하고 type·version·createdBy는 그대로 유지한다")
    void update_changesMutableFieldsOnly() {
        LocalDateTime originalEffectiveDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime newEffectiveDate = LocalDateTime.of(2026, 6, 1, 0, 0);
        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.AGE_VERIFICATION, "1.0", "제목", "내용", false, originalEffectiveDate, "admin"
        );

        policyDocument.update("새 제목", "새 내용", true, newEffectiveDate, "editor");

        assertThat(policyDocument.getTitle()).isEqualTo("새 제목");
        assertThat(policyDocument.getContent()).isEqualTo("새 내용");
        assertThat(policyDocument.isMandatory()).isTrue();
        assertThat(policyDocument.getEffectiveDate()).isEqualTo(newEffectiveDate);
        assertThat(policyDocument.getUpdatedBy()).isEqualTo("editor");
        assertThat(policyDocument.getType()).isEqualTo(PolicyType.AGE_VERIFICATION);
        assertThat(policyDocument.getVersion()).isEqualTo("1.0");
        assertThat(policyDocument.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime effectiveDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        PolicyDocument policyDocument = PolicyDocument.reconstitute(
            1L, PolicyType.ELECTRONIC_FINANCIAL_TRANSACTIONS, "1.0", "제목", "내용",
            true, true, effectiveDate, "admin", "editor", createdAt, updatedAt
        );

        assertThat(policyDocument.getId()).isEqualTo(1L);
        assertThat(policyDocument.getPolicyDocumentId()).isEqualTo(PolicyDocumentId.of(1L));
        assertThat(policyDocument.isCurrent()).isTrue();
        assertThat(policyDocument.getCreatedAt()).isEqualTo(createdAt);
        assertThat(policyDocument.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getPolicyDocumentId를 호출하면 PolicyDocumentId 불변식 위반으로 예외가 발생한다")
    void getPolicyDocumentId_onTransient_throws() {
        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.TERMS_OF_SERVICE, "1.0", "제목", "내용", true, LocalDateTime.now(), "admin"
        );

        assertThatThrownBy(policyDocument::getPolicyDocumentId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
