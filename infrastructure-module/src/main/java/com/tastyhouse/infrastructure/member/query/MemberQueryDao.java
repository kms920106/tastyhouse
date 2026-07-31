package com.tastyhouse.infrastructure.member.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

/**
 * 회원 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code MemberRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api/admin-api)의
 * {@code MemberQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에 admin 마커를
 * 붙이지 않고 순수 동작명을 쓰며, 소비자별로 필요한 필드 셋이 달라 Result를 통합하지 않는다
 * (회원 관리 목록은 {@link MemberListItemResult}, 프로필 요약은 {@link MemberWithProfileImageResult}).
 *
 * <p>프로필 이미지는 파일 경로만 투영하고 표시용 URL 조립은 소비 모듈이 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 회원 관리 목록 조회(admin) — 닉네임/아이디/휴대폰 부분일치와 상태·등급 필터를 적용한다.
     */
    public PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery) {
        List<MemberListItemResult> content = queryFactory
            .select(new QMemberListItemResult(
                memberJpaEntity.id,
                memberJpaEntity.username,
                memberJpaEntity.nickname,
                memberJpaEntity.fullName,
                memberJpaEntity.phoneNumber.value,
                memberJpaEntity.gender,
                memberJpaEntity.memberGrade,
                memberJpaEntity.memberStatus,
                uploadedFileJpaEntity.filePath,
                memberJpaEntity.createdAt
            ))
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                nicknameContains(condition.nickname()),
                usernameContains(condition.username()),
                phoneContains(condition.phone()),
                statusEq(condition.status()),
                gradeEq(condition.grade())
            )
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberJpaEntity.count())
            .from(memberJpaEntity)
            .where(
                nicknameContains(condition.nickname()),
                usernameContains(condition.username()),
                phoneContains(condition.phone()),
                statusEq(condition.status()),
                gradeEq(condition.grade())
            )
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 닉네임 부분일치 회원 검색(web) — 팔로우 대상 찾기 화면이 소비한다.
     */
    public PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery) {
        List<MemberWithProfileImageResult> content = queryFactory
            .select(memberWithProfileImageProjection())
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberJpaEntity.count())
            .from(memberJpaEntity)
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 단건 프로필 요약 조회 — 내 프로필·타 회원 프로필 화면이 소비한다.
     */
    public Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId) {
        return Optional.ofNullable(
            queryFactory
                .select(memberWithProfileImageProjection())
                .from(memberJpaEntity)
                .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
                .where(memberJpaEntity.id.eq(memberId.value()))
                .fetchOne()
        );
    }

    /**
     * 여러 회원의 프로필 요약을 한 번의 쿼리로 조회해 식별자로 색인한다 — 목록 화면이 작성자 정보를
     * 합성할 때 N+1을 피하기 위한 경로다(과거 단건 조회를 회원 수만큼 반복하던 것을 in 절로 대체).
     */
    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctIds = memberIds.stream().distinct().toList();

        return queryFactory
            .select(memberWithProfileImageProjection())
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberJpaEntity.profileImageFileId.eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.id.in(distinctIds))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                MemberWithProfileImageResult::id,
                Function.identity(),
                (existing, replacement) -> existing
            ));
    }

    private QMemberWithProfileImageResult memberWithProfileImageProjection() {
        return new QMemberWithProfileImageResult(
            memberJpaEntity.id,
            memberJpaEntity.nickname,
            memberJpaEntity.memberGrade,
            memberJpaEntity.statusMessage,
            uploadedFileJpaEntity.filePath
        );
    }

    private BooleanExpression nicknameContains(String nickname) {
        return StringUtils.hasText(nickname) ? memberJpaEntity.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression usernameContains(String username) {
        return StringUtils.hasText(username) ? memberJpaEntity.username.containsIgnoreCase(username) : null;
    }

    private BooleanExpression phoneContains(String phone) {
        return StringUtils.hasText(phone) ? memberJpaEntity.phoneNumber.value.containsIgnoreCase(phone) : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? memberJpaEntity.memberStatus.eq(status) : null;
    }

    private BooleanExpression gradeEq(MemberGrade grade) {
        return grade != null ? memberJpaEntity.memberGrade.eq(grade) : null;
    }
}
