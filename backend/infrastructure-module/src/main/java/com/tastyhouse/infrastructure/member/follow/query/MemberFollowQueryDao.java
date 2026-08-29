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

/**
 * 팔로우 read 어댑터(CQRS query 측).
 *
 * <p>팔로잉/팔로워 목록을 회원·프로필 이미지와 조인해 Result DTO로 직접 투영한다. 소비 모듈(web-api)의
 * {@code MemberFollowQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>프로필 이미지는 조인으로 얻은 저장 경로를 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에
 * 담는다 — {@code Projections.constructor}는 record 생성자로 직접 투영하므로 변환을 투영식에 끼울 수 없어, fetch
 * 직후 재조립한다.
 */
@Repository
public class MemberFollowQueryDao implements MemberFollowQueryPort {

    private static final QMemberFollowJpaEntity viewerFollow = new QMemberFollowJpaEntity("viewerFollow");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MemberFollowQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 내가(memberId) 팔로우하는 회원 목록. {@code viewerMemberId}가 주어지면 각 항목에 뷰어의 팔로우
     * 여부를 함께 투영하고, 비로그인(null)이면 모두 false로 둔다.
     */
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

    /**
     * 나를(memberId) 팔로우하는 회원 목록. 뷰어 팔로우 여부 투영 규칙은 팔로잉 목록과 같다.
     */
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code Projections.constructor}가 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
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

    /**
     * 뷰어가 대상 회원을 팔로우 중인지. 표현용 단건 판정이라 write 포트가 아니라 이 어댑터가 답한다.
     */
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

    /**
     * 이 회원이 팔로우하는 수(팔로잉 카운트). 프로필 화면 표시용 집계다.
     */
    @Override
    public long countFollowing(MemberId memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(memberId.value()))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 이 회원을 팔로우하는 수(팔로워 카운트). 프로필 화면 표시용 집계다.
     */
    @Override
    public long countFollower(MemberId memberId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(memberId.value()))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 이 회원이 팔로우하는 회원 ID 목록. 팔로잉 타임라인 조회의 선행 입력이다.
     */
    @Override
    public List<Long> findFollowingIds(MemberId followerId) {
        return queryFactory
            .select(memberFollowJpaEntity.followingId)
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(followerId.value()))
            .fetch();
    }

}
