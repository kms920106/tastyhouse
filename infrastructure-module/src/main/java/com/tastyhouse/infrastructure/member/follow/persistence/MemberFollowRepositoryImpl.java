package com.tastyhouse.infrastructure.member.follow.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.model.MemberFollow;
import com.tastyhouse.core.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.infrastructure.member.follow.persistence.QMemberFollowJpaEntity.memberFollowJpaEntity;

/**
 * {@code member}는 infrastructure-module로 이동한 {@code MemberJpaEntity}를 가리킨다.
 * core-module은 infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core)
 * 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로 JPA 엔티티명("MemberJpaEntity")을
 * 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 * {@code memberFollowJpaEntity.followerId}/{@code followingId}는 {@code @Convert(MemberIdConverter)}로
 * {@link MemberId} VO 경로이므로, raw {@code Long} PK({@code memberIdCol})와 직접 조인·비교할 수 없다.
 * {@link Expressions#numberPath}로 두 컬럼의 raw Long 경로를 우회 노출해 조인·비교에 사용한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberFollowRepositoryImpl implements MemberFollowRepository {

    private static final PathBuilder<Object> member = new PathBuilder<>(Object.class, "MemberJpaEntity");
    private static final NumberPath<Long> memberIdCol = member.getNumber("id", Long.class);
    private static final StringPath memberNicknameCol = member.getString("nickname");
    private static final NumberPath<Long> memberProfileImageFileIdCol = member.getNumber("profileImageFileId", Long.class);
    private static final EnumPath<MemberGrade> memberGradeCol = member.getEnum("memberGrade", MemberGrade.class);

    private static final NumberPath<Long> followerIdCol = Expressions.numberPath(Long.class, memberFollowJpaEntity, "followerId");
    private static final NumberPath<Long> followingIdCol = Expressions.numberPath(Long.class, memberFollowJpaEntity, "followingId");

    private static final QMemberFollowJpaEntity viewerFollow = new QMemberFollowJpaEntity("viewerFollow");
    private static final NumberPath<Long> viewerFollowerIdCol = Expressions.numberPath(Long.class, viewerFollow, "followerId");
    private static final NumberPath<Long> viewerFollowingIdCol = Expressions.numberPath(Long.class, viewerFollow, "followingId");

    private final JPAQueryFactory queryFactory;
    private final MemberFollowJpaRepository memberFollowJpaRepository;

    @Override
    public Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId) {
        MemberFollowJpaEntity entity = queryFactory
            .selectFrom(memberFollowJpaEntity)
            .where(
                memberFollowJpaEntity.followerId.eq(followerId),
                memberFollowJpaEntity.followingId.eq(followingId)
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
                memberFollowJpaEntity.followerId.eq(followerId),
                memberFollowJpaEntity.followingId.eq(followingId)
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

    @Override
    public List<Long> findFollowingIdsByFollowerId(MemberId followerId) {
        return queryFactory
            .select(followingIdCol)
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(followerId))
            .fetch();
    }

    @Override
    public long countByFollowerId(MemberId followerId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followerId.eq(followerId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countByFollowingId(MemberId followingId) {
        Long count = queryFactory
            .select(memberFollowJpaEntity.count())
            .from(memberFollowJpaEntity)
            .where(memberFollowJpaEntity.followingId.eq(followingId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollowerIdCol.eq(viewerMemberId.value()),
                    viewerFollowingIdCol.eq(memberIdCol)
                )
                .exists()
            : Expressions.FALSE;

        List<FollowMemberResult> content = queryFactory
            .select(Projections.constructor(FollowMemberResult.class,
                memberIdCol,
                memberNicknameCol,
                memberGradeCol,
                uploadedFile.filePath,
                isFollowing
            ))
            .from(memberFollowJpaEntity)
            .join(member).on(followingIdCol.eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
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
    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery) {
        BooleanExpression isFollowing = viewerMemberId != null
            ? JPAExpressions.selectOne()
                .from(viewerFollow)
                .where(
                    viewerFollowerIdCol.eq(viewerMemberId.value()),
                    viewerFollowingIdCol.eq(memberIdCol)
                )
                .exists()
            : Expressions.FALSE;

        List<FollowMemberResult> content = queryFactory
            .select(Projections.constructor(FollowMemberResult.class,
                memberIdCol,
                memberNicknameCol,
                memberGradeCol,
                uploadedFile.filePath,
                isFollowing
            ))
            .from(memberFollowJpaEntity)
            .join(member).on(followerIdCol.eq(memberIdCol))
            .leftJoin(uploadedFile).on(memberProfileImageFileIdCol.eq(uploadedFile.id))
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
}
