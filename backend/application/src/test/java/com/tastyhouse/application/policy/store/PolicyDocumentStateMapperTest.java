package com.tastyhouse.application.policy.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDocumentStateMapperTest {

    @Test
    @DisplayName("PolicyDocument → PolicyDocumentState → PolicyDocument 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        PolicyDocument original = PolicyDocument.reconstitute(
            9L, PolicyType.PRIVACY_POLICY, "1.2", "개인정보처리방침", "본문", true, false,
            LocalDateTime.of(2026, 1, 1, 0, 0), "creator", "updater",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0));

        PolicyDocument restored = PolicyDocumentStateMapper.toDomain(PolicyDocumentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("current·mandatory boolean이 뒤바뀌지 않는다")
    void booleanFieldsAreNotSwapped() {
        PolicyDocument original = PolicyDocument.reconstitute(
            10L, PolicyType.AGE_VERIFICATION, "2.0", "t", "c", false, true,
            LocalDateTime.of(2026, 4, 1, 0, 0), null, null, null, null);

        PolicyDocument restored = PolicyDocumentStateMapper.toDomain(PolicyDocumentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
