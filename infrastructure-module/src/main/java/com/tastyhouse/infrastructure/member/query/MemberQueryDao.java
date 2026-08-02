package com.tastyhouse.infrastructure.member.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

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
 * <p>프로필 이미지는 조인으로 얻은 저장 경로를 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에
 * 담는다 — {@code @QueryProjection}은 record 생성자로 직접 투영하므로 변환을 투영식에 끼울 수 없어, fetch
 * 직후 재조립한다.
 */
@Repository
@RequiredArgsConstructor
public class MemberQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

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
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
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
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

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
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

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
                    .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
                    .where(memberJpaEntity.id.eq(memberId.value()))
                    .fetchOne()
            )
            .map(this::withResolvedProfileImageUrl);
    }

    /**
     * 회원 상세 조립용 프로필 이미지 표시용 URL 단건 조회. 상세 응답은 도메인 모델({@code Member})을
     * 그대로 써서 조립하지만, 프로필 이미지만은 이 조회로 대체해 파일 단건 재조회를 없앤다.
     */
    public Optional<String> findProfileImageUrl(MemberId memberId) {
        String filePath = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.id.eq(memberId.value()))
            .fetchOne();

        return Optional.ofNullable(fileUrlResolver.resolve(filePath));
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
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.id.in(distinctIds))
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. {@code @QueryProjection}이 생성자 직접 투영이라
     * 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private MemberListItemResult withResolvedProfileImageUrl(MemberListItemResult row) {
        return new MemberListItemResult(
            row.id(),
            row.username(),
            row.nickname(),
            row.fullName(),
            row.phoneNumber(),
            row.gender(),
            row.memberGrade(),
            row.memberStatus(),
            fileUrlResolver.resolve(row.profileImageUrl()),
            row.createdAt()
        );
    }

    private MemberWithProfileImageResult withResolvedProfileImageUrl(MemberWithProfileImageResult row) {
        return new MemberWithProfileImageResult(
            row.id(),
            row.nickname(),
            row.memberGrade(),
            row.statusMessage(),
            fileUrlResolver.resolve(row.profileImageUrl())
        );
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code MEMBER.profile_image_file_id}를 raw {@code Long}으로 비교하기
     * 위한 path.
     */
    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
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
