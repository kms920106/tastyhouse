package com.tastyhouse.webapi.follow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.follow.repository.MemberFollowRepository;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.member.follow.query.FollowMemberResult;
import com.tastyhouse.infrastructure.member.follow.query.MemberFollowQueryDao;
import com.tastyhouse.infrastructure.member.query.MemberQueryDao;
import com.tastyhouse.webapi.follow.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.response.FollowMemberSearchListItemResponse;

/**
 * 팔로우 조회 서비스.
 *
 * <p>목록은 infra read 어댑터({@link MemberFollowQueryDao})가 뷰어의 팔로우 여부까지 함께 투영하고,
 * 닉네임 검색은 회원 DAO({@link MemberQueryDao})를 쓴다. 단건 팔로우 여부·카운트는 원시값 반환이라
 * write 포트의 검증용 메서드를 그대로 쓴다.
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 응답에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowQueryService {

    private final MemberFollowQueryDao memberFollowQueryDao;
    private final MemberQueryDao memberQueryDao;
    private final MemberFollowRepository memberFollowRepository;

    public FollowQueryService(
        MemberFollowQueryDao memberFollowQueryDao,
        MemberQueryDao memberQueryDao,
        MemberFollowRepository memberFollowRepository
    ) {
        this.memberFollowQueryDao = memberFollowQueryDao;
        this.memberQueryDao = memberQueryDao;
        this.memberFollowRepository = memberFollowRepository;
    }

    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return memberFollowRepository.existsByFollowerIdAndFollowingId(
            MemberId.of(viewerMemberId), MemberId.of(targetMemberId)
        );
    }

    public long countFollowing(Long memberId) {
        return memberFollowRepository.countByFollowerId(MemberId.of(memberId));
    }

    public long countFollower(Long memberId) {
        return memberFollowRepository.countByFollowingId(MemberId.of(memberId));
    }

    public PageResult<FollowMemberListItemResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryDao
            .findFollowingList(MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size))
            .map(this::toFollowMemberListItemResponse);
    }

    public PageResult<FollowMemberListItemResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryDao
            .findFollowerList(MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size))
            .map(this::toFollowMemberListItemResponse);
    }

    public PageResult<FollowMemberSearchListItemResponse> searchMembersByNickname(
        String nickname,
        Long viewerMemberId,
        int page,
        int size
    ) {
        return memberQueryDao.findByNicknameContaining(nickname, PageQuery.of(page, size))
            .map(result -> FollowMemberSearchListItemResponse.of(
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

    private FollowMemberListItemResponse toFollowMemberListItemResponse(FollowMemberResult result) {
        return FollowMemberListItemResponse.of(
            result.memberId(),
            result.nickname(),
            result.memberGrade().name(),
            result.profileImageUrl(),
            result.following()
        );
    }
}
