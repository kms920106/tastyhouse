package com.tastyhouse.infrastructure.member.follow.query;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

/**
 * 팔로우 read 어댑터(CQRS query 측).
 *
 * <p>팔로잉/팔로워 목록을 회원·프로필 이미지와 조인해 Result DTO로 직접 투영한다. 소비 모듈(web-api)의
 * {@code MemberFollowQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>{@code memberFollowJpaEntity.followerId}/{@code followingId}는 {@code @Convert(MemberIdConverter)}로
 * {@link MemberId} VO 경로이므로 raw {@code Long} PK({@code memberJpaEntity.id})와 직접 조인·비교할 수
 * 없다. {@link Expressions#numberPath}로 두 컬럼의 raw Long 경로를 우회 노출해 조인·비교에 사용한다
 * (write 어댑터 {@code MemberFollowRepositoryImpl}과 같은 이유의 같은 우회다).
 */
@Repository
@RequiredArgsConstructor
public class MemberFollowQueryDao {

    private static final NumberPath<Long> followerIdCol =
        Expressions.numberPath(Long.class, memberFollowJpaEntity, "followerId");
    private static final NumberPath<Long> followingIdCol =
        Expressions.numberPath(Long.class, memberFollowJpaEntity, "followingId");

    private static final QMemberFollowJpaEntity viewerFollow = new QMemberFollowJpaEntity("viewerFollow");
    private static final NumberPath<Long> viewerFollowerIdCol =
        Expressions.numberPath(Long.class, viewerFollow, "followerId");
    private static final NumberPath<Long> viewerFollowingIdCol =
        Expressions.numberPath(Long.class, viewerFollow, "followingId");

    private final JPAQueryFactory queryFactory;

    /**
     * 내가(memberId) 팔로우하는 회원 목록. {@code viewerMemberId}가 주어지면 각 항목에 뷰어의 팔로우
     * 여부를 함께 투영하고, 비로그인(null)이면 모두 false로 둔다.
     */
    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(followingIdCol.eq(memberJpaEntity.id))
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

    /**
     * 나를(memberId) 팔로우하는 회원 목록. 뷰어 팔로우 여부 투영 규칙은 팔로잉 목록과 같다.
     */
    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        List<FollowMemberResult> content = queryFactory
            .select(followMemberProjection(viewerMemberId))
            .from(memberFollowJpaEntity)
            .join(memberJpaEntity).on(followerIdCol.eq(memberJpaEntity.id))
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

    private QFollowMemberResult followMemberProjection(MemberId viewerMemberId) {
        return new QFollowMemberResult(
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
                viewerFollowerIdCol.eq(viewerMemberId.value()),
                viewerFollowingIdCol.eq(memberJpaEntity.id)
            )
            .exists();
    }
}
