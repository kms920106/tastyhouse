package com.tastyhouse.core.domain.member.follow.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.Follow;
import com.tastyhouse.core.domain.member.follow.domain.model.QFollow;
import com.tastyhouse.core.domain.member.follow.domain.repository.FollowRepository;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;
import static com.tastyhouse.core.domain.member.follow.domain.model.QFollow.follow;

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
    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId.value()),
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
            .where(follow.followerId.eq(memberId.value()))
            .orderBy(follow.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(memberId.value()))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollow.followerId.eq(viewerMemberId.value()),
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
            .where(follow.followingId.eq(memberId.value()))
            .orderBy(follow.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.followingId.eq(memberId.value()))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }
}
