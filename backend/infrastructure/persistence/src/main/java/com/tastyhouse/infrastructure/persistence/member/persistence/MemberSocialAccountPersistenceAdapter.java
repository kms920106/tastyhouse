package com.tastyhouse.infrastructure.persistence.member.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountSavePort;

import static com.tastyhouse.infrastructure.persistence.member.persistence.QMemberSocialAccountJpaEntity.memberSocialAccountJpaEntity;

@Repository
class MemberSocialAccountPersistenceAdapter implements MemberSocialAccountLoadPort, MemberSocialAccountSavePort {

    private final JPAQueryFactory queryFactory;
    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    public MemberSocialAccountPersistenceAdapter(JPAQueryFactory queryFactory, MemberSocialAccountJpaRepository memberSocialAccountJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberSocialAccountJpaRepository = memberSocialAccountJpaRepository;
    }

    @Override
    public Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        MemberSocialAccountJpaEntity entity = queryFactory
            .selectFrom(memberSocialAccountJpaEntity)
            .where(
                providerEq(provider),
                providerIdEq(providerId)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(MemberSocialAccountMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return queryFactory.selectOne()
            .from(memberSocialAccountJpaEntity)
            .where(
                providerEq(provider),
                providerIdEq(providerId)
            )
            .fetchFirst() != null;
    }

    @Override
    public MemberSocialAccount save(MemberSocialAccount socialAccount) {
        if (socialAccount.getId() == null) {
            MemberSocialAccountJpaEntity saved = memberSocialAccountJpaRepository.save(MemberSocialAccountMapper.toEntity(socialAccount));
            return MemberSocialAccountMapper.toDomain(saved);
        }

        MemberSocialAccountJpaEntity entity = memberSocialAccountJpaRepository.findById(socialAccount.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 소셜 계정입니다: " + socialAccount.getId()));
        MemberSocialAccountMapper.applyChanges(entity, socialAccount);
        return MemberSocialAccountMapper.toDomain(entity);
    }

    private BooleanExpression providerEq(MemberSocialProvider provider) {
        return provider == null
            ? memberSocialAccountJpaEntity.provider.isNull()
            : memberSocialAccountJpaEntity.provider.eq(provider.name());
    }

    private BooleanExpression providerIdEq(String providerId) {
        return providerId == null
            ? memberSocialAccountJpaEntity.providerId.isNull()
            : memberSocialAccountJpaEntity.providerId.eq(providerId);
    }
}
