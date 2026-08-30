package com.tastyhouse.application.member.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;

/**
 * 회원 요약 + 프로필 이미지 URL read model.
 *
 * <p>프로필 카드·작성자 요약 등 여러 화면이 공유하는 최소 표현 단위다. 이미지는 DAO가 표시용 URL까지
 * 변환해 담으므로, 소비 모듈은 이 값을 그대로 응답에 전달한다.
 */
public record MemberWithProfileImageResult(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageUrl
) {
}
