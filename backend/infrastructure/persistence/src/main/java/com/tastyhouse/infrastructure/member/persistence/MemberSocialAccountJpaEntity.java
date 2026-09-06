package com.tastyhouse.infrastructure.member.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "MEMBER_SOCIAL_ACCOUNT",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_member_social_account_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
)
public class MemberSocialAccountJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberSocialProvider provider;

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

    protected MemberSocialAccountJpaEntity() {
    }

    private MemberSocialAccountJpaEntity(
        Long memberId,
        MemberSocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl,
        LocalDateTime lastLoginAt
    ) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
        this.providerNickname = providerNickname;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.lastLoginAt = lastLoginAt;
    }

    static MemberSocialAccountJpaEntity create(
        Long memberId,
        MemberSocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl,
        LocalDateTime lastLoginAt
    ) {
        return new MemberSocialAccountJpaEntity(
            memberId, provider, providerId, providerEmail, providerNickname, providerProfileImageUrl, lastLoginAt
        );
    }

    void applyChanges(
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl,
        LocalDateTime lastLoginAt
    ) {
        this.providerEmail = providerEmail;
        this.providerNickname = providerNickname;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public MemberSocialProvider getProvider() {
        return this.provider;
    }

    public String getProviderId() {
        return this.providerId;
    }

    public String getProviderEmail() {
        return this.providerEmail;
    }

    public String getProviderNickname() {
        return this.providerNickname;
    }

    public String getProviderProfileImageUrl() {
        return this.providerProfileImageUrl;
    }

    public LocalDateTime getLastLoginAt() {
        return this.lastLoginAt;
    }
}
