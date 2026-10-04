package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.model.CeoStatus;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class CeoMapperTest {

    @Test
    @DisplayName("Ceo → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void ceoToEntity() {
        CeoJpaEntity entity = CeoMapper.toEntity(ceo());

        assertThat(entity.getUsername()).isEqualTo("ceo_user");
        assertThat(entity.getPassword()).isEqualTo("{bcrypt}hash");
        assertThat(entity.getName()).isEqualTo("홍길동");
        assertThat(entity.getBusinessRegistrationNumber()).isEqualTo("123-45-67890");
        assertThat(entity.getPhoneNumber().value()).isEqualTo("01055556666");
        assertThat(entity.getEmail()).isEqualTo("ceo@example.com");
        assertThat(entity.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("엔티티 → Ceo 변환 시 id를 포함한 모든 필드가 보존된다")
    void ceoToDomain() {
        Ceo original = ceo();
        CeoJpaEntity entity = CeoMapper.toEntity(original);
        setAuditFields(entity, 1L);

        Ceo restored = CeoMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("Ceo의 nullable 필드가 null이면 엔티티 컬럼도 null이다")
    void ceoToEntityWithNulls() {
        CeoJpaEntity entity = CeoMapper.toEntity(ceoWithNulls());

        assertThat(entity.getBusinessRegistrationNumber()).isNull();
        assertThat(entity.getPhoneNumber()).isNull();
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("엔티티의 nullable 컬럼이 null이어도 Ceo로 변환된다")
    void ceoToDomainWithNulls() {
        Ceo original = ceoWithNulls();
        CeoJpaEntity entity = CeoMapper.toEntity(original);
        setAuditFields(entity, 2L);

        Ceo restored = CeoMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("CeoLoginHistory → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void loginHistoryToEntity() {
        CeoLoginHistoryJpaEntity entity = CeoLoginHistoryMapper.toEntity(loginHistory());

        assertThat(entity.getCeoId()).isEqualTo(4L);
        assertThat(entity.getResult()).isEqualTo("FAILURE");
        assertThat(entity.getFailureReason()).isEqualTo("ACCOUNT_INACTIVE");
        assertThat(entity.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(entity.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("엔티티 → CeoLoginHistory 변환 시 id·생성 시각을 포함한 모든 필드가 보존된다")
    void loginHistoryToDomain() {
        CeoLoginHistory original = loginHistory();
        CeoLoginHistoryJpaEntity entity = CeoLoginHistoryMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 3L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 2, 3, 4, 6));

        CeoLoginHistory restored = CeoLoginHistoryMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("CeoReplyPhrase → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void replyPhraseToEntity() {
        CeoReplyPhraseJpaEntity entity = CeoReplyPhraseMapper.toEntity(replyPhrase());

        assertThat(entity.getCeoId()).isEqualTo(6L);
        assertThat(entity.getName()).isEqualTo("감사 인사");
        assertThat(entity.getContent()).isEqualTo("방문해 주셔서 감사합니다");
        assertThat(entity.getSort()).isEqualTo(3);
    }

    @Test
    @DisplayName("엔티티 → CeoReplyPhrase 변환 시 id·생성·수정 시각을 포함한 모든 필드가 보존된다")
    void replyPhraseToDomain() {
        CeoReplyPhrase original = replyPhrase();
        CeoReplyPhraseJpaEntity entity = CeoReplyPhraseMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 5L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 3, 4, 5, 6));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        CeoReplyPhrase restored = CeoReplyPhraseMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static void setAuditFields(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 2, 0, 0));
    }

    private static Ceo ceo() {
        return Ceo.reconstitute(
            1L, "ceo_user", "{bcrypt}hash", "홍길동", "123-45-67890",
            new PhoneNumber("01055556666"), "ceo@example.com", CeoStatus.INACTIVE);
    }

    private static Ceo ceoWithNulls() {
        return Ceo.reconstitute(
            2L, "ceo2", "pw", "이름", null, null, null, CeoStatus.ACTIVE);
    }

    private static CeoLoginHistory loginHistory() {
        return CeoLoginHistory.reconstitute(
            3L, CeoId.of(4L), CeoLoginResult.FAILURE, CeoLoginFailureReason.ACCOUNT_INACTIVE,
            "10.0.0.1", "Mozilla/5.0", LocalDateTime.of(2026, 1, 2, 3, 4, 5));
    }

    private static CeoReplyPhrase replyPhrase() {
        return CeoReplyPhrase.reconstitute(
            5L, CeoId.of(6L), "감사 인사", "방문해 주셔서 감사합니다", 3,
            LocalDateTime.of(2026, 2, 3, 4, 5, 6),
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
    }
}
