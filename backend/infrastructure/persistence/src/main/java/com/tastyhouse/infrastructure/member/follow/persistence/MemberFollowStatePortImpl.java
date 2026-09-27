package com.tastyhouse.infrastructure.member.follow.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.follow.port.out.write.MemberFollowState;
import com.tastyhouse.application.member.follow.port.out.write.MemberFollowStatePort;

import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;

@Repository
public class MemberFollowStatePortImpl implements MemberFollowStatePort {
    private final JPAQueryFactory queryFactory;
    private final MemberFollowJpaRepository memberFollowJpaRepository;

    public MemberFollowStatePortImpl(JPAQueryFactory queryFactory, MemberFollowJpaRepository memberFollowJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberFollowJpaRepository = memberFollowJpaRepository;
    }

    @Override
    public Optional<MemberFollowState> findByFollowerIdAndFollowingId(Long followerId, Long followingId) {
        MemberFollowJpaEntity entity = queryFactory
            .selectFrom(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId),
                memberFollowJpaEntity.followingId.eq(followingId)
            )
            .fetchOne();

        return Optional.ofNullable(entity).map(MemberFollowMapper::toState);
    }

    @Override
    public boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId),
                memberFollowJpaEntity.followingId.eq(followingId)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public MemberFollowState save(MemberFollowState state) {
        MemberFollowJpaEntity saved = memberFollowJpaRepository.save(MemberFollowMapper.toEntity(state));
        return MemberFollowMapper.toState(saved);
    }

    @Override
    public void delete(Long id) {
        memberFollowJpaRepository.deleteById(id);
    }
}
