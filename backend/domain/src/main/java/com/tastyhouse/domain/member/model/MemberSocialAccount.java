package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public class MemberSocialAccount {
    private final Long id;
    private final MemberId memberId;
    private final MemberSocialProvider provider;
    private final String providerId;
    private String providerEmail;
    private String providerNickname;
    private String providerProfileImageUrl;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private MemberSocialAccount(
        Long id,
        MemberId memberId,
        MemberSocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
        this.providerNickname = providerNickname;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberSocialAccount of(
        MemberId memberId,
        MemberSocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl
    ) {
        return new MemberSocialAccount(
            null, memberId, provider, providerId, providerEmail, providerNickname, providerProfileImageUrl,
            LocalDateTime.now(), null, null
        );
    }

    public static MemberSocialAccount reconstitute(
        Long id,
        MemberId memberId,
        MemberSocialProvider provider,
        String providerId,
        String providerEmail,
        String providerNickname,
        String providerProfileImageUrl,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MemberSocialAccount(
            id, memberId, provider, providerId, providerEmail, providerNickname, providerProfileImageUrl,
            lastLoginAt, createdAt, updatedAt
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

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
