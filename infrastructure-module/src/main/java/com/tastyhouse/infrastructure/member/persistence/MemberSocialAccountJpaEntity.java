package com.tastyhouse.infrastructure.member.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.core.domain.member.domain.model.MemberSocialProvider;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 소셜 계정 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberSocialAccount}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberSocialAccountMapper}가 수행한다.
 */
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
public class MemberSocialAccountJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

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

    private MemberSocialAccountJpaEntity(
        MemberId memberId,
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberSocialAccountMapper#toEntity}에서만 호출한다.
     */
    static MemberSocialAccountJpaEntity create(
        MemberId memberId,
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

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
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
}
