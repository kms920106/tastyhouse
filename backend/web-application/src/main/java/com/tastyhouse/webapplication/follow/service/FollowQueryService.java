package com.tastyhouse.webapplication.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.webapplication.follow.port.out.FollowMemberSearchResult;
import com.tastyhouse.webapplication.follow.port.in.FollowQueryUseCase;

/**
 * 팔로우 조회 서비스.
 *
 * <p>목록은 읽기 포트({@link MemberFollowQueryPort})가 뷰어의 팔로우 여부까지 함께 투영하므로 그 결과를
 * 그대로 내보내고, 닉네임 검색은 회원 읽기 포트({@link MemberQueryPort})의 결과에 팔로우 여부를 얹어
 * {@link FollowMemberSearchResult}로 합성한다. 단건 팔로우 여부·카운트도 표현용 조회이므로 write 포트가
 * 아니라 같은 읽기 포트가 답한다(CQRS 교차 주입 금지).
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 전달한다.
 * 도메인 enum({@code MemberGrade})은 검색 합성 지점에서 상수명 문자열로 낮춘다 — 인바운드 포트가 도메인
 * 타입을 노출하지 않게 하려면 강등이 이 계층에서 끝나야 한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowQueryService implements FollowQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;
    private final MemberQueryPort memberQueryPort;

    public FollowQueryService(
        MemberFollowQueryPort memberFollowQueryPort,
        MemberQueryPort memberQueryPort
    ) {
        this.memberFollowQueryPort = memberFollowQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return memberFollowQueryPort.existsFollow(
            MemberId.of(viewerMemberId), MemberId.of(targetMemberId)
        );
    }

    @Override
    public long countFollowing(Long memberId) {
        return memberFollowQueryPort.countFollowing(MemberId.of(memberId));
    }

    @Override
    public long countFollower(Long memberId) {
        return memberFollowQueryPort.countFollower(MemberId.of(memberId));
    }

    @Override
    public PageResult<FollowMemberResult> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryPort.findFollowingList(
            MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size)
        );
    }

    @Override
    public PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryPort.findFollowerList(
            MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size)
        );
    }

    @Override
    public PageResult<FollowMemberSearchResult> searchMembersByNickname(
        String nickname,
        Long viewerMemberId,
        int page,
        int size
    ) {
        return memberQueryPort.findByNicknameContaining(nickname, PageQuery.of(page, size))
            .map(result -> new FollowMemberSearchResult(
                result.id(),
                result.nickname(),
                result.memberGrade().name(),
                result.profileImageUrl(),
                viewerMemberId != null && isFollowing(viewerMemberId, result.id())
            ));
    }

    private MemberId toViewerId(Long viewerMemberId) {
        return viewerMemberId == null ? null : MemberId.of(viewerMemberId);
    }
}
