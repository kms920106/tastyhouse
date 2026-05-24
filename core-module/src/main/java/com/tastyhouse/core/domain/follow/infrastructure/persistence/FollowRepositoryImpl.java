package com.tastyhouse.core.domain.follow.infrastructure.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.domain.follow.domain.model.Follow;
import com.tastyhouse.core.domain.follow.domain.model.QFollow;
import com.tastyhouse.core.domain.follow.domain.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.follow.domain.model.QFollow.follow;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private static final QFollow viewerFollow = new QFollow("viewerFollow");

    private final JPAQueryFactory queryFactory;
    private final FollowJpaRepository followJpaRepository;

    @Override
    public Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId) {
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
        return queryFactory
            .select(follow.followingId)
            .from(follow)
            .where(follow.followerId.eq(followerId))
            .fetch();
    }

    @Override
    public long countByFollowerId(Long followerId) {
        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(followerId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countByFollowingId(Long followingId) {
        Long count = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followingId.eq(followingId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public Page<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, Pageable pageable) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId),
                    viewerFollow.followingId.eq(member.id)
                )
                .exists()
            : com.querydsl.core.types.dsl.Expressions.FALSE;

        List<FollowMemberResult> content = queryFactory
            .select(Projections.constructor(FollowMemberResult.class,
                member.id,
                member.nickname,
                member.memberGrade,
                uploadedFile.filePath,
                isFollowing
            ))
            .from(follow)
            .join(member).on(follow.followingId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
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
    public Page<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, Pageable pageable) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId),
                    viewerFollow.followingId.eq(member.id)
                )
                .exists()
            : com.querydsl.core.types.dsl.Expressions.FALSE;

        List<FollowMemberResult> content = queryFactory
            .select(Projections.constructor(FollowMemberResult.class,
                member.id,
                member.nickname,
                member.memberGrade,
                uploadedFile.filePath,
                isFollowing
            ))
            .from(follow)
            .join(member).on(follow.followerId.eq(member.id))
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
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
