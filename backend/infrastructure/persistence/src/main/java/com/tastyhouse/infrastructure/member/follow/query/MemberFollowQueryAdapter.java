package com.tastyhouse.infrastructure.member.follow.query;

import java.util.List;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
public class MemberFollowQueryAdapter implements MemberFollowQueryPort {
    private static final QMemberFollowJpaEntity viewerFollow = new QMemberFollowJpaEntity("viewerFollow");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MemberFollowQueryAdapter(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(memberFollowJpaEntity.followingId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberFollowJpaEntity.followerId.eq(memberId))
            .orderBy(memberFollowJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(memberId))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(memberFollowJpaEntity.followerId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberFollowJpaEntity.followingId.eq(memberId))
            .orderBy(memberFollowJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(memberId))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private ConstructorExpression<FollowMemberResult> followMemberProjection(Long viewerMemberId) {
        return Projections.constructor(FollowMemberResult.class,
            memberJpaEntity.id,
            memberJpaEntity.nickname,
            memberJpaEntity.memberGrade.stringValue(),
            fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
            isFollowedByViewer(viewerMemberId)
        );
    }

    private BooleanExpression isFollowedByViewer(Long viewerMemberId) {
        if (viewerMemberId == null) {
            return Expressions.FALSE;
        }
        return JPAExpressions.selectOne()
            .from(viewerFollow)
            .where(
                viewerFollow.followerId.eq(viewerMemberId),
                viewerFollow.followingId.eq(memberJpaEntity.id)
            )
            .exists();
    }

    @Override
    public boolean existsFollow(Long followerId, Long followingId) {
        Integer found = queryFactory
            .selectOne()
            .from(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId),
                memberFollowJpaEntity.followingId.eq(followingId)
            )
            .fetchFirst();

        return found != null;
    }

    @Override
    public long countFollowing(Long memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(memberId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countFollower(Long memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(memberId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public List<Long> findFollowingIds(Long followerId) {
        return queryFactory
            .select(memberFollowJpaEntity.followingId)
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(followerId))
            .fetch();
    }
}
