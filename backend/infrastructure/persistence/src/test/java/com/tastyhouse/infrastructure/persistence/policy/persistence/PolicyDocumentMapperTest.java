package com.tastyhouse.infrastructure.persistence.policy.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDocumentMapperTest {

    @Test
    @DisplayName("PolicyDocument → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        PolicyDocumentJpaEntity entity = PolicyDocumentMapper.toEntity(fullDocument());

        assertThat(entity.getType()).isEqualTo("PRIVACY_POLICY");
        assertThat(entity.getVersion()).isEqualTo("1.2");
        assertThat(entity.getTitle()).isEqualTo("개인정보처리방침");
        assertThat(entity.getContent()).isEqualTo("본문");
        assertThat(entity.isCurrent()).isTrue();
        assertThat(entity.isMandatory()).isFalse();
        assertThat(entity.getEffectiveDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getCreatedBy()).isEqualTo("creator");
        assertThat(entity.getUpdatedBy()).isEqualTo("updater");
    }

    @Test
    @DisplayName("엔티티 → PolicyDocument 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        PolicyDocument original = fullDocument();

        PolicyDocument restored = PolicyDocumentMapper.toDomain(persisted(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("current·mandatory boolean이 뒤바뀌지 않는다")
    void booleanFieldsAreNotSwapped() {
        PolicyDocument original = PolicyDocument.reconstitute(
            10L, PolicyType.AGE_VERIFICATION, "2.0", "t", "c", false, true,
            LocalDateTime.of(2026, 4, 1, 0, 0), null, null, null, null);

        PolicyDocumentJpaEntity entity = PolicyDocumentMapper.toEntity(original);

        assertThat(entity.isCurrent()).isFalse();
        assertThat(entity.isMandatory()).isTrue();
        assertThat(entity.getCreatedBy()).isNull();
        assertThat(entity.getUpdatedBy()).isNull();
        assertThat(PolicyDocumentMapper.toDomain(persisted(original))).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 제목·본문·필수·시행일·수정자·현재 여부를 옮긴다")
    void applyChangesCopiesWritableFields() {
        PolicyDocumentJpaEntity entity = PolicyDocumentMapper.toEntity(PolicyDocument.reconstitute(
            10L, PolicyType.PRIVACY_POLICY, "1.2", "t", "c", false, true,
            LocalDateTime.of(2026, 4, 1, 0, 0), null, null, null, null));

        PolicyDocumentMapper.applyChanges(entity, fullDocument());

        assertThat(entity.getTitle()).isEqualTo("개인정보처리방침");
        assertThat(entity.getContent()).isEqualTo("본문");
        assertThat(entity.isMandatory()).isFalse();
        assertThat(entity.getEffectiveDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getUpdatedBy()).isEqualTo("updater");
        assertThat(entity.isCurrent()).isTrue();
    }

    private static PolicyDocument fullDocument() {
        return PolicyDocument.reconstitute(
            9L, PolicyType.PRIVACY_POLICY, "1.2", "개인정보처리방침", "본문", true, false,
            LocalDateTime.of(2026, 1, 1, 0, 0), "creator", "updater",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0));
    }

    private static PolicyDocumentJpaEntity persisted(PolicyDocument policyDocument) {
        PolicyDocumentJpaEntity entity = PolicyDocumentMapper.toEntity(policyDocument);
        ReflectionTestUtils.setField(entity, "id", policyDocument.getId());
        ReflectionTestUtils.setField(entity, "createdAt", policyDocument.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", policyDocument.getUpdatedAt());
        return entity;
    }
}
