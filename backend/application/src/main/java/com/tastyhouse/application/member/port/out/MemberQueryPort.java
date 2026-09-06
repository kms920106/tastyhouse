package com.tastyhouse.application.member.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface MemberQueryPort {

    PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Optional<MemberPersonalInfoResult> findPersonalInfoById(MemberId memberId);

    boolean existsByNickname(String nickname);

    boolean existsByActivePhoneNumber(String phoneNumber);
}
