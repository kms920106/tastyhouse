package com.tastyhouse.application.member.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;

/**
 * 회원 관리 목록 read model(admin-api 소비).
 *
 * <p>비-admin 형제 Result가 같은 패키지에 없으므로 {@code Management} 한정어 없이 순수명을 쓴다
 * (admin 전용 네이밍 규칙).
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이 값을 그대로 응답에 전달한다.
 */
public record MemberListItemResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    MemberGender gender,
    MemberGrade memberGrade,
    MemberStatus memberStatus,
    String profileImageUrl,
    LocalDateTime createdAt
) {
}
