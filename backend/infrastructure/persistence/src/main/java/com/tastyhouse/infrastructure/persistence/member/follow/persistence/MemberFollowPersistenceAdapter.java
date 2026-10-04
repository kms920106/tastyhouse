package com.tastyhouse.infrastructure.persistence.member.follow.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowPersistencePort;

import static com.tastyhouse.infrastructure.persistence.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;

@Repository
class MemberFollowPersistenceAdapter implements MemberFollowPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final MemberFollowJpaRepository memberFollowJpaRepository;

    public MemberFollowPersistenceAdapter(JPAQueryFactory queryFactory, MemberFollowJpaRepository memberFollowJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberFollowJpaRepository = memberFollowJpaRepository;
    }

    @Override
    public Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId) {
        MemberFollowJpaEntity entity = queryFactory
            .selectFrom(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId.value()),
                memberFollowJpaEntity.followingId.eq(followingId.value())
            )
            .fetchOne();

        return Optional.ofNullable(entity).map(MemberFollowMapper::toDomain);
    }

    @Override
    public boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId.value()),
                memberFollowJpaEntity.followingId.eq(followingId.value())
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public MemberFollow save(MemberFollow memberFollow) {
        MemberFollowJpaEntity saved = memberFollowJpaRepository.save(MemberFollowMapper.toEntity(memberFollow));
        return MemberFollowMapper.toDomain(saved);
    }

    @Override
    public void delete(MemberFollow memberFollow) {
        memberFollowJpaRepository.deleteById(memberFollow.getId());
    }
}
