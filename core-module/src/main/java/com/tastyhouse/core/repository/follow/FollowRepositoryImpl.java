package com.tastyhouse.core.repository.follow;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.follow.Follow;
import com.tastyhouse.core.entity.follow.QFollow;
import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
import com.tastyhouse.core.entity.user.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final JPAQueryFactory queryFactory;
    private final FollowJpaRepository followJpaRepository;

    @Override
    public Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId) {
        QFollow follow = QFollow.follow;

        Follow result = queryFactory
            .selectFrom(follow)
            .where(
                follow.followerId.eq(followerId),
                follow.followingId.eq(followingId)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId) {
        QFollow follow = QFollow.follow;

        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .where(
                follow.followerId.eq(followerId),
                follow.followingId.eq(followingId)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public boolean existsByFollowingId(Long followingId) {
        QFollow follow = QFollow.follow;
        return queryFactory
            .selectOne()
            .from(follow)
            .where(follow.followingId.eq(followingId))
            .fetchFirst() != null;
    }

    @Override
    public Follow save(Follow follow) {
        return followJpaRepository.save(follow);
    }

    @Override
    public void delete(Follow follow) {
        followJpaRepository.delete(follow);
    }

    @Override
    public List<Long> findFollowingIdsByFollowerId(Long followerId) {
        QFollow follow = QFollow.follow;

        return queryFactory
            .select(follow.followingId)
            .from(follow)
            .where(follow.followerId.eq(followerId))
            .fetch();
    }

    @Override
    public long countByFollowerId(Long followerId) {
        QFollow follow = QFollow.follow;

        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(followerId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countByFollowingId(Long followingId) {
        QFollow follow = QFollow.follow;

        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followingId.eq(followingId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public Page<FollowMemberDto> findFollowingList(Long memberId, Long viewerMemberId, Pageable pageable) {
        QFollow follow = QFollow.follow;
        QFollow viewerFollow = new QFollow("viewerFollow");
        QMember member = QMember.member;

        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId),
                    viewerFollow.followingId.eq(member.id)
                )
                .exists()
            : com.querydsl.core.types.dsl.Expressions.FALSE;

        List<FollowMemberDto> content = queryFactory
            .select(Projections.constructor(FollowMemberDto.class,
                member.id,
                member.nickname,
                member.memberGrade,
                member.profileImageFileId,
                isFollowing
            ))
            .from(follow)
            .join(member).on(follow.followingId.eq(member.id))
            .where(follow.followerId.eq(memberId))
            .orderBy(follow.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(memberId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<FollowMemberDto> findFollowerList(Long memberId, Long viewerMemberId, Pageable pageable) {
        QFollow follow = QFollow.follow;
        QFollow viewerFollow = new QFollow("viewerFollow");
        QMember member = QMember.member;

        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId),
                    viewerFollow.followingId.eq(member.id)
                )
                .exists()
            : com.querydsl.core.types.dsl.Expressions.FALSE;

        List<FollowMemberDto> content = queryFactory
            .select(Projections.constructor(FollowMemberDto.class,
                member.id,
                member.nickname,
                member.memberGrade,
                member.profileImageFileId,
                isFollowing
            ))
            .from(follow)
            .join(member).on(follow.followerId.eq(member.id))
            .where(follow.followingId.eq(memberId))
            .orderBy(follow.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followingId.eq(memberId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
