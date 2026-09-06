package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberSocialAccountTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 lastLoginAt이 채워진다")
    void of_createsTransientSocialAccount() {
        MemberId memberId = MemberId.of(1L);

        MemberSocialAccount socialAccount = MemberSocialAccount.of(
            memberId, MemberSocialProvider.KAKAO, "provider-id-1",
            "test@example.com", "닉네임", "https://image.url"
        );

        assertThat(socialAccount.getId()).isNull();
        assertThat(socialAccount.getMemberId()).isEqualTo(memberId);
        assertThat(socialAccount.getProvider()).isEqualTo(MemberSocialProvider.KAKAO);
        assertThat(socialAccount.getProviderId()).isEqualTo("provider-id-1");
        assertThat(socialAccount.getProviderEmail()).isEqualTo("test@example.com");
        assertThat(socialAccount.getLastLoginAt()).isNotNull();
        assertThat(socialAccount.getCreatedAt()).isNull();
        assertThat(socialAccount.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("updateProviderInfo는 null이 아닌 필드만 변경하고 lastLoginAt을 갱신한다")
    void updateProviderInfo_changesOnlyNonNullFields() {
        MemberSocialAccount socialAccount = MemberSocialAccount.of(
            MemberId.of(1L), MemberSocialProvider.NAVER, "provider-id-1",
            "old@example.com", "옛닉네임", "https://old.url"
        );
        LocalDateTime firstLoginAt = socialAccount.getLastLoginAt();

        socialAccount.updateProviderInfo("new@example.com", null, "https://new.url");

        assertThat(socialAccount.getProviderEmail()).isEqualTo("new@example.com");
        assertThat(socialAccount.getProviderNickname()).isEqualTo("옛닉네임");
        assertThat(socialAccount.getProviderProfileImageUrl()).isEqualTo("https://new.url");
        assertThat(socialAccount.getLastLoginAt()).isAfterOrEqualTo(firstLoginAt);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime lastLoginAt = LocalDateTime.of(2026, 1, 3, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        MemberSocialAccount socialAccount = MemberSocialAccount.reconstitute(
            10L, MemberId.of(1L), MemberSocialProvider.APPLE, "provider-id-1",
            "test@example.com", "닉네임", "https://image.url", lastLoginAt, createdAt, updatedAt
        );

        assertThat(socialAccount.getId()).isEqualTo(10L);
        assertThat(socialAccount.getLastLoginAt()).isEqualTo(lastLoginAt);
        assertThat(socialAccount.getCreatedAt()).isEqualTo(createdAt);
        assertThat(socialAccount.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
