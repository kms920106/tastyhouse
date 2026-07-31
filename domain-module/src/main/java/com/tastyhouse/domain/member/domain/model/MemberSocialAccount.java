package com.tastyhouse.domain.member.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 소셜 계정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberSocialAccountJpaEntity} + {@code MemberSocialAccountMapper}가 담당한다.
 * 도메인이 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 호출부가 명시적으로
 * {@code MemberSocialAccountRepository#save}를 호출해야 한다.
 */
@Getter
public class MemberSocialAccount {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final MemberSocialProvider provider;
    private final String providerId;
    private String providerEmail;
    private String providerNickname;
    private String providerProfileImageUrl;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

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

    /**
     * 신규 소셜 계정을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
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
}
