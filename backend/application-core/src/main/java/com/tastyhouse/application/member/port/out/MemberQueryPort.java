package com.tastyhouse.application.member.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MemberQueryPort {

    PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(Long memberId);

    Optional<MemberPersonalInfoResult> findPersonalInfoById(Long memberId);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, String excludedStatus);
}
