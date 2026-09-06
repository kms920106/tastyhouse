package com.tastyhouse.infrastructure.member.follow.query;

import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.ConstructorExpression;
import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
public class MemberFollowQueryDao implements MemberFollowQueryPort {
    private static final QMemberFollowJpaEntity viewerFollow = new QMemberFollowJpaEntity("viewerFollow");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MemberFollowQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(memberFollowJpaEntity.followingId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberFollowJpaEntity.followerId.eq(memberId.value()))
            .orderBy(memberFollowJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

        Long total = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(memberId.value()))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(memberFollowJpaEntity.followerId.eq(memberJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberFollowJpaEntity.followingId.eq(memberId.value()))
            .orderBy(memberFollowJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

        Long total = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(memberId.value()))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private FollowMemberResult withResolvedProfileImageUrl(FollowMemberResult row) {
        return new FollowMemberResult(
            row.memberId(),
            row.nickname(),
            row.memberGrade(),
            fileUrlResolver.resolve(row.profileImageUrl()),
            row.following()
        );
    }

    private ConstructorExpression<FollowMemberResult> followMemberProjection(MemberId viewerMemberId) {
        return Projections.constructor(FollowMemberResult.class,
            memberJpaEntity.id,
            memberJpaEntity.nickname,
            memberJpaEntity.memberGrade,
            uploadedFileJpaEntity.filePath,
            isFollowedByViewer(viewerMemberId)
        );
    }

    private BooleanExpression isFollowedByViewer(MemberId viewerMemberId) {
        if (viewerMemberId == null) {
            return Expressions.FALSE;
        }
        return JPAExpressions.selectOne()
            .from(viewerFollow)
            .where(
                viewerFollow.followerId.eq(viewerMemberId.value()),
                viewerFollow.followingId.eq(memberJpaEntity.id)
            )
            .exists();
    }

    @Override
    public boolean existsFollow(MemberId followerId, MemberId followingId) {
        Integer found = queryFactory
            .selectOne()
            .from(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId.value()),
                memberFollowJpaEntity.followingId.eq(followingId.value())
            )
            .fetchFirst();

        return found != null;
    }

    @Override
    public long countFollowing(MemberId memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(memberId.value()))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countFollower(MemberId memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(memberId.value()))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public List<Long> findFollowingIds(MemberId followerId) {
        return queryFactory
            .select(memberFollowJpaEntity.followingId)
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(followerId.value()))
            .fetch();
    }
}
