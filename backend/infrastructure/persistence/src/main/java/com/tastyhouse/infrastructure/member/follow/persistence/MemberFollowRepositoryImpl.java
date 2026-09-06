package com.tastyhouse.infrastructure.member.follow.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.follow.model.MemberFollow;
import com.tastyhouse.domain.member.follow.repository.MemberFollowRepository;
import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;

/**
 * 팔로우 write 어댑터.
 *
 * <p>단건 로드·중복 검증·저장·삭제만 담당한다. 팔로잉/팔로워 목록과 통계 카운트(회원·프로필 이미지 조인
 * 투영 포함)는 읽기 계약 {@code MemberFollowQueryPort}와 그 구현
 * {@code member/follow/query/MemberFollowQueryDao}가 담당한다.
 */
@Repository
public class MemberFollowRepositoryImpl implements MemberFollowRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberFollowJpaRepository memberFollowJpaRepository;

    public MemberFollowRepositoryImpl(JPAQueryFactory queryFactory, MemberFollowJpaRepository memberFollowJpaRepository) {
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
