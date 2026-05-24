package com.tastyhouse.core.domain.member.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
    name = "MEMBER_SOCIAL_ACCOUNT",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_member_social_account_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
)
public class MemberSocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(name = "provider_email", length = 200)
    private String providerEmail;

    @Column(name = "provider_nickname", length = 100)
    private String providerNickname;

    @Column(name = "provider_profile_image_url", length = 500)
    private String providerProfileImageUrl;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private MemberSocialAccount(
        Long memberId,
        SocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl
    ) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
        this.providerNickname = providerNickname;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.lastLoginAt = LocalDateTime.now();
    }

    public static MemberSocialAccount of(
        Long memberId,
        SocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl
    ) {
        return new MemberSocialAccount(
            memberId, provider, providerId, providerEmail, providerNickname, providerProfileImageUrl
        );
    }

    public void updateProviderInfo(
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl
    ) {
        if (providerEmail != null) this.providerEmail = providerEmail;
        if (providerNickname != null) this.providerNickname = providerNickname;
        if (providerProfileImageUrl != null) this.providerProfileImageUrl = providerProfileImageUrl;
        this.lastLoginAt = LocalDateTime.now();
    }
}
