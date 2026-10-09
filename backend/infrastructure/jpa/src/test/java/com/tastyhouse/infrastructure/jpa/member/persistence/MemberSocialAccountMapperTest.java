package com.tastyhouse.infrastructure.jpa.member.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberSocialAccountMapperTest {

    @Test
    @DisplayName("MemberSocialAccount → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        MemberSocialAccountJpaEntity entity = MemberSocialAccountMapper.toEntity(socialAccount());

        assertThat(entity.getMemberId()).isEqualTo(102L);
        assertThat(entity.getProvider()).isEqualTo("NAVER");
        assertThat(entity.getProviderId()).isEqualTo("provider-id");
        assertThat(entity.getProviderEmail()).isEqualTo("p@mail.com");
        assertThat(entity.getProviderNickname()).isEqualTo("p-nick");
        assertThat(entity.getProviderProfileImageUrl()).isEqualTo("https://img.example/p.png");
        assertThat(entity.getLastLoginAt()).isEqualTo(LocalDateTime.of(2026, 7, 7, 7, 7));
    }

    @Test
    @DisplayName("엔티티 → MemberSocialAccount 변환 시 모든 필드가 보존된다")
    void toDomain() {
        MemberSocialAccount original = socialAccount();
        MemberSocialAccountJpaEntity entity = MemberSocialAccountMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        MemberSocialAccount restored = MemberSocialAccountMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static MemberSocialAccount socialAccount() {
        return MemberSocialAccount.reconstitute(
            101L, MemberId.of(102L), MemberSocialProvider.NAVER, "provider-id", "p@mail.com", "p-nick",
            "https://img.example/p.png",
            LocalDateTime.of(2026, 7, 7, 7, 7),
            LocalDateTime.of(2026, 8, 8, 8, 8),
            LocalDateTime.of(2026, 9, 9, 9, 9));
    }
}
